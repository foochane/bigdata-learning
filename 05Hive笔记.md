

## 1 基本介绍

1 HIVE是什么
`Hive`是一个可以将`sql`翻译为`MR`程序的工具
`Hive`支持用户将`HDFS`上的文件映射为表结构，然后用户就可以输入SQL对这些表（HDFS上的文件）进行查询分析
`Hive`将用户定义的库、表结构等信息存储hive的元数据库（可以是本地derby，也可以是远程mysql）中


2 Hive的用途
解放大数据分析程序员，不用自己写大量的mr程序来分析数据，只需要写sql脚本即可
Hive可用于构建大数据体系下的数据仓库


**`hive 2` 以后 把底层引擎从`MapReduce`换成了`Spark`**

启动hive前要先启动hdfs 和yarn

## 2 使用方式

### 2.1 方式1：直接使用hive服务端

输入命令 `$ hive`即可：
```sql
hadoop@Master:~$ hive
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/usr/local/bigdata/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/usr/local/bigdata/hadoop-2.7.1/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]

Logging initialized using configuration in file:/usr/local/bigdata/hive-2.3.5/conf/hive-log4j2.properties Async: true
Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
hive>show databases;
OK
dbtest
default
Time taken: 3.539 seconds, Fetched: 2 row(s)
hive>

```

技巧：
让提示符显示当前库：
```sql
hive>set hive.cli.print.current.db=true;

```

显示查询结果是显示自带名称：
```sql
hive>set hive.cli.print.header=true;

```

这样设置只是对当前窗口有效，永久生效可以在当前用户目录下建一个`.hiverc`文件。
加入如下内容：
```sql
set hive.cli.print.current.db=true;
set hive.cli.print.header=true;

```



### 2.2 方式2：使用beeline客户端

将hive启动为一个服务端，然后可以在任意一台机器上使用beeline客户端连接hive服务，进行交互式查询

hive是一个单机的服务端可以在任何一台机器里安装，它访问的是hdfs集群。

启动hive服务 ：

```sql
$ nohup hiveserver2 1>/dev/null 2>&1 &

```

启动后，可以用beeline去连接，beeline是一个客户端，可以在任意机器启动,只要能够跟hive服务端相连即可。

在本地启动beeline
```sql
$ beeline -u jdbc:hive2://localhost:10000 -n hadoop -p hadoop

```

在启动机器上启动beeline
```sql
$ beeline -u jdbc:hive2://Master:10000 -n hadoop -p hadoop

```

示例：

```shell
hadoop@Master:~$ beeline -u jdbc:hive2://Master:10000 -n hadoop -p hadoop
Connecting to jdbc:hive2://Master:10000
19/06/25 01:50:12 INFO jdbc.Utils: Supplied authorities: Master:10000
19/06/25 01:50:12 INFO jdbc.Utils: Resolved authority: Master:10000
19/06/25 01:50:13 INFO jdbc.HiveConnection: Will try to open client transport with JDBC Uri: jdbc:hive2://Master:10000
Connected to: Apache Hive (version 2.3.5)
Driver: Hive JDBC (version 1.2.1.spark2)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 1.2.1.spark2 by Apache Hive
0: jdbc:hive2://Master:10000> 

```

- u ：指定连接方式
- n ：登录的用户（系统用户）
- p ：用户密码


报错：
```shell
 errorMessage:Failed to open new session: java.lang.RuntimeException: org.apache.hadoop.ipc.RemoteException(org.apache.hadoop.security.authorize.AuthorizationException): User: hadoop is not allowed to impersonate hadoop), serverProtocolVersion:null)

```

解决:

在 hadoop配置文件中的core-site.xml 文件中添加如下内容,然后重启hadoop集群：
```xml
<property>
      <name>hadoop.proxyuser.hadoop.groups</name>
      <value>hadoop</value>
      <description>Allow the superuser oozie to impersonate any members of the group group1 and group2</description>
 </property>
 
 <property>
      <name>hadoop.proxyuser.hadoop.hosts</name>
      <value>Master,127.0.0.1,localhost</value>
      <description>The superuser can connect only from host1 and host2 to impersonate a user</description>
  </property>

```


### 2.3 方式3：使用hive命令运行sql

- 直接用 hive -e 运行 sql命令
```sql
hive -e "sql1;sql2;sql3;sql4"

```

- 事先将sql语句写入一个文件比如 q.hql ，然后用hive命令执行：　　
```
bin/hive -f q.hql

```

### 2.4 方式4：写脚本

可以将方式3写入一个xxx.sh脚本中  



## 3 表的基本操作

### 3.1 建库

```sql
create database db1;

```

示例：
```sql
0: jdbc:hive2://Master:10000> create database db1;
No rows affected (1.123 seconds)
0: jdbc:hive2://Master:10000> show databases;
+----------------+--+
| database_name  |
+----------------+--+
| db1            |
| dbtest         |
| default        |
+----------------+--+

```

成功后，hive就会在`/user/hive/warehouse/`下建一个文件夹： `db1.db`

### 3.2 删库
```sql
drop database db1;

```

