

## 1、什么是大数据
基本概念
《数据处理》
在互联网技术发展到现今阶段，大量日常、工作等事务产生的数据都已经信息化，人类产生的数据量相比以前有了爆炸式的增长，以前的传统的数据处理技术已经无法胜任，需求催生技术，一套用来处理海量数据的软件工具应运而生，这就是大数据！

处理海量数据的核心技术：
- 海量数据存储：分布式
- 海量数据运算：分布式

这些核心技术的实现是不需要用户从零开始造轮子的
存储和运算，都已经有大量的成熟的框架来用

存储框架：
- HDFS——分布式文件存储系统（HADOOP中的存储框架）
- HBASE——分布式数据库系统
- KAFKA——分布式消息缓存系统(实时流式数据处理场景中应用广泛)

运算框架：（要解决的核心问题就是帮用户将处理逻辑在很多机器上并行）
- MAPREDUCE—— 离线批处理/HADOOP中的运算框架
- SPARK —— 离线批处理/实时流式计算
- STORM —— 实时流式计算

辅助类的工具（解放大数据工程师的一些繁琐工作）：
- HIVE —— 数据仓库工具：可以接收sql，翻译成mapreduce或者spark程序运行
- FLUME——数据采集
- SQOOP——数据迁移
- ELASTIC SEARCH —— 分布式的搜索引擎
- .......


换个角度说，大数据是：
- 1、有海量的数据
- 2、有对海量数据进行挖掘的需求
- 3、有对海量数据进行挖掘的软件工具（hadoop、spark、storm、flink、tez、impala......）



## 2 大数据在现实生活中的具体应用
数据处理的最典型应用：公司的产品运营情况分析

例如：https://www.umeng.com/

电商推荐系统：基于海量的浏览行为、购物行为数据，进行大量的算法模型的运算，得出各类推荐结论，以供电商网站页面来为用户进行商品推荐


精准广告推送系统：基于海量的互联网用户的各类数据，统计分析，进行用户画像（得到用户的各种属性标签），然后可以为广告主进行有针对性的精准的广告投放

## 3 安装hdfs集群

核心配置参数：
1)	指定hadoop的默认文件系统为：hdfs
2)	指定hdfs的namenode节点为哪台机器
3)	指定namenode软件存储元数据的本地目录
4)	指定datanode软件存放文件块的本地目录

hadoop的配置文件在：/root/apps/hadoop安装目录/etc/hadoop/

1) 修改hadoop-env.sh
环境变量
```
export JAVA_HOME=/root/apps/jdk1.8.0_60
```

2) 修改core-site.xml

```xml
<configuration>
    <property>
    <!-- 指定hadoop的默认文件系统为：hdfs -->
    <name>fs.defaultFS</name>
    <value>hdfs://hdp-01:9000/</value>
    </property>
</configuration>
```

3) 修改hdfs-site.xml
```xml
<configuration>
    <property>
    <!-- 指定hdfs的namenode节点为哪台机器 -->
        <name>dfs.namenode.name.dir</name>
        <value>/root/dfs/name</value>
    </property>

    <property>
    <!-- 指定datanode软件存放文件块的本地目录 -->
        <name>dfs.datanode.data.dir</name>
        <value>/root/dfs/data</value>
    </property>

</configuration>
```

4) 拷贝整个hadoop安装目录到其他机器
```
scp -r /root/apps/hadoop-2.8.0  hdp-02:/root/apps/
scp -r /root/apps/hadoop-2.8.0  hdp-03:/root/apps/
scp -r /root/apps/hadoop-2.8.0  hdp-04:/root/apps/
```


5) 启动HDFS

所谓的启动HDFS，就是在对的机器上启动对的软件

要点
提示：	要运行hadoop的命令，需要在linux环境中配置HADOOP_HOME和PATH环境变量
```
vi /etc/profile
export JAVA_HOME=/root/apps/jdk1.8.0_60
export HADOOP_HOME=/root/apps/hadoop-2.8.0
export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```


首先，初始化namenode的元数据目录
要在hdp-01上执行hadoop的一个命令来初始化namenode的元数据存储目录
```
hadoop namenode -format
```
- 创建一个全新的元数据存储目录
- 生成记录元数据的文件fsimage
- 生成集群的相关标识：如：集群id——clusterID

