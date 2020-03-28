## Mysql tar包 安装

>本文转载自：https://blog.csdn.net/qq_21137441/article/details/89925584。作者：林中静月下仙

### 1. 下载tar包放在Linux系统中，笔者存放的位置为`/opt`

```shell
tar -zxvf mysql……
mv mysql…… mysql
```

### 2. 创建`mysql`组和用户

```shell
groupadd mysql #创建用户组
useradd -g mysql mysql # 创建用户
```

### 3. 更改所属的组和用户

```shell
chown -R mysql mysql/
chgrp -R mysql mysql/
cd mysql 
mkdir data # 创建数据文件夹
chown -R mysql:mysql data //为数据文件夹赋予权限
```

### 4. 在/etc下创建my.cnf，并添加以下内容

```shell
vim /etc/my.cnf

[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8
 
[mysqld]
# 设置3306端口
port = 3306
 
# 设置mysql的安装目录
basedir=/opt/mysql
 
# 设置mysql数据库的数据的存放目录
datadir=/opt/mysql/data
 
# 允许最大连接数
max_connections=1000
 
# 服务端使用的字符集默认为8比特编码的latin1字符集
character-set-server=utf8
 
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
lower_case_table_names=1
max_allowed_packet=16M
# 表不区分大小写
lower_case_table_names=1
user = mysql
tmpdir = /tmp
 
[mysqld_safe]
log-error = /usr/local/mysql/data/error.log
pid-file = /usr/local/mysql/data/mysql.pid
```

### 5. 初始化mysql

```shell
/opt/mysql/bin/mysql_install_db --user=mysql --basedir=/opt/mysql/ --datadir=/opt/mysql/data/ --pid-file=/opt/mysql/data/mysql.pid --tmpdir=/tmp
```

### 6. 启动mysql

```shell
/etc/init.d/mysqld start
```

### 7. 设置开机启动

```shell
chkconfig --level 35 mysqld on
chkconfig --list mysqld
 
chmod +x /etc/rc.d/init.d/mysqld
chkconfig --add mysqld
chkconfig --list mysqld
service mysqld status
```

### 8. 配置环境变量

```shell
vim /etc/profile
export PATH=$PATH:/opt/mysql/bin

#重启文件配置
source /etc/profile
```

### 9. 获取mysql初始密码

```shell
cat /root/.mysql_srcret

#登录mysql,输入显示的密码，直接CV
mysql -uroot -p

#下面为mysql代码
set PASSWORD=PASSWORD('你的密码');
flush privileges;
```

### 10. 添加远程访问权限

```mysql
# 添加远程访问权限
mysql> use mysql
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A
Database changed
 
mysql> update user set host='%' where user='root';
Query OK, 0 rows affected (0.00 sec)
Rows matched: 1 Changed: 0 Warnings: 0
 
mysql> select host,user from user;
+-----------+---------------+
| host | user |
+-----------+---------------+
| % | root |
| localhost | mysql.session |
| localhost | mysql.sys |
+-----------+---------------+
```

### 11. 重启mysql

```shell
/etc/init.d/mysqld restart
```