示例：
```sql
0: jdbc:hive2://Master:10000> drop database db1;
No rows affected (0.969 seconds)
0: jdbc:hive2://Master:10000> show databases;
+----------------+--+
| database_name  |
+----------------+--+
| dbtest         |
| default        |
+----------------+--+

```

### 3.3 建内部表
```sql
use db1;
create table t_test(id int,name string,age int)
row format delimited
fields terminated by ',';
```

示例：
```sql
0: jdbc:hive2://Master:10000> use db1;
No rows affected (0.293 seconds)
0: jdbc:hive2://Master:10000> create table t_test(id int,name string,age int)
0: jdbc:hive2://Master:10000> row format delimited
0: jdbc:hive2://Master:10000> fields terminated by ',';
No rows affected (1.894 seconds)
0: jdbc:hive2://Master:10000> desc db1.t_test;
+-----------+------------+----------+--+
| col_name  | data_type  | comment  |
+-----------+------------+----------+--+
| id        | int        |          |
| name      | string     |          |
| age       | int        |          |
+-----------+------------+----------+--+
3 rows selected (0.697 seconds)

```

建表后，hive会在仓库目录中建一个表目录：  /user/hive/warehouse/db1.db/t_test


### 3.4 建外部表
```sql
create external table t_test1(id int,name string,age int)
row format delimited
fields terminated by ','
location '/user/hive/external/t_test1';

```

这里的`location`指的是`hdfs`上的目录，可以直接在该目录下放入相应格式的文件，就可以在`hive`表中查看到。

示例：
```sql
0: jdbc:hive2://Master:10000> create external table t_test1(id int,name string,age int)
0: jdbc:hive2://Master:10000> row format delimited
0: jdbc:hive2://Master:10000> fields terminated by ','
0: jdbc:hive2://Master:10000> location '/user/hive/external/t_test1';
No rows affected (0.7 seconds)
0: jdbc:hive2://Master:10000> desc db1.t_test1;
+-----------+------------+----------+--+
| col_name  | data_type  | comment  |
+-----------+------------+----------+--+
| id        | int        |          |
| name      | string     |          |
| age       | int        |          |
+-----------+------------+----------+--+
3 rows selected (0.395 seconds)

```

本地创建测试文件`user.data`
```sql
1,xiaowang,28
2,xiaoli,18
3,xiaohong,23

```

放入hdfs中：
```shell
$ hdfs dfs -mkdir -p /user/hive/external/t_test1
$ hdfs dfs -put ./user.data /user/hive/external/t_test1

```

此时在hive表中就可以查看到数据：
```sql
0: jdbc:hive2://Master:10000> select * from db1.t_test1;
+-------------+---------------+--------------+--+
| t_test1.id  | t_test1.name  | t_test1.age  |
+-------------+---------------+--------------+--+
| 1           | xiaowang      | 28           |
| 2           | xiaoli        | 18           |
| 3           | xiaohong      | 23           |
+-------------+---------------+--------------+--+
3 rows selected (8 seconds)

```

**注意：如果删除外部表，hdfs里的文件并不会删除**

也就是如果包`db1.t_test1`删除，hdfs下`/user/hive/external/t_test1/user.data`文件并不会被删除。

###3.4 导入数据
本质上就是把数据文件放入表目录；

可以用hive命令来做：
```sql
load data [local] inpath '/data/path' [overwrite] into table t_test;

```

加`local`代表导入本地数据。

导入本地数据
```sql
load data local inpath '/home/hadoop/user.data' into table t_test;

```

示例：
```sql
0: jdbc:hive2://Master:10000> load data local inpath '/home/hadoop/user.data' into table t_test;
No rows affected (2.06 seconds)
0: jdbc:hive2://Master:10000> select * from db1.t_test;
+------------+--------------+-------------+--+
| t_test.id  | t_test.name  | t_test.age  |
+------------+--------------+-------------+--+
| 1          | xiaowang     | 28          |
| 2          | xiaoli       | 18          |
| 3          | xiaohong     | 23          |
+------------+--------------+-------------+--+

```
导入hdfs中的数据
```sql
load data inpath '/user/hive/external/t_test1/user.data' into table t_test;

```
示例：
```sql
0: jdbc:hive2://Master:10000> load data inpath '/user/hive/external/t_test1/user.data' into table t_test;
No rows affected (1.399 seconds)
0: jdbc:hive2://Master:10000> select * from db1.t_test;
+------------+--------------+-------------+--+
| t_test.id  | t_test.name  | t_test.age  |
+------------+--------------+-------------+--+
| 1          | xiaowang     | 28          |
| 2          | xiaoli       | 18          |
| 3          | xiaohong     | 23          |
| 1          | xiaowang     | 28          |
| 2          | xiaoli       | 18          |
| 3          | xiaohong     | 23          |
+------------+--------------+-------------+--+
6 rows selected (0.554 seconds)

```

**注意：从本地导入数据，本地数据不是发生变化，从hdfs中导入数据，hdfs中的导入的文件会被移动到数据仓库相应的目录下**


### 3.6 建分区表
分区的意义在于可以将数据分子目录存储，以便于查询时让数据读取范围更精准
```sql
create table t_test1(id int,name string,age int,create_time bigint)
partitioned by (day string,country string)
row format delimited
fields terminated by ',';

```

