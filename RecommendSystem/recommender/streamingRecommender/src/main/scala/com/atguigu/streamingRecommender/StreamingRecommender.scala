package com.atguigu.streamingRecommender

import java.sql.ResultSet

import com.atguigu.commons.conf.ConfigurationManager
import com.atguigu.commons.constant.Constants
import com.atguigu.commons.model.MySqlConfig
import com.atguigu.commons.pool.{CreateMySqlPool, CreateRedisPool, MySqlProxy, QueryCallback}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

import scala.collection.JavaConversions._
import scala.collection.mutable

object StreamingRecommender {

  val USER_RECENTLY_RATING_COUNT = 50
  val SIMILAR_MOVIE_COUNT = 50
  val MAX_REC_COUNT = 20

  private val minSimilarity = 0.7

  /**
    * 从Redis中获取当前用户最近K次评分，返回 Buffer[(Int,Double)]
    *
    * @param K       获取当前用户K次评分
    * @param userId  用户ID
    * @param movieId 新评分的电影ID
    * @param score   新评分的电影评分
    * @return 最近K次评分的数组，每一项是<movieId, score>
    */
  def getUserRecentRatings(redisClient: Jedis, K: Int, userId: Int, movieId: Int, score: Double): Array[(Int, Double)] = {
    // lrange：返回指定key对应的list的指定范围的元素，返回List<String>
    redisClient.lrange("uid:" + userId.toString, 0, K).map { line =>
      // 每一个元素都是mid:score
      val attr = line.asInstanceOf[String].split(":")
      (attr(0).toInt, attr(1).toDouble)
    }.toArray
  }

  /**
    * 从广播变量中获取movieId最相似的K个电影，并通过MySQL来过滤掉已被评分的电影
    *
    * @param K
    * @return 返回相似的电影ID数组
    */
  def getSimilarMovies(redisClient: Jedis, mysqlClient: MySqlProxy, mid: Int, uid: Int, K: Int): Array[Int] = {

    // smembers()：返回指定key对应的set集合的所有元素
    // 获取指定电影最相似的K个电影
    val similarMoviesBeforeFilter = redisClient.smembers("set:" + mid).map { line =>
      // 每个元素是mid:similarity
      val attr = line.split(":")
      attr(0).toInt
    }.take(K)

    // 从RATING表中获取指定用户评过分的所有电影的mid
    val querySQL = "SELECT mid FROM " + Constants.DB_RATING + " WHERE uid=?"
    val params = Array[Any](uid)

    // 创建保存所有打过分的电影mid的ArrayBuffer
    val ratedMovies = mutable.ArrayBuffer[Int]()

    // 执行MySQL查询，获取
    mysqlClient.executeQuery(querySQL, params, new QueryCallback {
      override def process(rs: ResultSet): Unit = {
        while (rs.next) {
          // 将所有获取到的指定用户评过分的mid添加到ArrayBuffer中
          ratedMovies.add(rs.getString("mid").toInt)
        }
      }
    })

    // 过滤掉最相似K个电影中用户评过分的电影
    similarMoviesBeforeFilter.filter(!ratedMovies.contains(_)).toArray
  }

  /**
    * 从广播变量中获取movieId1与movieId2的相似度，不存在、或movieId1=movieId2视为毫无相似，相似度为0
    *
    * @param movieId1
    * @param movieId2
    * @return movieId1与movieId2的相似度
    */
  def getSimilarityBetween2Movies(redisClient: Jedis, movieId1: Int, movieId2: Int): Double = {

    // 所有的moviedId元组都保证较小的movieId在前
    val (smallerId, biggerId) = if (movieId1 < movieId2) (movieId1, movieId2) else (movieId2, movieId1)
    if (smallerId == biggerId) {
      return 0.0
    }

    // 如果field存在于指定key对应的hash中那么将此field的值提取出来
    if (redisClient.hexists("map:" + movieId1, movieId2.toString)) {
      redisClient.hget("map:" + movieId1, movieId2.toString).toDouble
    } else
      0.0
  }

  def log(m: Double): Double = math.log(m) / math.log(2)

