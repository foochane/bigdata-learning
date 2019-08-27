package com.atguigu.offline

import java.text.DecimalFormat

import com.atguigu.commons.conf.ConfigurationManager
import com.atguigu.commons.constant.Constants
import com.atguigu.commons.model.{MovieRecs, MySqlConfig, Recommendation, UserRecs}
import com.atguigu.commons.pool.{CreateRedisPool}
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import org.jblas.DoubleMatrix
import scala.collection.JavaConversions._

object RecommenderTrainerApp {

  val df = new DecimalFormat("#.00")

  def main(args: Array[String]): Unit = {
    var sparkMaster = "local[*]"

    if (args.length != 1) {
      System.out.println("Usage: alsRecommender <master>\n" +
        "  ===================================\n" +
        "  Using Default Values\n")
    } else {
      sparkMaster = args(0)
    }

    //创建全局配置
    val params = scala.collection.mutable.Map[String, String]()
    // Spark配置
    params += "spark.cores" -> sparkMaster
    // MySQL配置
    params += "mysql.url" -> ConfigurationManager.config.getString(Constants.JDBC_URL)
    params += "mysql.user" -> ConfigurationManager.config.getString(Constants.JDBC_USER)
    params += "mysql.password" -> ConfigurationManager.config.getString(Constants.JDBC_PASSWORD)
    // Redis配置
    params += "redis.host" -> ConfigurationManager.config.getString(Constants.REDIS_HOST)
    params += "redis.port" -> ConfigurationManager.config.getString(Constants.REDIS_PORT)

    // 定义MySqlDB的配置对象
    implicit val mySqlConfig = new MySqlConfig(params("mysql.url"), params("mysql.user"), params("mysql.password"))

    // 创建SparkConf
    val conf = new SparkConf().setAppName("RecommenderApp").setMaster(params("spark.cores")).set("spark.executor.memory", "6G")

    // 创建SparkSession
    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    // 计算推荐数据
    calculateRecs(spark, Constants.MAX_RECOMMENDATIONS)

    spark.close()

  }

  /**
    * 计算推荐数据
    *
    * @param maxRecs
    */
  def calculateRecs(spark: SparkSession, maxRecs: Int)(implicit mySqlConfig: MySqlConfig): Unit = {

    import spark.implicits._

    // 通过SparkSQL从MySQL中读取打分数据
    val ratings = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_RATING)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .select($"mid", $"uid", $"score")
      .cache

    // 通过select选择rating表中的uid列，去重，将Row类型转化为Int类型，缓存
    val users = ratings
      .select($"uid")
      .distinct
      .map(r => r.getAs[Int]("uid"))
      .cache

