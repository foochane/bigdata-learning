package com.foochane.mapreduce.flow;

import java.util.Map.Entry;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

public class TreeMapTest {
	
	public static void main(String[] args) {
		
		TreeMap<FlowBean, String> tm1 = new TreeMap<>(new Comparator<FlowBean>() {

			@Override
			public int compare(FlowBean o1, FlowBean o2) {
				
				if( o2.getAmountFlow()-o1.getAmountFlow()==0){
					return o1.getPhone().compareTo(o2.getPhone());
				}
				
				return o2.getAmountFlow()-o1.getAmountFlow();
			}
		});
		
		/*tm1.put("a", 2);
		tm1.put("b", 1);
		tm1.put("aa", 11);
		tm1.put("ab", 1);
		
		Set<Entry<String, Integer>> entrySet = tm1.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			System.out.println(entry.getKey() + "," + entry.getValue());
		}*/
		
		
		FlowBean b1 = new FlowBean("1367788", 500, 300);
		FlowBean b2 = new FlowBean("1367766", 400, 200);
		FlowBean b3 = new FlowBean("1367755", 600, 400);
		FlowBean b4 = new FlowBean("1367744", 300, 500);
		
		
		tm1.put(b1, null);
		tm1.put(b2, null);
		tm1.put(b3, null);
		tm1.put(b4, null);
		
		Set<Entry<FlowBean,String>> entrySet = tm1.entrySet();
		for (Entry<FlowBean,String> entry : entrySet) {
			System.out.println(entry.getKey() +"\t"+ entry.getValue());
		}
		
		
		
	}

}
