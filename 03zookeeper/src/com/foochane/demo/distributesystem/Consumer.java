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

	// ����һ��list���ڴ�����µ����߷������б�
	private volatile ArrayList<String> onlineServers = new ArrayList<>();

	// ����zk���Ӷ���
	ZooKeeper zk = null;

	// ����zk�ͻ�������
	public void connectZK() throws Exception {

		zk = new ZooKeeper("hdp-01:2181,hdp-02:2181,hdp-03:2181", 2000, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected && event.getType() == EventType.NodeChildrenChanged) {

					try {
						// �¼��ص��߼��У��ٴβ�ѯzk�ϵ����߷������ڵ㼴�ɣ���ѯ�߼������ٴ�ע�����ӽڵ�仯�¼�����
						getOnlineServers();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		});

	}

	// ��ѯ���߷������б�
	public void getOnlineServers() throws Exception {

		List<String> children = zk.getChildren("/servers", true);
		ArrayList<String> servers = new ArrayList<>();

		for (String child : children) {
			byte[] data = zk.getData("/servers/" + child, false, null);

			String serverInfo = new String(data);

			servers.add(serverInfo);
		}

		onlineServers = servers;
		System.out.println("��ѯ��һ��zk����ǰ���ߵķ������У�" + servers);

	}

	public void sendRequest() throws Exception {
		Random random = new Random();
		while (true) {
			try {
				// ��ѡһ̨��ǰ���ߵķ�����
				int nextInt = random.nextInt(onlineServers.size());
				String server = onlineServers.get(nextInt);
				String hostname = server.split(":")[0];
				int port = Integer.parseInt(server.split(":")[1]);

				System.out.println("����������ѡ�ķ�����Ϊ��" + server);

				Socket socket = new Socket(hostname, port);
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();

				out.write("haha".getBytes());
				out.flush();

				byte[] buf = new byte[256];
				int read = in.read(buf);
				System.out.println("��������Ӧ��ʱ��Ϊ��" + new String(buf, 0, read));

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
		// ����zk���Ӷ���
		consumer.connectZK();

		// ��ѯ���߷������б�
		consumer.getOnlineServers();

		// ����ҵ����һ̨����������ʱ���ѯ����
		consumer.sendRequest();

	}

}
