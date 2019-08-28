Kafka集群环境配置

## 1 环境准备
### 1.1 集群规划
|Node02|Node03|Node04|
|--|--|--|
|zk	|zk|zk|
|kafka|kafka|kafka|

### 1.2 jar包下载

安装包：kafka_2.11-0.8.2.1.tgz
下载地址：http://kafka.apache.org/downloads.html

## 2 Kafka集群部署
1）解压安装包
```s
$ tar -zxvf kafka_2.11-0.8.2.1.tgz -C /usr/local/bigdata

```
2） 进入到安装目录
```s
$ cd /usr/local/bigdata/kafka_2.11-0.8.2.1 

```
3）在/usr/local/bigdata/kafka_2.11-0.8.2.1 目录下创建logs文件夹
```s
$ mkdir logs

```
4）修改配置文件
```s
$ cd config/
$ vi server.properties

```
输入以下内容：
```s
#broker的全局唯一编号，不能重复,其他机器上有修改
broker.id=0
#删除topic功能使能
delete.topic.enable=true
#处理网络请求的线程数量
num.network.threads=3
#用来处理磁盘IO的现成数量
num.io.threads=8
#发送套接字的缓冲区大小
socket.send.buffer.bytes=102400
#接收套接字的缓冲区大小
socket.receive.buffer.bytes=102400
#请求套接字的缓冲区大小
socket.request.max.bytes=104857600
#kafka运行日志存放的路径
log.dirs=/usr/local/bigdata/kafka_2.11-0.8.2.1/logs
#topic在当前broker上的分区个数
num.partitions=1
#用来恢复和清理data下数据的线程数量
num.recovery.threads.per.data.dir=1
#segment文件保留的最长时间，超时将被删除
log.retention.hours=168
#配置连接Zookeeper集群地址
zookeeper.connect=Node02:2181,Node03:2181,Node04:2181

```
5）配置环境变量
```s
$ vi ~/.bashrc


```

```s
#KAFKA_HOME
export KAFKA_HOME=/usr/local/bigdata/kafka_2.11-0.8.2.1 
export PATH=$PATH:$KAFKA_HOME/bin
```s
$ source ~/.bashrc

```
6）分发安装包

```s
$ scp -r kafka_2.11-0.8.2.1/ hadoop@Node03:/usr/local/bigdata/
$ scp -r kafka_2.11-0.8.2.1/ hadoop@Node04:/usr/local/bigdata/

```

注意：分发之后记得配置其他机器的环境变量

7）分别在Node03和Node04上修改配置文件

修改/usr/local/bigdata/kafka_2.11-0.8.2.1/config/server.properties中的 `broker.id=1、broker.id=2`

**注：broker.id不得重复**

## 3 Kafka集群的启动和关闭

启动Kafka之前要先启动zookeeper

```s
#启动
$ /usr/local/bigdata/zookeeper-3.4.6/bin/zkServer.sh start

#查看状态

$ /usr/local/bigdata/zookeeper-3.4.6/bin/zkServer.sh status
```
- 启动集群
依次在Node02、Node03、Node04节点上启动kafka
```s
$ kafka-server-start.sh /usr/local/bigdata/kafka_2.11-0.8.2.1/config/server.properties & 

```
- 关闭集群

```s
$ kafka-server-stop.sh stop

```