然后，启动namenode进程（在hdp-01上）

```
hadoop-daemon.sh start namenode
```

启动完后，首先用jps查看一下namenode的进程是否存在

然后，在windows中用浏览器访问namenode提供的web端口：50070
http://hdp-01:50070

然后，启动众datanode们（在任意地方）

```
hadoop-daemon.sh start datanode
```

查看进程：
```
$ ps -aux | grep 8324
```

查看监听端口
```
$ netstat -nltp | grep 2321
```

6) 用自动批量启动脚本来启动HDFS
远程命令：
```
$ ssh hdp-04 "/root/apps/hadoop-2.8.1/sbin/hadoop-daeom.sh start datanode
```
1)	先配置hdp-01到集群中所有机器（包含自己）的免密登陆
```
$ ssh-keygen
$ ssh-copy-id hadoop@192.168.233.200
```

2)	配完免密后，可以执行一次  ssh 0.0.0.0
3)	修改hadoop安装目录中/etc/hadoop/slaves（把需要启动datanode进程的节点列入）
```
hdp-01
hdp-02
hdp-03
hdp-04
```
4)	在hdp-01上用脚本：start-dfs.sh 来自动启动整个集群
5)	如果要停止，则用脚本：stop-dfs.sh


## 4 客户端的理解

hdfs的客户端有多种形式：
- 网页形式
- 命令行形式
- 客户端在哪里运行，没有约束，只要运行客户端的机器能够跟hdfs集群联网

文件的切块大小和存储的副本数量，都是由客户端决定！
所谓的由客户端决定，是通过配置参数来定的

hdfs的客户端会读以下两个参数，来决定切块大小、副本数量：
- 切块大小的参数： dfs.blocksize
- 副本数量的参数： dfs.replication

上面两个参数应该配置在客户端机器的hadoop目录中的hdfs-site.xml中配置
```xml
<property>
<name>dfs.blocksize</name>
<value>64m</value>
</property>

<property>
<name>dfs.replication</name>
<value>2</value>
</property>
```


## 5 hdfs命令行客户端的常用操作命令
0、查看hdfs中的目录信息
```
hadoop fs -ls /hdfs路径
```

1、上传文件到hdfs中
```
hadoop fs -put /本地文件  /aaa
hadoop fs -copyFromLocal /本地文件  /hdfs路径   ##  copyFromLocal等价于 put
hadoop fs -moveFromLocal /本地文件  /hdfs路径  ## 跟copyFromLocal的区别是：从本地移动到hdfs中
```

2、下载文件到客户端本地磁盘
```
hadoop fs -get /hdfs中的路径   /本地磁盘目录
hadoop fs -copyToLocal /hdfs中的路径 /本地磁盘路径   ## 跟get等价
hadoop fs -moveToLocal /hdfs路径  /本地路径  ## 从hdfs中移动到本地
```

3、在hdfs中创建文件夹
```
hadoop fs -mkdir  -p /aaa/xxx
```

4、移动hdfs中的文件（更名）
```
hadoop fs -mv /hdfs的路径  /hdfs的另一个路径
```

5、删除hdfs中的文件或文件夹
```
hadoop fs -rm -r /aaa
```

6、修改文件的权限
```
hadoop fs -chown user:group /aaa
hadoop fs -chmod 700 /aaa
```

7、追加内容到已存在的文件
```
hadoop fs -appendToFile /本地文件   /hdfs中的文件
```

8、显示文本文件的内容
```
hadoop fs -cat /hdfs中的文件
hadoop fs -tail /hdfs中的文件
```

补充：hdfs命令行客户端的所有命令列表

