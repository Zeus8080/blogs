# 一、主从复制原理

1. 主数据库进行增删改操作后，相应操作记录的语句（比如 create database test）会记录到binlog日志文件中（binlog日志文件一般和数据库data文件夹在一起）
2. 从数据库会请求主数据库的binlog日志文件，获取到新的操作语句，然后在自己的从数据库上自动执行相同的操作语句，进而实现主从的同步.





# 二、具体过程

- 拉取mysql镜像，命令：

  ```dockerfile
  #这里用的是5.6版本的mysql
  docker pull mysql:5.6
  ```

  

- 创建两个容器，一个主数据库，一个辅数据库

  ```dockerfile
  #主数据库
  docker run -p 33306:3306 --name mysql_master -e MYSQL_ROOT_PASSWORD=root -d mysql:5.6
  #从数据库
  docker run -p 33307:3306 --name mysql_slave -e MYSQL_ROOT_PASSWORD=root -d mysql:5.6
  #创建完成后可以通过docker ps命令查看启动的容器
  ```

- 进入主数据库，修改mysql配置文件

  ```dockerfile
  #windows 环境下用git客户端进入数据的命令
  winpty docker exec -it 容器id bash
  
  #linux环境下命令
  docker exec -it 容器id /bin/bash
  
  #注意由于容器是一个精简的linux虚拟机，我们的mysql实际是部署到这个虚拟机上的，我们首先需要下vi，然后通过vi来改配置文档
  apt-get update；
  apt-get install vim；
  
  
  #下载完vim后进入/etc/mysql/my.cnf文件，添加如下内容
  [mysqld]
  server-id = 100  # 实际上值可以随便添加,确保唯一就行
  log-bin= mysql-bin   # 开启bin-log
  
  #然后退出容器，重启容器
  docker restart  容器id
  ```

- 进入从数据库修改配置文件

  ```sql
  #其他与上一步一致，只有配置文件内容不一样，直接重复上面的操作就行
  [mysqld]
  server-id = 101  # 实际上值可以随便添加,确保唯一就行查看3个ip地址
  ```

- 查看3个ip地址

  ```dockerfile
  #宿主机的ip，直接cmd工具执行ipconfig
  宿主机 ----127.0.0.1
  #主数据库容器的ip，通过下面命令查询
  docker inspect --format='{{.NetworkSettings.IPAddress}}' 容器id|容器名称
  
  #主数据库容器ip-----172.17.0.3（这个后面配置需要用到）
  #从数据库容器ip-----172.17.0.4
  ```

- 连接两个数据库

  通过连接工具连接两个容器的数据库（本人使用的是navicat），连接地址：

  主数据库-----localhost:33306 root/root

  从数据库-----localhost:33307 root/root

- 配置主数据库

  新建一个用专门用来同步master

  ```sql
  CREATE USER 'backup'@'%' IDENTIFIED BY '123456';
  ```

  给这个用户分配权限

  ```sql
  GRANT REPLICATION SLAVE ON *.* to 'backup'@'%' identified by '123456';
  
  flush privileges；
  ```

  主库配置完成，查看状态

  ```sql
  show master status;
  ```

  记住查询结果，后面配置从库的时候需要用到

  File: mysql-bin.000003

  Position: 688

- 配置从库

  ```sql
  stop slave；
  
  change master to master_host='172.17.0.2', #刚才记录的从库容器的id
      master_port=3306, #数据库监听端口
      master_user='backup', #主库分配的用户
      master_password='123456', 
      master_log_file='mysql-bin.000003', #主数据 show master status;查到的结果
      master_log_pos=688;
      
  start slave; #启动
  #查看，如果Slave_IO_Running， Slave_SQL_Running两个字段都是yes，说明成功
  show slave status；
  ```

- 测试

  在主库创建一个数据库，然后看从库，应该也会有一个数据库，至此完成配置