## MySQL锁概述

![](https://gitee.com/onlyzl/image/raw/master/img/wxhead.png)

### 1. 锁

锁是计算机协调多个进程或线程并发访问某一资源的机制。

在数据库中，除传统的计算资源的争用以外，数据也是一种供许多用户共享的资源。如何保证数据并发访问的一致性、有效性是所有数据库必须解决的问题，锁冲突也是影响数据库并发访问性能的一个重要的因素。

### 2. 分类

**从数据操作类型区分：**

- 读锁。又称共享锁：针对同一份数据，多个读操作可以同时进行而不会互相影响
- 写锁。又称排它锁：当前写操作没有完成前，他会阻断其他写锁和读锁

**从数据操作颗粒度：**

- 表锁。在进行数据操作的时候，将一张表全部加锁。即悲观锁
- 行锁。仅仅对操作的行进行加锁，不同行之间没有影响

#### 2.1 表锁（偏读）

`MyISAM`的锁就是表锁。

`MyISAM`在执行查询查询语句之前默认给所有的表加读锁，在执行增删改操作之前，会自动给涉及到的表加写锁

- 加读锁。

```mysql
lock table 表名 read ;
```

- 加写锁。

```mysql
lock table 表名 write ;
```

- 查看表中的所有锁

```mysql
show open tables ;
```

- 释放表锁

```mysql
unlock tables ;
```

##### 2.1.1 加读锁

- 给表`mylock`加读锁

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317105337.png)

- 当前MySQL会话中执行查询当前加锁表。**可以查询**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317105452.png)

- 当前MySQL会话中执行修改当前加锁表。**不能修改**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317105556.png)

- 当前MySQL会话中执行查询其他未加锁表。**不能查询**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317105911.png)

- 其他会话中执行查询加锁表。**可以查询**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317105715.png)

- 其他会话中执行修改加锁表。**进入阻塞状态**。当释放锁以后就会立即查询出数据

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317105830.png)

- 其他会话中执行查询其他未加锁表。**可以查询**

##### 2.1.2 加写锁

- 添加写锁

  ![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317110244.png)

- 当前会话中查询加锁表。**可以查询**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317110341.png)

- 当前会话中查询其他未加锁表。**不能查询**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317110427.png)

- 当前会话中修改加锁表。**可以更改**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317110529.png)

- 其他会话中读取加锁表。**进入阻塞**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317110714.png)

##### 2.1.3 表锁加读写锁的总结

- 对`MyISAM`表的读操作，不会阻塞其他进程对同一表的读操作，但会阻塞对同一个表的写操作。只有当读锁被释放以后，才可以进行写操作。
- 对`MyISAM`表的写操作，会阻塞其他进程对同一表的读操作，只有当写锁释放以后，才可以进行读操作
- 对于其他进程：读锁会阻塞写不会阻塞读，写锁将读和写都阻塞
- 对于当前进程：读锁会禁止其他表读取，当前表写操作

##### 2.1.4 分析表锁定

```mysql
show status like 'table%';
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317112944.png)

- `table_locks_immediate` :产生表级锁定的次数。标识可以立即获取锁的查询次数，没立即获取锁值加一
- `table_locks_waited`:出现标记锁定争用而发生的等待次数（不能获取立即获取锁的次数，没等待一次就加一）
- `table-open_cache_hits`:从`table share`的`free list`中找到一个表的缓存，如果找到则加一
- `table_open_cache_misses`:和上面的`hits`相反，如果在缓存中找不到实例则需要重新实例化，每次加一
- `table_open_cache_overflows`:超过缓存区大小的实例个数
- `MyISAM`的读写锁调度是以写优先，这也是`MyISAM`不适合做主表的引擎。因为写锁后，其他线程不能做任何的操作，大量的更新会使查询很难得到锁，从而造成永远阻塞。

#### 2.2 行锁（偏写）

`InnoDB`使用行锁，锁定粒度低，发生锁冲突的概率最低，并发度也最高。

**行锁演示：**

- `MySQL`在`InnoDB`引擎下的自动隔离级别为**事务**，每一次请求都相当于提交一次事务

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317115305.png)

- 当前会话，取消自动提交，修改一行的值。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317115450.png)

- 另一个会话，读取这一行的值。**读取到的是原数据**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317115751.png)

- 另一个会话，更新这一行。**进程阻塞**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317120037.png)

- 当前进程提交事务。**另一个会话，恢复**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317120117.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317120136.png)

##### 2.2.1 由于出现索引失效导致行锁变表锁

- 模拟索引字段类型转换导致索引失效。
- 插入一个整形的name，底层出现索引失效。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317152007.png)

- 另一个会话，出现进程阻塞

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317152129.png)

这个的主要原因是：**由于出现索引失效导致只能从全表读取，导致行锁变表锁**

##### 2.2.2 由于出现范围写操作导致出现间隙锁

- 更新一个范围内的字段

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317153120.png)

- 其他会话对这个范围内的数据进行写操作

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317153405.png)

间隙锁：**MySQL在执行范围写操作的时候，会默认将这个范围内的数据全部加锁，如果当前进行没有进行提交，那么其他进程访问这个范围内的数据将会被阻塞。**

#####  2.2.3 手动开启行锁

```mysql
mysql> begin ;
Query OK, 0 rows affected (0.00 sec)