插入数据到指定分区：
```sql
> load data [local] inpath '/data/path1' [overwrite] into table t_test partition(day='2019-06-04',country='China');
> load data [local] inpath '/data/path2' [overwrite] into table t_test partition(day='2019-06-05',country='China');
> load data [local] inpath '/data/path3' [overwrite] into table t_test partition(day='2019-06-04',country='England');

```
导入完成后，形成的目录结构如下：
```sql
/user/hive/warehouse/db1.db/t_test1/day=2019-06-04/country=China/...
/user/hive/warehouse/db1.db/t_test1/day=2019-06-04/country=England/...
/user/hive/warehouse/db1.db/t_test1/day=2019-06-05/country=China/...

```

## 4 查询语法

### 4.1 条件查询
```sql
select * from t_table where a<1000 and b>0;

```


### 4.2 join关联查询

各类join

测试数据：
a.txt：
```sql
a,1
b,2
c,3
d,4

```

b.txt:
```sql
b,16
c,17
d,18
e,19

```

建表导入数据：
```sql
create table t_a(name string,num int)
row format delimited
fields terminated by ',';

create table t_b(name string,age int)
row format delimited
fields terminated by ',';

load data local inpath '/home/hadoop/a.txt' into table t_a;
load data local inpath '/home/hadoop/b.txt' into table t_b;

```

表数据如下：
```sql
0: jdbc:hive2://Master:10000> select * from t_a;
+-----------+----------+--+
| t_a.name  | t_a.num  |
+-----------+----------+--+
| a         | 1        |
| b         | 2        |
| c         | 3        |
| d         | 4        |
+-----------+----------+--+
4 rows selected (0.523 seconds)
0: jdbc:hive2://Master:10000> select * from t_b;
+-----------+----------+--+
| t_b.name  | t_b.age  |
+-----------+----------+--+
| b         | 16       |
| c         | 17       |
| d         | 18       |
| e         | 19       |
+-----------+----------+--+

4 rows selected (0.482 seconds)

```

### 4.3 内连接

指定join条件

```sql
select a.*,b.*
from 
t_a a join t_b b on a.name=b.name;

```

示例：
```sql
0: jdbc:hive2://Master:10000> select a.*,b.*
0: jdbc:hive2://Master:10000> from
0: jdbc:hive2://Master:10000> t_a a join t_b b on a.name=b.name;
....
+---------+--------+---------+--------+--+
| a.name  | a.num  | b.name  | b.age  |
+---------+--------+---------+--------+--+
| b       | 2      | b       | 16     |
| c       | 3      | c       | 17     |
| d       | 4      | d       | 18     |
+---------+--------+---------+--------+--+

```


### 4.4 左外连接（左连接）
```sql
select a.*,b.*
from 
t_a a left outer join t_b b on a.name=b.name;

```

示例：
```sql
0: jdbc:hive2://Master:10000> select a.*,b.*
0: jdbc:hive2://Master:10000> from
0: jdbc:hive2://Master:10000> t_a a left outer join t_b b on a.name=b.name;
...
+---------+--------+---------+--------+--+
| a.name  | a.num  | b.name  | b.age  |
+---------+--------+---------+--------+--+
| a       | 1      | NULL    | NULL   |
| b       | 2      | b       | 16     |
| c       | 3      | c       | 17     |
| d       | 4      | d       | 18     |
+---------+--------+---------+--------+--+


```


### 4.5 右外连接（右连接）
```sql
select a.*,b.*
from 
t_a a right outer join t_b b on a.name=b.name;

```

示例：
```sql
0: jdbc:hive2://Master:10000> select a.*,b.*
0: jdbc:hive2://Master:10000> from
0: jdbc:hive2://Master:10000> t_a a right outer join t_b b on a.name=b.name;
....
+---------+--------+---------+--------+--+
| a.name  | a.num  | b.name  | b.age  |
+---------+--------+---------+--------+--+
| b       | 2      | b       | 16     |
| c       | 3      | c       | 17     |
| d       | 4      | d       | 18     |
| NULL    | NULL   | e       | 19     |
+---------+--------+---------+--------+--+

```


### 4.6全外连接
```sql
select a.*,b.*
from
t_a a full outer join t_b b on a.name=b.name;

```

示例：
```sql
0: jdbc:hive2://Master:10000> select a.*,b.*
0: jdbc:hive2://Master:10000> from
0: jdbc:hive2://Master:10000> t_a a full outer join t_b b on a.name=b.name;
WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
+---------+--------+---------+--------+--+
| a.name  | a.num  | b.name  | b.age  |
+---------+--------+---------+--------+--+
| a       | 1      | NULL    | NULL   |
| b       | 2      | b       | 16     |
| c       | 3      | c       | 17     |
| d       | 4      | d       | 18     |
| NULL    | NULL   | e       | 19     |
+---------+--------+---------+--------+--+

```


### 4.7左半连接

求存在于a表，且b表里也存在的数据。

```sql
select a.*
from 
t_a a left semi join t_b b on a.name=b.name;

```

