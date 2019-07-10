import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * Created by fucheng on 2019/7/9.
  */
object WordSplit {
  val logger = LoggerFactory.getLogger(WordCount.getClass())

  def main(args: Array[String]) {
    /**
      * 1 创建SparkConf()
      *     - 设置提交入口：
      * 提交到本地：.setMaster("local[*]")
      * 提交的spark集群：.setMaster("spark://Node02:7077")
      *     - 设置AppName:.setAppName("WordCount")
      *     - 设置使用CPU核数：.set("spark.executor.cores","1")
      */
    val conf = new SparkConf().setMaster("local[*]").setAppName("WordCount")

    /**
      * 2 创建SparkContext
      * 该对象是提交spark App的入口
      */
    //
    val sc = new SparkContext(conf)

    /**
      * 3 使用sc创建RDD并执行相应的transformation和action
      * 先按换行符 /n 切分
      * 再按"   "（两个空格切分）
      * 再切片
      * 如果是o就设为空（,）
      */
    val file = sc.textFile("file:///D:/data/train.txt")
      .flatMap(_.split("/n"))
      .flatMap(_.split("  "))

      val words = file
      .map(x => {if(x.slice(x.length - 1, x.length)!="o") (x.slice(0, x.length - 2), x.slice(x.length - 1, x.length))})
      .map((_,1))
      .reduceByKey(_+_, 1)
      .sortBy(_._2,false)

    val output = words.map(x=>(x._1))



    /**
      * 4 保存结果
      */
    output.collect().foreach(println _)
    output.saveAsTextFile("file:///D:/data/output")

    /**
      * 5 停止sc，结束该任务
      */
    sc.stop()

    logger.info("complete!")

  }

}