  /**
    * 核心算法，计算每个备选电影的预期评分
    *
    * @param recentRatings   用户最近评分的K个电影集合
    * @param candidateMovies 当前评分的电影的备选电影集合
    * @return 备选电影预计评分的数组，每一项是<movieId, maybe_rate>
    */
  def createUpdatedRatings(redisClient: Jedis, recentRatings: Array[(Int, Double)], candidateMovies: Array[Int]): Array[(Int, Double)] = {
    // 记录所有备选电影的评分
    // [(mid1, rate1),(mid1, rate2).....]
    val allSimilars = mutable.ArrayBuffer[(Int, Double)]()

    // 正向权重
    val increaseCounter = mutable.Map[Int, Int]()
    // 负向权重
    val reduceCounter = mutable.Map[Int, Int]()

    // 对每一个备选电影中的电影，都遍历一遍最近评过分的K个电影集合，计算与它们每一个的相关度(sim * rate)
    for (cmovieId <- candidateMovies; (rmovieId, rate) <- recentRatings) {
      // 获取指定的两个movie的相似度
      val sim = getSimilarityBetween2Movies(redisClient, rmovieId, cmovieId)
      // 如果两个movie的相似度大于minSimilarity则进行下一步
      if (sim > minSimilarity) {
        // 计算并保存相关度
        // [(cmovieId1, sim1_1),(cmovieId1, sim1_2),...,(cmovieId2, sim2_1),(cmovieId2, sim2_2),...]
        allSimilars += ((cmovieId, sim * rate))
        if (rate >= 3.0) {
          // 评分大于3.0，则对应增加权重
          increaseCounter(cmovieId) = increaseCounter.getOrElse(cmovieId, 0) + 1
        } else {
          // 评分小于3.0，则对应降低权重
          reduceCounter(cmovieId) = reduceCounter.getOrElse(cmovieId, 0) + 1
        }
      }
    }
    // 按照mid进行分组，获取一个备选电影的所有相关度
    // (cmovieId1,[(cmovieId1, sim1_1),(cmovieId1, sim1_2),...])
    allSimilars.toArray.groupBy { case (movieId, value) => movieId }
      // (cmovieId1,[(cmovieId1, sim1_1),(cmovieId1, sim1_2),...])
      .map { case (movieId, simArray) =>
        // simArray.map(_._2).sum / simArray.length：计算平均相关度
        // + log(increaseCounter.getOrElse[Int](movieId, 1)) - log(reduceCounter.getOrElse[Int](movieId, 1))：引入权重，为了降低影响使用log
        // 返回(movieId，相关度)
      (movieId, simArray.map(_._2).sum / simArray.length + log(increaseCounter.getOrElse[Int](movieId, 1)) - log(reduceCounter.getOrElse[Int](movieId, 1)))
      }.toArray
  }

  /**
    * 将备选电影的预期评分合并后回写到DB中
    *
    * @param newRecommends
    * @param startTimeMillis
    * @return
    */
  def updateRecommends2DB(mysqlClient: MySqlProxy, newRecommends: Array[(Int, Double)], uid: Int, startTimeMillis: Long): Unit = {

    val querySQL = "SELECT recs FROM " + Constants.DB_STREAM_RECS + " WHERE uid=?"
    val params = Array[Any](uid)

    // 最近一次推荐
    var lastTimeRecs = mutable.ArrayBuffer[(Int,Double)]()

    // 先将之前的推荐内容通过查询的形式获取到，存放在lastTimeRecs中
    mysqlClient.executeQuery(querySQL, params, new QueryCallback {
      override def process(rs: ResultSet): Unit = {
        if (rs.next) {
          rs.getString("recs").split("\\|").map{item=>
            val attr = item.split(":")
            lastTimeRecs.add((attr(0).toInt, attr(1).toDouble))
          }
        }
      }
    })

    // 将新推荐的数据添加到lastTimeRecs中
    lastTimeRecs.addAll(newRecommends.toIterable)

    // 对新旧融合的lastTimeRecs进行排序，并取出MAX_REC_COUNT个相关度最高的数据，转化为字符串形式
    val newRecs = lastTimeRecs.toList.sortWith(_._2 < _._2).slice(0, MAX_REC_COUNT).toArray
      .map(item=> item._1+":"+item._2).mkString("|")

    // 使用最新得到的MAX_REC_COUNT个最相关数据更新MySQL
    val updateSQL = "UPDATE "+Constants.DB_STREAM_RECS+" SET recs=? WHERE uid=? "
    val updateParams = Array[Any](newRecs,uid)
    mysqlClient.executeUpdate(updateSQL, updateParams)

  }

