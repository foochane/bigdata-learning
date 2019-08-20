echo "启动hdfs"
/usr/local/bigdata/hadoop-2.7.1/sbin/start-dfs.sh

echo "启动yarn"
 /usr/local/bigdata/hadoop-2.7.1/sbin/start-yarn.sh
 
echo "启动hadoop历史任务"
/usr/local/bigdata/hadoop-2.7.1/sbin/mr-jobhistory-daemon.sh start historyserver

echo "启动hive服务"
nohup hiveserver2 1>/dev/null 2>&1 &

echo "启动hive metastore服务端"
nohup hive --service metastore 1>/dev/null 2>&1 &

