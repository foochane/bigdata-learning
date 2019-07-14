package com.foochane

import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * Created by fucheng on 2019/7/14.
  */
object WordCount {
  val logger = LoggerFactory.getLogger(WordCount.getClass())

  def main(args: Array[String]) {
    /**
      * 1 创建SparkConf()
      *     - 设置AppName:.setAppName("WordCount")
      *     - 设置提交入口：
      *         - 提交到本地：.setMaster("local[*]")
      *         - 提交的spark集群：.setMaster("spark://Node02:7077")
      *     - 设置使用CPU核数：.set("spark.executor.cores","1")
      */


    /**
      * 运行方式1  使用IDEA在本地运行
      */
    //    val conf = new SparkConf()
    //      .setAppName("WordCount")
    //      .setMaster("local[*]")

    /**
      * 运行方式2  使用IDEA在本地编译然后提交到spark集群运行
      */
    /*
    打包后，在spark集群中使用如下命令运行
    spark-submit\
    --class com.foochane.WordCount\
    --master spark://Node02:7077\
    --executor-memory 1G \
    --total-executor-cores 2 \
    WordCount-1.0-SNAPSHOT.jar
    */
    //    val conf = new SparkConf()
    //      .setAppName("WordCount")

    /**
      * 运行方式3  使用IDEA在本地远程调用Spark集群运行
      *
      * 将IDEA作为Driver来提交应用程序，需要添加如下配置
      *   - 最终需要运行的Jar包：编译打包好的jar包路径
      *   - Driver程序的地址：本机的ip地址
      *   - Master的提交地址：指定master运行的地址
      */
    val conf = new SparkConf()
      .setAppName("WordCount")
      .setMaster("spark://Node02:7077")
      .setJars(List("D:\\home\\code\\bigdata\\bigdata-learning\\WordCount\\target\\WordCount-1.0-SNAPSHOT.jar"))
      .setIfMissing("spark.driver.host", "192.168.1.103")


    /**
      * 2 创建SparkContext
      * 该对象是提交spark App的入口
      */
    val sc = new SparkContext(conf)

    /**
      * 3 使用sc创建RDD并执行相应的transformation和action
      */


    val result = sc.textFile("hdfs://Node02:9000/test/test.txt")
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _, 1)
      .sortBy(_._2, false)


    /**
      * 4 保存结果
      */
    result.collect().foreach(println _)
    //    result.saveAsTextFile("hdfs://Node02:9000/test/output")


    /**
      * 5 停止sc，结束该任务
      */
    sc.stop()

    logger.info("complete!")

  }

}

