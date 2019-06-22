package com.foochane.mapreduce.flow;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class FlowCountReducer extends Reducer<Text, FlowBean, Text, FlowBean>{
	
	
	
	/**
	 *  key：是某个手机号
	 *  values：是这个手机号所产生的所有访问记录中的流量数据
	 *  
	 *  <135,flowBean1><135,flowBean2><135,flowBean3><135,flowBean4>
	 */
	@Override
	protected void reduce(Text key, Iterable<FlowBean> values, Reducer<Text, FlowBean, Text, FlowBean>.Context context)
			throws IOException, InterruptedException {

		int upSum = 0;
		int dSum = 0;
		
		for(FlowBean value:values){
			upSum += value.getUpFlow();
			dSum += value.getdFlow();
		}
		
		
		context.write(key, new FlowBean(key.toString(), upSum, dSum));
		
	}
	

}
