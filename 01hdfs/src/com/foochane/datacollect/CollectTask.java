package com.foochane.datacollect;


import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class CollectTask extends TimerTask {

	@Override
	public void run() {

		/**
		 * ������ʱ̽����־ԴĿ¼ ������ȡ��Ҫ�ɼ����ļ� �����ƶ���Щ�ļ���һ�����ϴ���ʱĿ¼
		 * �����������ϴ�Ŀ¼�и��ļ�����һ���䵽HDFS��Ŀ��·����ͬʱ��������ɵ��ļ��ƶ�������Ŀ¼
		 * 
		 */
		try {
			// ��ȡ���ò���
			Properties props = PropertyHolderLazy.getProps();

			// ����һ��log4j��־����
			Logger logger = Logger.getLogger("logRollingFile");

			// ��ȡ���βɼ�ʱ������
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
			String day = sdf.format(new Date());

			File srcDir = new File(props.getProperty(Constants.LOG_SOURCE_DIR));

			// �г���־ԴĿ¼����Ҫ�ɼ����ļ�
			File[] listFiles = srcDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.startsWith(props.getProperty(Constants.LOG_LEGAL_PREFIX))) {
						return true;
					}
					return false;
				}
			});

			// ��¼��־
			logger.info("̽�⵽�����ļ���Ҫ�ɼ���" + Arrays.toString(listFiles));

			// ��Ҫ�ɼ����ļ��ƶ������ϴ���ʱĿ¼
			File toUploadDir = new File(props.getProperty(Constants.LOG_TOUPLOAD_DIR));
			for (File file : listFiles) {
				FileUtils.moveFileToDirectory(file, toUploadDir, true);
			}

			// ��¼��־
			logger.info("�����ļ��ƶ����˴��ϴ�Ŀ¼" + toUploadDir.getAbsolutePath());

			// ����һ��HDFS�Ŀͻ��˶���

			FileSystem fs = FileSystem.get(new URI(props.getProperty(Constants.HDFS_URI)), new Configuration(), "root");
			File[] toUploadFiles = toUploadDir.listFiles();

			// ���HDFS�е�����Ŀ¼�Ƿ���ڣ���������ڣ��򴴽�
			Path hdfsDestPath = new Path(props.getProperty(Constants.HDFS_DEST_BASE_DIR) + day);
			if (!fs.exists(hdfsDestPath)) {
				fs.mkdirs(hdfsDestPath);
			}

			// ��鱾�صı���Ŀ¼�Ƿ���ڣ���������ڣ��򴴽�
			File backupDir = new File(props.getProperty(Constants.LOG_BACKUP_BASE_DIR) + day + "/");
			if (!backupDir.exists()) {
				backupDir.mkdirs();
			}

			for (File file : toUploadFiles) {
				// �����ļ���HDFS������access_log_
				Path destPath = new Path(hdfsDestPath + "/" + UUID.randomUUID() + props.getProperty(Constants.HDFS_FILE_SUFFIX));
				fs.copyFromLocalFile(new Path(file.getAbsolutePath()), destPath);

				// ��¼��־
				logger.info("�ļ����䵽HDFS��ɣ�" + file.getAbsolutePath() + "-->" + destPath);

				// ��������ɵ��ļ��ƶ�������Ŀ¼
				FileUtils.moveFileToDirectory(file, backupDir, true);

				// ��¼��־
				logger.info("�ļ�������ɣ�" + file.getAbsolutePath() + "-->" + backupDir);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
