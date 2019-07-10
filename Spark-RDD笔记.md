## 1 RDD创建

在Spark中创建RDD的创建方式大概可以分为三种：
- 从集合中创建RDD
- 从外部存储创建RDD
- 从其他RDD创建

### 1.1 从结合中创建RDD

从集合中创建RDD，Spark主要提供了两种函数：parallelize和makeRDD

```
scala> val rdd1 = sc.parallelize(Array(1,2,3,4,5))
rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:24

scala> rdd1.collect
res0: Array[Int] = Array(1, 2, 3, 4, 5)
```

```
scala> val rdd2 = sc.parallelize(List(1,2,3,4,5))
rdd2: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:24

scala> rdd2.collect
res1: Array[Int] = Array(1, 2, 3, 4, 5)
```

```
scala> val rdd3 = sc.makeRDD(Array(1,2,3,4))
rdd3: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[2] at makeRDD at <console>:24

scala> rdd3.collect
res2: Array[Int] = Array(1, 2, 3, 4)

```

### 1.2 从外部存储创建RDD

```
scala> val rdd4 = sc.textFile("hdfs://Node02:9000/test/test.txt")
rdd4: org.apache.spark.rdd.RDD[String] = hdfs://Node02:9000/test/test.txt MapPartitionsRDD[14] at textFile at <console>:24

scala> rdd4.collect
res9: Array[String] = Array(aa bbb ccc, "dd ee aa cc dd dd dd ", ww ff gg)
```

## 2 TransFormation

### 2.1 map

返回一个新的RDD，该RDD由每一个输入元素经过func函数转换后组成

```
scala> val rdd1 = sc.makeRDD(1 to 10)
rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[15] at makeRDD at <console>:24

scala> rdd1.collect
res10: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> val rdd1 = sc.makeRDD(1 to 10)
rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[16] at makeRDD at <console>:24

scala> rdd1.map(_*2).collect
res11: Array[Int] = Array(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)


scala> rdd1.map((_,1)).collect
res13: Array[(Int, Int)] = Array((1,1), (2,1), (3,1), (4,1), (5,1), (6,1), (7,1), (8,1), (9,1), (10,1))

```

### 2.2 mapPartitions

类似于map，但独立地在RDD的每一个分片上运行，因此在类型为T的RDD上运行时，func的函数类型必须是Iterator[T] => Iterator[U]。假设有N个元素，有M个分区，那么map的函数的将被调用N次,而mapPartitions被调用M次,一个函数一次处理所有分区

```
scala> val rdd = sc.parallelize(List(("kpop","female"),("zorro","male"),("mobin","male"),("lucy","female")))
rdd: org.apache.spark.rdd.RDD[(String, String)] = ParallelCollectionRDD[19] at parallelize at <console>:24

scala> :paste
// Entering paste mode (ctrl-D to finish)

def partitionsFun(iter : Iterator[(String,String)]) : Iterator[String] = {
  var woman = List[String]()
  while (iter.hasNext){
    val next = iter.next()
    next match {
       case (_,"female") => woman = next._1 :: woman
       case _ =>
    }
  }
  woman.iterator
}

// Exiting paste mode, now interpreting.

partitionsFun: (iter: Iterator[(String, String)])Iterator[String]

scala> val result = rdd.mapPartitions(partitionsFun)
result: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[20] at mapPartitions at <console>:27

scala> result.collect()
res14: Array[String] = Array(kpop, lucy)
```



### 2.3 flatMap

```
scala> val rdd2 = sc.parallelize(1 to 5).collect
rdd2: Array[Int] = Array(1, 2, 3, 4, 5)

scala> rdd2.flatMap(1 to _).collect
<console>:26: error: missing argument list for method collect in trait TraversableLike
Unapplied methods are only converted to functions when a function type is expected.
You can make this conversion explicit by writing `collect _` or `collect(_)(_)` instead of `collect`.
       rdd2.flatMap(1 to _).collect
                            ^

scala> rdd2.flatMap(1 to _)
res20: Array[Int] = Array(1, 1, 2, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4, 5)
```


### 2.4 glom
将每一个分区形成一个数组，形成新的RDD类型是RDD[Array[T]]

```
scala> val rdd = sc.parallelize(1 to 16,4)
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[21] at parallelize at <console>:24

scala> rdd.glom().collect()
res15: Array[Array[Int]] = Array(Array(1, 2, 3, 4), Array(5, 6, 7, 8), Array(9, 10, 11, 12), Array(13, 14, 15, 16))
```

### 2.5 sample(withReplacement, fraction, seed)

以指定的随机种子随机抽样出数量为fraction的数据，withReplacement表示是抽出的数据是否放回，true为有放回的抽样，false为无放回的抽样，seed用于指定随机数生成器种子。例子从RDD中随机且有放回的抽出50%的数据，随机种子值为3（即可能以1 2 3的其中一个起始值）

```
scala> val rdd = sc.parallelize(1 to 10)
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[27] at parallelize at <console>:24

scala> rdd.sample(true,0.4,3).collect
res22: Array[Int] = Array(3, 7, 9, 9)
```


