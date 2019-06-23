package com.foochane.mytest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 测试读取properties配置文件
 *
 */

public class GetConfTest {

	public static void main(String[] args) throws IOException {
		Properties prop = new Properties();
		
		// 使用ClassLoader加载properties配置文件生成对应的输入流		
		InputStream in = GetConfTest.class.getClassLoader().getResourceAsStream("collect.properties");
		
		 // 使用properties对象加载输入流
		prop.load(in);
		
		 //获取key对应的value值
		String dir = prop.getProperty("LOG_SOURCE_DIR");
		
		System.out.println(dir);
	}

}
