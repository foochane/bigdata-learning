package com.atguigu.statisticsRecommender

import java.text.SimpleDateFormat
import java.util.Date

import com.atguigu.commons.conf.ConfigurationManager
import com.atguigu.commons.constant.Constants
import com.atguigu.commons.model.{Movie, MovieRating, MySqlConfig}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

// 基于统计的推荐
object StatisticsRecommender {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("statistics").setMaster("local[*]")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val mySqlConfig = MySqlConfig(ConfigurationManager.config.getString("jdbc.url"),
      ConfigurationManager.config.getString("jdbc.user"),
      ConfigurationManager.config.getString("jdbc.password"))

    // 获取原始用户评分表数据
    import spark.implicits._
    val ratingDS = spark.read
      .format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_RATING)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .load()
      .as[MovieRating]

    ratingDS.cache()

    ratingDS.createOrReplaceTempView("ratings")

    spark.udf.register("halfUp",(num:Double, scale:Int) => {
      val bd = BigDecimal(num)
      bd.setScale(scale, BigDecimal.RoundingMode.HALF_UP).doubleValue()
    })

    // 统计电影的平均评分
    val movieAverageScoreDF = spark.sql("select mid, halfUp(avg(score),2) avg from ratings group by mid")

    movieAverageScoreDF.createOrReplaceTempView("averageMovies")

    // 将电影的平均评分数据写入MySQL
    movieAverageScoreDF
      .write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_AVERAGE_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    // 获取原始电影信息表
    val moviesDS = spark.read
      .format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_MOVIE)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .load()
      .as[Movie]

    moviesDS.createOrReplaceTempView("movies")

    // 将电影平均评分表与电影信息表进行关联
    val movieWithScore = spark.sql("select a.mid, genres, if(isnull(b.avg),0,b.avg) score from movies a left join averageMovies b on a.mid = b.mid")

    spark.udf.register("splitGe",(genres:String) => {
      genres.split("\\|")
    })

    // 类别Top10电影统计
    val genresTop10Movies = spark.sql("select * from (select " +
      "mid," +
      "gen," +
      "score, " +
      "row_number() over(partition by gen order by score desc) rank " +
      "from " +
      "(select mid,score,explode(splitGe(genres)) gen from movieWithScore) genresMovies) rankGenresMovies " +
      "where rank <= 10")

    // 将统计结果写入MySQL
    genresTop10Movies.write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_GENRES_TOP_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()


    // 统计优质电影
    val youzhidianyingDF = spark.sql("SELECT " +
      "average.mid, " +
      "halfUp(average.avg,2) avg, " +
      "average.count, " +
      "row_number() over(order by average.avg desc,average.count desc) rank " +
      " FROM " +
      " (SELECT mid, avg(score) avg, count(*) count FROM ratings GROUP BY mid) average WHERE avg > 3.5 AND count > 50")

    // 将统计结果写入MySQL
    youzhidianyingDF
      .write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_RATE_MORE_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    // 近期TOP10热门电影统计
    val hotMovies = spark.sql("select " +
      "mid, " +
      "count(*) count  " +
      "from  ratings " +
      "where timestamp > (select max(timestamp)-7776000 max from ratings) " +
      "group by mid " +
      "order by count desc " +
      "limit 10")

    // 将统计结果写入MySQL
    hotMovies
      .write.format("jdbc")
      .option("url",mySqlConfig.url)
      .option("dbtable",Constants.DB_RATE_MORE_RECENTLY_MOVIES)
      .option("user",mySqlConfig.user)
      .option("password",mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()

    rateMoreRecently(spark)

    spark.stop()
  }

  /**
    * 近期电影评分个数统计（30天）
    */
  def rateMoreRecently(spark: SparkSession)(implicit mySqlConfig: MySqlConfig): Unit = {

    val simpleDateFormat = new SimpleDateFormat("yyyyMM")
    spark.udf.register("changDate", (x: Long) => simpleDateFormat.format(new Date(x * 1000L)).toLong)

    // 从ratings表中获取所有数据，并将timestamp的格式转化为yyyyMM的形式
    val yeahMonthOfRatings = spark.sql("select mid, uid, score, changDate(timestamp) as yeahmonth from ratings")

    // 将转换了时间格式的数据创建为新的临时表
    yeahMonthOfRatings.createOrReplaceTempView("ymRatings")

    // 将一个月中评分次数最多的电影视为本月热门电影
    // 统计一个月中评分次数最多的电影
    val rateMoreRecentlyDF = spark.sql("select mid, count(mid) as count,yeahmonth from ymRatings group by yeahmonth,mid order by yeahmonth desc,count desc")

    rateMoreRecentlyDF.write
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_RATE_MORE_RECENTLY_MOVIES)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .mode(SaveMode.Overwrite)
      .save()
  }

}
