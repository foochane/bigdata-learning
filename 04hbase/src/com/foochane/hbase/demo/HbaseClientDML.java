package com.foochane.hbase.demo;


import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;

public class HbaseClientDML {
	Connection conn = null;
	
	@Before
	public void getConn() throws Exception{
		// 构建一个连接对象
		Configuration conf = HBaseConfiguration.create(); // 会自动加载hbase-site.xml
		conf.set("hbase.zookeeper.quorum", "Master:2181,Slave01:2181,Slave02:2181");
		
		conn = ConnectionFactory.createConnection(conf);
	}
	
	
	/**
	 * 增
	 * 改:put来覆盖
	 * @throws Exception 
	 */
	@Test
	public void testPut() throws Exception{
		
		// 获取一个操作指定表的table对象,进行DML操作
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		// 构造要插入的数据为一个Put类型(一个put对象只能对应一个rowkey)的对象
		Put put = new Put(Bytes.toBytes("001"));
		put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("username"), Bytes.toBytes("张三"));
		put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("age"), Bytes.toBytes("18"));
		put.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"), Bytes.toBytes("北京"));
		
		
		Put put2 = new Put(Bytes.toBytes("002"));
		put2.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("username"), Bytes.toBytes("李四"));
		put2.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("age"), Bytes.toBytes("28"));
		put2.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"), Bytes.toBytes("上海"));
	
		
		ArrayList<Put> puts = new ArrayList<>();
		puts.add(put);
		puts.add(put2);
		
		
		// 插进去
		table.put(puts);
		
		table.close();
		conn.close();
		
	}
	
	
	/**
	 * 循环插入大量数据
	 * @throws Exception 
	 */
	@Test
	public void testManyPuts() throws Exception{
		
		Table table = conn.getTable(TableName.valueOf("user_info"));
		ArrayList<Put> puts = new ArrayList<>();
		
		for(int i=0;i<100000;i++){
			Put put = new Put(Bytes.toBytes(""+i));
			put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("username"), Bytes.toBytes("张三"+i));
			put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("age"), Bytes.toBytes((18+i)+""));
			put.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"), Bytes.toBytes("北京"));
			
			puts.add(put);
		}
		
		table.put(puts);
		
	}
	
	/**
	 * 删
	 * @throws Exception 
	 */
	@Test
	public void testDelete() throws Exception{
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		// 构造一个对象封装要删除的数据信息
		Delete delete1 = new Delete(Bytes.toBytes("001"));
		
		Delete delete2 = new Delete(Bytes.toBytes("002"));
		delete2.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"));
		
		ArrayList<Delete> dels = new ArrayList<>();
		dels.add(delete1);
		dels.add(delete2);
		
		table.delete(dels);
		
		
		table.close();
		conn.close();
	}
	
	/**
	 * 查
	 * @throws Exception 
	 */
	@Test
	public void testGet() throws Exception{
		
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		Get get = new Get("002".getBytes());
		
		Result result = table.get(get);
		
		// 从结果中取用户指定的某个key的value
		byte[] value = result.getValue("base_info".getBytes(), "age".getBytes());
		System.out.println(new String(value));
		
		System.out.println("-------------------------");
		
		// 遍历整行结果中的所有kv单元格
		CellScanner cellScanner = result.cellScanner();
		while(cellScanner.advance()){
			Cell cell = cellScanner.current();
			
			byte[] rowArray = cell.getRowArray();  //本kv所属的行键的字节数组
			byte[] familyArray = cell.getFamilyArray();  //列族名的字节数组
			byte[] qualifierArray = cell.getQualifierArray();  //列名的字节数据
			byte[] valueArray = cell.getValueArray(); // value的字节数组
			
			System.out.println("行键: "+new String(rowArray,cell.getRowOffset(),cell.getRowLength()));
			System.out.println("列族名: "+new String(familyArray,cell.getFamilyOffset(),cell.getFamilyLength()));
			System.out.println("列名: "+new String(qualifierArray,cell.getQualifierOffset(),cell.getQualifierLength()));
			System.out.println("value: "+new String(valueArray,cell.getValueOffset(),cell.getValueLength()));
			
		}
		
		table.close();
		conn.close();
		
	}
	
	
	/**
	 * 按行键范围查询数据
	 * @throws Exception 
	 */
	@Test
	public void testScan() throws Exception{
		
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		// 包含起始行键，不包含结束行键,但是如果真的想查询出末尾的那个行键，那么，可以在末尾行键上拼接一个不可见的字节（\000）
		Scan scan = new Scan("10".getBytes(), "10000\001".getBytes());
		
		ResultScanner scanner = table.getScanner(scan);
		
		Iterator<Result> iterator = scanner.iterator();
		
		while(iterator.hasNext()){
			
			Result result = iterator.next();
			// 遍历整行结果中的所有kv单元格
			CellScanner cellScanner = result.cellScanner();
			while(cellScanner.advance()){
				Cell cell = cellScanner.current();
				
				byte[] rowArray = cell.getRowArray();  //本kv所属的行键的字节数组
				byte[] familyArray = cell.getFamilyArray();  //列族名的字节数组
				byte[] qualifierArray = cell.getQualifierArray();  //列名的字节数据
				byte[] valueArray = cell.getValueArray(); // value的字节数组
				
				System.out.println("行键: "+new String(rowArray,cell.getRowOffset(),cell.getRowLength()));
				System.out.println("列族名: "+new String(familyArray,cell.getFamilyOffset(),cell.getFamilyLength()));
				System.out.println("列名: "+new String(qualifierArray,cell.getQualifierOffset(),cell.getQualifierLength()));
				System.out.println("value: "+new String(valueArray,cell.getValueOffset(),cell.getValueLength()));
			}
			System.out.println("----------------------");
		}
	}
	
	@Test
	public void test(){
		String a = "000";
		String b = "000\0";
		
		System.out.println(a);
		System.out.println(b);
		
		
		byte[] bytes = a.getBytes();
		byte[] bytes2 = b.getBytes();
		
		System.out.println("");
		
	}
	
	

}
