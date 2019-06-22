package com.foochane.mapreduce.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class JobSubmitterWindowsLocal {
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
        
		//在windows运行默认及时加载本地程序，所以也可以不配置
		//conf.set("fs.defaultFS", "file:///");  //windows本地
		//conf.set("mapreduce.framework.name", "local");

		Job job = Job.getInstance(conf);
		
		job.setJarByClass(JobSubmitterLinuxToYarn.class);
		
		job.setMapperClass(WordcountMapper.class);
		job.setReducerClass(WordcountReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.setInputPaths(job, new Path("data/wordcount/input"));
		FileOutputFormat.setOutputPath(job, new Path("data/wordcount/output"));
		
		job.setNumReduceTasks(3);
		
		boolean res = job.waitForCompletion(true);
		System.exit(res?0:1);
		
	}
	



}
