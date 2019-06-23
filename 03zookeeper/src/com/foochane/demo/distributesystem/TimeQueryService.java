package com.foochane.demo.distributesystem;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TimeQueryService extends Thread{
	
	int port = 0;
	public TimeQueryService(int port){
		
		this.port = port;
	}
	
	
	@Override
	public void run() {
		
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println("业务线程已绑定端口"+port+"准备接受消费端请求了.....");
			while(true){
				Socket sc = ss.accept();
				InputStream inputStream = sc.getInputStream();
				OutputStream outputStream = sc.getOutputStream();
				outputStream.write(new Date().toString().getBytes());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	

}
