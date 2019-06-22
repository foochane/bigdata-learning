package com.foochane.wordcount;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

public class HdfsWordcount {
	
	public static void main(String[] args) throws Exception{
		
		/**
		 * 初始化工作
		 */
		Properties props = new Properties();
		props.load(HdfsWordcount.class.getClassLoader().getResourceAsStream("job.properties"));
		
		Path input = new Path(props.getProperty("INPUT_PATH"));
		Path output = new Path(props.getProperty("OUTPUT_PATH"));
		
		
		
		Class<?> mapper_class = Class.forName(props.getProperty("MAPPER_CLASS"));
		Mapper mapper = (Mapper) mapper_class.newInstance();
		
		Context context  =  new Context();
		
		/**
		 * 处理数据
		 */
		
		FileSystem fs = FileSystem.get(new URI("hdfs://hdp-01:9000"), new Configuration(), "root");
		RemoteIterator<LocatedFileStatus> iter = fs.listFiles(input, false);
		
		while(iter.hasNext()){
			LocatedFileStatus file = iter.next();
			FSDataInputStream in = fs.open(file.getPath());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			// 逐行读取
			while ((line = br.readLine()) != null) {
				// 调用一个方法对每一行进行业务处理
				mapper.map(line, context);
				
			}
			
			br.close();
			in.close();
			
		}
		
	
		
		
		
		/**
		 * 输出结果
		 */
		HashMap<Object, Object> contextMap = context.getContextMap();
		
		if(fs.exists(output)){
			throw new RuntimeException("指定的输出目录已存在，请更换......!");
		}
		
		
		FSDataOutputStream out = fs.create(new Path(output,new Path("res.dat")));
		
		Set<Entry<Object, Object>> entrySet = contextMap.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			out.write((entry.getKey().toString()+"\t"+entry.getValue()+"\n").getBytes());
		}
		
		out.close();
		
		fs.close();
		
		
		System.out.println("恭喜！数据统计完成.....");
		
	}
	
	
}

