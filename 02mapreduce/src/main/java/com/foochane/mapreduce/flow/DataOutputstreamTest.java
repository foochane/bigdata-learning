package com.foochane.mapreduce.flow;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class DataOutputstreamTest {
	
	public static void main(String[] args) throws Exception {
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream("d:/a.dat"));
		
		dos.write("我爱你".getBytes("utf-8"));
		
		dos.close();
		
		
		DataOutputStream dos2 = new DataOutputStream(new FileOutputStream("d:/b.dat"));
		
		dos2.writeUTF("我爱你");
		
		dos2.close();
	}

}
