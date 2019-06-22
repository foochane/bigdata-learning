package com.foochane.hdfsclient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class HdfsClient {
	public static void main(String[] args) throws Exception {
		/**
		 * Configuration��������Ļ��ƣ�
		 *    ����ʱ�������jar���е�Ĭ������ xx-default.xml
		 *    �ټ��� �û�����xx-site.xml  �����ǵ�Ĭ�ϲ���
		 *    �������֮�󣬻�����conf.set("p","v")�����ٴθ����û������ļ��еĲ���ֵ
		 */
		// new Configuration()�����Ŀ��classpath�м���core-default.xml hdfs-default.xml core-site.xml hdfs-site.xml���ļ�
		Configuration conf = new Configuration();
		
		// ָ�����ͻ����ϴ��ļ���hdfsʱ��Ҫ����ĸ�����Ϊ��2
		conf.set("dfs.replication", "2");
		// ָ�����ͻ����ϴ��ļ���hdfsʱ�п�Ĺ���С��64M
		conf.set("dfs.blocksize", "64m");
		
		// ����һ������ָ��HDFSϵͳ�Ŀͻ��˶���: ����1:����HDFSϵͳ��URI������2�������ͻ���Ҫ�ر�ָ���Ĳ���������3���ͻ��˵����ݣ��û�����
		FileSystem fs = FileSystem.get(new URI("hdfs://192.168.233.200:9000/"),conf,"hadoop");
		
		// �ϴ�һ���ļ���HDFS��  D:/home/big-data/software/dev/bashrc.txt
		fs.copyFromLocalFile(new Path("D:/home/big-data/software/dev/bashrc.txt"), new Path("/test"));
		
		fs.close();
	}
	
	FileSystem fs = null;
	
	@Before
	public void init() throws Exception{
		Configuration conf = new Configuration();
		conf.set("dfs.replication", "2");
		conf.set("dfs.blocksize", "64m");
		
		fs = FileSystem.get(new URI("hdfs://192.168.233.200:9000/"), conf, "hadoop");
		
	}
	
	
	/**
	 * ��HDFS�������ļ����ͻ��˱��ش���
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */

	@Test
	@Ignore
	public void testGet() throws IllegalArgumentException, IOException{
		
		fs.copyToLocalFile(new Path("/bashrc.txt_bak"), new Path("d:/"));
		fs.close();
		
	}
	
	/**
	 * ��hdfs�ڲ��ƶ��ļ�\�޸�����
	 */
	@Ignore
	@Test
	public void testRename() throws Exception{
		
		fs.rename(new Path("/bashrc.txt"), new Path("/bashrc.txt_bak"));
		
		fs.close();
		
	}
	
	
	/**
	 * ��hdfs�д����ļ���
	 */
	@Test
	@Ignore
	public void testMkdir() throws Exception{
		
		fs.mkdirs(new Path("/xx/yy/zz"));
		
		fs.close();
	}
	
	
	/**
	 * ��hdfs��ɾ���ļ����ļ���
	 */
	@Test
	@Ignore
	public void testRm() throws Exception{
		
		fs.delete(new Path("/xx/yy/zz"), true);
		
		fs.close();
	}
	
	
	
	/**
	 * ��ѯhdfsָ��Ŀ¼�µ��ļ���Ϣ
	 */
	@Test
	@Ignore
	public void testLs() throws Exception{
		// ֻ��ѯ�ļ�����Ϣ,�������ļ��е���Ϣ
		RemoteIterator<LocatedFileStatus> iter = fs.listFiles(new Path("/"), true);
		
		while(iter.hasNext()){
			LocatedFileStatus status = iter.next();
			System.out.println("�ļ�ȫ·����"+status.getPath());
			System.out.println("���С��"+status.getBlockSize());
			System.out.println("�ļ����ȣ�"+status.getLen());
			System.out.println("����������"+status.getReplication());
			System.out.println("����Ϣ��"+Arrays.toString(status.getBlockLocations()));
			
			System.out.println("--------------------------------");
		}
		fs.close();
	}
	
	/**
	 * ��ѯhdfsָ��Ŀ¼�µ��ļ����ļ�����Ϣ
	 */
	@Test
	@Ignore
	public void testLs2() throws Exception{
		FileStatus[] listStatus = fs.listStatus(new Path("/"));
		
		for(FileStatus status:listStatus){
			System.out.println("�ļ�ȫ·����"+status.getPath());
			System.out.println(status.isDirectory()?"�����ļ���":"�����ļ�");
			System.out.println("���С��"+status.getBlockSize());
			System.out.println("�ļ����ȣ�"+status.getLen());
			System.out.println("����������"+status.getReplication());
			
			System.out.println("--------------------------------");
		}
		fs.close();
	}
	
	
	/**
	 * ��ȡhdfs�е��ļ�������
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testReadData() throws IllegalArgumentException, IOException {

		FSDataInputStream in = fs.open(new Path("/test.txt"));

		BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));

		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}

		br.close();
		in.close();
		fs.close();

	}

	/**
	 * ��ȡhdfs���ļ���ָ��ƫ������Χ������

	 * ��ҵ�⣺�ñ����е�֪ʶ��ʵ�ֶ�ȡһ���ı��ļ��е�ָ��BLOCK���е���������
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	@Test
	@Ignore
	public void testRandomReadData() throws IllegalArgumentException, IOException {

		FSDataInputStream in = fs.open(new Path("/xx.dat"));

		// ����ȡ����ʼλ�ý���ָ��
		in.seek(12);

		// ��16���ֽ�
		byte[] buf = new byte[16];
		in.read(buf);

		System.out.println(new String(buf));

		in.close();
		fs.close();

	}

	/**
	 * ��hdfs�е��ļ�д����
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */

	@Test
	@Ignore
	public void testWriteData() throws IllegalArgumentException, IOException {

		FSDataOutputStream out = fs.create(new Path("/zz.jpg"), false);

		// D:\images\006l0mbogy1fhehjb6ikoj30ku0ku76b.jpg

		FileInputStream in = new FileInputStream("D:/images/006l0mbogy1fhehjb6ikoj30ku0ku76b.jpg");

		byte[] buf = new byte[1024];
		int read = 0;
		while ((read = in.read(buf)) != -1) {
			out.write(buf,0,read);
		}
		
		in.close();
		out.close();
		fs.close();

	}

}