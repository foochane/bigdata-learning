package com.foochane.hbase.demo;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.junit.Before;
import org.junit.Test;


/**
 *  
 *  1����������
 *  2����������ȡ��һ����DDL��������admin
 *  3��admin.createTable(����������);
 *  4��admin.disableTable(����);
	5��admin.deleteTable(����);
	6��admin.modifyTable(����������������);	
 *  
 * @author hunter.d
 *
 */
public class HbaseClientDDL {
	Connection conn = null;
	
	@Before
	public void getConn() throws Exception{
		// ����һ�����Ӷ���
		Configuration conf = HBaseConfiguration.create(); // ���Զ�����hbase-site.xml
		conf.set("hbase.zookeeper.quorum", "Master,Slave01,Slave02:2181");
		
		conn = ConnectionFactory.createConnection(conf);
	}
	
	
	
	/**
	 * DDL
	 * @throws Exception 
	 */
	@Test
	public void testCreateTable() throws Exception{

		// �������й���һ��DDL������
		Admin admin = conn.getAdmin();
		
		// ����һ��������������
		HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf("user_info"));
		
		// �������嶨����������
		HColumnDescriptor hColumnDescriptor_1 = new HColumnDescriptor("base_info");
		hColumnDescriptor_1.setMaxVersions(3); // ���ø������д洢���ݵ����汾��,Ĭ����1
		
		HColumnDescriptor hColumnDescriptor_2 = new HColumnDescriptor("extra_info");
		
		// �����嶨����Ϣ���������������
		hTableDescriptor.addFamily(hColumnDescriptor_1);
		hTableDescriptor.addFamily(hColumnDescriptor_2);
		
		
		// ��ddl����������admin ������
		admin.createTable(hTableDescriptor);
		
		// �ر�����
		admin.close();
		conn.close();
		
	}
	
	
	/**
	 * ɾ����
	 * @throws Exception 
	 */
	@Test
	public void testDropTable() throws Exception{
		
		Admin admin = conn.getAdmin();
		
		// ͣ�ñ�
		admin.disableTable(TableName.valueOf("user_info"));
		// ɾ����
		admin.deleteTable(TableName.valueOf("user_info"));
		
		
		admin.close();
		conn.close();
	}
	
	// �޸ı���--���һ������
	@Test
	public void testAlterTable() throws Exception{
		
		Admin admin = conn.getAdmin();
		
		// ȡ���ɵı�����Ϣ
		HTableDescriptor tableDescriptor = admin.getTableDescriptor(TableName.valueOf("user_info"));
		
		
		// �¹���һ�����嶨��
		HColumnDescriptor hColumnDescriptor = new HColumnDescriptor("other_info");
		hColumnDescriptor.setBloomFilterType(BloomType.ROWCOL); // ���ø�����Ĳ�¡����������
		
		// �����嶨����ӵ����������
		tableDescriptor.addFamily(hColumnDescriptor);
		
		
		// ���޸Ĺ��ı��彻��adminȥ�ύ
		admin.modifyTable(TableName.valueOf("user_info"), tableDescriptor);
		
		
		admin.close();
		conn.close();
		
	}
	
	

}
