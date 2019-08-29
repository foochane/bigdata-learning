# 手机APP分析系统

## 1 系统说明
### 1.1 框架说明

### 1.2 代码结构：
- tomcat_servelet:  servelet代码
- data_producer:    模拟日志数据生产
- taildirsourse：   Flume移植代码
- log-analysis：    日志处理代码




## 2 Nginx 安装

在一台机器上安装一个nginx和两个tomcat



### 2.1 安装Nginx所需的依赖

**ubuntu 下直接apt-get 安装如下库即可,不需要进行上面的操作：**
 ```sh
#PCRE库
$ sudo apt-get install libpcre3 libpcre3-dev  

#zlib库
$ sudo apt-get install zlib1g-dev

#OpenSSL库
$ sudo apt-get install openssl libssl-dev 
```


### 2.2 安装nginx

**注意使用root用户**
```
# wget http://nginx.org/download/nginx-1.12.2.tar.gz

# tar zxvf nginx-1.12.2.tar.gz

# cd nginx-1.12.2

# ./configure --prefix=/usr/local/nginx 

# make && make install
```

查看 Nginx 版本
 ```s
 $ /usr/local/nginx/sbin/nginx -v

 ```


 ### 2.2 nginx负载均衡配置

conf/nginx.conf

```conf
#user  nobody;
worker_processes  1;

#error_log logs/error.log;
#error_log logs/error.log notice;
#error_log logs/error.log info;

#pid logs/nginx.pid;

events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;


    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #服务器的集群 
    upstream netitcast.com { 
        #服务器集群名字 
        #服务器配置 weight 是权重的意思，权重越大，分配的概率越大。
        server 192.168.1.112:18080; 
        server 192.168.1.112:28080; 
    } 


    #gzip  on;

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            # root   html;
            # index  index.html index.htm;
            proxy_pass http://netitcast.com;
            proxy_redirect default;
        }

        #error_page  404              /404.html
        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

## 3 Tomcat安装

**用普通用户安装**

### 3.1 安装
```s
$ sudo cd /opt/modules
$ sudo chown -R ubuntu:ubuntu /opt/modules/
$ tar -zxvf apache-tomcat-7.0.72.tar.gz -C /opt/modules/
$ cd /opt/modules
$ mv apache-tomcat-7.0.72/ apache-tomcat-7.0.72_01
$ cp -r apache-tomcat-7.0.72_01 apache-tomcat-7.0.72_02
```

### 3.2 修改配置文件 

分别修改tomcat下的conf/server.xml

tomcat_1：
```xml
<?xml version='1.0' encoding='utf-8'?>

<Server port="18005" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />

  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />

  <Listener className="org.apache.catalina.core.JasperListener" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />


  <GlobalNamingResources>

    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">
    <Connector port="18080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
    <Connector port="18009" protocol="AJP/1.3" redirectPort="8443" />

    <Engine name="Catalina" defaultHost="localhost">

      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log." suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

      </Host>
    </Engine>
  </Service>
</Server>

```

tomcat_2:
```xml

<?xml version='1.0' encoding='utf-8'?>
<Server port="28005" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <Listener className="org.apache.catalina.core.JasperListener" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">

    <Connector port="28080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
    <Connector port="28009" protocol="AJP/1.3" redirectPort="8443" />
    <Engine name="Catalina" defaultHost="localhost">
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log." suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />
      </Host>
    </Engine>
  </Service>
</Server>
```



## 4 Tomcat部署

### 4.1 部署tomcat_servelet代码到tomcat

修改tomcat_serverlet项目中log4j.properties文件的log4j.appender.atguigu.File.file属性，分别打war包放入两个tomcat中。
放入 Tomcat 安装目录的 webapps 目录下，之后会自
动解压，生成对应的目录，完成部署后需要重启 Tomcat 从而使部署生效。

打包过程：

在 tomcat_serverlet 项目中点选择 log-collectr-common 点击右侧的maven（Lifecycle)：【clean】--> 【package】-->【install】(install会将包安装在maven仓库中)；

然后选择log-collector-web，同样在右侧的maven(Lifecycle里)：【clean】-->【package】，切换到Plugins，选择【war/war:war】打一个war包。

### 4.2 启动tomcat和nginx

启动tomcat：
```
/opt/modules/apache-tomcat-7.0.72_01/bin/startup.sh && /opt/modules/apache-tomcat-7.0.72_02/bin/startup.sh

```

停止tomcat：
```
/opt/modules/apache-tomcat-7.0.72_01/bin/shutdown.sh && /opt/modules/apache-tomcat-7.0.72_02/bin/shutdown.sh

```


启动nginx:
```
sudo /usr/local/nginx/sbin/nginx

```

停止nginx：
```
sudo /usr/local/nginx/sbin/nginx -s stop

