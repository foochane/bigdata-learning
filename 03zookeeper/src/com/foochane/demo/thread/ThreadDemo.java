package com.foochane.demo.thread;


public class ThreadDemo {
	
	public static void main(String[] args) throws InterruptedException {
		
		
		System.out.println("���߳̿�ʼִ��.........");
		
		
		System.out.println("���߳�׼������һ�����߳�........");
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("���߳̿�ʼִ��.......");
				while(true){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("���̴߳�ӡ.........");
				}
				
			}
		});
		
		// setDaemon(true) ��������߳̾ͱ�����ػ��߳�
		thread.setDaemon(true);
		thread.start();
		
		
		System.out.println("���߳��������̺߳�����.......");
		
		Thread.sleep(10000);
		
	}

}