示例：
```sql
0: jdbc:hive2://Master:10000> select a.*
0: jdbc:hive2://Master:10000> from
0: jdbc:hive2://Master:10000> t_a a left semi join t_b b on a.name=b.name;
.....
+---------+--------+--+
| a.name  | a.num  |
+---------+--------+--+
| b       | 2      |
| c       | 3      |
| d       | 4      |
+---------+--------+--+

```

### 4.8 group by分组聚合

构建测试数据
```sql
192.168.33.3,http://www.xxx.cn/stu,2019-08-04 15:30:20
192.168.33.3,http://www.xxx.cn/teach,2019-08-04 15:35:20
192.168.33.4,http://www.xxx.cn/stu,2019-08-04 15:30:20
192.168.33.4,http://www.xxx.cn/job,2019-08-04 16:30:20

192.168.33.5,http://www.xxx.cn/job,2019-08-04 15:40:20
192.168.33.3,http://www.xxx.cn/stu,2019-08-05 15:30:20
192.168.44.3,http://www.xxx.cn/teach,2019-08-05 15:35:20
192.168.33.44,http://www.xxx.cn/stu,2019-08-05 15:30:20
192.168.33.46,http://www.xxx.cn/job,2019-08-05 16:30:20

192.168.33.55,http://www.xxx.cn/job,2019-08-05 15:40:20
192.168.133.3,http://www.xxx.cn/register,2019-08-06 15:30:20
192.168.111.3,http://www.xxx.cn/register,2019-08-06 15:35:20
192.168.34.44,http://www.xxx.cn/pay,2019-08-06 15:30:20
192.168.33.46,http://www.xxx.cn/excersize,2019-08-06 16:30:20
192.168.33.55,http://www.xxx.cn/job,2019-08-06 15:40:20
192.168.33.46,http://www.xxx.cn/excersize,2019-08-06 16:30:20
192.168.33.25,http://www.xxx.cn/job,2019-08-06 15:40:20
192.168.33.36,http://www.xxx.cn/excersize,2019-08-06 16:30:20
192.168.33.55,http://www.xxx.cn/job,2019-08-06 15:40:20
```

建分区表，导入数据：
```sql
create table t_pv(ip string,url string,time string)
partitioned by (dt string)
row format delimited 
fields terminated by ',';

load data local inpath '/home/hadoop/pv.log.0804' into table t_pv partition(dt='2019-08-04');
load data local inpath '/home/hadoop/pv.log.0805' into table t_pv partition(dt='2019-08-05');
load data local inpath '/home/hadoop/pv.log.0806' into table t_pv partition(dt='2019-08-06');

```

查看数据：
```sql
0: jdbc:hive2://Master:10000> select * from t_pv;
+----------------+------------------------------+----------------------+-------------+--+
|    t_pv.ip     |           t_pv.url           |      t_pv.time       |   t_pv.dt   |
+----------------+------------------------------+----------------------+-------------+--+
| 192.168.33.3   | http://www.xxx.cn/stu        | 2019-08-04 15:30:20  | 2019-08-04  |
| 192.168.33.3   | http://www.xxx.cn/teach      | 2019-08-04 15:35:20  | 2019-08-04  |
| 192.168.33.4   | http://www.xxx.cn/stu        | 2019-08-04 15:30:20  | 2019-08-04  |
| 192.168.33.4   | http://www.xxx.cn/job        | 2019-08-04 16:30:20  | 2019-08-04  |
| 192.168.33.5   | http://www.xxx.cn/job        | 2019-08-04 15:40:20  | 2019-08-05  |
| 192.168.33.3   | http://www.xxx.cn/stu        | 2019-08-05 15:30:20  | 2019-08-05  |
| 192.168.44.3   | http://www.xxx.cn/teach      | 2019-08-05 15:35:20  | 2019-08-05  |
| 192.168.33.44  | http://www.xxx.cn/stu        | 2019-08-05 15:30:20  | 2019-08-05  |
| 192.168.33.46  | http://www.xxx.cn/job        | 2019-08-05 16:30:20  | 2019-08-05  |
| 192.168.33.55  | http://www.xxx.cn/job        | 2019-08-05 15:40:20  | 2019-08-06  |
| 192.168.133.3  | http://www.xxx.cn/register   | 2019-08-06 15:30:20  | 2019-08-06  |
| 192.168.111.3  | http://www.xxx.cn/register   | 2019-08-06 15:35:20  | 2019-08-06  |
| 192.168.34.44  | http://www.xxx.cn/pay        | 2019-08-06 15:30:20  | 2019-08-06  |
| 192.168.33.46  | http://www.xxx.cn/excersize  | 2019-08-06 16:30:20  | 2019-08-06  |
| 192.168.33.55  | http://www.xxx.cn/job        | 2019-08-06 15:40:20  | 2019-08-06  |
| 192.168.33.46  | http://www.xxx.cn/excersize  | 2019-08-06 16:30:20  | 2019-08-06  |
| 192.168.33.25  | http://www.xxx.cn/job        | 2019-08-06 15:40:20  | 2019-08-06  |
| 192.168.33.36  | http://www.xxx.cn/excersize  | 2019-08-06 16:30:20  | 2019-08-06  |
| 192.168.33.55  | http://www.xxx.cn/job        | 2019-08-06 15:40:20  | 2019-08-06  |
+----------------+------------------------------+----------------------+-------------+--+
```

