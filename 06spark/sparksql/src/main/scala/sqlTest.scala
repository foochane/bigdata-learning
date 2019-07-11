import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

/**
  * Created by fucheng on 2019/7/11.
  */
object sqlTest {

  def main(args: Array[String]): Unit = {
    //获取sparkConf
    val conf = new SparkConf().setAppName("sqltest").setMaster("local[*]")

    val sc = new SparkContext(conf)

    //获取sparkSession
//    val spark = new SparkSession(sc)
    val spark = SparkSession.builder().config(conf).getOrCreate()
    //生成DataFrame
    val df = spark.read.json("D:\\home\\code\\bigdata\\bigdata-learning\\06spark\\sparksql\\src\\main\\resources\\employees.json")

    //DSL
    df.select("name").show()

    //SQL
    df.createTempView("people")
    spark.sql("select *from people").show()

    spark.close()
    sc.stop()
  }

}


