## hadoop的HA机制原理

HA:为了解决hadoop集群但单点故障问题。

hadoop中namenode单点故障的解决：
- active
- standby

![](images/hadoop的HA工作机制示意图.png)


## hadoop的HA集群搭建过程

### 前期准备
- 1.修改Linux主机名
- 2.修改IP
- 3.修改主机名和IP的映射关系 /etc/hosts
- 4.关闭防火墙
- 5.ssh免登陆
- 6.安装JDK，配置环境变量等

### 集群规划：
|主机名|IP|安装的软件|运行的进程|
|:--|:--|:--|:--|
|hadoop00|192.168.1.200|jdk、hadoop|NameNode、DFSZKFailoverController(zkfc)|
|hadoop01|192.168.1.201|jdk、hadoop|NameNode、DFSZKFailoverController(zkfc)|
|hadoop02|192.168.1.202|jdk、hadoop|ResourceManager|
|hadoop03|192.168.1.203|jdk、hadoop|ResourceManager|
|hadoop04|192.168.1.204|jdk、hadoop|DataNode、NodeManager|
|hadoop05|192.168.1.205|jdk、hadoop、zookeeper|DataNode、NodeManager、JournalNode、QuorumPeerMain|
|hadoop06|192.168.1.206|jdk、hadoop、zookeeper|DataNode、NodeManager、JournalNode、QuorumPeerMain|
|hadoop07|192.168.1.207|jdk、hadoop、zookeeper|DataNode、NodeManager、JournalNode、QuorumPeerMain|


### 说明：
1.在hadoop2.0中通常由两个NameNode组成，一个处于active状态，另一个处于standby状态。Active NameNode对外提供服务，而Standby NameNode则不对外提供服务，仅同步active namenode的状态，以便能够在它失败时快速进行切换。

hadoop2.0官方提供了两种HDFS HA的解决方案，一种是NFS，另一种是QJM。这里我们使用简单的QJM。在该方案中，主备NameNode之间通过一组JournalNode同步元数据信息，一条数据只要成功写入多数JournalNode即认为写入成功。通常配置奇数个JournalNode

这里还配置了一个zookeeper集群，用于ZKFC（DFSZKFailoverController）故障转移，当Active NameNode挂掉了，会自动切换Standby NameNode为standby状态

2.hadoop-2.2.0中依然存在一个问题，就是ResourceManager只有一个，存在单点故障，hadoop-2.6.4解决了这个问题，有两个ResourceManager，一个是Active，一个是Standby，状态由zookeeper进行协调

### 安装步骤：

#### 1.安装配置zooekeeper集群（在hadoop05上）

##### 1.1解压
```
tar -zxvf zookeeper-3.4.5.tar.gz -C /home/hadoop/app/
```
##### 1.2修改配置
```
cd /home/hadoop/app/zookeeper-3.4.5/conf/
cp zoo_sample.cfg zoo.cfg
vim zoo.cfg
```
修改：dataDir=/home/hadoop/app/zookeeper-3.4.5/tmp

在最后添加：
```
server.1=hadoop05:2888:3888
server.2=hadoop06:2888:3888
server.3=hadoop07:2888:3888
```
保存退出
然后创建一个tmp文件夹
```
mkdir /home/hadoop/app/zookeeper-3.4.5/tmp
echo 1 > /home/hadoop/app/zookeeper-3.4.5/tmp/myid
```
##### 1.3将配置好的zookeeper拷贝到其他节点

首先分别在hadoop06、hadoop07根目录下创建一个hadoop目录：mkdir /hadoop
```
scp -r /home/hadoop/app/zookeeper-3.4.5/ hadoop06:/home/hadoop/app/
scp -r /home/hadoop/app/zookeeper-3.4.5/ hadoop07:/home/hadoop/app/
```
注意：修改hadoop06、hadoop07对应/hadoop/zookeeper-3.4.5/tmp/myid内容
hadoop06：
```
echo 2 > /home/hadoop/app/zookeeper-3.4.5/tmp/myid
hadoop07：
echo 3 > /home/hadoop/app/zookeeper-3.4.5/tmp/myid
```

#### 2.安装配置hadoop集群（在hadoop00上操作）

##### 2.1解压
```
tar -zxvf hadoop-2.6.4.tar.gz -C /home/hadoop/app/
```

##### 2.2配置HDFS（hadoop2.0所有的配置文件都在$HADOOP_HOME/etc/hadoop目录下）

将hadoop添加到环境变量中
```
vim /etc/profile
```
```
export JAVA_HOME=/usr/java/jdk1.7.0_55
export HADOOP_HOME=/hadoop/hadoop-2.6.4
export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin
```

hadoop2.0的配置文件全部在$HADOOP_HOME/etc/hadoop下
```
cd /home/hadoop/app/hadoop-2.6.4/etc/hadoop
```
###### 2.2.1修改hadoo-env.sh
```
export JAVA_HOME=/home/hadoop/app/jdk1.7.0_55
```

