package com.foochane.mapreduce.wordcount;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 在本地打成jar包提交到linux上通过`hadoop jar 包名`来运行：
 * hadoop jar 02mapreduce-0.0.1-SNAPSHOT.jar com.foochane.mapreduce.wordcount.JobSubmitterLinuxToYarn
 * 
 * 如果要在hadoop集群的某台机器上启动这个job提交客户端的话
 * conf里面就不需要指定 fs.defaultFS   mapreduce.framework.name
 * 
 * 因为在集群机器上用 hadoop jar xx.jar cn.edu360.mr.wc.JobSubmitter2 命令来启动客户端main方法时，
 *   hadoop jar这个命令会将所在机器上的hadoop安装目录中的jar包和配置文件加入到运行时的classpath中
 *   
 *   那么，我们的客户端main方法中的new Configuration()语句就会加载classpath中的配置文件，自然就有了 
 *   fs.defaultFS 和 mapreduce.framework.name 和 yarn.resourcemanager.hostname 这些参数配置
 *   
 *
 */
public class JobSubmitterLinuxToYarn {
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://192.168.233.200:9000");
		conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
		// 没指定默认文件系统
		// 没指定mapreduce-job提交到哪运行

		Job job = Job.getInstance(conf);
		
		
		job.setJarByClass(JobSubmitterLinuxToYarn.class);
		
		
		job.setMapperClass(WordcountMapper.class);
		job.setReducerClass(WordcountReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.setInputPaths(job, new Path("/wordcount/input"));
		FileOutputFormat.setOutputPath(job, new Path("/wordcount/output")); // 注意：输出路径必须不存在
		
		job.setNumReduceTasks(3);
		
		boolean res = job.waitForCompletion(true);
		System.exit(res?0:1);
		
	}
	

}
