## MySQL索引

### 1. 定义

索引是帮助MySQL高效获取数据的数据结构。索引内部存在一个键值和对应数据的物理地址，当数据很多的时候，索引文件会很大，所以一般以文件的形式存储于磁盘中，后缀名为`.myi`。

### 2. 常用索引类型

- 聚集索引
- 次要索引
- 覆盖索引
- 复合索引
- 前缀索引
- 唯一索引

### 3. 索引的优势

- 提高数据检索效率，降低了数据库的IO成本
- 对数据进行排序，降低了数据排序成本，降低了CPU的功耗
- 其作用为：排序和查找。

### 4. 索引的劣势

- 占用内存空间
- 降低了写操作的速度
- 开发者的难度增加

### 5. MySQL索引分类

- 单值索引。一个索引包含单个列，一个表可以有多个单值索引
- 唯一索引。索引列的值必须唯一，单允许有空值，如约束：`unique`
- 复合索引。一个索引包含多个列。

### 6. 基本语法

```mysql
# 创建索引
create [unique] index indexName on table(columnName(length)...)
alter table add [unique] index indexName on(columnName(length)...)
#删除
drop index indexName on table
#查看
show index from tableName
```

### 7. 索引数据结构之B树

#### 7.1 B+树结构

B+Tree索引是非常普遍的一种数据库索引结构。其特点是定位高效、利用率高、自我平衡。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200314215803.png)

这是一个BTree数据结构图。具体的实现思路是：存在一个根节点存放数据的范围（该范围可以存在多个），其支节点存放的该根节点所在层的具体值，然后支节点的叶子节点中存放的是具体的数据。值得一提的是，其叶子节点为双向链表，保存邻近的叶子节点的地址。

下面模拟一下查找56的过程。

- 先到根节点，查找56所在的区间范围
- 然后确定支节点的地址，寻找56所在的范围
- 然后找到具体的数据存储地址

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200314220101.png)

注意的几个点：

- BTree的三层架构可以抗住1000万数据，即100条数据和1000万条数据所需要消耗的IO相同。7.

#### 7.2 B树平衡扩张

B树的每一个节点都有一个固定的层级大小。那么必然会出现的情况是，根节点所在层满了，无法继续添加数据。这个时候怎么办？这个时候索引会进行拆分处理，分配两个数据块A,B，如果新添加的数据大于当前最大的元素，则将该元素放于B，其他的全部放入A；如果新添加的元素小于最大元素则平分数据。刚开始的根节点扩大之前的数据范围，此时其层节点将不再变满。扩张结束。

#### 7.3 数据删除导致查找到废弃节点

当数据删除的时候其索引中的数据是不会删除的，所以此时如果想要获取最大数据，就会找到一个废弃的节点，这个时候，就发现内部没有数据。由于叶子节点之间是双向链表，所以会寻找当前值邻近的节点数据。此时花费的时间就会增加。而解决这种情况的方法就是重新构建索引。

### 8. 索引的使用时机

#### 8.1 什么时候使用索引

- 主键（唯一索引）
- 频繁查询的字段
- 外键
- 需要排序的字段
- 需要分组的字段

#### 8.2 什么时候不适用索引

- where条件中不使用的字段
- 频繁更新的字段
- 表记录很少的时候
- 经常写操作的表
- 数据重复且分布比较平均的字段

### 9. SQL性能分析

>当客户端向MySQL请求一条Query，命令解析器模块完成请求分类，区别出是SELECT并转发给 MySQL Query Optimizer（查询优化器），MySQL Query Optimizer 首先会对整条Query进行优化，处理掉一些常量表达式的预算，直接将值换算为常量值。并对Query中的查询条件进行简化和转换，如去掉一些无用或者显而易见的条件、结构调整等。然后分析Query中的Hint信息，看显示Hint信息是否可以完全确定该Query的执行计划。如果没有Hint或Hint信息还不足以完全确定执行计划，则会读取所设计对象的信息，根据Query进行写相应的计算分析，然后在得出最后的执行计划。

MySQL的架构中的服务层中存在一个SQL语句优化的模块。他的主要功能是：通过计算分析系统手机到的统计信息，为客户端请求的Query提供他认为最优的执行计划。