###### 2.2.2修改core-site.xml
```
<configuration>
<!-- 指定hdfs的nameservice为ns1 -->
<property>
<name>fs.defaultFS</name>
<value>hdfs://hdp24/</value>
</property>
<!-- 指定hadoop临时目录 -->
<property>
<name>hadoop.tmp.dir</name>
<value>/root/hdptmp/</value>
</property>

<!-- 指定zookeeper地址 -->
<property>
<name>ha.zookeeper.quorum</name>
<value>hdp-05:2181,hdp-06:2181,hdp-07:2181</value>
</property>
</configuration>
```


###### 2.2.3修改hdfs-site.xml
```
<configuration>
<!--指定hdfs的nameservice为bi，需要和core-site.xml中的保持一致 -->
<property>
<name>dfs.nameservices</name>
<value>hdp24</value>
</property>
<!-- hdp24下面有两个NameNode，分别是nn1，nn2 -->
<property>
<name>dfs.ha.namenodes.hdp24</name>
<value>nn1,nn2</value>
</property>
<!-- nn1的RPC通信地址 -->
<property>
<name>dfs.namenode.rpc-address.hdp24.nn1</name>
<value>hdp-01:9000</value>
</property>
<!-- nn1的http通信地址 -->
<property>
<name>dfs.namenode.http-address.hdp24.nn1</name>
<value>hdp-01:50070</value>
</property>
<!-- nn2的RPC通信地址 -->
<property>
<name>dfs.namenode.rpc-address.hdp24.nn2</name>
<value>hdp-02:9000</value>
</property>
<!-- nn2的http通信地址 -->
<property>
<name>dfs.namenode.http-address.hdp24.nn2</name>
<value>hdp-02:50070</value>
</property>


<!-- 指定NameNode的edits元数据在机器本地磁盘的存放位置 -->
<property>
<name>dfs.namenode.name.dir</name>
<value>/root/hdpdata/name</value>
</property>

<property>
<name>dfs.datanode.data.dir</name>
<value>/root/hdpdata/data</value>
</property>


<!-- 指定NameNode的共享edits元数据在JournalNode上的存放位置 -->
<property>
<name>dfs.namenode.shared.edits.dir</name>
<value>qjournal://hdp-05:8485;hdp-06:8485;hdp-07:8485/hdp24</value>
</property>

<!-- 指定JournalNode在本地磁盘存放数据的位置 -->
<property>
<name>dfs.journalnode.edits.dir</name>
<value>/root/hdpdata/journaldata</value>
</property>

<!-- 开启NameNode失败自动切换 -->
<property>
<name>dfs.ha.automatic-failover.enabled</name>
<value>true</value>
</property>
<!-- 配置失败自动切换实现方式 -->
<property>
<name>dfs.client.failover.proxy.provider.hdp24</name>
<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
</property>
<!-- 配置隔离机制方法，多个机制用换行分割，即每个机制暂用一行-->
<property>
<name>dfs.ha.fencing.methods</name>
<value>
sshfence
shell(/bin/true)
</value>
</property>
<!-- 使用sshfence隔离机制时需要ssh免登陆 -->
<property>
<name>dfs.ha.fencing.ssh.private-key-files</name>
<value>/root/.ssh/id_rsa</value>
</property>
<!-- 配置sshfence隔离机制超时时间 -->
<property>
<name>dfs.ha.fencing.ssh.connect-timeout</name>
<value>30000</value>
</property>
</configuration>
```

###### 2.2.4修改mapred-site.xml
```
<configuration>
<!-- 指定mr框架为yarn方式 -->
<property>
<name>mapreduce.framework.name</name>
<value>yarn</value>
</property>
</configuration>
```

###### 2.2.5修改yarn-site.xml
```
<configuration>
<!-- 开启RM高可用 -->
<property>
<name>yarn.resourcemanager.ha.enabled</name>
<value>true</value>
</property>
<!-- 指定RM的cluster id -->
<property>
<name>yarn.resourcemanager.cluster-id</name>
<value>yrc</value>
</property>
<!-- 指定RM的逻辑名字 -->
<property>
<name>yarn.resourcemanager.ha.rm-ids</name>
<value>rm1,rm2</value>
</property>
<!-- 分别指定RM的地址 -->
<property>
<name>yarn.resourcemanager.hostname.rm1</name>
<value>hdp-03</value>
</property>
<property>
<name>yarn.resourcemanager.hostname.rm2</name>
<value>hdp-04</value>
</property>
<!-- 指定zk集群地址 -->
<property>
<name>yarn.resourcemanager.zk-address</name>
<value>hdp-01:2181,hdp-02:2181,hdp-03:2181</value>
</property>
<property>
<name>yarn.nodemanager.aux-services</name>
<value>mapreduce_shuffle</value>
</property>
</configuration>
```

###### 2.2.6修改slaves

slaves是指定子节点的位置，因为要在hadoop01上启动HDFS、在hadoop03启动yarn，所以hadoop01上的slaves文件指定的是datanode的位置，hadoop03上的slaves文件指定的是nodemanager的位置
```
hadoop05
hadoop06
hadoop07
```
###### 2.2.7配置免密码登陆

