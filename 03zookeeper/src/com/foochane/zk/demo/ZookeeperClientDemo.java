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
		// 构造一个连接zookeeper的客户端对象
		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, null);
	}
	
	
	@Test
	public void testCreate() throws Exception{

		// 参数1：要创建的节点路径  参数2：数据  参数3：访问权限  参数4：节点类型
		String create = zk.create("/eclipse", "hello eclipse".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		
		System.out.println(create);
		
		zk.close();
		
	}
	
	
	@Test
	public void testUpdate() throws Exception {
		
		// 参数1：节点路径   参数2：数据    参数3：所要修改的版本，-1代表任何版本
		zk.setData("/eclipse", "我爱你".getBytes("UTF-8"), -1);
		
		zk.close();
		
	}
	
	
	@Test	
	public void testGet() throws Exception {
		// 参数1：节点路径    参数2：是否要监听    参数3：所要获取的数据的版本,null表示最新版本
		byte[] data = zk.getData("/eclipse", false, null);
		System.out.println(new String(data,"UTF-8"));
		
		zk.close();
	}
	
	
	
	@Test	
	public void testListChildren() throws Exception {
		// 参数1：节点路径    参数2：是否要监听   
		// 注意：返回的结果中只有子节点名字，不带全路径
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