此时就会延生出一个问题：开发者自己写的SQL与MySQL优化器执行的过程不一样。这种情况之下就会浪费很多的时间。

#### 9.1 MySQL性能瓶颈

- `CPU`饱和。常常发生在将数据加载到内存中或者从磁盘中读取数据的时候。
- `IO`饱和。常常发生在装入数据远大于内存容量的时候。

#### 9.2 EXPLAIN

>MySQL通过explain 关键字模拟优化器执行SQL语句的过程，从而对SQL语句进行优化。

##### 9.2.1 如何使用

- `explain SQL`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315135346.png)

```mysql
| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra 
```

##### 9.2.2 id

select 查询的序列号，表示了执行select查询的顺序或操作表的顺序。

可能出现的情况：

- 出现的ID都相同。（按照顺序从上到下执行，执行顺序和我们写的表顺序不一定相同）

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315135852.png)

- 出现ID都不同。(ID值越大，就先被执行)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315145258.png)

- 出现的ID既有相同的也有不同的。（先执行ID值最大的，然后ID值相同的就按照顺序执行）

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315145829.png)

- `derived2`这个代表一张临时表，`2`为生成临时表的ID即`t3`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315150118.png)

#####  9.2.3 select_type

查询类型。用来区分普通查询，联合查询，子查询等的复杂查询

- `SIMPLE`。简单的`select`查询，查询中不包括子查询或者`union`
- `PRIMARY`。查询中若包含任何复杂的子部分，最外层被标记为`primary`
- `SUBQUERY`。在`SELECT`或者`WHERE`列表中中包含了子查询，被标记为`subquery`
- `DERIVEd`。在`From`列表中包含的子查询被标记为`derived`（衍生表）。
  - MySQL会递归执行这些子查询，将结果放置于临时表中
- `UNION`。若第二个`SELECT`出现在`union`之后，则被标记为`union`
  - 如果`union`包含在`from`子句的子查询中，外层的`select`被标记为`derived`
- `union result`。从`union`表中获取结果的`select`

##### 9.2.4 table

显示这一行数据是关于哪一张表的

##### 9.2.5 type

查询的访问类型,查找到需要的数据的访问方法

```mysql
# 从最好---->最差 
system -> const -> eq_ref -> ref -> range -> index -> all
```

- `system`。表中只有一条记录的查询。速度最快，在生产中一般不会出现
- `const`。通过索引仅仅查找一次就找到了。用于`primary key`和`unique`索引，数据唯一。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315152452.png)

- `eq_ref`。表中仅仅存在一个值与之相对应。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315152733.png)

- `ref`。非唯一索引，返回满足该值的所有行。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315182337.png)

- `range`。仅仅检索指定范围的行，使用一个索引来选择行。如使用了`between`,`<>`,`in`等的查询条件
- `index`。仅仅依靠索引查询。
- `all`。遍历全表，不使用索引。

**小结：**`system`是表中仅仅一条记录；`const`是表中有多条记录，其查询条件可以视作为常量的值，子查询也算常量处理；`eq_ref`查询条件为变量，另一个表中仅仅存在一条记录与之对应；`ref`是另一个表中存在多条记录与之匹配;`range`是获取指定范围的值，不需要全表扫描;`index`通过索引扫描数据;`all`进行全表扫描数据；

##### 9.2.6 possible_keys

这次查询可能使用到的索引。理论计算得出，实际可能并未使用；

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315152733.png)

##### 9.2.7 key

实际使用的索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315152733.png)

##### 9.2.8 key_len

使用索引所占的字节大小，越少越好。条件越复杂其字节数越大。

##### 9.2.9 ref 

引用其他表的字段

##### 9.2.10 rows 

查询到所需要的数据扫描的行数。

##### 9.2.10 partitions

是否为分区表

##### 9.2.11 extra

包含不适合在其他列中显示但十分重要的额外信息

1. `Using filesort`。对数据使用一个外部的索引排序，而不是按照表内索引的顺序进行排序。

出现这种情况的场景为：一般是联合索引，进行分组或者排序的字段的顺序和构建索引时的字段顺序不同，导致内部排序的时候需要再次进行一次排序。**非常影响性能。**

- 目前`t1`表中的联合索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315203910.png)

- 仅仅根据一个字段进行分组。出现`using filesort`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315203747.png)

