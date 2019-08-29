package com.atguigu.online

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

import com.atguigu.model.StartupReportLogs
import com.atguigu.utils.JSONUtil
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaCluster, KafkaUtils}
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}

import scala.collection.mutable

object OnlineScala {

  def main(args: Array[String]): Unit = {
    val streamingContext = StreamingContext.getActiveOrCreate(loadProperties("streaming.checkpoint.path"), createContextFunc())

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def createContextFunc(): () => _root_.org.apache.spark.streaming.StreamingContext = {
    () => {
      // 创建sparkConf
      val sparkConf = new SparkConf().setAppName("online").setMaster("local[*]")
      // 配置sparkConf优雅的停止
      sparkConf.set("spark.streaming.stopGracefullyOnShutdown", "true")
      // 配置Spark Streaming每秒钟从kafka分区消费的最大速率
      sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "100")
      // 指定Spark Streaming的序列化方式为Kryo方式
      sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // 指定Kryo序列化方式的注册器
      sparkConf.set("spark.kryo.registrator", "com.atguigu.registrator.MyKryoRegistrator")

      // 创建streamingContext
      val interval = loadProperties("streaming.interval")
      val streamingContext = new StreamingContext(sparkConf, Seconds(interval.toLong))
      // 启动checkpoint
      val checkPointPath = loadProperties("streaming.checkpoint.path")
      streamingContext.checkpoint(checkPointPath)

      // 获取kafka配置参数
      val kafka_brokers = loadProperties("kafka.broker.list")
      val kafka_topic = loadProperties("kafka.topic")
      val kafka_topic_set: Set[String] = Set(kafka_topic)
      val kafka_group = loadProperties("kafka.groupId")

      // 创建kafka配置参数Map
      val kafkaParam = Map(
        "bootstrap.servers" -> kafka_brokers,
        "group.id" -> kafka_group
      )

      // 创建kafkaCluster
      val kafkaCluster = new KafkaCluster(kafkaParam)
      // 获取Zookeeper上指定主题分区的offset
      // topicPartitionOffset: HashMap[(TopicAndPartition, offset)]
      val topicPartitionOffset = getOffsetFromZookeeper(kafkaCluster, kafka_group, kafka_topic_set)

      // 创建DirectDStream
      // KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](streamingContext, kafkaParam, kafka_topic_set)
      val onlineLogDStream = KafkaUtils.createDirectStream[String, String,
        StringDecoder, StringDecoder, String](streamingContext, kafkaParam, topicPartitionOffset,
        (mess: MessageAndMetadata[String, String]) => mess.message())

      // checkpoint原始数据
      onlineLogDStream.checkpoint(Duration(10000))

      // 过滤垃圾数据
      val onlineFilteredDStream = onlineLogDStream.filter {
        case message =>
          var success = true

          if (!message.contains("appVersion") && !message.contains("currentPage") &&
            !message.contains("errorMajor")) {
            success = false
          }

          if (message.contains("appVersion")) {
            val startupReportLog = json2StartupLog(message)
            if (startupReportLog.getUserId == null || startupReportLog.getAppId == null)
              success = false
          }
          success
      }

      // 完成需求统计并写入HBase
      onlineFilteredDStream.foreachRDD {
        // {"activeTimeInMs":749182,"appId":"app00001","appPlatform":"ios","appVersion":"1.0.1","city":"Hunan","startTimeInMs":1534301120672,"userId":"user116"}
        rdd =>
          rdd.foreachPartition {
            items =>
              val table = getHBaseTabel(getProperties())
              while (items.hasNext) {
                val item = items.next()
                val startupReportLog = json2StartupLog(item)
                val date = new Date(startupReportLog.getStartTimeInMs)
                // yyyy-MM-dd
                val dateTime = dateToString(date)
                val rowKey = dateTime + "_" + startupReportLog.getCity
                table.incrementColumnValue(Bytes.toBytes(rowKey), Bytes.toBytes("StatisticData"), Bytes.toBytes("userNum"), 1L)
                println(rowKey)
              }
          }
      }

      // 完成需求统计后更新Zookeeper数据
      offsetToZookeeper(onlineLogDStream, kafkaCluster, kafka_group)

      streamingContext
    }
  }

  def offsetToZookeeper(onlineLogDStream: InputDStream[String], kafkaCluster: KafkaCluster, kafka_group: String) = {
    onlineLogDStream.foreachRDD {
      rdd =>
        val offsetsList = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        for (offsets <- offsetsList) {
          val topicAndPartition = TopicAndPartition(offsets.topic, offsets.partition)
          val ack = kafkaCluster.setConsumerOffsets(kafka_group, Map((topicAndPartition, offsets.untilOffset)))
          if (ack.isLeft) {
            println(s"Error updating the offset to Kafka cluster: ${ack.left.get}")
          } else {
            println(s"update the offset to Kafka cluster: ${offsets.untilOffset} successfully")
          }
        }
    }
  }

  def getOffsetFromZookeeper(kafkaCluster: KafkaCluster, kafka_group: String, kafka_topic_set: Set[String]) = {
    // 创建Map存储Topic和分区对应的offset
    val topicPartitionOffsetMap = new mutable.HashMap[TopicAndPartition, Long]()
    // 获取传入的Topic的所有分区
    val topicAndPartitions = kafkaCluster.getPartitions(kafka_topic_set)
    // 如果成功获取到Topic所有分区
    if (topicAndPartitions.isRight) {
      // 获取分区数据
      val partitions = topicAndPartitions.right.get
      // 获取指定分区的offset
      val offsetInfo = kafkaCluster.getConsumerOffsets(kafka_group, partitions)
      if (offsetInfo.isLeft) {
        // 如果没有offset信息则存储0
        for (top <- partitions)
          topicPartitionOffsetMap += (top -> 0L)
      } else {
        // 如果有offset信息则存储offset
        val offsets = offsetInfo.right.get
        for ((top, offset) <- offsets)
          topicPartitionOffsetMap += (top -> offset)
      }
    }
    topicPartitionOffsetMap.toMap
  }

  def getHBaseTabel(prop: Properties) = {
    // 创建HBase配置
    val config = HBaseConfiguration.create
    // 设置HBase参数
    config.set("hbase.zookeeper.property.clientPort", loadProperties("hbase.zookeeper.property.clientPort"))
    config.set("hbase.zookeeper.quorum", loadProperties("hbase.zookeeper.quorum"))
    // 创建HBase连接
    val connection = ConnectionFactory.createConnection(config)
    // 获取HBaseTable
    val table = connection.getTable(TableName.valueOf("online_city_click_count"))
    table
  }

  def dateToString(date: Date): String = {
    val dateString = new SimpleDateFormat("yyyy-MM-dd")
    val dateStr = dateString.format(date)
    dateStr
  }

  def json2StartupLog(json: String) = {
    val obj = JSONUtil.json2Object(json, classOf[StartupReportLogs]);
    obj
  }

  def getProperties(): Properties = {
    val properties = new Properties()
    val in = OnlineScala.getClass.getClassLoader.getResourceAsStream("config.properties")
    properties.load(in);
    properties
  }

  def loadProperties(key: String): String = {
    val properties = new Properties()
    val in = OnlineScala.getClass.getClassLoader.getResourceAsStream("config.properties")
    properties.load(in);
    properties.getProperty(key)
  }

}