首先要配置hadoop00到hadoop01、hadoop02、hadoop03、hadoop04、hadoop05、hadoop06、hadoop07的免密码登陆

在hadoop01上生产一对钥匙
```
ssh-keygen -t rsa
```
将公钥拷贝到其他节点，包括自己
```
ssh-coyp-id hadoop00
ssh-coyp-id hadoop01
ssh-coyp-id hadoop02
ssh-coyp-id hadoop03
ssh-coyp-id hadoop04
ssh-coyp-id hadoop05
ssh-coyp-id hadoop06
ssh-coyp-id hadoop07
```

配置hadoop02到hadoop04、hadoop05、hadoop06、hadoop07的免密码登陆

在hadoop02上生产一对钥匙
```
ssh-keygen -t rsa
```

将公钥拷贝到其他节点
```
ssh-coyp-id hadoop03
ssh-coyp-id hadoop04
ssh-coyp-id hadoop05
ssh-coyp-id hadoop06
ssh-coyp-id hadoop07
```


注意：两个namenode之间要配置ssh免密码登陆，别忘了配置hadoop01到hadoop00的免登陆

在hadoop01上生产一对钥匙
```
ssh-keygen -t rsa
ssh-coyp-id -i hadoop00
```

### 2.4将配置好的hadoop拷贝到其他节点
```
scp -r /hadoop/ hadoop02:/
scp -r /hadoop/ hadoop03:/
scp -r /hadoop/hadoop-2.6.4/ hadoop@hadoop04:/hadoop/
scp -r /hadoop/hadoop-2.6.4/ hadoop@hadoop05:/hadoop/
scp -r /hadoop/hadoop-2.6.4/ hadoop@hadoop06:/hadoop/
scp -r /hadoop/hadoop-2.6.4/ hadoop@hadoop07:/hadoop/
```


###注意：严格按照下面的步骤!!!!!!!!!

### 2.5启动zookeeper集群（分别在hdp-05、hdp-06、hdp-07上启动zk）
```
cd /hadoop/zookeeper-3.4.5/bin/
./zkServer.sh start
```

查看状态：一个leader，两个follower
```
./zkServer.sh status
```

### 2.6手动启动journalnode

 分别在在hdp-05、hdp-06、hdp-07上执行
```
cd /hadoop/hadoop-2.6.4
sbin/hadoop-daemon.sh start journalnode
```

运行jps命令检验，hadoop05、hadoop06、hadoop07上多了JournalNode进程

### 2.7格式化namenode

在hdp-01上执行命令:
```
hdfs namenode -format
```

格式化后会在根据core-site.xml中的hadoop.tmp.dir配置生成个文件，这里我配置的是/hadoop/hadoop-2.6.4/tmp，然后将/hadoop/hadoop-2.6.4/tmp拷贝到hadoop02的/hadoop/hadoop-2.6.4/下。
```
scp -r tmp/ hadoop02:/home/hadoop/app/hadoop-2.6.4/
```

也可以这样，建议hdfs namenode -bootstrapStandby

### 2.8格式化ZKFC(在hdp-01上执行即可)
```
hdfs zkfc -formatZK
```

### 2.9启动HDFS(在hadoop00上执行)
```
sbin/start-dfs.sh
```
### 2.10启动YARN

**注意：是在hadoop02上执行start-yarn.sh，把namenode和resourcemanager分开是因为性能问题，因为他们都要占用大量资源，所以把他们分开了，他们分开了就要分别在不同的机器上启动**
```
sbin/start-yarn.sh
```


到此，hadoop-2.6.4配置完毕，可以统计浏览器访问:
http://hadoop00:50070

NameNode 'hadoop01:9000' (active)

http://hadoop01:50070

NameNode 'hadoop02:9000' (standby)


验证HDFS HA

首先向hdfs上传一个文件
```
hadoop fs -put /etc/profile /profile
hadoop fs -ls /
```
然后再kill掉active的NameNode
```
kill -9 <pid of NN>
```
通过浏览器访问：http://192.168.1.202:50070

NameNode 'hadoop02:9000' (active)

这个时候hadoop02上的NameNode变成了active

在执行命令：
```
hadoop fs -ls /
-rw-r--r--   3 root supergroup       1926 2014-02-06 15:36 /profile
```
刚才上传的文件依然存在！！！

手动启动那个挂掉的NameNode
```
sbin/hadoop-daemon.sh start namenode
```

通过浏览器访问：http://192.168.1.201:50070

NameNode 'hadoop01:9000' (standby)

验证YARN：

运行一下hadoop提供的demo中的WordCount程序：
```
hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.4.1.jar wordcount /profile /out
```
OK，大功告成！！！




测试集群工作状态的一些指令 ：
```
bin/hdfs dfsadmin -report 查看hdfs的各节点状态信息


bin/hdfs haadmin -getServiceState nn1 获取一个namenode节点的HA状态

sbin/hadoop-daemon.sh start namenode  单独启动一个namenode进程


./hadoop-daemon.sh start zkfc   单独启动一个zkfc进程
```

 
