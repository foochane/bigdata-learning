## zookeeper数据存储形式
zookeeper中对用户的数据采用kv形式存储

只是zk有点特别：
key：是以路径的形式表示的，各key之间有父子关系，比如 `/ `是顶层key

用户建的key只能在/ 下作为子节点，比如建一个key： `/aa`  这个key可以带value数据

也可以建一个key：   `/bb`

也可以建key： `/aa/xx `

zookeeper中，对每一个数据key，称作一个znode

 

## znode类型
zookeeper中的znode有多种类型：

- 1、PERSISTENT  持久的：创建者就算跟集群断开联系，该类节点也会持久存在与zk集群中
- 2、EPHEMERAL  短暂的：创建者一旦跟集群断开联系，zk就会将这个节点删除
- 3、SEQUENTIAL  带序号的：这类节点，zk会自动拼接上一个序号，而且序号是递增的

组合类型：
- PERSISTENT  ：持久不带序号
- EPHEMERAL  ：短暂不带序号 
- PERSISTENT  且 SEQUENTIAL   ：持久且带序号
- EPHEMERAL  且 SEQUENTIAL  ：短暂且带序号



## 安装zookeeper
上传安装包，解压
修改conf/zoo.cfg

```
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just
# example sakes.
dataDir=/root/zkdata
# the port at which the clients will connect
clientPort=2181
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
server.1=hdp-01:2888:3888
server.2=hdp-02:2888:3888
server.3=hdp-03:2888:3888
```

对3台节点，都创建目录 mkdir /root/zkdata

对3台节点，在工作目录中生成myid文件，但内容要分别为各自的id： 1,2,3
```
hdp20-01上：  echo 1 > /root/zkdata/myid
hdp20-02上：  echo 2 > /root/zkdata/myid
hdp20-03上：  echo 3 > /root/zkdata/myid
```

从hdp20-01上scp安装目录到其他两个节点
```
scp -r zookeeper-3.4.6/ hdp20-02$PWD
scp -r zookeeper-3.4.6/ hdp20-03:$PWD
```


## 启动zookeeper集群
zookeeper没有提供自动批量启动脚本，需要手动一台一台地起zookeeper进程
在每一台节点上，运行命令：
 
```
bin/zkServer.sh start
```

启动后，用jps应该能看到一个进程：QuorumPeerMain

查看状态
```
bin/zkServer.sh status
```

## 编写启动脚本zkmanage.sh：
```sh
#!/bin/bash
for host in hdp-01 hdp-02 hdp-02
do
echo "${host}:${1}ing....."
ssh $host "source ~/.bashrc;/root/apps/zookeeper-3.4.6/bin/zkServer.sh $1"
done

sleep 2

for host in hdp-01 hdp-02 hdp-02
do
ssh $host "source ~/.bashrc;/root/apps/zookeeper-3.4.6/bin/zkServer.sh status"
done
```

$1 :指接收第一个参数

运行命令：
```
sh zkmanage.sh start #启动
sh zkmanage.sh stop  #停止
```

## zookeeper命令行客户端
启动本地客户端：
```
bin/zkCli.sh
```
启动其他机器的客户端：
```
bin/zkCli.sh -server hdp-01:2181
```

基本命令
- 查看帮助：help
- 查看目录：ls /
- 查看节点数据：get /zookeeper
- 插入数据： create /节点  数据 ， 如：create /aa hello
- 更改某节点数据： set /aa helloworld
- 删除数据：rmr /aa/bb
- 注册监听：get /aa watch -->数据发生改变会通知 ； ls /aa watch -->目录发现改变也会通知


## zookeeper 编程

建立java工程，手动导入zookeeper下面的jar包