查看表分区：

```sql
show partitions t_pv;

```

```sql
0: jdbc:hive2://Master:10000> show partitions t_pv;
+----------------+--+
|   partition    |
+----------------+--+
| dt=2019-08-04  |
| dt=2019-08-05  |
| dt=2019-08-06  |
+----------------+--+
3 rows selected (0.575 seconds)

```

##### 每一行的url变成大写
- 针对每一行进行运算

```sql
select ip,upper(url),time
from t_pv

```

```sql
0: jdbc:hive2://Master:10000> select ip,upper(url),time
0: jdbc:hive2://Master:10000> from t_pv
+----------------+------------------------------+----------------------+--+
|       ip       |             _c1              |         time         |
+----------------+------------------------------+----------------------+--+
| 192.168.33.3   | HTTP://WWW.XXX.CN/STU        | 2019-08-04 15:30:20  |
| 192.168.33.3   | HTTP://WWW.XXX.CN/TEACH      | 2019-08-04 15:35:20  |
| 192.168.33.4   | HTTP://WWW.XXX.CN/STU        | 2019-08-04 15:30:20  |
| 192.168.33.4   | HTTP://WWW.XXX.CN/JOB        | 2019-08-04 16:30:20  |
| 192.168.33.5   | HTTP://WWW.XXX.CN/JOB        | 2019-08-04 15:40:20  |
| 192.168.33.3   | HTTP://WWW.XXX.CN/STU        | 2019-08-05 15:30:20  |
| 192.168.44.3   | HTTP://WWW.XXX.CN/TEACH      | 2019-08-05 15:35:20  |
| 192.168.33.44  | HTTP://WWW.XXX.CN/STU        | 2019-08-05 15:30:20  |
| 192.168.33.46  | HTTP://WWW.XXX.CN/JOB        | 2019-08-05 16:30:20  |
| 192.168.33.55  | HTTP://WWW.XXX.CN/JOB        | 2019-08-05 15:40:20  |
| 192.168.133.3  | HTTP://WWW.XXX.CN/REGISTER   | 2019-08-06 15:30:20  |
| 192.168.111.3  | HTTP://WWW.XXX.CN/REGISTER   | 2019-08-06 15:35:20  |
| 192.168.34.44  | HTTP://WWW.XXX.CN/PAY        | 2019-08-06 15:30:20  |
| 192.168.33.46  | HTTP://WWW.XXX.CN/EXCERSIZE  | 2019-08-06 16:30:20  |
| 192.168.33.55  | HTTP://WWW.XXX.CN/JOB        | 2019-08-06 15:40:20  |
| 192.168.33.46  | HTTP://WWW.XXX.CN/EXCERSIZE  | 2019-08-06 16:30:20  |
| 192.168.33.25  | HTTP://WWW.XXX.CN/JOB        | 2019-08-06 15:40:20  |
| 192.168.33.36  | HTTP://WWW.XXX.CN/EXCERSIZE  | 2019-08-06 16:30:20  |
| 192.168.33.55  | HTTP://WWW.XXX.CN/JOB        | 2019-08-06 15:40:20  |
+----------------+------------------------------+----------------------+--+

```


##### 求每条url的访问次数
```sql
select url ,count(1) --对分好组的数据进行逐行运算
from t_pv
group by url;

```

```sql
0: jdbc:hive2://Master:10000> select url ,count(1)
0: jdbc:hive2://Master:10000> from t_pv
0: jdbc:hive2://Master:10000> group by url;
·····
+------------------------------+------+--+
|             url              | _c1  |
+------------------------------+------+--+
| http://www.xxx.cn/excersize  | 3    |
| http://www.xxx.cn/job        | 7    |
| http://www.xxx.cn/pay        | 1    |
| http://www.xxx.cn/register   | 2    |
| http://www.xxx.cn/stu        | 4    |
| http://www.xxx.cn/teach      | 2    |
+------------------------------+------+--+

```

可以给_c1加入字段名称：
```sql
select url ,count(1) as count
from t_pv
group by url;

```


##### 求每个页面的访问者中ip最大的一个
```sql
select url,max(ip)
from t_pv
group by url;

```

```sql
0: jdbc:hive2://Master:10000> select url,max(ip)
0: jdbc:hive2://Master:10000> from t_pv
0: jdbc:hive2://Master:10000> group by url;
WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
+------------------------------+----------------+--+
|             url              |      _c1       |
+------------------------------+----------------+--+
| http://www.xxx.cn/excersize  | 192.168.33.46  |
| http://www.xxx.cn/job        | 192.168.33.55  |
| http://www.xxx.cn/pay        | 192.168.34.44  |
| http://www.xxx.cn/register   | 192.168.133.3  |
| http://www.xxx.cn/stu        | 192.168.33.44  |
| http://www.xxx.cn/teach      | 192.168.44.3   |
+------------------------------+----------------+--+

```


##### 求每个用户访问同一个页面的所有记录中，时间最晚的一条
```sql
select ip,url,max(time)
from t_pv
group by ip,url;

```

