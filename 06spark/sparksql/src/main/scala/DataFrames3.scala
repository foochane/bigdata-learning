
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * Created by fucheng on 2019/7/14.
  * 在Spark SQL中SparkSession是创建DataFrames和执行SQL的入口，创建DataFrames有三种方式，
  *  1 通过Spark的数据源进行创建。
  *  2 从一个存在的RDD进行转换，
  *  3 从Hive Table进行查询返回，
  */
object DataFrames3 {
  def main(args: Array[String]): Unit = {

    /**
      * 若要把Spark SQL连接到一个部署好的Hive上，你必须把hive-site.xml复制到 Spark的配置文件目录中($SPARK_HOME/conf)。
      * 即使没有部署好Hive，Spark SQL也可以运行,但是如果没有部署好Hive，
      * Spark SQL会在当前的工作目录中创建出自己的Hive 元数据仓库，叫作 metastore_db。
      * 此外，如果尝试使用 HiveQL 中的 CREATE TABLE (并非 CREATE EXTERNAL TABLE)语句来创建表，
      * 这些表会被放在你默认的文件系统中的 /user/hive/warehouse 目录中(如果你的 classpath 中有配好的 hdfs-site.xml，
      * 默认的文件系统就是 HDFS，否则就是本地文件系统)
      */

    val sparkConf = new SparkConf().setAppName("DataFrames3")

    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()



    spark.sql("show databases").collect().foreach(println)
//    spark.sql("CREATE TABLE IF NOT EXISTS t_people (name STRING, age INT)")
//    spark.sql("LOAD DATA LOCAL INPATH 'sparksql/src/main/resources/people.txt' INTO TABLE t_people")
//
//    // Queries are expressed in HiveQL
//    spark.sql("SELECT * FROM t_people").show()


    spark.close()


  }

}

/*
spark-submit \
--class DataFrames3 \
--master spark://Node02:7077 \
--jars /usr/share/java/mysql-connector-java-5.1.45.jar \
./sparksql-1.0-SNAPSHOT.jar

*/