- 根据两个字段进行分组。此时就没有再次进行排序了。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315204113.png)

2. `Using temporary`使用了临时表保存数据，常见于`group By`和`order by`。和上面的原因相同。**非常影响性能。**
3. `Using index` 表示在 进行`select`操作的时候使用了覆盖索引，避免访问了表的数据行，增强了性能。如果同时出现了`using where`则表明索引用来读取数据而不是进行查找操作。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200315204724.png)

4. `using where` 表示使用索引进行过滤数据
5. `using join buffer`表示使用了连接缓存
6. `impossible where` 表示该语句永远不能获取数据
7. `select tables optimized away`表示在没有分组的情况下，基于索引优化`MIN/MAX`操作或者对于`MyISAM`存储引擎优化`Count(*)`操作，不必等到执行阶段再进行计算，查询执行计划生成的阶段即完成优化。
8. `distinct`优化distinct。找到第一组匹配的值以后就不再查找。

>开发中经常需要考虑的就是避免`Using filesort`和`Using temporary`操作，增加`Using index`操作。

### 10. 索引优化

#### 10.1 索引优化方法

- 进行左连接的时候，将右表的字段作为索引；右连接使用左表的字段作为索引。
  - 原因：左连接的时候会加载左表的全部数据，所以将左表作为驱动表，右表不需要加载全部数据，所以作为被驱动表。右连接也是相同。
- 查询条件均为索引字段
- 查询的字段最好使用覆盖索引，这个时候不需要查询表，直接在索引中拿数据即可

#### 10.2 索引失效

##### 10.2.1 最佳左前缀原则

- 创建的索引。复合索引为`name,age,deptId`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316123934.png)

- 查询`name,age,deptId`的条件，使用了索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316124157.png)

- 查询`name,age`字段的条件，使用了索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316123735.png)

- 查询`name`个字段的条件，使用了索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/image-20200316124218475.png)

**索引失效的情况:**

- 查询`age,deptId`，没有使用索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316124342.png)

- 查询`age`，没有使用。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/image-20200316124439271.png)

**使用了部分索引的情况**

- 查询`name,deptId`，使用了部分索引。仅仅在查询`name`的时候使用了索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/image-20200316124632350.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/image-20200316124721757.png)

查询`name`和查询两个字段的字节数相同。

##### 10.2.2 在索引列使用了计算，函数，类型转换的操作

- 进行了函数取位的操作

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316124858.png)

- 字符串没有加引号，导致MySQL底层自动类型转换

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316125016.png)

##### 10.2.3 查询条件使用了范围计算

- `where`中使用了`between and, <> in`等范围修饰符。使用了部分索引，仅仅对`name`使用了索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/image-20200316125309557.png)

##### 10.2.4 使用!=,<>,is null ,not is null 

- 使用上面的这些运算符都会导致索引失效

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316125544.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316125924.png)

##### 10.2.5 like通配符

- `%like%`此时索引失效

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316130247.png)

- `%like`此时索引失效

- `like%`此时使用了部分索引

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316130348.png)

- 可以将`%like`看做一种范围查询

##### 10.2.6 or的使用

- 使用`or`以后也会导致索引失效

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316130623.png)

#### 10.3 白话索引优化与失效

其实索引失效的起因就是无法从已经排序的内容拿到数据。比如最佳左前缀法则，其索引排序为`name,age,deptId`即树上的排序就是先排`name`相同的，然后到`age`，再到`deptId`，即，此时的`age`和`deptId`的单独顺序已经被`name`打乱。

举个栗子：

```mysql
name age
111	 12
112  11

此时在数据结构中的体现就是先111 12再到112 11 ,这个时候其age的顺序就被打乱了。所以不能使用索引对没有name开头的进行查询了。
```

至于说对数据列进行操作，引发其数据本身变化，这样的操作导致B树中的数据和索引中的数据不一样，肯定不能使用索引进行查询了。

#### 10.4 Order By和Group By对索引影响

- 看一种情况。这个时候仅仅使用了一个索引进行查询，但实际上索引都用了，只不过`age，deptId`用来进行排序了，没有用来查找

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316135001.png)

- 如果我们将排序条件逆序，这个时候必然出现文件内排序

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316135111.png)