```sql
0: jdbc:hive2://Master:10000> select ip,url,max(time)
0: jdbc:hive2://Master:10000> from t_pv
0: jdbc:hive2://Master:10000> group by ip,url;
.....
+----------------+------------------------------+----------------------+--+
|       ip       |             url              |         _c2          |
+----------------+------------------------------+----------------------+--+
| 192.168.111.3  | http://www.xxx.cn/register   | 2019-08-06 15:35:20  |
| 192.168.133.3  | http://www.xxx.cn/register   | 2019-08-06 15:30:20  |
| 192.168.33.25  | http://www.xxx.cn/job        | 2019-08-06 15:40:20  |
| 192.168.33.3   | http://www.xxx.cn/stu        | 2019-08-05 15:30:20  |
| 192.168.33.3   | http://www.xxx.cn/teach      | 2019-08-04 15:35:20  |
| 192.168.33.36  | http://www.xxx.cn/excersize  | 2019-08-06 16:30:20  |
| 192.168.33.4   | http://www.xxx.cn/job        | 2019-08-04 16:30:20  |
| 192.168.33.4   | http://www.xxx.cn/stu        | 2019-08-04 15:30:20  |
| 192.168.33.44  | http://www.xxx.cn/stu        | 2019-08-05 15:30:20  |
| 192.168.33.46  | http://www.xxx.cn/excersize  | 2019-08-06 16:30:20  |
| 192.168.33.46  | http://www.xxx.cn/job        | 2019-08-05 16:30:20  |
| 192.168.33.5   | http://www.xxx.cn/job        | 2019-08-04 15:40:20  |
| 192.168.33.55  | http://www.xxx.cn/job        | 2019-08-06 15:40:20  |
| 192.168.34.44  | http://www.xxx.cn/pay        | 2019-08-06 15:30:20  |
| 192.168.44.3   | http://www.xxx.cn/teach      | 2019-08-05 15:35:20  |
+----------------+------------------------------+----------------------+--+

```

##### 求8月4号以后，每天http://www.xxx.cn/job的总访问次数，及访问者中ip地址中最大的
```sql
select dt,'http://www.xxx.cn/job',count(1),max(ip)
from t_pv
where url='http://www.xxx.cn/job'
group by dt having dt>'2019-08-04';


select dt,max(url),count(1),max(ip)
from t_pv
where url='http://www.xxx.cn/job'
group by dt having dt>'2019-08-04';


select dt,url,count(1),max(ip)
from t_pv
where url='http://www.xxx.cn/job'
group by dt,url having dt>'2019-08-04';



select dt,url,count(1),max(ip)
from t_pv
where url='http://www.xxx.cn/job' and dt>'2019-08-04'
group by dt,url;

```

##### 求8月4号以后，每天每个页面的总访问次数，及访问者中ip地址中最大的

```sql
select dt,url,count(1),max(ip)
from t_pv
where dt>'2019-08-04'
group by dt,url;

```

##### 求8月4号以后，每天每个页面的总访问次数，及访问者中ip地址中最大的，且只查询出总访问次数>2 的记录

- 方式1：
```
select dt,url,count(1) as cnts,max(ip)
from t_pv
where dt>'2019-08-04'
group by dt,url having cnts>2;
```

- 方式2：用子查询
```
select dt,url,cnts,max_ip
from
(select dt,url,count(1) as cnts,max(ip) as max_ip
from t_pv
where dt>'2019-08-04'
group by dt,url) tmp
where cnts>2;
```


## 5 基本数据类型

### 5.1 数字类型

### 5.2 日期类型

### 5.3 字符串类型

### 5.4 布尔类型

### 5.5 复合类型

#### 5.5.1 数组类型

有如下数据：
```sql
玩具总动员4,汤姆·汉克斯:蒂姆·艾伦:安妮·波茨,2019-06-21
流浪地球,屈楚萧:吴京:李光洁:吴孟达,2019-02-05
千与千寻,柊瑠美:入野自由:夏木真理:菅原文太,2019-06-21
战狼2,吴京:弗兰克·格里罗:吴刚:张翰:卢靖姗,2017-08-16
```

建表导入数据：
```sql
--建表映射：
create table t_movie(movie_name string,actors array<string>,first_show date)
row format delimited fields terminated by ','
collection items terminated by ':';

--导入数据
load data local inpath '/home/hadoop/actor.dat' into table t_movie;

```

```sql
0: jdbc:hive2://Master:10000> select * from t_movie;
+---------------------+-----------------------------------+---------------------+--+
| t_movie.movie_name  |          t_movie.actors           | t_movie.first_show  |
+---------------------+-----------------------------------+---------------------+--+
| 玩具总动员4              | ["汤姆·汉克斯","蒂姆·艾伦","安妮·波茨"]        | 2019-06-21          |
| 流浪地球                | ["屈楚萧","吴京","李光洁","吴孟达"]          | 2019-02-05          |
| 千与千寻                | ["柊瑠美","入野自由","夏木真理","菅原文太"]      | 2019-06-21          |
| 战狼2                 | ["吴京","弗兰克·格里罗","吴刚","张翰","卢靖姗"]  | 2017-08-16          |
+---------------------+-----------------------------------+---------------------+--+

```
##### array[]