```
Usage: hadoop fs [generic options]
        [-appendToFile <localsrc> ... <dst>]
        [-cat [-ignoreCrc] <src> ...]
        [-checksum <src> ...]
        [-chgrp [-R] GROUP PATH...]
        [-chmod [-R] <MODE[,MODE]... | OCTALMODE> PATH...]
        [-chown [-R] [OWNER][:[GROUP]] PATH...]
        [-copyFromLocal [-f] [-p] [-l] [-d] <localsrc> ... <dst>]
        [-copyToLocal [-f] [-p] [-ignoreCrc] [-crc] <src> ... <localdst>]
        [-count [-q] [-h] [-v] [-t [<storage type>]] [-u] [-x] <path> ...]
        [-cp [-f] [-p | -p[topax]] [-d] <src> ... <dst>]
        [-createSnapshot <snapshotDir> [<snapshotName>]]
        [-deleteSnapshot <snapshotDir> <snapshotName>]
        [-df [-h] [<path> ...]]
        [-du [-s] [-h] [-x] <path> ...]
        [-expunge]
        [-find <path> ... <expression> ...]
        [-get [-f] [-p] [-ignoreCrc] [-crc] <src> ... <localdst>]
        [-getfacl [-R] <path>]
        [-getfattr [-R] {-n name | -d} [-e en] <path>]
        [-getmerge [-nl] [-skip-empty-file] <src> <localdst>]
        [-help [cmd ...]]
        [-ls [-C] [-d] [-h] [-q] [-R] [-t] [-S] [-r] [-u] [<path> ...]]
        [-mkdir [-p] <path> ...]
        [-moveFromLocal <localsrc> ... <dst>]
        [-moveToLocal <src> <localdst>]
        [-mv <src> ... <dst>]
        [-put [-f] [-p] [-l] [-d] <localsrc> ... <dst>]
        [-renameSnapshot <snapshotDir> <oldName> <newName>]
        [-rm [-f] [-r|-R] [-skipTrash] [-safely] <src> ...]
        [-rmdir [--ignore-fail-on-non-empty] <dir> ...]
        [-setfacl [-R] [{-b|-k} {-m|-x <acl_spec>} <path>]|[--set <acl_spec> <path>]]
        [-setfattr {-n name [-v value] | -x name} <path>]
        [-setrep [-R] [-w] <rep> <path> ...]
        [-stat [format] <path> ...]
        [-tail [-f] <file>]
        [-test -[defsz] <path>]
        [-text [-ignoreCrc] <src> ...]
        [-touchz <path> ...]
        [-truncate [-w] <length> <path> ...]
        [-usage [cmd ...]]
```

## 6 hdfs的java客户端编程

导库：
```
hadoop-2.7.1\share\hadoop\common\hadoop-common-2.7.1.jar
hadoop-2.7.1\share\hadoop\common\lib\*
hadoop-2.7.1\share\hadoop\hdfs\hadoop-hdfs-2.7.1.jar
hadoop-2.7.1\share\hadoop\hdfs\lib\*
```

### 上传文件到hdfs

```java
package hdfs01;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class HdfsClient {
	public static void main(String[] args) throws Exception {
		/**
		 * Configuration参数对象的机制：
		 *    构造时，会加载jar包中的默认配置 xx-default.xml
		 *    再加载 用户配置xx-site.xml  ，覆盖掉默认参数
		 *    构造完成之后，还可以conf.set("p","v")，会再次覆盖用户配置文件中的参数值
		 */
		// new Configuration()会从项目的classpath中加载core-default.xml hdfs-default.xml core-site.xml hdfs-site.xml等文件
		Configuration conf = new Configuration();
		
		// 指定本客户端上传文件到hdfs时需要保存的副本数为：2
		conf.set("dfs.replication", "2");
		// 指定本客户端上传文件到hdfs时切块的规格大小：64M
		conf.set("dfs.blocksize", "64m");
		
		// 构造一个访问指定HDFS系统的客户端对象: 参数1:——HDFS系统的URI，参数2：——客户端要特别指定的参数，参数3：客户端的身份（用户名）
		FileSystem fs = FileSystem.get(new URI("hdfs://192.168.233.200:9000/"),conf,"hadoop");
		
		// 上传一个文件到HDFS中  D:/home/big-data/software/dev/bashrc.txt
		fs.copyFromLocalFile(new Path("D:/home/big-data/software/dev/bashrc.txt"), new Path("/test"));
		
		fs.close();
	}
	
}
```
### 其他操作

**从hdfs下载到本地windows需要配置HADOOP_HOME,因为下载到本地需要操作本地磁盘，调用的c语言库，windows下需要重新编译hadoop。**

```
D:\home\big-data\software\dev\hadoop-2.8.1-windows\hadoop-2.8.1
```
需要的文件
```
winutils.exe
hadoop.dll
```

在 C:\Windows\System32 下面 放入`hadoop.dll`文件

代码：

