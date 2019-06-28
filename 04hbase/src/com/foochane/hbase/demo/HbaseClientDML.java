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
		// ����һ�����Ӷ���
		Configuration conf = HBaseConfiguration.create(); // ���Զ�����hbase-site.xml
		conf.set("hbase.zookeeper.quorum", "Master:2181,Slave01:2181,Slave02:2181");
		
		conn = ConnectionFactory.createConnection(conf);
	}
	
	
	/**
	 * ��
	 * ��:put������
	 * @throws Exception 
	 */
	@Test
	public void testPut() throws Exception{
		
		// ��ȡһ������ָ�����table����,����DML����
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		// ����Ҫ���������Ϊһ��Put����(һ��put����ֻ�ܶ�Ӧһ��rowkey)�Ķ���
		Put put = new Put(Bytes.toBytes("001"));
		put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("username"), Bytes.toBytes("����"));
		put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("age"), Bytes.toBytes("18"));
		put.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"), Bytes.toBytes("����"));
		
		
		Put put2 = new Put(Bytes.toBytes("002"));
		put2.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("username"), Bytes.toBytes("����"));
		put2.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("age"), Bytes.toBytes("28"));
		put2.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"), Bytes.toBytes("�Ϻ�"));
	
		
		ArrayList<Put> puts = new ArrayList<>();
		puts.add(put);
		puts.add(put2);
		
		
		// ���ȥ
		table.put(puts);
		
		table.close();
		conn.close();
		
	}
	
	
	/**
	 * ѭ�������������
	 * @throws Exception 
	 */
	@Test
	public void testManyPuts() throws Exception{
		
		Table table = conn.getTable(TableName.valueOf("user_info"));
		ArrayList<Put> puts = new ArrayList<>();
		
		for(int i=0;i<100000;i++){
			Put put = new Put(Bytes.toBytes(""+i));
			put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("username"), Bytes.toBytes("����"+i));
			put.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("age"), Bytes.toBytes((18+i)+""));
			put.addColumn(Bytes.toBytes("extra_info"), Bytes.toBytes("addr"), Bytes.toBytes("����"));
			
			puts.add(put);
		}
		
		table.put(puts);
		
	}
	
	/**
	 * ɾ
	 * @throws Exception 
	 */
	@Test
	public void testDelete() throws Exception{
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		// ����һ�������װҪɾ����������Ϣ
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
	 * ��
	 * @throws Exception 
	 */
	@Test
	public void testGet() throws Exception{
		
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		Get get = new Get("002".getBytes());
		
		Result result = table.get(get);
		
		// �ӽ����ȡ�û�ָ����ĳ��key��value
		byte[] value = result.getValue("base_info".getBytes(), "age".getBytes());
		System.out.println(new String(value));
		
		System.out.println("-------------------------");
		
		// �������н���е�����kv��Ԫ��
		CellScanner cellScanner = result.cellScanner();
		while(cellScanner.advance()){
			Cell cell = cellScanner.current();
			
			byte[] rowArray = cell.getRowArray();  //��kv�������м����ֽ�����
			byte[] familyArray = cell.getFamilyArray();  //���������ֽ�����
			byte[] qualifierArray = cell.getQualifierArray();  //�������ֽ�����
			byte[] valueArray = cell.getValueArray(); // value���ֽ�����
			
			System.out.println("�м�: "+new String(rowArray,cell.getRowOffset(),cell.getRowLength()));
			System.out.println("������: "+new String(familyArray,cell.getFamilyOffset(),cell.getFamilyLength()));
			System.out.println("����: "+new String(qualifierArray,cell.getQualifierOffset(),cell.getQualifierLength()));
			System.out.println("value: "+new String(valueArray,cell.getValueOffset(),cell.getValueLength()));
			
		}
		
		table.close();
		conn.close();
		
	}
	
	
	/**
	 * ���м���Χ��ѯ����
	 * @throws Exception 
	 */
	@Test
	public void testScan() throws Exception{
		
		Table table = conn.getTable(TableName.valueOf("user_info"));
		
		// ������ʼ�м��������������м�,�������������ѯ��ĩβ���Ǹ��м�����ô��������ĩβ�м���ƴ��һ�����ɼ����ֽڣ�\000��
		Scan scan = new Scan("10".getBytes(), "10000\001".getBytes());
		
		ResultScanner scanner = table.getScanner(scan);
		
		Iterator<Result> iterator = scanner.iterator();
		
		while(iterator.hasNext()){
			
			Result result = iterator.next();
			// �������н���е�����kv��Ԫ��
			CellScanner cellScanner = result.cellScanner();
			while(cellScanner.advance()){
				Cell cell = cellScanner.current();
				
				byte[] rowArray = cell.getRowArray();  //��kv�������м����ֽ�����
				byte[] familyArray = cell.getFamilyArray();  //���������ֽ�����
				byte[] qualifierArray = cell.getQualifierArray();  //�������ֽ�����
				byte[] valueArray = cell.getValueArray(); // value���ֽ�����
				
				System.out.println("�м�: "+new String(rowArray,cell.getRowOffset(),cell.getRowLength()));
				System.out.println("������: "+new String(familyArray,cell.getFamilyOffset(),cell.getFamilyLength()));
				System.out.println("����: "+new String(qualifierArray,cell.getQualifierOffset(),cell.getQualifierLength()));
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
