## 手机APP分析系统


## 1 代码结构：
- tomcat_servelet:  servelet代码
- data_producer:    模拟日志数据生产
- taildirsourse：   Flume移植代码
- log-analysis：    日志处理代码


## 2 Flume代码移植

由于 Flume 1.6 版本没有 Taildir Source
组件，因此，需要将 Flume 1.7 中的 Taildir Source 组件源码编译打包后，放入 Flume1.6 安装
目录的 lib 文件目录下。

所谓移植，就是将 Flume1.7 版本中 Taildir Source 的相关文件全部提取出来，然后将这
些文件放入新建的项目中进行编译打包，将打包出的 jar 包放入 Flume1.6 安装目录的 lib 目
录下即可。

在本项目中，已经将 Taildir Source 的源码放入了 taildirsource 项目中，直接编译项目，
打包后放入 Flume1.6 安装目录的 lib 文件目录下即可。
在 Flume 配置文件中指定全类名即可使用 Taildir Source 组件。
```
a1.sources.r1.type = com.atguigu.flume.source.TaildirSource

```

打包：

在IDEA中，打开taildirsource项目，点击右侧的maven先双击`clean`然后双击`package`打包，之后变回在target目录下生成一个`flume-taildirsource.jar`文件



## 3 项目配置

在一台机器上安装一个nginx和两个tomcat

### 3.1 Nginx 安装

**注意使用root用户**

#### 3.1.1 安装PCRE
```
#下载 PCRE 安装包

wget http://downloads.sourceforge.net/project/pcre/pcre/8.35/pcre-8.35.tar.gz

#解压安装包
tar zxvf pcre-8.35.tar.gz

进入安装包目录
cd pcre-8.35

#编译安装
./configure

#安装
make && make install

# 查看 pcre 版本
pcregrep -V
```

**ubuntu 下直接apt-get 安装如下库即可,不需要进行上面的操作：**
 ```sh
#PCRE库
sudo apt-get install libpcre3 libpcre3-dev  

#zlib库
sudo apt-get install zlib1g-dev

#OpenSSL库
sudo apt-get install openssl libssl-dev 
```


### 3.1.2 安装nginx

安装
```
wget http://nginx.org/download/nginx-1.12.2.tar.gz

tar zxvf nginx-1.12.2.tar.gz

cd nginx-1.12.2

./configure --prefix=/usr/local/nginx 

make && make install
```

 查看 Nginx 版本
 ```
 /usr/local/nginx/sbin/nginx -v
 ```


 ### 3.1.2 nginx负载均衡配置


```conf
#user  nobody;
worker_processes  1;

#error_log logs/error.log;
#error_log logs/error.log notice;
#error_log logs/error.log info;

#pid logs/nginx.pid;

events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;


    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #服务器的集群 
    upstream netitcast.com { 
        #服务器集群名字 
        #服务器配置 weight 是权重的意思，权重越大，分配的概率越大。
        server 192.168.1.112:18080; 
        server 192.168.1.112:28080; 
    } 


    #gzip  on;

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            # root   html;
            # index  index.html index.htm;
            proxy_pass http://netitcast.com;
            proxy_redirect default;
        }

        #error_page  404              /404.html
        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```
