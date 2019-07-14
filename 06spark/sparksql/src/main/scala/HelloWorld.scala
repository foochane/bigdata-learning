import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

/**
  * Created by fucheng on 2019/7/11.
  *
  * Spark SQL中SparkSession是创建DataFrames和执行SQL的入口
  * SparkSession实质上是SQLContext和HiveContext的组合，所以在SQLContext和HiveContext上可用的API在SparkSession上同样是可以使用的。
  * SparkSession内部封装了sparkContext，所以计算实际上是由sparkContext完成的。
  */
object HelloWorld {

  def main(args: Array[String]): Unit = {


    //    创建方法1
    /*
      //获取sparkConf
        val conf = new SparkConf().setAppName("HelloWorld").setMaster("local[*]")

        val sc = new SparkContext(conf)

        //获取sparkSession
        // val spark = new SparkSession(sc)
        val spark = SparkSession.builder().config(conf).getOrCreate()
    */

    //  创建方法2
    // 如果需要hive支持，添加 .enableHiveSupport()
    val spark = SparkSession
      .builder()
      .appName("HelloWorld")
      .master("local[*]")
      .getOrCreate()


    //生成DataFrame
    val df = spark.read.json("sparksql/src/main/resources/employees.json")

    println("df.show()")
    df.show()

    println("df.filter($\"salary\">21).show()")
    //    import spark.implicits._的引入是用于将DataFrames隐式转换成RDD，使df能够使用RDD中的方法。
    import spark.implicits._
    df.filter($"salary" > 3500).show()


    //DSL
    println("df.select(\"name\").show()")
    df.select("name").show()

    //SQL
    println("spark.sql(\"select *from people where salary>3500\").show()")
    df.createOrReplaceTempView("people")
    spark.sql("select * from people where salary>3500").show()

    spark.close()
    //    sc.stop()
  }

}


