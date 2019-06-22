package com.foochane.mapreduce.flow;

import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * 本类是提供给MapTask用的
 * MapTask通过这个类的getPartition方法，来计算它所产生的每一对kv数据该分发给哪一个reduce task
 * @author ThinkPad
 *
 */
public class ProvincePartitioner extends Partitioner<Text, FlowBean>{
	static HashMap<String,Integer> codeMap = new HashMap<>();
	static{
		
		codeMap.put("135", 0);
		codeMap.put("136", 1);
		codeMap.put("137", 2);
		codeMap.put("138", 3);
		codeMap.put("139", 4);
		
	}
	
	
	
	@Override
	public int getPartition(Text key, FlowBean value, int numPartitions) {
		
		Integer code = codeMap.get(key.toString().substring(0, 3));
		return code==null?5:code;
	}

}
