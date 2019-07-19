import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * Created by fucheng on 2019/7/16.
  */
object SparkHiveDemo {
  def main(args: Array[String]): Unit = {

    //设置HADOOP_USER_NAME，否则会有权限问题,后面已经在修改过hadoop配置文件了，所以可以不配置了
//    System.setProperty("HADOOP_USER_NAME", "hadoop")
//    System.setProperty("user.name", "hadoop")

    // 创建sparkConf
    //    val sparkConf = new SparkConf()
    //      .setAppName("Test Spark ")
    //      .setMaster("spark://Node02:7077")
    //      .set("spark.dynamicAllocation.enabled", "false")
    //      .set("spark.executor.memory", "1g")
    //      .set("spark.executor.instances", "1")
    //      .set("spark.executor.cores", "2")
    //      .set("spark.driver.memory", "2g")
    //      .set("spark.driver.cores", "1")
    //      .set("spark.ui.port", "4040")
    //      .setExecutorEnv("SPARK_JAVA_OPTS", " -Xms8024m -Xmx12040m -XX:MaxPermSize=30840m")
    //      .set("spark.sql.warehouse.dir", "/user/hive/warehouse/")
    //      .set("spark.authenticate.secret", "true")

    /**
      * 注意在windows本地运行的时候setMaster要设为local[*]，打包到集群里运行的时候再改为spark://Node02:7077
      * 如果在windows本地使用spark://Node02:7077，应该添加spark.driver.host
      */
    val sparkConf = new SparkConf()
      .setAppName("Test Spark ")
//      .setMaster("local[*]")
      //调用集群
      .setMaster("spark://Node02:7077")
//      .setJars(List("D:\\home\\code\\bigdata\\bigdata-learning\\06spark\\sparksql\\target\\sparksql-1.0-SNAPSHOT.jar"))
//      .setIfMissing("spark.driver.host", "192.168.1.103")

    // 创建sparkSession
    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("show databases").show()
    spark.sql("SELECT * FROM analysis.test_transheader").show()
    spark.close()
  }
}


/*
注意要打开metastore的服务端：hive --service metastore

提交命令：
spark-submit \
--class SparkHiveDemo \
--master spark://Node02:7077 \
--jars /usr/share/java/mysql-connector-java-5.1.45.jar \
./sparksql-1.0-SNAPSHOT.jar
*/