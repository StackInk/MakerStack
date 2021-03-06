## NoSql简介

### 1. 互联网技术栈演变

- 一台`MYSQL`撑起整个时代

![](https://gitee.com/onlyzl/image/raw/master/img/mysql1.png)

- `Memcached`缓存+`MYSQL`集群

![](https://gitee.com/onlyzl/image/raw/master/img/memcached.png)

- `Mysql`主从复制，读写分离

数据库写入压力增加，读写放于一个库中，数据库压力太大。所以采用主从复制。读写分离的思路，减轻服务器负担

![](![](https://gitee.com/onlyzl/image/raw/master/img/masterslave.png)

- 分库分表+水平拆分+`mysql`集群

数据量递增的情况下，由于`MYISAM`使用表锁，在高并发的情况下会出现严重的锁问题，所以使用`INNODB`代替`MYISAM`，同时采用了分库分表的技术，搭建`MYSQL`集群

![](https://gitee.com/onlyzl/image/raw/master/img//masterslaves.png)

- `MYSQL`扩展瓶颈。

`MYSQL`对于存储大文本数据，或者数据库恢复比较慢，所以不能应用于所有的场景，而`MYSQL`扩展性差，大数据下`IO`压力大，表结构更改困难。

- 引入`NOSQL`

### 2.NoSql是什么？

泛指非关系数据库，数据之间没有关系，可以很好的横向扩展

### 3.NoSql的特征

- 易扩展
- 高性能
- 数据模型多

### 4.NoSql和RDBMS的区别

`RDBMS`

- 高度组织化结构化数据
- 结构化查询语言
- 数据和 关系存在一个单独的表中
- 数据操作语言
- 一致性
- 事务

`NoSql`

- 代表着不仅仅是SQL
- 没有声明式查询语言
- 没有预定义模式
- 键值
- 一致性
- CAP定理
- 高性能，高可用，可伸缩

### 5. 3V+3高

大数据时代的3V

- 海量数据 Volume
- 数据来源多样Variety
- 数据讲究实时性 Velocity

互联网需求三高

- 高并发
- 高可扩
- 高性能

### 6. NoSql数据库分类

KV键值

- `redis` , `tair` ,`memcache`,`berkeleyDB`

文档型数据库以`BSON`数据类型为主

- `CouchDB`,`MongoDB`

>BSON 一种类似于JSON的存储文件格式，语法和JSON完全相同

列存储数据库

- `Cassandra`,`HBase`
- 分布式文件系统

图关系数据库

- `Neo4J`,`InfoGrid`
- 专注于构建关系图谱

### 7. 分布式数据库中的CAP原理

传统的ACID

- A(Atomicity) 原子性。事务是一个不可分割的工作单位，里面的操作要么都发生，要么都不发生
- C(Consistency) 一致性。事务将数据库从一种正确的状态到达另一种正确的状态，如果期间出现错误，回滚事务回到开始的状态
- I(Isolation)隔离性。
  - 四种隔离级别
    - 脏读。事务A在读取数据的时候，事务B对数据进行了修改，并且事务B进行了回滚。而事务A读到的是事务B修改以后的数据
    - 不可重复读。事务A在读取数据以后，再次读该数据的时候，事务B对该数据进行了修改，导致事务A两次读取的数据不一样
    - 幻读。事务A修改数据的时候事务B将事务A修改以后的数据修改为了原来的状态，事务A发生幻读
    - 串行化。一行一行的执行，为数据加锁
- D(durability)持久性。事务提交以后，其对数据库的修改信息将会一直得到保存，除非得到修改。

CAP

- C(Consistency)强一致性。保证每一个分区的数据同步
- A(Availability)可用性。保证每一个分区的机器可用
- P(Partition tolerance)分区容错性。相当于分区通信，在实际开发中必须保证每一个分区之间是可以相互通信的。

为什么C和A不能共存？

比如现在存在两个分区A,B，客户端C给了分区A一个写的操作，将分区A中的数据a改为了b，而此时如果要保证数据一致性，那么需要给分区B加读写锁，再将数据a改为b，此时的分区B的不可用的，所以两者不能同时满足。

### 8. Base

- 基本可用(Basically Available)
- 软状态(Soft state)
- 最终一致(Eventually consistent)

牺牲某一时刻数据的一致性，保证整个系统的性能，系统仅仅保证可用，分区通信，最终数据一致即可