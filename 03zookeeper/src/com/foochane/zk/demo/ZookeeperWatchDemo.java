package com.foochane.zk.demo;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperWatchDemo {

	ZooKeeper zk = null;

	@Before
	public void init() throws Exception {
		// ����һ������zookeeper�Ŀͻ��˶���
		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, new Watcher() {

			@Override
			public void process(WatchedEvent event) {

				if (event.getState() == KeeperState.SyncConnected && event.getType() == EventType.NodeDataChanged) {
					System.out.println(event.getPath()); // �յ����¼��������Ľڵ�·��
					System.out.println(event.getType()); // �յ����¼�������
					System.out.println("�Ͻ�����Ƭ����ԡ�������ϴԡ��װ....."); // �յ��¼������ǵĴ����߼�

					try {
						zk.getData("/mygirls", true, null);

					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}
				}else if(event.getState() == KeeperState.SyncConnected && event.getType() == EventType.NodeChildrenChanged){
					
					System.out.println("�ӽڵ�仯��......");
				}

			}
		});
	}

	@Test
	public void testGetWatch() throws Exception {

		byte[] data = zk.getData("/mygirls", true, null); // �����ڵ����ݱ仯
		
		List<String> children = zk.getChildren("/mygirls", true); //�����ڵ���ӽڵ�仯�¼�

		System.out.println(new String(data, "UTF-8"));

		Thread.sleep(Long.MAX_VALUE);

	}

}
