## Redis入门

### 1. Resid简介

>一款开源免费的，用C语言编写的，遵守BSD协议，高性能的分布式内存数据库并且支持持久化的NoSql

特点：

- 可以数据持久化
- 支持很多的数据类型
- 支持数据备份

作用：

- 异步将数据持久化，同时服务还在运行
- 可以直接从redis中取出需要的数据
- 可以模仿过期时间

### 2.Redis安装

- 解压`redis`文件
- 编译文件，使用`make`指令
  - 报错的话需要安装`gcc`编译器
  - `yum install gcc-c++`
- 执行安装`make install`
- 查看用户应用信息`usr/local/bin`目录下

```shell
-rw-r--r-- 1 root root     134 Mar  6 16:56 dump.rdb //数据库备份文件
-rwxr-xr-x 1 root root 4807856 Mar  6 13:30 redis-cli // 客户端
lrwxrwxrwx 1 root root      12 Mar  6 13:30 redis-sentinel -> redis-server //哨兵模式开启
-rwxr-xr-x 1 root root 8125184 Mar  6 13:30 redis-server //服务端
-rwxr-xr-x 1 root root 8125184 Mar  6 13:30 redis-check-aof //检测aof文件是否有错误
-rwxr-xr-x 1 root root 8125184 Mar  6 13:30 redis-check-rdb //检测rdb文件是否有错误
```

### 3.Redis数据类型

- Redis中对于数据库的操作

```shell
select index (从0开始)进入一个数据库，默认创建16个库
dbsize 返回当前数据库中的数据的个数
flushdb 清除当前库中的所有数据
flushALL 清除redis数据库中的所有的数据
```

- Redis对于key的操作

```shell
keys * 查看所有的键
expire key time 设置key的过期时间（单位秒）
ttl key 查看剩余过期时间
type key 查看当前key的类型
exists key 查看当前key是否存在
move db key 将这个键移动到db库中(db是一个索引)
```

- 当库中已经有了一个key，再次创建相同的key将会被创建

#### 3.1 字符串类型数据

- 单值单value

```shell
set k1 v1 增加一个键值对
get k1 获取这个字段
append k1 value 给k1追加一个value
strlen k1 查看当前k的值的长度
mset k1 v1 k2 v2 设置多个键值
mget k1 k2 获取多个键值
setex key time value 设置这个键的过期时间
setnx key value 如果这个键不存在则增加这个键
getrange k1 0 -1 获取一个键的所有值
setrange k1 start value 设置k1的值
incr k1 给k1递增一
decr k1 给k1递减一
incrby k1 value k1加value
decrby k1 value k1减value
```

- 无序

#### 3.2 List类型数据

- 单值多value

```shell
lpush key value1 value2 value3 …… # 增加一个键值链表。先进后出。栈的存储方式
rpush key value1 value2 value3 …… # 增加一个键值链表。先进先出。堆的存储方式
lrange key start end # 查看这个key从start开始到end的值
lpop key # 从队列中拿出第一个元素
rpop key # 从队列的尾部拿一个元素
lindex key index # 获取指定下标的值
llen key # 查看当前key的长度
lrem key count value # 删除key中count个value
ltrim key start end # 从key中截取start 到 end 赋值给当前key
rpoplpush k1 k2 #将k1的最后一个元素添加到k2的第一个元素
lset key index value #设置key中的index下标的值
linsert key before/after target value # 给key中的Target元素之前或者之后添加value
```

- 存入有序

#### 3.3 Set数据类型

- 单值多value

```shell
sadd key value01 value02 value03……#增加一个set
smembers key #查看当前key中指
sismembers key index #查看当前key中对应index的值
scard key #查看当前key中指的个数
srandmember key count #从key中随机出count个数
spop key count #随机出count个数
smove key1 key2 value # 将key1中的value赋值给key2
sdiff key1 key2 # 查看key1和key2的差集（以key1为标准）
sinter key1 key2 #查看key1和key2的交集
sunion key1 key2 #查看key1和key2的并集
```

- 无序

#### 3.4 Hash数据类型

- 单值，V是一个键值对。可以存放多个键值对

```shell
hset key filed value #在key中存储一个键值对
hget key filed # 获取这个key中指定字段的值
hmset key filed value filed value filed value #设置多个键值
hmget key filed1 filed2 #获取这个键中多个字段
hgetall key 获取这个键中所有的键值对
hdel key filed #删除这个key中的一个或者多个filed
```

- 有序

#### 3.5 Zset(sorted set)

- 在set的基础上加了一个score值

