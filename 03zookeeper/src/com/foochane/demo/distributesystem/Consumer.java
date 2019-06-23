package com.foochane.demo.distributesystem;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

public class Consumer {

	// 定义一个list用于存放最新的在线服务器列表
	private volatile ArrayList<String> onlineServers = new ArrayList<>();

	// 构造zk连接对象
	ZooKeeper zk = null;

	// 构造zk客户端连接
	public void connectZK() throws Exception {

		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected && event.getType() == EventType.NodeChildrenChanged) {

					try {
						// 事件回调逻辑中，再次查询zk上的在线服务器节点即可，查询逻辑中又再次注册了子节点变化事件监听
						getOnlineServers();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		});

	}

	// 查询在线服务器列表
	public void getOnlineServers() throws Exception {

		List<String> children = zk.getChildren("/servers", true);
		ArrayList<String> servers = new ArrayList<>();

		for (String child : children) {
			byte[] data = zk.getData("/servers/" + child, false, null);

			String serverInfo = new String(data);

			servers.add(serverInfo);
		}

		onlineServers = servers;
		System.out.println("查询了一次zk，当前在线的服务器有：" + servers);

	}

	public void sendRequest() throws Exception {
		Random random = new Random();
		while (true) {
			try {
				// 挑选一台当前在线的服务器
				int nextInt = random.nextInt(onlineServers.size());
				String server = onlineServers.get(nextInt);
				String hostname = server.split(":")[0];
				int port = Integer.parseInt(server.split(":")[1]);

				System.out.println("本次请求挑选的服务器为：" + server);

				Socket socket = new Socket(hostname, port);
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();

				out.write("haha".getBytes());
				out.flush();

				byte[] buf = new byte[256];
				int read = in.read(buf);
				System.out.println("服务器响应的时间为：" + new String(buf, 0, read));

				out.close();
				in.close();
				socket.close();

				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) throws Exception {

		Consumer consumer = new Consumer();
		// 构造zk连接对象
		consumer.connectZK();

		// 查询在线服务器列表
		consumer.getOnlineServers();

		// 处理业务（向一台服务器发送时间查询请求）
		consumer.sendRequest();

	}

}
