package com.foochane.zk.demo;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperClientDemo {
	ZooKeeper zk = null;
	@Before
	public void init()  throws Exception{
		// ����һ������zookeeper�Ŀͻ��˶���
		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, null);
	}
	
	
	@Test
	public void testCreate() throws Exception{

		// ����1��Ҫ�����Ľڵ�·��  ����2������  ����3������Ȩ��  ����4���ڵ�����
		String create = zk.create("/eclipse", "hello eclipse".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		
		System.out.println(create);
		
		zk.close();
		
	}
	
	
	@Test
	public void testUpdate() throws Exception {
		
		// ����1���ڵ�·��   ����2������    ����3����Ҫ�޸ĵİ汾��-1�����κΰ汾
		zk.setData("/eclipse", "�Ұ���".getBytes("UTF-8"), -1);
		
		zk.close();
		
	}
	
	
	@Test	
	public void testGet() throws Exception {
		// ����1���ڵ�·��    ����2���Ƿ�Ҫ����    ����3����Ҫ��ȡ�����ݵİ汾,null��ʾ���°汾
		byte[] data = zk.getData("/eclipse", false, null);
		System.out.println(new String(data,"UTF-8"));
		
		zk.close();
	}
	
	
	
	@Test	
	public void testListChildren() throws Exception {
		// ����1���ڵ�·��    ����2���Ƿ�Ҫ����   
		// ע�⣺���صĽ����ֻ���ӽڵ����֣�����ȫ·��
		List<String> children = zk.getChildren("/cc", false);
		
		for (String child : children) {
			System.out.println(child);
		}
		
		zk.close();
	}
	
	
	@Test
	public void testRm() throws InterruptedException, KeeperException{
		
		zk.delete("/eclipse", -1);
		
		zk.close();
	}
	
	
	

}