  def createKafkaStream(ssc: StreamingContext, brokers: String, topic: String) = {
    //kafka的地址 val brobrokers = "192.168.56.150:9092,192.168.56.151:9092,192.168.56.152:9092"

    //kafka消费者配置
    val kafkaParam = Map(
      "bootstrap.servers" -> brokers, //用于初始化链接到集群的地址
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      //用于标识这个消费者属于哪个消费团体
      "group.id" -> "streamingRecommenderConsumer",
      //如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性
      //可以使用这个配置，latest自动重置偏移量为最新的偏移量
      "auto.offset.reset" -> "latest",
      //如果是true，则这个消费者的偏移量会在后台自动提交
      "enable.auto.commit" -> (false: java.lang.Boolean)
    );

    //创建DStream，返回接收到的输入数据
    var stream = KafkaUtils.createDirectStream[String, String](ssc, LocationStrategies.PreferConsistent, ConsumerStrategies.Subscribe[String, String](Array(topic), kafkaParam))
    stream
  }

  def main(args: Array[String]) {

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    var sparkMaster = "local[*]"

    if (args.length != 4) {
      System.out.println("Usage: streaming <master>\n" +
        "  <master> is the sparkMaster address\n" +
        "  ===================================\n" +
        "  Using Default Values\n")
    } else {
      sparkMaster = args(0)
    }

    // 创建全局配置
    val params = scala.collection.mutable.Map[String, String]()
    // Spark配置
    params += "spark.cores" -> sparkMaster
    // MySQL配置
    params += "mysql.url" -> ConfigurationManager.config.getString(Constants.JDBC_URL)
    params += "mysql.user" -> ConfigurationManager.config.getString(Constants.JDBC_USER)
    params += "mysql.password" -> ConfigurationManager.config.getString(Constants.JDBC_PASSWORD)

    // 定义MySQL的配置对象
    implicit val mySqlConfig = new MySqlConfig(params("mysql.url"), params("mysql.user"), params("mysql.password"))

    val sparkConf = new SparkConf().setAppName("streamingRecommendingSystem")
      .setMaster(params("spark.cores"))
      .set("spark.executor.memory", "4g")

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(5))

    //kafka消费者配置
    val kafkaParam = Map(
      "bootstrap.servers" -> ConfigurationManager.config.getString(Constants.KAFKA_BROKERS), //用于初始化链接到集群的地址
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      //用于标识这个消费者属于哪个消费团体
      "group.id" -> "recommender-consumer-group",
      //如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性
      //可以使用这个配置，latest自动重置偏移量为最新的偏移量
      "auto.offset.reset" -> "latest",
      //如果是true，则这个消费者的偏移量会在后台自动提交
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    //创建DStream，返回接收到的输入数据
    var unifiedStream = KafkaUtils.createDirectStream[String, String](ssc, LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array(ConfigurationManager.config.getString(Constants.KAFKA_TO_TOPIC)), kafkaParam))

    // 将流式数据汇总的msg转化为指定的数据格式
    val dataDStream = unifiedStream.map { case msg =>
      val dataArr: Array[String] = msg.value().split("\\|")
      val userId = dataArr(0).toInt
      val movieId = dataArr(1).toInt
      val score = dataArr(2).toDouble
      val timestamp = dataArr(3).toLong
      (userId, movieId, score, timestamp)
    }

    // (userId, movieId, score, timestamp)
    dataDStream.foreachRDD(rdd => {
      if (!rdd.isEmpty) {
        rdd.map { case (userId, movieId, score, timestamp) =>

          // 从redis池中获取redis连接
          val redisPool = CreateRedisPool()
          val redisClient = redisPool.borrowObject()

          // 从MySQL池中获取MySQL连接
          val mysqlPool = CreateMySqlPool()
          val mysqlClient = mysqlPool.borrowObject()

          //从Redis中获取当前用户近期K次评分记录
          val recentRatings = getUserRecentRatings(redisClient, USER_RECENTLY_RATING_COUNT, userId, movieId, score)

          //获取当前评分电影相似的K个备选电影
          val candidateMovies = getSimilarMovies(redisClient, mysqlClient, movieId, userId, SIMILAR_MOVIE_COUNT)

          //为备选电影推测评分结果
          // (mid, rating)
          val updatedRecommends = createUpdatedRatings(redisClient, recentRatings, candidateMovies)

          //当前推荐与往期推荐进行Merge结果回写到MySQL
          updateRecommends2DB(mysqlClient, updatedRecommends, userId, timestamp)

          redisPool.returnObject(redisClient)
          mysqlPool.returnObject(mysqlClient)

        }.count()
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}