### 2.6 partitionBy
对RDD进行分区操作，如果原有的partionRDD和现有的partionRDD是一致的话就不进行分区， 否则会生成ShuffleRDD
```
scala> val rdd = sc.parallelize(Array((1,"aaa"),(2,"bbb"),(3,"ccc"),(4,"ddd")),4)
rdd: org.apache.spark.rdd.RDD[(Int, String)] = ParallelCollectionRDD[32] at parallelize at <console>:24

scala> rdd.partitions.size
res25: Int = 4

scala>

scala> var rdd2 = rdd.partitionBy(new org.apache.spark.HashPartitioner(2))
rdd2: org.apache.spark.rdd.RDD[(Int, String)] = ShuffledRDD[33] at partitionBy at <console>:25

scala> rdd2.partitions.size
res26: Int = 2
```

### 2.7 sortBy(func,[ascending], [numTasks])

用func先对数据进行处理，按照处理后的数据比较结果排序。

```
scala> val rdd = sc.parallelize(List(1,2,3,4))
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[34] at parallelize at <console>:24

scala> rdd.sortBy(x => x%3).collect()
res27: Array[Int] = Array(3, 1, 4, 2)
```

### 2.8 union(otherDataset)

对源RDD和参数RDD求并集后返回一个新的RDD 
```
scala> val rdd1 = sc.parallelize(1 to 5)
rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[23] at parallelize at <console>:24

scala> val rdd2 = sc.parallelize(5 to 10)
rdd2: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[24] at parallelize at <console>:24

scala> val rdd3 = rdd1.union(rdd2)
rdd3: org.apache.spark.rdd.RDD[Int] = UnionRDD[25] at union at <console>:28

scala> rdd3.collect()
res18: Array[Int] = Array(1, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10)
```

### 2.9 subtract (otherDataset)
计算差的一种函数，去除两个RDD中相同的元素，不同的RDD将保留下来 
```
scala> val rdd = sc.parallelize(3 to 8)
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[70] at parallelize at <console>:24

scala> val rdd1 = sc.parallelize(1 to 5)
rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[71] at parallelize at <console>:24

scala> rdd.subtract(rdd1).collect()
res27: Array[Int] = Array(8, 6, 7)
```

### 2.10  groupByKey

groupByKey也是对每个key进行操作，但只生成一个sequence。

```
scala> val words = Array("one", "two", "two", "three", "three", "three")
words: Array[String] = Array(one, two, two, three, three, three)

scala> val wordPairsRDD = sc.parallelize(words).map(word => (word, 1))
wordPairsRDD: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[3] at map at <console>:26

scala> val group = wordPairsRDD.groupByKey()
group: org.apache.spark.rdd.RDD[(String, Iterable[Int])] = ShuffledRDD[4] at groupByKey at <console>:25

scala> group.collect
res9: Array[(String, Iterable[Int])] = Array((three,CompactBuffer(1, 1, 1)), (two,CompactBuffer(1, 1)), (one,CompactBuffer(1)))

scala> group.map(t=>(t._1,t._2.sum))
res10: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[5] at map at <console>:26

scala> res10.collect
res11: Array[(String, Int)] = Array((three,3), (two,2), (one,1))
```

### 2.11 reduceByKey(func, [numTasks])

在一个(K,V)的RDD上调用，返回一个(K,V)的RDD，使用指定的reduce函数，将相同key的值聚合到一起，reduce任务的个数可以通过第二个可选的参数来设置。
```
scala> val r1 = sc.parallelize(List(("female",1),("male",5),("female",5),("male",2)))
r1: org.apache.spark.rdd.RDD[(String, Int)] = ParallelCollectionRDD[9] at parallelize at <console>:24

scala> r1.reduceByKey(_+_).collect
res20: Array[(String, Int)] = Array((male,7), (female,6))
```

## 3 Action

### 3.1 reduce(func)
通过func函数聚集RDD中的所有元素，这个功能必须是可交换且可并联的

```
scala> val rdd1 = sc.makeRDD(1 to 10,2).collect
rdd1: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> rdd1.reduce(_+_)
res22: Int = 55
```

### 3.2 collect()
在驱动程序中，以数组的形式返回数据集的所有元素

### 3.3 count()
返回RDD的元素个数

### 3.4 first()
返回RDD的第一个元素（类似于take(1)）

### 3.5 take(n)
返回一个由数据集的前n个元素组成的数组

### 3.6 saveAsTextFile(path)
将数据集的元素以textfile的形式保存到HDFS文件系统或者其他支持的文件系统，对于每个元素，Spark将会调用toString方法，将它装换为文件中的文本

### 3.7 saveAsSequenceFile(path) 
将数据集中的元素以Hadoop sequencefile的格式保存到指定的目录下，可以使HDFS或者其他Hadoop支持的文件系统。

### 3.8 saveAsObjectFile(path) 
用于将RDD中的元素序列化成对象，存储到文件中。





























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