```shell
zadd key score member #增加一个值或者多个值，携带一个分数
zrange key start end [withscores]#查看指定key的值，withscores 添加以后打印对应的分数
zrem key member 删除某个member对应的值
zcard key #查看当前key中的元素个数
zcount key startScore endScore# 查看当前分数在start end之间的元素个数
zrank key member #获取member对应的下标
zscore key member #获取member对应的分数
zrevrank key member #逆序获取下标值
zrangebyscore key startScore endScore #获取指定分数之间的值和分数
zrevrangebyscore key startScore endScore #反序获取
) #表示不包含这个分数
zrangebyscore key startScore endScore withscores limit startIndex endIndex #在指定的下标区间内寻找分数在startScore到endScore之间
```

- 存入有序

### 4.Redis配置文件redis.conf详解

#### 4.1 unit

```properties
# 1k => 1000 bytes
# 1kb => 1024 bytes
# 1m => 1000000 bytes
# 1mb => 1024*1024 bytes
# 1g => 1000000000 bytes
# 1gb => 1024*1024*1024 bytes
# units are case insensitive so 1GB 1Gb 1gB are all the same.
```

- 单位的表示数量
- 单位不区分大小写

#### 4.2  Includes

```properties
# include /path/to/local.conf
# include /path/to/other.conf

```

- 导入了其他的配置文件

#### 4.3 General(通用配置)

```properties
# 配置redis的启动方式，yes为守护进程的方式，no不是守护线程。默认为no
daemonize no 
#标识该程序的ID，其他程序可以通过这个文件获取这个应用PID，执行一些其他的任务
pidfile /var/run/redis.pid
#端口
port 6379
#一个连接队列，
# backlog队列总和=未完成三次握手队列 + 已经完成三次握手队列
#/proc/sys/net/core/somaxconn的值，所以需要确认增大somaxconn和tcp_max_syn_backlog两个值
tcp-backlog 

# 设置当用户无操作多少时间内和客户端解除连接。。当为0时不设置超时时间。
timeout 0
#距离多长时间未接收到报文而开始重新检测，如果设置为0则说明不尽兴检测
tcp-keepalice 0
#设置日志级别。日志级别逐渐递增
# debug （开发用）
# verbose
#notice
#warning
loglevel notice
#设置日志文件的输出名字。会输出在当前目录。可以进行修改
logfile ""
#开启系统日志
syslog-enabled no
#设置日志标志
syslog-ident redis
#指定日志输出的设备给，可以是user或者local0-7
syslog-facility local0
#设置数据库的个数
databases 16

```

#### 4.3 snapshotting

> 快照。当redis服务停止的时候，将数据存储到磁盘中

```properties
# save seconds change
# 设置多少秒以后至少有多少次更改操作会进行数据存储，
# 当设置为 ""时停止了持久化
#默认为：
#	15分钟至少一次操作
#	5分钟至少十次操作
#	1分钟至少10000次操作
save 
# 当系统出现问题时候，是否停止写入到磁盘
stop-writes-on-bgsave-error yes
# 是否进行压缩存储
rdbcompression
#进行数据校验，大约影响10%的性能
rdbchecksum
# 产生的数据备份的名字
dbfilename dump.rdb
#产生数据文件的路径
dir ./
```



#### 4.4 replication

### 5.快照详解

- 在指定的时间间隔之内将内存中的数据集快照写入到磁盘中，也就是SanpShot快照。
- `Redis`会`fork`一个线程用来进行持久化，会先将数据写入到一个临时文件中，待持久化过程都结束了，再用这个临时文件替换上次持久化好的文件
- 由于持久化的工作全部由子进程执行，所以主进程不需要任何的IO操作
- 存在两种方式，`RDB`和`AOF`

>Fork：复制一个与当前进程一样的进程。新进程的所有数据数值均与原进程相同，但是它是一个全新的进程，并且作为了原进程的子进程

#### 5.1RDB快照

- `Redis`中默认的快照方式。
- 优点：
  - 适合大规模的数据恢复
  - 对数据的完整性和一致性要求不高
- 缺点：
  - 在一定时间内隔一段时间做一次备份，所以redis意外关闭以后。会丢失所有的数据
  - Fork的时候，内存中的数据被克隆了一份，所以有可能会造成内存溢出
- `Redis`中关于触发`RDB`快照的方式
  - 命令`save`阻塞其他进程，先进行持久化操作
  - 命令`bgsave`一部进行保存
  - 执行`shutdown`或者`flushDB`或者`flushALL`自动写入
  - 通过`lastsave`获取最后一次执行快照的时间
