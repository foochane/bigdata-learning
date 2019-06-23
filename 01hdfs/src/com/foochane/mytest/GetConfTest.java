package com.foochane.mytest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ���Զ�ȡproperties�����ļ�
 *
 */

public class GetConfTest {

	public static void main(String[] args) throws IOException {
		Properties prop = new Properties();
		
		// ʹ��ClassLoader����properties�����ļ����ɶ�Ӧ��������		
		InputStream in = GetConfTest.class.getClassLoader().getResourceAsStream("collect.properties");
		
		 // ʹ��properties�������������
		prop.load(in);
		
		 //��ȡkey��Ӧ��valueֵ
		String dir = prop.getProperty("LOG_SOURCE_DIR");
		
		System.out.println(dir);
	}

}
