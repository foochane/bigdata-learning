import java.util.Properties

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by fucheng on 2019/7/16.
  */
object SaveData2MySQL2 {

  def main(args: Array[String]): Unit = {

    // 创建sparkConf
    val sparkConf = new SparkConf().setAppName("Test Spark ")
      .setMaster("local[*]")

    // 创建sparkSession
    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    //读取数据
    val rdd = spark.sql("SELECT * FROM analysis.test_transheader")
    rdd.show()

    //数据库相关配置
    val url = "jdbc:mysql://localhost:3306/business_data?useUnicode=true&characterEncoding=utf8"
    val prop = new Properties()
    prop.setProperty("user", "root")
    prop.setProperty("password", "123456")
    prop.setProperty("useSSL", "false")
    val tableName = "test1" //表名

    //写入mysql数据库 ， Overwrite:覆盖 ;Append: 追加; Ignore:忽略
    rdd.toDF().write.mode(SaveMode.Append) jdbc(url, tableName, prop)

    spark.stop()
  }

}


/*
注意要打开metastore的服务端：hive --service metastore

spark-submit \
--class SaveData2MySQL \
--master spark://Node02:7077 \
--jars /usr/share/java/mysql-connector-java-5.1.45.jar \
/home/hadoop/business-data-spark-1.0-SNAPSHOT.jar
 */