- 获取快照的保存路径`config get dir`。默认的文件名为`dump.rdb`
- 停止`RDB`保存规则的方法`redis-cli config set save ""`
- 执行机制

![](image/RDB.png)

#### 5.2AOF快照

- 解决`RDB`存在的问题的一种算法
- 以日志的形式记录每一个写操作，追加模式记录。将`redis`的写操作，记录在一个`.aof`文件中，每一次启动`redis`服务的时候就会运行这个文件中的所有的指令
- 默认名称为：`appengdonly.aof`
- `AOF`的配置

```properties
#设置AOF快照启动
appendonly yes

# The name of the append only file (default: "appendonly.aof")
appendfilename "appendonly.aof"

#三种同步修改的策略
# appendfsync always 同步持久化，当数据发生变更的时候就进行一次持久化
appendfsync everysec  异步操作，每秒记录。
# appendfsync no 不同步

#是否使用重写策略
no-appendfsync-on-rewrite no
#重写的基准值
auto-aof-rewrite-percentage 100
#当文件达到这个数值的时候执行重写
auto-aof-rewrite-min-size 64mb

#是否加载不完整的AOF文件启动
aof-load-truncated yes

```

- 重写：
  - `AOF`文件采用了文件追加的方式，所以会出现文件越来越大的情况，因此为了避免这种情况，增加了重写的机制。当文件超过`auto-aof-rewriter-min-size`设定的文件大小的时候，`Redis`就会启动`AOF`文件压缩。
  - 如果仅仅保存恢复数据的最小指令集，可以使用`bgrewriteaof`
  - 原理：AOF文件持续增长超过一定的大小的时候，主进程`fork`一个新的进程将文件重写。遍历`redis`数据库中的数据，然后直接生成一个写出这些数据的`aof`文件（不会读旧的aof文件）
    - 何时触发重写：Redis会记录上次的AOF文件的大小。默认配置是当AOF文件大小是上次重写后大小的一倍且文件大于指定的`rewriter-min-size`时触发
- 优点：
  - 异步持久化，误差保存在了`1s`
- 缺点
  - 相同数据集的`aof`文件远大于`rdb`文件，恢复速度慢于`rdb`

注意：

- 当`aof`的同步策略为不同步的时候，其性能和`rDB`相同
- 当AOF和`RDB`文件同时存在的时候，优先加载`AOF`文件，但是当`AOF`或者`RDB`文件损坏的时候，`Redis`服务就无法运行
- 修复两种文件的方式
  - `redis-check-aof --fix 文件名` 
  - `redis-check-dump --fix 文件名`

执行机制：

![图片来源尚硅谷周阳老师](image/AOF.png)

#### 5.3 区别

- `RDB`持久化可以在指定的时间间隔对数据进行快照存储
- `AOF`持久化通过记录客户端的写操作，当服务器重启的时候重新执行这些命令
- 同时开启数据持久化。

### 6.事务

>Redis支持部分事物

#### 6.1 悲观锁和乐观锁

**悲观锁：**

-	认为每一进行数据操作的时候，都会有其他线程同时进行数据修改，所以会在进行数据操作的时候将整个表加锁

**乐观锁：**

- 当前线程认为在执行数据操作的时候不会有其他线程同时进行操作，所以不进行加锁。
- 在表的最后一个字段后添加了一个版本号的字段，记录每次修改的版本号
- 当前线程修改数据的时候，其版本号必须大于该记录目前版本号才可以修改，否则直接报异常

#### 6.2 Redis对于事务操作的支持

>redis会将在事务中的所有操作都进行序列化，按照串行化执行，不容许加塞

五种事务支持场景

- 正常执行。在这个期间没有进程修改数据，直接执行完成
- 放弃事务。通过`discard`命令直接放弃正在执行的事务
- 全体失败。事务中出现编译时期异常时，这个事务就不成功。
- 单一失败。事务执行时产生运行时异常，只有产生异常的命令没有执行成功
- 监听数据。对数据进行事务操作的时候，首先对该数据进行监听。如果该数据监听以后被其他线程改变了，那么当前线程必须关闭监听，重新开启监听。如果不重新监听，会直接报异常，数据修改失败。(类似于乐观锁的实现)

#### 6.3 Redis事务操作代码

- 正常执行和监听

```shell
127.0.0.1:6379> keys *
1) "balance"
2) "idet"
127.0.0.1:6379> watch balance idet # 监听这两值
OK
127.0.0.1:6379> MULTI #开启事务
OK
127.0.0.1:6379> decrby balance 20 
QUEUED
127.0.0.1:6379> incrby idet 20
QUEUED
127.0.0.1:6379> EXEC # 执行事务成功
1) (integer) 780
2) (integer) 60


```

