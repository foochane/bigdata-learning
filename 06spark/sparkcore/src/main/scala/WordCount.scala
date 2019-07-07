import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * Created by foochane on 2019/7/7.
  */
object WordCount {
  val logger = LoggerFactory.getLogger(WordCount.getClass())

  def main(args: Array[String]) {
    /**
      * 1 创建SparkConf()
      *     - 设置提交入口：
      *         提交到本地：.setMaster("local[*]")
      *         提交的spark集群：.setMaster("spark://Node02:7077")
      *     - 设置AppName:.setAppName("WordCount")
      *     - 设置使用CPU核数：.set("spark.executor.cores","1")
      */
    val conf = new SparkConf().setMaster("local[*]").setAppName("WordCount")

    /**
      * 2 创建SparkContext
      *   该对象是提交spark App的入口
      */
    //
    val sc = new SparkContext(conf)

    /**
      * 3 使用sc创建RDD并执行相应的transformation和action
      */
    val result = sc.textFile("hdfs://Node02:9000/test/test.txt").flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_, 1).sortBy(_._2, false)


    /**
      * 4 保存结果
      */
    result.collect().foreach(println _)
    //    result.saveAsTextFile("hdfs://Node02:9000/test/output")


    /**
      *  5 停止sc，结束该任务
      */
    sc.stop()

    logger.info("complete!")

  }

}
