### MySQL配置文件

- 二进制日志`log-bin`主从复制
- 错误日志`log-error`
- 查询日志`log`
- 数据文件
  - `frm`文件存放表文件
  - `myd`存放数据文件
  - `myi`存放数据文件
- 配置方法
  - `windows`的`my.ini`文件
  - `linux`的`/etc/my.cnf`

### MySQL架构

先来看一下MySQL的架构思路

![](https://img2018.cnblogs.com/blog/1066923/201902/1066923-20190221103706946-1180917597.png)

>MySQL的架构可以在多种不同的业务场景中应用，并且发挥良好的作用。主要体现在存储引擎的架构上，插件式的存储引擎将查询处理和其他的系统任务及数据的存储提取相分离。
>
>MySQL为四层架构方案
>
>​	连接层--->服务层--->引擎层--->存储层

#### 1. MySQL向外提供的交互接口（Connectors）

Connectors组件，是MySQL向外提供的交互组件，如java，php等语言可以该组件来操作SQL语句，实现与SQL的交互

#### 2. 管理服务组件和工具组件（Management Service & Utilities）

提供对MySQL的集成管理，如备份(Backup),恢复（Recovery），安全管理（security）等。

#### 3. 连接池组件（Connection Pool ）

负责监听对客户端向MySQL Server的各种请求，接受请求，转发请求到目标模块中。每个成功连接MySQL Server的客户请求都会被创建或者分配一个线程，该线程负责客户端与MySQL Server端的通信，接受客户端发送的命令，传递服务端的结果信息等。

#### 4. SQL接口组件（SQL Interface）

接受用户SQL命令，如DML，DDL和存储过程等，并将最终结果返回给用户。

#### 5. 查询分析器组件（Parser）

分析SQL命令语法的合法性，并尝试将SQL命令分解成数据结构，若分解失败，则提示SQL语句不合理

#### 6. 优化器组件（Optimizer）

对SQL命令按照标准流程就行优化分析

#### 7. 缓存组件（Cache是&Buffers）

缓存和缓冲组件

#### 8. MySQL存储引擎（Storage Engines）

常用的存储引擎：InnoDB，MyISAM

```mysql
show engines ; 查询MySQL提供的引擎
# 查看默认的引擎
show variables like '%storage_engine%';
```

|        | InnoDB           | MyIASM     |
| ------ | ---------------- | ---------- |
| 锁     | 行锁             | 表锁       |
| 事务   | 支持             | 不支持     |
| 缓存   | 索引和数据都缓存 | 仅缓存索引 |
| 表空间 | 大               | 小         |
| 关注点 | 事务             | 性能       |
| 主外键 | 支持             | 不支持     |

### MySQL执行顺序

#### 1.开发者书写SQL顺序

```sql
select distinct
	<select_list>
from
	<left_table> <join type>
Join <right_table> on <join_confition>
where
	<where_condition>
group by
	<group_by_list>
having
	<having_condition>
order by
	<order_by_condition>
Limit <limit_number>
```

#### 2. MySQL解析顺序

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200313223537.png)

```sql
FROM <left_table>
ON <join_condition>
<join_type>JOIN <right_table>
WHERE<where_condition>
GROUP BY<group_by_list>
HAVING<having_condition>
SELECT
DISTINCT <select_list>
ORDER BY<order_by_condition>
LIMIT<limit_number>
```

总的来说：MySQL进行解析的时候，会首先加载`from`的表，然后加载`on`的条件，在根据`Join`的类型链接另一张表，链接以后，通过`ON`的条件，筛选一部分数据。之后通过`where`再进行一次筛选。之后再`group`表，进行分组，分组结束以后，就相当于所需要的数据已经全部拿到，之后进行查指定的字段，然后进行排序，最后根据`limit`进行输出数据。



