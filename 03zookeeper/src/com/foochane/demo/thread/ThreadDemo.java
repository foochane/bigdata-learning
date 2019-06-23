package com.foochane.demo.thread;


public class ThreadDemo {
	
	public static void main(String[] args) throws InterruptedException {
		
		
		System.out.println("主线程开始执行.........");
		
		
		System.out.println("主线程准备启动一个子线程........");
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("子线程开始执行.......");
				while(true){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("子线程打印.........");
				}
				
			}
		});
		
		// setDaemon(true) ，这个子线程就变成了守护线程
		thread.setDaemon(true);
		thread.start();
		
		
		System.out.println("主线程启动子线程后的语句.......");
		
		Thread.sleep(10000);
		
	}

}