```

## 5 运行模拟日志发送程序
启动 data_producer 项目中的 GenBeahavior 程序，开始模拟日志的发送。

运行模型程序后，会在/opt/modules/apache-tomcat-7.0.72_01/logs/LogsCollect和/opt/modules/apache-tomcat-7.0.72_01/logs/LogsCollect目录下交替生成日志。

可以使用 ` tail -F atguigu.log`进行查看。


## 6 Flume配置

### 6.1 Flume代码移植

由于 Flume 1.6 版本没有 Taildir Source
组件，因此，需要将 Flume 1.7 中的 Taildir Source 组件源码编译打包后，放入 Flume1.6 安装
目录的 lib 文件目录下。

所谓移植，就是将 Flume1.7 版本中 Taildir Source 的相关文件全部提取出来，然后将这
些文件放入新建的项目中进行编译打包，将打包出的 jar 包放入 Flume1.6 安装目录的 lib 目
录下即可。

在本项目中，已经将 Taildir Source 的源码放入了 taildirsource 项目中，直接编译项目，
打包后放入 Flume1.6 安装目录的 lib 文件目录下即可。
在 Flume 配置文件中指定全类名即可使用 Taildir Source 组件。
```s
a1.sources.r1.type = com.atguigu.flume.source.TaildirSource

```

打包：

在IDEA中，打开taildirsource项目，点击右侧的maven先双击`clean`然后双击`package`打包，之后变回在target目录下生成一个`flume-taildirsource.jar`文件

然后

**将flume-atguigu-taildirsource.jar文件拷贝到flume的lib目录下**

### 6.2 Flume分配

完整的框架图如下，目前已经完成了从tomcat到数据落盘的过程，接下来是flume的配置。
![AppAnalysis理想分析框架图1](https://raw.githubusercontent.com/foochane/bigdata-learning/master/images/AppAnalysis离线分析框架图1.png)

在前面的配置中我们使用了两个tomcat以及一个nginx做负载均衡。下面的配置过程讲对上面的框架图进行简化，具体如下：
![AppAnalysis理想分析框架图2](https://raw.githubusercontent.com/foochane/bigdata-learning/master/images/AppAnalysis离线分析框架图2.png)


第一层flume跟tocat在一台机器上用于采集tomcat中的数据，第二层的两个Flume分别安装在另外两台机器上。

第一层flume安装在Node02上，第二层flume安装在Node03和Node04上。

### 6.3 第一层Flume

配置文件如下：

```conf
a1.sources = r1
a1.channels = c1

a1.sinkgroups = g1
a1.sinks = k1 k2

a1.sources.r1.type = com.atguigu.flume.source.TaildirSource
a1.sources.r1.channels = c1
a1.sources.r1.positionFile = /usr/local/bigdata/flume-1.6.0/checkpoint/behavior/taildir_position.json
a1.sources.r1.filegroups = f1
# 多个文件用 .*
a1.sources.r1.filegroups.f1 = /opt/modules/apache-tomcat-7.0.72_01/logs/LogsCollect/atguigu.log
a1.sources.r1.fileHeader = true

a1.channels.c1.type = file
a1.channels.c1.checkpointDir = /usr/local/bigdata/flume-1.6.0/checkpoint/behavior
a1.channels.c1.dataDirs = /usr/local/bigdata/flume-1.6.0/data/behavior/
a1.channels.c1.maxFileSize = 104857600
a1.channels.c1.capacity = 90000000
a1.channels.c1.keep-alive = 60

a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = load_balance
a1.sinkgroups.g1.processor.backoff = true
a1.sinkgroups.g1.processor.selector = round_robin
a1.sinkgroups.g1.processor.selector.maxTimeOut=10000

a1.sinks.k1.type = avro
a1.sinks.k1.channel = c1
a1.sinks.k1.batchSize = 1
a1.sinks.k1.hostname = Node03
a1.sinks.k1.port = 1234

a1.sinks.k2.type = avro
a1.sinks.k2.channel = c1
a1.sinks.k2.batchSize = 1
a1.sinks.k2.hostname = Node04
a1.sinks.k2.port = 1234
```

启动Node02下的flume：
```s
$ /usr/local/bigdata/flume-1.6.0/bin/flume-ng agent --conf conf/ -f conf/flume-analysis.conf -n a1

```

可以在/data/behavior目录下查看的文件

### 6.4 第二层Flume

```conf
a1.sources = r1
a1.channels = c1
a1.sinks = k1

a1.sources.r1.type = avro
a1.sources.r1.channels = c1
a1.sources.r1.bind = 0.0.0.0
a1.sources.r1.port = 1234

