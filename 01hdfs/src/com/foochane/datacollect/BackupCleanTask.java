package com.foochane.datacollect;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;

public class BackupCleanTask extends TimerTask {

	@Override
	public void run() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
		long now = new Date().getTime();
		try {
			// ̽�Ȿ�ر���Ŀ¼
			File backupBaseDir = new File("d:/logs/backup/");
			File[] dayBackDir = backupBaseDir.listFiles();

			// �жϱ���������Ŀ¼�Ƿ��ѳ�24Сʱ
			for (File dir : dayBackDir) {
				long time = sdf.parse(dir.getName()).getTime();
				if(now-time>24*60*60*1000L){
					FileUtils.deleteDirectory(dir);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