mysql>select * from 表名 where id = 3 for update #锁定id=3的行

mysql> commit ; # 如果不进行提交，那么其他进程将不能对该行进行写操作
```

##### 2.2.4 分析行锁定

```mysql
mysql> show status like 'innodb_row_lock%';
+-------------------------------+--------+
| Variable_name                 | Value  |
+-------------------------------+--------+
| Innodb_row_lock_current_waits | 0      |
| Innodb_row_lock_time          | 397427 |
| Innodb_row_lock_time_avg      | 39742  |
| Innodb_row_lock_time_max      | 51038  |
| Innodb_row_lock_waits         | 10     |
+-------------------------------+--------+
5 rows in set (0.00 sec)

```

- `Innodb_row_lock_current_waits`。当前正在等待锁定的数量
- `Innodb_row_lock_time`。锁定以后进程等待时间总和
- `Innodb_row_lock_time_avg`。每次等待的平均时间
- `Innodb_row_lock_time_max`。等待的最大时间
- `Innodb_row_lock_waits`。等待次数

##### 2.2.5 行锁优化

- 尽可能让所有的数据检索都通过索引来完成，避免无索引导致行锁升级为表锁
- 合理设计索引，缩小锁的范围
- 尽可能使用准确的数值检索或者范围较小，避免间隙锁
- 尽量控制事务大小，减少锁定资源量和时间长度
- 尽可能低级别事务隔离

#### 3. 页锁

开销和加锁时间界于表锁和行锁之间：会出现死锁；锁定粒度界于表锁和行锁之间，并发度一般。

## MySQL主从复制

### 1. MySQL主从复制过程

- `master`将改变记录记录到二进制文件中。
- `slave`将`master`的二进制日志文件中的记录拷贝到它的中继日志文件`relay log`中
- `slave`读取这个中继文件，将改变应用到自己的数据库中。MySQL的复制是异步且串行化的

### 2. 主从复制的基本原则

- 每一个`slave`只有一个`master`
- 每个`slave`只能有一个唯一的服务器ID
- 每个`master`可以有多个`slave`

### 3. 配置主从复制

#### 3.1 主master

`window`和`linux`的配置相同，只不过修改的配置文件不一样而已。在`window`中修改`my.ini`，在`linux`中修改`my.cnf`文件。

- 设置服务器唯一ID。`server-id=1`
- 启动二进制日志文件。`log-bin=一个路径/mysqlbin`
- 启动错误日志文件。`log-err=一个路径/mysqlerr`**可选参数**
- 设置MySQL根目录。`basedir='安装目录'`**可选参数**
- 设置临时目录。`tmpdir=`**可选参数**
- 设置主机的读写情况。`read-only=0`读写均可
- 设置不需要复制的数据库。`binlog-lgnore-db=数据库名`**可选参数**
- 设置需要复制的数据库名字。`binlog-do-db`**可选参数**

#### 3.2 从slave

- 设置服务器唯一ID。`server-id=2`
- 启用二进制文件。

#### 3.3 其他配置

- 重启`mysql`服务
- 关闭防火墙。Linux关闭命令`service iptable stop`
- 主机添加授权账户，并刷新；

```mysql
GRANT REPLICATION SLAVE  ON*.* TO 'zhangsan'@'从机器数据库IP‘ IDENTIFIED BY '123456';
flush privileges;
```

- 查询`master`状态。记录`File`和`position`的值

```mysql
show master status ;
```

- 从机配置需要启动服务的主机

```mysql
CHANGE MASTER TO MASTER_HOST='主机IP',
        MASTER_USER='zhangsan'，
        MASTER_PASSWORD='123456',
        MASTER_LOG_FILE='File名字'，
        MASTER_LOG_POS=Position数字；
```

- 从机启动`slave`

```mysql
start slave;
# 当Slave_IO_Running:YES
# Slave_SQL_Running:YES
# 这两个字段均为YES的时候才说明主从复制配置成功

stop slave;
#停止主从复制
```