a1.channels.c1.type = file
a1.channels.c1.checkpointDir = /usr/local/bigdata/flume-1.6.0/checkpoint/behavior_collect
a1.channels.c1.dataDirs = /usr/local/bigdata/flume-1.6.0/data/behavior_collect
a1.channels.c1.maxFileSize = 104857600
a1.channels.c1.capacity = 90000000
a1.channels.c1.keep-alive = 60

a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.topic = analysis-test
a1.sinks.k1.brokerList = Node02:9092,Node03:9092,Node04:9092
a1.sinks.k1.requiredAcks = 1
a1.sinks.k1.kafka.producer.type = sync
a1.sinks.k1.batchSize = 1
a1.sinks.k1.channel = c1
```

启动zookeeper（全部机器都启动）

```
$ /usr/local/bigdata/zookeeper-3.4.6/bin/zkServer.sh start
```
启动kafka
```
$ kafka-server-start.sh /opt/modules/kafka/config/server.properties &
```

如果时间不统一的话，同步时间

```s
#被同步的机器要安装其他可以不装
$ sudo apt install ntp

$ sudo apt install ntpdate

#同步时间
$ sudo ntpdate -u 192.168.1.112

```

启动Node03和 Node04下的flume：
```s
$ /usr/local/bigdata/flume-1.6.0/bin/flume-ng agent --conf /usr/local/bigdata/flume-1.6.0/conf/ -f /usr/local/bigdata/flume-1.6.0/conf/flume-analysis.conf -n a1

```

查看kafka里是否有数据：

```s
$ kafka-topics.sh --zookeeper Node02:2181 --list
analysis-test

$ kafka-console-consumer.sh --zookeeper Node02:2181 --topic analysis-test
{"activeTimeInMs":39051,"appId":"app00001","appPlatform":"android","appVersion":"1.0.1","city":"Xian","startTimeInMs":1566976694583,"userId":"user117"}
{"activeTimeInMs":62544,"appId":"app00001","appPlatform":"ios","appVersion":"1.0.1","city":"Shenyang","startTimeInMs":1566976696590,"userId":"user1168"}
{"activeTimeInMs":1191287,"appId":"app00001","appPlatform":"android","appVersion":"1.0.1","city":"Hangzhou","startTimeInMs":1566976698596,"userId":"user114"}
{"activeTimeInMs":819555,"appId":"app00001","appPlatform":"android","appVersion":"1.0.2","city":"Xian","startTimeInMs":1566976700603,"userId":"user1167"}
{"activeTimeInMs":816384,"appId":"app00001","appPlatform":"android","appVersion":"1.0.2","city":"Hunan","startTimeInMs":1566976702609,"userId":"user1156"}

........

```

到此数据已经成功写入kafka里面了。

## 7 运行Kafka消费者代码

接下来是数据从Kafka写入HDFS,代码位于项目log-analysis下的data-processing模块下的KafkaConsumer.java中

运行代码前要先启动hdfs和yarn。

运行运行Kafka消费者代码就可以在HDFS上查看到数据。

## 8 Hive配置

### 8.1 配置Hive支持JSON存储

在 Hive 中采用 Json 作为存储格式，需要建表时指定 Serde。Insert into 时，Hive 使用 json
格式进行保存，查询时，通过 json 库进行解析。Hive 默认输出是压缩格式，这里改成不压
缩。

具体操作步骤如下：
1. 将 json-serde-1.3.8-jar-with-dependencies.jar 导入到 hive 的/usr/local/bigdata/hive-2.3.5/lib 路径
下。
2. 在/usr/local/bigdata/hive-2.3.5/conf/hive-site.xml 文件中添加如下配置：
代码清单 3-9 Hive支持JSON存储配置
```xml
<property>
<name>hive.aux.jars.path</name>
<value>file:///usr/local/bigdata/hive-2.3.5/lib/json-serde-1.3.8-jar-with-dependencies.jar</value>
</property>
<property>
<name>hive.exec.compress.output</name>
<value>false</value>
</property>
```

### 8.2 创建分区表

```sql
--查看数据库
hive> show databases;
--如果 applogs_db 存在则删除数据库：
hive> drop database applogs_db;
--创建数据库
hive> create database applogsdb;