###### 查询每部电影主演
```sql
select movie_name,actors[0],first_show from t_movie;
```
```sql
0: jdbc:hive2://Master:10000> select movie_name,actors[0],first_show from t_movie;
+-------------+---------+-------------+--+
| movie_name  |   _c1   | first_show  |
+-------------+---------+-------------+--+
| 玩具总动员4      | 汤姆·汉克斯  | 2019-06-21  |
| 流浪地球        | 屈楚萧     | 2019-02-05  |
| 千与千寻        | 柊瑠美     | 2019-06-21  |
| 战狼2         | 吴京      | 2017-08-16  |
+-------------+---------+-------------+--+

```

##### array_contains

###### 查询包含'吴京'的电影
```sql
select movie_name,actors,first_show
from t_movie where array_contains(actors,'吴京');

```

```sql
0: jdbc:hive2://Master:10000> select movie_name,actors,first_show
0: jdbc:hive2://Master:10000> from t_movie where array_contains(actors,'吴京');
+-------------+-----------------------------------+-------------+--+
| movie_name  |              actors               | first_show  |
+-------------+-----------------------------------+-------------+--+
| 流浪地球        | ["屈楚萧","吴京","李光洁","吴孟达"]          | 2019-02-05  |
| 战狼2         | ["吴京","弗兰克·格里罗","吴刚","张翰","卢靖姗"]  | 2017-08-16  |
+-------------+-----------------------------------+-------------+--+
```

##### size

###### 每部电影查询列出的演员数量
```sql
select movie_name
,size(actors) as actor_number
,first_show
from t_movie;
```

```sql
0: jdbc:hive2://Master:10000> from t_movie;
+-------------+---------------+-------------+--+
| movie_name  | actor_number  | first_show  |
+-------------+---------------+-------------+--+
| 玩具总动员4      | 3             | 2019-06-21  |
| 流浪地球        | 4             | 2019-02-05  |
| 千与千寻        | 4             | 2019-06-21  |
| 战狼2         | 5             | 2017-08-16  |
+-------------+---------------+-------------+--+
```


#### 5.5.2 map类型

##### 数据
```sql
1,zhangsan,father:xiaoming#mother:xiaohuang#brother:xiaoxu,28
2,lisi,father:mayun#mother:huangyi#brother:guanyu,22
3,wangwu,father:wangjianlin#mother:ruhua#sister:jingtian,29
4,mayun,father:mayongzhen#mother:angelababy,26
```

导入数据
```sql
-- 建表映射上述数据
create table t_family(id int,name string,family_members map<string,string>,age int)
row format delimited fields terminated by ','
collection items terminated by '#'
map keys terminated by ':';

-- 导入数据
load data local inpath '/root/hivetest/fm.dat' into table t_family;
```
```sql
0: jdbc:hive2://Master:10000> select * from t_family;
+--------------+----------------+----------------------------------------------------------------+---------------+--+
| t_family.id  | t_family.name  |                    t_family.family_members                     | t_family.age  |
+--------------+----------------+----------------------------------------------------------------+---------------+--+
| 1            | zhangsan       | {"father":"xiaoming","mother":"xiaohuang","brother":"xiaoxu"}  | 28            |
| 2            | lisi           | {"father":"mayun","mother":"huangyi","brother":"guanyu"}       | 22            |
| 3            | wangwu         | {"father":"wangjianlin","mother":"ruhua","sister":"jingtian"}  | 29            |
| 4            | mayun          | {"father":"mayongzhen","mother":"angelababy"}                  | 26            |
+--------------+----------------+----------------------------------------------------------------+---------------+--+
```


##### 查出每个人的 爸爸、姐妹
```sql
select id,name,family_members["father"] as father,family_members["sister"] as sister,age
from t_family;
```

##### 查出每个人有哪些亲属关系
```sql
select id,name,map_keys(family_members) as relations,age
from  t_family;
```
##### 查出每个人的亲人名字
```sql
select id,name,map_values(family_members) as relations,age
from  t_family;
```

##### 查出每个人的亲人数量
```sql
select id,name,size(family_members) as relations,age
from  t_family;
```

##### 查出所有拥有兄弟的人及他的兄弟是谁
```sql
-- 方案1：一句话写完
select id,name,age,family_members['brother']
from t_family  where array_contains(map_keys(family_members),'brother');


-- 方案2：子查询
select id,name,age,family_members['brother']
from
(select id,name,age,map_keys(family_members) as relations,family_members 
from t_family) tmp 
where array_contains(relations,'brother');

```

#### 5.5.3 stuct类型

