

```s
scala> val r1 = sc.textFile("file:/home/hadoop/testdata/test.txt")
r1: org.apache.spark.rdd.RDD[String] = file:/home/hadoop/testdata/test.txt MapPartitionsRDD[1] at textFile at <console>:24


scala> r1.collect
res0: Array[String] = Array(aa bbb ccc, "dd ee aa cc dd dd dd ", ww ff gg)

scala> val r2 = r1.flatMap(_.split(" "))
r2: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[2] at flatMap at <console>:25

scala> r2.collect
res1: Array[String] = Array(aa, bbb, ccc, dd, ee, aa, cc, dd, dd, dd, ww, ff, gg)

scala> val r3 = r2.map((_,1))
r3: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[3] at map at <console>:25

scala> r3.collect
res2: Array[(String, Int)] = Array((aa,1), (bbb,1), (ccc,1), (dd,1), (ee,1), (aa,1), (cc,1), (dd,1), (dd,1), (dd,1), (ww,1), (ff,1),
(gg,1))


scala> val r4 = r3.reduceByKey(_+_)
r4: org.apache.spark.rdd.RDD[(String, Int)] = ShuffledRDD[4] at reduceByKey at <console>:25

scala> r4.collect
res3: Array[(String, Int)] = Array((ee,1), (aa,2), (gg,1), (bbb,1), (dd,4), (ww,1), (ff,1), (cc,1), (ccc,1))

```


RDD : 分布式数据集





































1.RDD是一个基本的抽象，操作RDD就像操作一个本地集合一样，降低了编程的复杂度

RDD的算子分为两类，一类是Transformation（lazy），一类是Action（触发任务执行）
RDD不存真正要计算的数据，而是记录了RDD的转换关系（调用了什么方法，传入什么函数）


创建RDD有哪些中方式呢？
	1.通过外部的存储系统创建RDD
	2.将Driver的Scala集合通过并行化的方式编程RDD（试验、测验）
	3.调用一个已经存在了的RDD的Transformation，会生成一个新的RDD

	RDD的Transformation的特点
	1.lazy
	2.生成新的RDD


RDD分区的数据取决于哪些因素？
	1.如果是将Driver端的Scala集合并行化创建RDD，并且没有指定RDD的分区，RDD的分区就是为该app分配的中的和核数
	2.如果是重hdfs中读取数据创建RDD，并且设置了最新分区数量是1，那么RDD的分区数据即使输入切片的数据，如果不设置最小分区的数量，即spark调用textFile时会默认传入2，那么RDD的分区数量会打于等于输入切片的数量

-------------------------------------------
RDD的map方法，是Executor中执行时，是一条一条的将数据拿出来处理


mapPartitionsWithIndex 一次拿出一个分区（分区中并没有数据，而是记录要读取哪些数据，真正生成的Task会读取多条数据），并且可以将分区的编号取出来

功能：取分区中对应的数据时，还可以将分区的编号取出来，这样就可以知道数据是属于哪个分区的（哪个区分对应的Task的数据）

	//该函数的功能是将对应分区中的数据取出来，并且带上分区编号
    val func = (index: Int, it: Iterator[Int]) => {
      it.map(e => s"part: $index, ele: $e")
    }

-------------------------------------------

aggregateByKey   是Transformation
reduceByKey      是Transformation
filter           是Transformation
flatMap			 是Transformation
map              是ransformation
mapPartition     是ransformation
mapPartitionWithIndex 是ransformation


collect          是Action
aggregate        是Action
saveAsTextFile   是Action
foreach          是Action
foreachPartition 是Action

-------------------------------------------
作业，求最受欢迎的老师
1.在所有的老师中求出最受欢迎的老师Top3
2.求每个学科中最受欢迎老师的top3（至少用2到三种方式实现）

作业，把你以前用mapReduce实现的案例，全部用spark实现


--------------------------------------------

 *  - A list of partitions  （一系列分区，分区有编号，有顺序的）
 *  - A function for computing each split  （每一个切片都会有一个函数作业在上面用于对数据进行处理）
 *  - A list of dependencies on other RDDs  （RDD和RDD之间存在依赖关系）
 *  - Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned)
 	（可选，key value类型的RDD才有RDD[(K,V)]）如果是kv类型的RDD，会一个分区器，默认是hash-partitioned
 *  - Optionally, a list of preferred locations to compute each split on (e.g. block locations for
 *    an HDFS file)
 	（可以，如果是从HDFS中读取数据，会得到数据的最优位置（向Namenode请求元数据））

