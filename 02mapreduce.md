


## 安装yarn集群
配置yarn，修改yarn-site.xml文件

```
<property>
	<name>yarn.resourcemanager.hostname</name>
	<value>hdp-04</value>
</property>

<property>
	<name>yarn.nodemanager.aux-services</name>
	<value>mapreduce_shuffle</value>
</property>
```

另外，可以指定内存大小和cpu核，不指定则按默认值，每个节点8G,8核。
```
<property>
	<name>yarn.nodemanager.resource.memory-mb</name>
	<value>2048</value>
</property

<property>
	<name>yarn.nodemanager.resource.cpu-vcores</name>
	<value>2</value>
</property>
```



yarn集群中有两个角色：
- 主节点：Resource Manager  1台
- 从节点：Node Manager   N台

而且：
- Resource Manager一般安装在一台专门的机器上
- Node Manager应该与HDFS中的data node重叠在一起

启动yarn是在指定安装的那台机器上启动，其他机器不启动。


启动yarn集群：
```
sbin/start-yarn.sh
```
停止：
```
sbin/stop-yarn.sh
```

查看端口：
```
netstat -nltp | grep 2321
```
启动完成后，可以在windows上用浏览器访问resourcemanager的web端口：
http://hdp-04:8088
看resource mananger是否认出了所有的node manager节点

查看内存剩余：
```
free -m
```


配置maven
找配置文件：https://search.maven.org/
搜索：hadoop-client

```xml
<dependency>
  <groupId>org.apache.hadoop</groupId>
  <artifactId>hadoop-client</artifactId>
  <version>2.7.1</version>
</dependency>
```


打包 [选择工程] ---> [run as] -->[Maven install]

报错：
```
[INFO ] 2019-06-22 16:01:06,512 method:org.apache.hadoop.yarn.client.RMProxy.createRMProxy(RMProxy.java:98)
Connecting to ResourceManager at /0.0.0.0:8032
Invalid Command Usage : 
Exception in thread "main" java.lang.IllegalArgumentException: cmdLineSyntax not provided
	at org.apache.commons.cli.HelpFormatter.printHelp(HelpFormatter.java:472)
	at org.apache.commons.cli.HelpFormatter.printHelp(HelpFormatter.java:418)
	at org.apache.commons.cli.HelpFormatter.printHelp(HelpFormatter.java:334)
	at org.apache.hadoop.yarn.client.cli.ApplicationCLI.printUsage(ApplicationCLI.java:255)
	at org.apache.hadoop.yarn.client.cli.ApplicationCLI.run(ApplicationCLI.java:243)
	at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:70)
	at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:84)
	at org.apache.hadoop.yarn.client.cli.ApplicationCLI.main(ApplicationCLI.java:83)
```

```
<property>
    <name>yarn.resourcemanager.address</name>
    <value>Master:8032</value>
</property>
<property>
    <name>yarn.resourcemanager.scheduler.address</name>
    <value>Master:8030</value>
</property>
  <property>
    <name>yarn.resourcemanager.resource-tracker.address</name>
    <value>Master:8031</value>
</property>
```
  
  
运行java程序
 
 java -cp jar包  类名
 
 hadoop jar 编译好的jar包
 
 会把机器里面，所有的hadoop的jar加到当前路径里面
 
 如果没有配置 mapred-site.xml 文件，提交程序后mapreduce默认在本地运行
 ```
 <!--默认只为local-->
         <property>
                <name>mapreduce.framework.name</name>
                <value>yarn</value>
        </property>
```

