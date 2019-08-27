package com.atguigu.offline

import com.atguigu.commons.conf.ConfigurationManager
import com.atguigu.commons.constant.Constants
import com.atguigu.commons.model.MySqlConfig
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.math.sqrt

object ALSTrainerApp {

  def main(args: Array[String]): Unit = {

    var sparkMaster = "local[*]"

    if (args.length != 1) {
      System.out.println("Usage: alsRecommender <master>\n" +
        "  ===================================\n" +
        "  Using Default Values\n")
    } else {
      sparkMaster = args(0)
    }

    val params = scala.collection.mutable.Map[String, String]()
    params += "spark.cores" -> sparkMaster

    params += "mysql.url" -> ConfigurationManager.config.getString(Constants.JDBC_URL)
    params += "mysql.user" -> ConfigurationManager.config.getString(Constants.JDBC_USER)
    params += "mysql.password" -> ConfigurationManager.config.getString(Constants.JDBC_PASSWORD)

    val mySqlConfig = new MySqlConfig(params("mysql.url"), params("mysql.user"), params("mysql.password"))

    val conf = new SparkConf().setAppName("RecommenderTrainerApp").setMaster(params("spark.cores")).set("spark.executor.memory", "6G")

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    import spark.implicits._

    val ratings = spark.read
      .format("jdbc")
      .option("url", mySqlConfig.url)
      .option("dbtable", Constants.DB_RATING)
      .option("user", mySqlConfig.user)
      .option("password", mySqlConfig.password)
      .load()
      .select($"mid", $"uid", $"score")
      .cache

    val users = ratings
      .select($"uid")
      .distinct
      .map(r => r.getAs[Int]("uid"))
      .cache

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

    val trainData = ratings.map { line =>
      Rating(line.getAs[Int]("uid"), line.getAs[Int]("mid"), line.getAs[Double]("score"))
    }.rdd.cache()

    println(parameterAdjust(trainData, trainData))

    spark.stop()
  }

  def parameterAdjust(trainData: RDD[Rating], realRatings: RDD[Rating]): (Int, Double) = {
    val evaluations =
      for (rank <- Array(10, 50);
           lambda <- Array(1.0, 0.0001))
        yield {
          val model = ALS.train(trainData, rank, 10, lambda)
          val rmse = computeRmse(model, realRatings)
          ((rank, lambda), rmse)
        }
    val ((rank, lambda), rmse) = evaluations.sortBy(_._2).head
    println("After parameter adjust, the best rmse = " + rmse)
    (rank, lambda)
  }

  def computeRmse(model: MatrixFactorizationModel, realRatings: RDD[Rating]): Double = {
    val testingData = realRatings.map { case Rating(user, product, rate) =>
      (user, product)
    }

    val prediction = model.predict(testingData).map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }

    val realPredict = realRatings.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.join(prediction)

    sqrt(realPredict.map { case ((user, product), (rate1, rate2)) =>
      val err = rate1 - rate2
      err * err
    }.mean()) //mean = sum(list) / len(list)
  }

}
