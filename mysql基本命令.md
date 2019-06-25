
## 查看数据库当前用户及权限

```
use mysql; #信息放在mysql.user表下
desc users;
select host,user from mysql.user;
```

## 创建用户
命令:
```
CREATE USER 'username'@'host' IDENTIFIED BY 'password';
```

说明：
- `username`：你将创建的用户名
- host：指定该用户在哪个主机上可以登陆，如果是本地用户可用localhost，如果想让该用户可以从任意远程主机登陆，可以使用通配符%
- `password`：该用户的登陆密码，密码可以为空，如果为空则该用户可以不需要密码登陆服务器

如：
```
CREATE USER 'test'@'%' IDENTIFIED BY '123456'
```

## 用户授权
命令:
```
GRANT privileges ON databasename.tablename TO 'username'@'host'
```

说明:
- `privileges`：用户的操作权限，如`SELECT`，`INSERT`，`UPDATE`等，如果要授予所的权限则使用`ALL`
- `databasename`：数据库名
- `tablename`：表名，如果要授予该用户对所有数据库和表的相应操作权限则可用*表示，如*.*

例子:
```
GRANT SELECT, INSERT ON test_database.test_table TO 'testuser'@'%';
GRANT ALL ON test_database.* TO 'testuser'@'%';
GRANT ALL ON *.* TO 'testuser'@'%';
```

注意:

用以上命令授权的用户不能给其它用户授权，如果想让该用户可以授权，用以下命令:
```
GRANT privileges ON databasename.tablename TO 'username'@'host' WITH GRANT OPTION;
```

## 撤销用户权限
命令:
```
REVOKE privilege ON databasename.tablename FROM 'username'@'host';
```

说明:
说明:
- `privileges`：用户的操作权限，如`SELECT`，`INSERT`，`UPDATE`等，如果要授予所的权限则使用ALL
- `databasename`：数据库名
- `tablename`：表名，如果要授予该用户对所有数据库和表的相应操作权限则可用`*`表示，如`*.*`

例子:
```
REVOKE ALL ON *.* FROM 'testuser'@'%';
```

## 删除用户
命令:
```
DROP USER 'username'@'host';
```

## 设置与更改用户密码
命令:
```
SET PASSWORD FOR 'username'@'host' = PASSWORD('newpassword');
```

如果是当前登陆用户用:
```
SET PASSWORD = PASSWORD("newpassword");
```

## 查看当前登录用户,当前数据库

```
select user();

select database();
```
注意这里 `uer()`,`database()`不是sql语句，是函数。


## 创建表删除表
创建：
```
create database databasename;
create database if not exists databasename;
```

删除：
```
drop database databasename;
```


## mysql启动停止查看状态
```
 service mysql status
 service mysql start
 service mysql stop
 service mysql restart
 ```
 
 
## mysql无法远程访问问题解决

1 查看配置文件，看是否只允许本地访问

配置文件具体位置 `/etc/mysql/mysql.conf.d/mysqld.cnf`

不同版本可能不一样。

如有如下内容，把它注释掉：
```
#bind-address           = 127.0.0.1
```

2 防火墙问题

查看端口占用情况：
```
netstat -nalp | grep "3306"

关闭防火墙
```
sudo ufw status
sudo ufw disable
```

3 mysql用户没有授权
```
CREATE USER 'hiveuser'@'%' IDENTIFIED BY '123456';
grant all privileges on *.* to 'hiveuser'@'%' identified by '123456' with grant option; 
flush privileges;
create database metastore;
```



