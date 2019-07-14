
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by fucheng on 2019/7/14.
  * 在Spark SQL中SparkSession是创建DataFrames和执行SQL的入口，创建DataFrames有三种方式，
  *  1 通过Spark的数据源进行创建。
  *  2 从一个存在的RDD进行转换，
  *  3 从Hive Table进行查询返回，
  */
object DataFrames2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("DataFrames2")


    //从RDD进行转换成DataFrame

    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().config(conf).getOrCreate()

    import spark.implicits._
    val rdd = sc.textFile("sparksql/src/main/resources/people.txt")
    val df = rdd.map(_.split(","))
      .map(paras => (paras(0), paras(1).trim().toInt))
      .toDF("name", "age")

    df.show()

    sc.stop()
    spark.close()


  }

}
