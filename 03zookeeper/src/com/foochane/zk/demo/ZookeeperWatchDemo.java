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
		// 构造一个连接zookeeper的客户端对象
		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, new Watcher() {

			@Override
			public void process(WatchedEvent event) {

				if (event.getState() == KeeperState.SyncConnected && event.getType() == EventType.NodeDataChanged) {
					System.out.println(event.getPath()); // 收到的事件所发生的节点路径
					System.out.println(event.getType()); // 收到的事件的类型
					System.out.println("赶紧换照片，换浴室里面的洗浴套装....."); // 收到事件后，我们的处理逻辑

					try {
						zk.getData("/mygirls", true, null);

					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}
				}else if(event.getState() == KeeperState.SyncConnected && event.getType() == EventType.NodeChildrenChanged){
					
					System.out.println("子节点变化了......");
				}

			}
		});
	}

	@Test
	public void testGetWatch() throws Exception {

		byte[] data = zk.getData("/mygirls", true, null); // 监听节点数据变化
		
		List<String> children = zk.getChildren("/mygirls", true); //监听节点的子节点变化事件

		System.out.println(new String(data, "UTF-8"));

		Thread.sleep(Long.MAX_VALUE);

	}

}