数据
```sql
1,zhangsan,18:male:深圳
2,lisi,28:female:北京
3,wangwu,38:male:广州
4,laowang,26:female:上海
5,yangyang,35:male:杭州
```
导入数据：
```sql

-- 建表映射上述数据

drop table if exists t_user;
create table t_user(id int,name string,info struct<age:int,sex:string,addr:string>)
row format delimited fields terminated by ','
collection items terminated by ':';

-- 导入数据
load data local inpath '/home/hadoop/user.dat' into table t_user;

```
```sql
0: jdbc:hive2://Master:10000> select * from t_user;
+------------+--------------+----------------------------------------+--+
| t_user.id  | t_user.name  |              t_user.info               |
+------------+--------------+----------------------------------------+--+
| 1          | zhangsan     | {"age":18,"sex":"male","addr":"深圳"}    |
| 2          | lisi         | {"age":28,"sex":"female","addr":"北京"}  |
| 3          | wangwu       | {"age":38,"sex":"male","addr":"广州"}    |
| 4          | laowang      | {"age":26,"sex":"female","addr":"上海"}  |
| 5          | yangyang     | {"age":35,"sex":"male","addr":"杭州"}    |
+------------+--------------+----------------------------------------+--+
```

##### 查询每个人的id name和地址
```sql
select id,name,info.addr
from t_user;
```
```sql
0: jdbc:hive2://Master:10000> select id,name,info.addr
0: jdbc:hive2://Master:10000> from t_user;
+-----+-----------+-------+--+
| id  |   name    | addr  |
+-----+-----------+-------+--+
| 1   | zhangsan  | 深圳    |
| 2   | lisi      | 北京    |
| 3   | wangwu    | 广州    |
| 4   | laowang   | 上海    |
| 5   | yangyang  | 杭州    |
+-----+-----------+-------+--+
```


## 6 常用内置函数

测试函数
```sql
select substr("abcdef",1,3);

```
```sql
0: jdbc:hive2://Master:10000> select substr("abcdef",1,3);
+------+--+
| _c0  |
+------+--+
| abc  |
+------+--+
```


### 6.1 时间处理函数
```sql
from_unixtime(21938792183,'yyyy-MM-dd HH:mm:ss') 

```
返回： '2017-06-03 17:50:30'

### 6.2 类型转换函数
```sql
select cast("8" as int);
select cast("2019-2-3" as data)
```


### 6.3 字符串截取和拼接
```sql
substr("abcde",1,3)  -->   'abc'
concat('abc','def')  -->  'abcdef'
```

```sql
0: jdbc:hive2://Master:10000> select substr("abcde",1,3);
+------+--+
| _c0  |
+------+--+
| abc  |
+------+--+
1 row selected (0.152 seconds)
0: jdbc:hive2://Master:10000> select concat('abc','def');
+---------+--+
|   _c0   |
+---------+--+
| abcdef  |
+---------+--+
1 row selected (0.165 seconds)
```

### 6.4 Json数据解析函数

```sql
get_json_object('{\"key1\":3333，\"key2\":4444}' , '$.key1') 

```

返回：`3333`
```sql
json_tuple('{\"key1\":3333，\"key2\":4444}','key1','key2') as(key1,key2)

```
返回：`3333, 4444`


### 6.5 url解析函数
```sql
parse_url_tuple('http://www.xxxx.cn/bigdata?userid=8888','HOST','PATH','QUERY','QUERY:userid')

```

返回： `www.xxxx.cn  /bigdata     userid=8888   8888`




## 7 自定义函数

数据：
```sql
1,zhangsan:18-1999063117:30:00-beijing
2,lisi:28-1989063117:30:00-shanghai
3,wangwu:20-1997063117:30:00-tieling

```

建表导入数据：
```sql
create table t_user_info(info string)
row format delimited;

```

导入数据：
```
load data local inpath '/root/udftest.data' into table t_user_info;

```

###### 需求：利用上表生成如下新表
```sql
t_user：uid,uname,age,birthday,address

```

思路：可以自定义一个函数parse_user_info()，能传入一行上述数据，返回切分好的字段


然后可以通过如下sql完成需求：
```sql
create t_user
as
select 
parse_user_info(info,0) as uid,
parse_user_info(info,1) as uname,
parse_user_info(info,2) as age,
parse_user_info(info,3) as birthday_date,
parse_user_info(info,4) as birthday_time,
parse_user_info(info,5) as address
from t_user_info;
```

实现关键：  自定义parse_user_info() 函数

实现步骤：

1、写一个java类实现函数所需要的功能
```java
public class UserInfoParser extends UDF{	
	// 1,zhangsan:18-1999063117:30:00-beijing
	public String evaluate(String line,int index) {
		String newLine = line.replaceAll(",", "\001").replaceAll(":", "\001").replaceAll("-", "\001");
		StringBuilder sb = new StringBuilder();
		String[] split = newLine.split("\001");
		StringBuilder append = sb.append(split[0])
		.append("\t")
		.append(split[1])
		.append("\t")
		.append(split[2])
		.append("\t")
		.append(split[3].substring(0, 8))
		.append("\t")
		.append(split[3].substring(8, 10)).append(split[4]).append(split[5])
		.append("\t")
		.append(split[6]);
		
		String res = append.toString();

		return res.split("\t")[index];
	}
}
```

2、将java类打成jar包: d:/up.jar

3、上传jar包到hive所在的机器上  /root/up.jar

4、在hive的提示符中添加jar包
```sql
hive>  add jar /root/up.jar;

```

5、创建一个hive的自定义函数名 跟  写好的jar包中的java类对应
```sql
hive>  create temporary function parse_user_info as 'com.doit.hive.udf.UserInfoParser';

```