- 再次添加一个条件`age = 12` 即另`age`等于一个常量，所以此时没有进行文件内排序

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316135335.png)



- `group by`对索引字段进行排序，此时分组顺序正序,直接使用索引数据进行分组排序

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316140252.png)

- `group by `对索引字段进行排序，此时分组顺序逆序。出现文件内排序，并使用临时表

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316140033.png)

#### 10.5 索引使用的建议

- 对于单值索引，尽量选择对查询过滤最好的字段。
- 在组合索引中，查询过滤中效果最好的字段位置越靠前越好
- 组合索引中，最好包含更多的`where`条件的字段值。(当然避免范围查询字段索引)
- 通过分析SQL来判断当前索引是否符合当前的目的
- 对于`like`而言，其只要以`%`开头其索引就无法使用。

### 11. 查询优化

#### 11.1 小表驱动大表

永远使用数据集小的表去驱动数据集大的表

```mysql
#假设现在存在一张表A数据多于B，此时需要找到表A中与B重合字段的数据，仅仅需要A的数据
#这是in的写法
select * from A where id in (select id from B) ;

#这是exist 的写法
select * from B where exists(selct 1 from A where A.id = B.id)
```

下面看一下两者执行顺序

- `in`方案执行。将子查询的数据放到主查询中。即将B表的数据检索结果放在A表的结果中

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316161938.png)

- `exist`方案执行。将主查询的数据放到子查询中，于是子查询的SQL执行类型变为了`eq_ref`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316162534.png)

可以看到对于A表进行了全表扫描，然后对是否输出A表数据，进行了判断

**小总结：**如果仅仅需要获取`A`表中的数据且该数据和B表中的为共有，除了使用`join`以外，也可以使用`in和exists`。两者使用的区别是：`in`将子查询的数据放置在主查询中作为条件，比较适用于主表数据多于从表数据；而`exists`是将主表查询结果放置于子查询中，比较适用于主表数据少于从表数据。

#### 11.2 Order By

MySQL存在两种排序的算法，`FileSort`和`Index`排序，其中`FileSort`的效率比较低

##### 11.2.1 Index

使用索引进行排序。出现这种排序的场景为

- `ORDER BY`使用索引最左列排序
- 使用`where`字句与`order by`字句满足索引最左前列。如，排序字段为第二个索引字段，而第一个字段在`where`条件中为常量，此时会使用Index排序

##### 11.2.2 FileSort

使用文件内排序，采用的算法主要有多路排序和单路排序

- 多路排序。`MySQL4.1`之前使用双路排序，即扫描两次磁盘，首先读取一个指针和需要排序的列，然后写入`buffer`中，排序完成以后，再次获取所有的列；即进行了两次IO
- 单路排序。改进多路算法，主要思想是第一次扫描磁盘的时候就将所有需要的数据获取，然后排序。它使用的空间更多了。

**存在的问题：**

单路算法也延伸了一个问题，其占用空间很大，有可能超过了`sort_buffer`的最大容量，所以只能进行分片处理，这个时候其IO量就会增加。

**解决办法：**

- 增大`sort_buffer_size`参数的值
- 增大`max_length_for_sort_data`参数
- 在实际开发中，如果添加的数据量大于`max_length_for_sort_data`则使用多路算法，否则使用单路算法

#### 11.3 排序使用索引

- MySQL的两种排序方式：文件内排序和有序索引排序
- MySQL能为排序和查询使用相同的索引

```mysql
key a_b_c(a,b,c) # 创建一个名为key的复合索引在a_b_c表中

order by # 可以使用索引的左前缀
order by a 
order by a , b
order by a,b ,c
order by a desc , b desc , c desc 

order by #如果where的左前缀为常量，则可以使用索引
where a = const order by b , c
where a = const and b = const order by c 
where a = const and b <const order by b,c

#不能使用索引进行排序
order by a asc , b desc  #排序不一致
where g = const order b ,c ; #丢失a索引
where a = const order c ; #丢失b索引
where a = const order by a ,d # d不是索引
where a in () order by b,c #范围查询
```

### 12. 慢查询日志

#### 12.1 简介

MySQL提供的一种日志记录，用来记录在MySQL中响应时间超过阙值的语句，具体指运行时间操作`long_query_time`值的SQL，会被记录到慢查询日志中