- 监听以后其他线程修改了其中的一个数据

```shell
#线程一执行这段事务
127.0.0.1:6379> get balance
"100"
127.0.0.1:6379> watch balance
OK
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379> decrby balance 30

#当线程一执行到这里的时候，线程二修改了balance的值为200
127.0.0.1:6379> set balance 200
OK
127.0.0.1:6379> get balance 
"200"


# 此时线程一继续执行。此时整个事务直接失败
127.0.0.1:6379> decrby balance 30
QUEUED
127.0.0.1:6379> INCRBY idet 30
QUEUED
127.0.0.1:6379> EXEC
(nil)

```

#### 6.4Redis事务的特性

- 单独的隔离操作：事务中的所有命令都会序列化、顺序地执行。事务在执行的时候不会被其他客户端发送来的命令请求打断
- 没有隔离级别的概念。队列中的命令没有提交之前都不会被执行
- 不保证原子性：redis同一个事务中如果有一条命令执行失败，气候命令仍然会被执行，没有回滚

### 7.Redis消息支持

**使用步骤：**

- 订阅消息。`subscribe c1 c2 c3`
- 发布消息。`publish c2 hello`
- 订阅多个，使用通配符。`pshubscirbe new*`
- 发布消息。`publish new 1 redis`

### 8.Redis复制

**含义：**主机数据更新后根据配置和策略，自动将数据同步到备机的`master/slaver`的机制，`Master`以写为主，`Slave`以读为主

**作用：**实现读写分离，容灾恢复

**常用的使用方式：**

#### 8.1一主二仆(名词来源于尚硅谷周阳老师)

>一台主机，负责写；两台备用机器。负责备份

- 查看当前机器的状态`info replcation`
- 设置该机器的主机`slaveof Ip 端口`

**常见的场景：**（主机为A，备份机器为B,C）

- 主机A添加了数据，此时的B,C没有成为A的从机。给主机A,B,C分别添加数据以后，将B,C作为A的从机，此时发现B,C机器中原有的数据被清空，只剩下了主机A的数据
- 当主机A服务中断时，从机B,C原地等候。当主机A重启以后，从机B,C恢复原来的身份，作为A 的从机
- 当从机服务中断时，再次启动以后已经不再是A的从机了。需要再次启动主从复制

**总结：**

- 当成为从机以后，就不能进行任何的写操作
- 成为从机以后本机的数据被清楚
- 从机服务中断以后再次启动需要重新进行从库配置
- 主机服务终端以后再次启动仍然为原来的状态

#### 8.2薪火相传

>一传一，一传一一直下去

**场景：**

- 主机A的从机为B，从机B的从机是C，从机C的从机为D……
- 从机不允许写

#### 8.3 反客为主

>当主机挂掉的时候，将一个从机作为主机

**场景：**

- 主机A服务停止。从机B通过`slaveof on one`将该从机作为一个主机使用，但是此时的从机C仍然为主机A的从机，所以需要主动将从机C改为主机B的从机。
- 当主机A再次启动的时候，作为主机，和B,C没有了从属关系

#### 8.4 复制的原理

- `slave`启动成功连接到`master`后会发送一个`sync`命令
- `Master`接到命令，启动后台存盘进程，同时收集所有接受到的用于修改数据集的命令，在后台进程执行完毕之后，`master`将传送整个数据文件到`slave`，以完成一次同步

**分类：**

- 全量复制。`slave`服务在接受到数据库文件以后，将其加载到内存中。
- 增量复制。`Master`将新的指令集传给`slave`

只要重新连接master，就会进行全量复制

#### 8.5 哨兵模式

>反客为主的自动版，能够后台监控主机是否出现故障，如果出现故障了则根据投票数自动将从库转换为主库

- 创建`sentinel.conf`文件，通过`Redis`提供的命令`redis-sentinel` 使用这个文件。这个 文件会开启的哨兵，用来监听这个文件内部配置的主机状况，如果这个主机挂掉，会通过投票的方式，从其从机中选出一个作为主机。当以前的那个主机再次启动以后，这个旧的主机会作为新的主机的从机

```shell
				主机名称(随便起) 监听的IP地址 端口 投票数
sentinel monitor host8379 127.0.0.1 6381 1
```

**不足：**

- 每一次的数据复制，都是现在`master`中操作，然后添加到`slave`中，这样会造成延迟，特别是当`slave`很多的时候