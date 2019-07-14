import org.apache.spark.sql.SparkSession

/**
  * Created by fucheng on 2019/7/14.
  *
  * 在Spark SQL中SparkSession是创建DataFrames和执行SQL的入口，创建DataFrames有三种方式，
  *  1 通过Spark的数据源进行创建。
  *  2 从一个存在的RDD进行转换，
  *  3 从Hive Table进行查询返回，
  *
  */
object DataFrames1 {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("HelloWorld")
      .master("spark://Node02:7077")
      .getOrCreate()

    //从数据源创建DataFrames
    val df = spark.read.json("sparksql/src/main/resources/employees.json")
    df.show()

  }

}
