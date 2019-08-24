import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by fucheng on 2019/7/19 14:59
  */
object CreateRDD {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setAppName("CreateRDD")
      .setMaster("spark://Node02:7077")
//      .setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    /**
      * 1 从集合中创建RDD
      */
    val rdd = sc.parallelize(List(1,2,3))
    val rdd1 = sc.makeRDD(Array("a","b","c"))

    rdd.foreach(println _)
    rdd1.foreach(println _)
    sc.stop()
  }
}







