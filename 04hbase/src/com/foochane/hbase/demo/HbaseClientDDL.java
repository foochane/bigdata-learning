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
 *  1、构建连接
 *  2、从连接中取到一个表DDL操作工具admin
 *  3、admin.createTable(表描述对象);
 *  4、admin.disableTable(表名);
	5、admin.deleteTable(表名);
	6、admin.modifyTable(表名，表描述对象);	
 *  
 * @author hunter.d
 *
 */
public class HbaseClientDDL {
	Connection conn = null;
	
	@Before
	public void getConn() throws Exception{
		// 构建一个连接对象
		Configuration conf = HBaseConfiguration.create(); // 会自动加载hbase-site.xml
		conf.set("hbase.zookeeper.quorum", "Master,Slave01,Slave02:2181");
		
		conn = ConnectionFactory.createConnection(conf);
	}
	
	
	
	/**
	 * DDL
	 * @throws Exception 
	 */
	@Test
	public void testCreateTable() throws Exception{

		// 从连接中构造一个DDL操作器
		Admin admin = conn.getAdmin();
		
		// 创建一个表定义描述对象
		HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf("user_info"));
		
		// 创建列族定义描述对象
		HColumnDescriptor hColumnDescriptor_1 = new HColumnDescriptor("base_info");
		hColumnDescriptor_1.setMaxVersions(3); // 设置该列族中存储数据的最大版本数,默认是1
		
		HColumnDescriptor hColumnDescriptor_2 = new HColumnDescriptor("extra_info");
		
		// 将列族定义信息对象放入表定义对象中
		hTableDescriptor.addFamily(hColumnDescriptor_1);
		hTableDescriptor.addFamily(hColumnDescriptor_2);
		
		
		// 用ddl操作器对象：admin 来建表
		admin.createTable(hTableDescriptor);
		
		// 关闭连接
		admin.close();
		conn.close();
		
	}
	
	
	/**
	 * 删除表
	 * @throws Exception 
	 */
	@Test
	public void testDropTable() throws Exception{
		
		Admin admin = conn.getAdmin();
		
		// 停用表
		admin.disableTable(TableName.valueOf("user_info"));
		// 删除表
		admin.deleteTable(TableName.valueOf("user_info"));
		
		
		admin.close();
		conn.close();
	}
	
	// 修改表定义--添加一个列族
	@Test
	public void testAlterTable() throws Exception{
		
		Admin admin = conn.getAdmin();
		
		// 取出旧的表定义信息
		HTableDescriptor tableDescriptor = admin.getTableDescriptor(TableName.valueOf("user_info"));
		
		
		// 新构造一个列族定义
		HColumnDescriptor hColumnDescriptor = new HColumnDescriptor("other_info");
		hColumnDescriptor.setBloomFilterType(BloomType.ROWCOL); // 设置该列族的布隆过滤器类型
		
		// 将列族定义添加到表定义对象中
		tableDescriptor.addFamily(hColumnDescriptor);
		
		
		// 将修改过的表定义交给admin去提交
		admin.modifyTable(TableName.valueOf("user_info"), tableDescriptor);
		
		
		admin.close();
		conn.close();
		
	}
	
	

}
