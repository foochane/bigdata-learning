package com.foochane.demo.distributesystem;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class TimeQueryServer {
	ZooKeeper zk = null;
	
	// ����zk�ͻ�������
	public void connectZK() throws Exception{
		
		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, null);
		
		
	}
	
	
	
	// ע���������Ϣ
	public void registerServerInfo(String hostname,String port) throws Exception{
		
		/**
		 * ���ж�ע��ڵ�ĸ��ڵ��Ƿ���ڣ���������ڣ��򴴽�
		 */
		Stat stat = zk.exists("/servers", false);
		if(stat==null){
			zk.create("/servers", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		
		// ע����������ݵ�zk��Լ��ע��ڵ���
		String create = zk.create("/servers/server", (hostname+":"+port).getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		
		System.out.println(hostname+" ��������zkע����Ϣ�ɹ���ע��Ľڵ�Ϊ��" + create);
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		TimeQueryServer timeQueryServer = new TimeQueryServer();
		
		// ����zk�ͻ�������
		timeQueryServer.connectZK();
		
		// ע���������Ϣ
		timeQueryServer.registerServerInfo(args[0], args[1]);
		
		// ����ҵ���߳̿�ʼ����ҵ��
		new TimeQueryService(Integer.parseInt(args[1])).start();
		
	}
	

}