`long_query_time`默认为10，运行时间在10秒以上的SQL

#### 12.2 使用慢查询日志

临时改变日志的方式，当MySQL服务重启以后该修改就失效了

- 默认慢查询日志是关闭的

```mysql
#查看当前数据库的慢查询开启情况和日志存放位置
mysql> show variables like '%slow_query_log%' ;
+---------------------+-------------------------------+
| Variable_name       | Value                         |
+---------------------+-------------------------------+
| slow_query_log      | OFF                           |
| slow_query_log_file | /opt/mysql/log/slow_query.log |
+---------------------+-------------------------------+
2 rows in set (0.08 sec)

#开启慢查询
mysql> set global slow_query_log=1;
Query OK, 0 rows affected (0.01 sec)

#默认慢查询界定时间大于这个值的时候被记录
mysql> show variables like 'long_query_time%';
+-----------------+----------+
| Variable_name   | Value    |
+-----------------+----------+
| long_query_time | 1.000000 |
+-----------------+----------+
1 row in set (0.01 sec)

#设置慢查询时间
mysql> set global long_query_time=3;
Query OK, 0 rows affected (0.00 sec)


#查询当前设置的慢查询时间，如果不添加global则需要在另一个会话中才可以查询到当前的改变
mysql> show global variables like 'long_query_time%';
+-----------------+----------+
| Variable_name   | Value    |
+-----------------+----------+
| long_query_time | 3.000000 |
+-----------------+----------+
1 row in set (0.00 sec)

#查询当前SQL中慢查询的条数
mysql> show global status like '%Slow_queries';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| Slow_queries  | 1     |
+---------------+-------+
1 row in set (0.06 sec)

```

---

配置文件版

```mysql
在my.cnf文件中添加
slow_query_log=1;
slow_query_log_file=/opt/mysql/日志名字
long_query_time=3;
log_output=FILE
```

#### 12.3 日志分析工具mysqldumpshow

```mysql
s:是表示按何种方式排序
c:访问次数
l:锁定时间
r:返回记录
t:查询时间
al:平均锁定时间
ar:平均返回记录数
at:平均查询时间
t:即为返回前面多少条的数据
g:后边搭配一个正则匹配模式，大小写不敏感的
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316191316.png)

### 13. show profiles

MySQL中提供给开发者的分析当前会话中语句执行的资源消耗情况。可以用于SQL的调优

默认状态为关闭状态。且默认保存15条SQL

```mysql
# 查询当前数据库的profile状态
mysql> show variables like 'profiling' ;
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| profiling     | OFF   |
+---------------+-------+
1 row in set (0.00 sec)

# 开启数据库profile
mysql> set global profiling=on;
Query OK, 0 rows affected, 1 warning (0.00 sec)

#查看最近执行的SQL
mysql> show profiles ;
+----------+------------+----------------------------------------------+
| Query_ID | Duration   | Query                                        |
+----------+------------+----------------------------------------------+
|        1 | 0.03361600 | show variables like 'profiling'              |
|        2 | 0.00012075 | select name from t_emp group by age          |
|        3 | 0.00037275 | select name,age from t_emp group by age,name |
|        4 | 0.00018950 | select name from t_emp group by name         |
+----------+------------+--------------

# 查看指定SQL的系统消耗信息
show profile 参数 for query_id 
```

- 可以添加的参数

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316212604.png)

- 样例查询

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200316212755.png)

**日常开发中需要注意的事情：**

- `converting heap to myisam`查询结果太大，内部不够用存放于磁盘中
- `creating tmp table`创建了临时表，用完删除
- `copying to tmp table on disk`将内存中临时表复制到磁盘中
- `locked`加锁

### 14. 全局查询日志

在MySQL中的配置文件中，配置

```shell
# 开启全局查询日志
general_log=1
#记录日志文件的路径
general_log_file=/opt/mysql/log
#输出格式
log_output=file
```

命令行中配置，MySQL服务器重启以后失效

```mysql
set global general_log=1;
set global log_ouput = 'TABLE';
此后所有的SQL都将被记录到mysql.general_log系统表中
select * from mysql.general_log;
```

不建议使用这个功能，可以直接使用`profile`功能更加强大。