    // 通过select选择rating表中的mid列，去重，将Row类型转化为Int类型，缓存
    val movies = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_MOVIE)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .select($"mid")
      .distinct
      .map(r => r.getAs[Int]("mid"))
      .cache

    // 将从MySQL获取到的rating数据，转化为训练数据格式
    val trainData = ratings.map { line =>
      Rating(line.getAs[Int]("uid"), line.getAs[Int]("mid"), line.getAs[Double]("score"))
    }.rdd.cache()

    // rank：要使用的特征个数，即维度
    // iterations：ALS算法迭代次数
    // lambda：正则化参数
    val (rank, iterations, lambda) = (50, 5, 0.01)
    // 将训练数据和参数传入ALS模型进行训练，返回结果模型
    val model = ALS.train(trainData, rank, iterations, lambda)

    // 计算为用户推荐的电影集合矩阵 RDD[UserRecommendation(id: Int, recs: Seq[Rating])]
    calculateUserRecs(maxRecs, model, users, movies)
    // 计算与某个电影最相似的N个电影
    calculateProductRecs(maxRecs, model, movies)

    // 将缓存的数据从内存中移除
    ratings.unpersist()
    users.unpersist()
    movies.unpersist()
    trainData.unpersist()

  }


  /**
    * 计算为用户推荐的电影集合矩阵 RDD[UserRecommendation(id: Int, recs: Seq[Rating])]
    *
    * @param maxRecs
    * @param model
    * @param users
    * @param products
    */
  private def calculateUserRecs(maxRecs: Int, model: MatrixFactorizationModel, users: Dataset[Int], products: Dataset[Int])(implicit mySqlConfig: MySqlConfig): Unit = {

    import users.sparkSession.implicits._

    // 对userId和mid执行笛卡尔积
    /**
        users:
        +-------+
        |userId|
        +-------+
        |   1   |
        |   2   |
        |   3   |
        |   4   |
        +-------+

        products：
        +---+
        |mid|
        +---+
        | 1 |
        | 2 |
        | 3 |
        | 4 |
        +---+

        userProductsJoin：
        +---------+
        |userId|mid|
        +---------+
        |  1  | 1 |
        |  1  | 2 |
        |  1  | 3 |
        |  1  | 4 |
        |  2  | 1 |
        |  2  | 2 |
        |  2  | 3 |
        |  2  | 4 |
          ... ...
        +---------+
      */
    val userProductsJoin = users.crossJoin(products)

    // 对userProductsJoin的每一行进行操作，每一行数据为：(userId, mid)，转化为RDD
    val userRating = userProductsJoin.map { row => (row.getAs[Int](0), row.getAs[Int](1)) }.rdd

    // 定义RatingOrder类，用于排序
    object RatingOrder extends Ordering[Rating] {
      def compare(x: Rating, y: Rating) = y.rating compare x.rating
    }

    // 根据已经训练完成后的模型对userRating进行预测
    // 获取到的预测结果数据是：Rating(userId, productId, rating)
    val recommendations = model.predict(userRating)
      // 去除分数小于0的数据
      .filter(_.rating > 0)
      // 按照userId进行分组：(userId, Iterable(Rating))
      .groupBy(p => p.user)
      // (userId, Iterable(Rating))
      .map { case (uid, predictions) =>
        // 按照RatingOrder中的compare方法对评分进行排序
        // sorted()需要传入一个继承Ordering的类，并使用这个类的compare方法进行排序
        val recommendations = predictions.toSeq.sorted(RatingOrder)
          // 取前maxRecs个数据
          // Seq(Rating)
          .take(maxRecs)
          // 将前maxRecs个数据转换为Recommendation类
          // Seq(Recommendation)
          .map(p => Recommendation(p.product, df.format(p.rating).toDouble))

        // 以(userId, (mid1, rate1)|(mid2, rate2)|(mid3, rate3)|...)格式返回结果
        UserRecs(uid, recommendations.mkString("|"))
      }.toDF()

    // 将推荐结果写入MySQL
    recommendations.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_USER_RECS)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()
  }

  /**
    * 计算每个电影的前N个最相似电影
    * @param maxRecs
    * @param model
    * @param products
    * @param mySqlConfig
    */
  private def calculateProductRecs(maxRecs: Int, model: MatrixFactorizationModel, products: Dataset[Int])(implicit mySqlConfig: MySqlConfig): Unit = {

    import products.sparkSession.implicits._

    // 用于sorted()排序
    object RatingOrder extends Ordering[(Int, Int, Double)] {
      def compare(x: (Int, Int, Double), y: (Int, Int, Double)) = y._3 compare x._3
    }

    // 获取电影特征矩阵
    // model.productFeatures的返回结果是(mid, factor:Array[Double])
    val productsVectorRdd = model.productFeatures
      .map { case (movieId, factor) =>
        // 将Array[Double]类型的factor转化为行向量
        val factorVector = new DoubleMatrix(factor)
        // 返回mid和行向量形式的特征向量
        (movieId, factorVector)
      }

    // 最低相似度
    val minSimilarity = 0.6

    // 对电影特征矩阵RDD进行笛卡儿积，获得每个电影与任意其他电影的特征向量对应关系：((movieId1, vector1), (movieId2, vector2))
    val movieRecommendation = productsVectorRdd.cartesian(productsVectorRdd)
      // 过滤掉相同电影之间的对应关系
      .filter { case ((movieId1, vector1), (movieId2, vector2)) => movieId1 != movieId2 }
      // 计算两步电影之间的相似度
      .map { case ((movieId1, vector1), (movieId2, vector2)) =>
        val sim = cosineSimilarity(vector1, vector2)
        (movieId1, movieId2, sim)
      // 过滤掉相似度小于最低相似度的电影相似度项
      // (movieId1, movieId2, sim)
      }.filter(_._3 >= minSimilarity)
      // 按照movieId1进行分组聚合
      // (movieId1, Iterable(movieId1, movieId2, sim))
      .groupBy(p => p._1)
      // 对分组数据进行处理，取最相似的N个电影
      // (movieId1, Iterable(movieId1, movieId2, sim))
      .map { case (mid: Int, predictions: Iterable[(Int, Int, Double)]) =>
        // 首先按照相似度大小排序
        val sortedRecs = predictions.toSeq.sorted(RatingOrder)

        // 获取Redis连接
        val redisPool = CreateRedisPool()
        val client = redisPool.borrowObject()

        // 向Redis插入某个电影对应的50个最相似的电影
        // 实时推荐算法中会用到此数据
        client.del("set:"+mid)
        // 插入的数据格式为：movieId2:similarity
        client.sadd("set:"+mid, sortedRecs.take(50).map(item => item._2 + ":" + item._3):_*)

        // 将所有的电影相似度对应关系都保存到redis中
        client.del("map:"+mid)
        client.hmset("map:"+mid, sortedRecs.map(item => (item._2.toString,item._3.toString)).toMap)

        // 将client返回redis连接池
        redisPool.returnObject(client)

        // 选取最相似的maxRecs个数据转换为Recommendation类，再以(mid1, (mid2,sim1_2)|(mid6,sim1_6)|(mid4,sim1_4)|...)
        val recommendations = sortedRecs.take(maxRecs).map(p => Recommendation(p._2, df.format(p._3).toDouble))
        MovieRecs(mid, recommendations.mkString("|"))
      }.toDF().cache()

    // 将数据插入到MySQL
    movieRecommendation.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_MOVIE_RECS)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    // 将缓存数据从内存中移除
    movieRecommendation.unpersist()
  }

  /**
    * 余弦相似度
    *
    * @param vec1
    * @param vec2
    * @return
    */
  private def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }

  //private def updateRedis()

}