--使用 applogs_db 数据库：
hive (default)> use applogsdb;
--创建分区表
--startup
CREATE external TABLE ext_startup_logs(userId string,appPlatform string,appId
string,startTimeInMs bigint,activeTimeInMs bigint,appVersion string,city
string)PARTITIONED BY (ym string, day string,hm string) ROW FORMAT SERDE 
'org.openx.data.jsonserde.JsonSerDe' STORED AS TEXTFILE; 
--查看数据库中的分区表
hive> show tables;
--退出 Hive
hive> quit;
```


### 8.3 创建并执行crontab调度脚本
创建hdfstohive.sh并且放在 /usr/local/bigdata/shell文件夹下

```bash
#!/bin/bash
# 获取三分钟之前的时间
systime=`date -d "-3 minute" +%Y%m-%d-%H%M`
# 获取年月
ym=`echo ${systime} | awk -F '-' '{print $1}'`
# 获取日
day=`echo ${systime} | awk -F '-' '{print $2}'`
# 获取小时分钟
hm=`echo ${systime} | awk -F '-' '{print $3}'`
# 执行 hive 命令
/usr/local/bigdata/hive-2.3.5/bin/hive -e "load data inpath '/user/app-analysis/data/${ym}/${day}/${hm}' into table applogsdb.ext_startup_logs partition(ym='${ym}',day='${day}',hm='${hm}')"
```


1. 进入编写 crontab 调度
```s
$ crontab –e

```
添加命令，实现每分钟执行一次
```s
* * * * * /usr/local/bigdata/shell/hdfstohive.sh

```

启动crontab
```s
service crond start

```


【注】：crontab 常用命令

1. 查看状态
service cron status
2. 停止状态：
service cron stop
3. 启动状态：
service cron start
4. 编辑 crontab 定时任务
crontab –e
5. 查询 crontab 任务
crontab –l
6. 删除当前用户所有的 crontab 任务
crontab –r


### 8.4 自定义UTF函数

添加 app_logs_hive.jar 到类路径/usr/local/bigdata/hive-2.3.5/lib 下

在 hive-site.xml 文件中添加：
代码清单 3-16 hive-site.xml配置
```xml
<property>
 <name>hive.aux.jars.path</name>
 <value>file:///usr/local/bigdata/hive-2.3.5/lib/app_logs_hive.jar</value>
</property>

```
由于之前添加过 json 的 jar 包所以修改为如下方式：
代码清单 3-17 hive-site.xml配置
```xml
<property>
 <name>hive.aux.jars.path</name>
<value>file:///usr/local/bigdata/hive-2.3.5/lib/json-serde-1.3.8-jar-with-dependencies.jar,file:///usr/local/bigdata/hive-2.3.5/lib/app_logs_hive.jar</value>
</property>
```

注册永久函数:
```sql
hive (default)>create function getdaybegin AS 'com.atguigu.hive.DayBeginUDF';
hive (default)>create function getweekbegin AS 'com.atguigu.hive.WeekBeginUDF';
hive (default)>create function getmonthbegin AS 'com.atguigu.hive.MonthBeginUDF';
hive (default)>create function formattime AS 'com.atguigu.hive.FormatTimeUDF';

```

## 9 离线分析

```sql
use applogsdb;

-- 1) 今天新增用户
select
count(*)
from
(select min(startTimeInMs) mintime
from ext_startup_logs
where appId = 'app00001'
group by userId
having mintime >= getdaybegin() and mintime < getdaybegin(1)
)t ;

--2) 昨天新增用户
select
count(*)
from
(select min(startTimeInMs) mintime
from ext_startup_logs
where appId = 'app00001'
group by userId
having mintime >= getdaybegin(-1) and mintime < getdaybegin()
)t ;



--3) 指定天新增用户
select
count(*)
from
(select min(startTimeInMs) mintime
from ext_startup_logs
where appId = 'app00001'
group by userId
having mintime >= getdaybegin('2017/11/10 00:00:00') and mintime < getdaybegin('2017/11/10 
00:00:00',1)
)t ;

--2. 任意周新增用户
--1) 本周新增用户
select
count(*)
from
(select min(startTimeInMs) mintime
from ext_startup_logs
where appId = 'app00001'
group by userId
having mintime >= getweekbegin() and mintime < getweekbegin(1)
)t ;

--2) 上一周新增用户
select
count(*)
from
(select min(startTimeInMs) mintime
from ext_startup_logs
where appId = 'app00001'
group by userId
having mintime >= getweekbegin(-1) and mintime < getweekbegin()
)t ;


```


flume日志采集上线：每秒50M

## 10 实时系统

先启动hbase

```s
#启动集群
$ /usr/local/bigdata/hbase-2.0.5/bin/start-hbase.sh
$ /usr/local/bigdata/hbase-2.0.5/bin/hbase-daemon.sh start regionserver
#启动hbase客户端
$ /usr/local/bigdata/hbase-2.0.5/bin/hbase shell
```
建表：

```sql
>list
>disable 表名
>drop 表名
>create 'online_city_click_count','StatisticData'
```

运行onlineScala.scala程序
```sql
>scan 'online_city_click_count'
```

## 10 部署

启动顺序

kafka（Node02 Node03 Node04）

第二层flume(Node03 Node04)

第一层flume（Node02)

hdfs yarn （Node02 Node03 Node04）

zookeeper （Node02 Node03 Node04）

hbase（Node02 Node03 Node04）

tomcat（Node02)

nginx（Node02)