```java
	FileSystem fs = null;
	
	@Before
	public void init() throws Exception{
		Configuration conf = new Configuration();
		conf.set("dfs.replication", "2");
		conf.set("dfs.blocksize", "64m");
		
		fs = FileSystem.get(new URI("hdfs://192.168.233.200:9000/"), conf, "hadoop");
		
	}
	
	
	/**
	 * 从HDFS中下载文件到客户端本地磁盘
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */

	@Test
	public void testGet() throws IllegalArgumentException, IOException{
		
		fs.copyToLocalFile(new Path("/bashrc.txt_bak"), new Path("d:/"));
		fs.close();
		
	}
	
	/**
	 * 在hdfs内部移动文件\修改名称
	 */
	@Test
	public void testRename() throws Exception{
		
		fs.rename(new Path("/bashrc.txt"), new Path("/bashrc.txt_bak"));
		
		fs.close();
		
	}
	
	
	/**
	 * 在hdfs中创建文件夹
	 */
	@Test
	public void testMkdir() throws Exception{
		
		fs.mkdirs(new Path("/xx/yy/zz"));
		
		fs.close();
	}
	
	
	/**
	 * 在hdfs中删除文件或文件夹
	 */
	@Test
	public void testRm() throws Exception{
		
		fs.delete(new Path("/xx/yy/zz"), true);
		
		fs.close();
	}
	
	
	
	/**
	 * 查询hdfs指定目录下的文件信息
	 */
	@Test
	public void testLs() throws Exception{
		// 只查询文件的信息,不返回文件夹的信息
		RemoteIterator<LocatedFileStatus> iter = fs.listFiles(new Path("/"), true);
		
		while(iter.hasNext()){
			LocatedFileStatus status = iter.next();
			System.out.println("文件全路径："+status.getPath());
			System.out.println("块大小："+status.getBlockSize());
			System.out.println("文件长度："+status.getLen());
			System.out.println("副本数量："+status.getReplication());
			System.out.println("块信息："+Arrays.toString(status.getBlockLocations()));
			
			System.out.println("--------------------------------");
		}
		fs.close();
	}
	
	/**
	 * 查询hdfs指定目录下的文件和文件夹信息
	 */
	@Test
	public void testLs2() throws Exception{
		FileStatus[] listStatus = fs.listStatus(new Path("/"));
		
		for(FileStatus status:listStatus){
			System.out.println("文件全路径："+status.getPath());
			System.out.println(status.isDirectory()?"这是文件夹":"这是文件");
			System.out.println("块大小："+status.getBlockSize());
			System.out.println("文件长度："+status.getLen());
			System.out.println("副本数量："+status.getReplication());
			
			System.out.println("--------------------------------");
		}
		fs.close();
	}
```


## 7 hdfs的核心工作原理
namenode元数据管理要点

1、什么是元数据？

hdfs的目录结构及每一个文件的块信息（块的id，块的副本数量，块的存放位置<datanode>）

2、元数据由谁负责管理？

namenode

3、namenode把元数据记录在哪里？

- namenode的实时的完整的元数据存储在内存中；
- namenode还会在磁盘中（dfs.namenode.name.dir）存储内存元数据在某个时间点上的镜像文件；
- namenode会把引起元数据变化的客户端操作记录在edits日志文件中；

secondarynamenode会定期从namenode上下载fsimage镜像和新生成的edits日志，然后加载fsimage镜像到内存中，然后顺序解析edits文件，对内存中的元数据对象进行修改（整合）
整合完成后，将内存元数据序列化成一个新的fsimage，并将这个fsimage镜像文件上传给namenode

>上述过程叫做：checkpoint操作

>提示：secondary namenode每次做checkpoint操作时，都需要从namenode上下载上次的fsimage镜像文件吗？
>第一次checkpoint需要下载，以后就不用下载了，因为自己的机器上就已经有了。



 

#### 补充：secondary namenode启动位置的配置
默认值	
```
<property>
  <name>dfs.namenode.secondary.http-address</name>
  <value>0.0.0.0:50090</value>
</property>
```
把默认值改成你想要的机器主机名即可

secondarynamenode保存元数据文件的目录配置：
```
<property>
  <name>dfs.namenode.checkpoint.dir</name>
  <value>file://${hadoop.tmp.dir}/dfs/namesecondary</value>
</property>
```

改成自己想要的路径即可：/root/dfs/namesecondary



