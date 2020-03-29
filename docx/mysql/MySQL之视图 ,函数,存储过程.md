### 1. 视图

一张虚拟表，就是将一个经常被使用的查询作为一个虚拟表，开发者查询的时候不需要再次书写SQL，而是直接调用对应的视图就可，调用视图以后MySQL会执行这个查询SQL。

```mysql
# 创建视图
create view vi_select as select * from emps ;
# 创建或者代替已有视图
create or replace view vi_select as select * from emps inner join dept on dept.id=emps.id
#修改视图
alter view 视图名 as select 语句

#显示视图的创建情况
show create view 视图名
#查看视图
show tables like 'vi_%' ;#和查询表的相同，所以在创建视图的时候最好有一个前缀，通过模糊查询查询结果

#删除视图
drop view 视图名;

#重命名
rename table 视图名 to 新视图名;

```

#### 1.1 对视图不能进行DML操作的情况

- `select`子句中包含`distinct`
- `select`子句中包含组函数
- `select`语句中包含`group by`
- `select`语句中包含`order by`
- `select`语句中包含`union`或者`union all`等集合运算符
- `where`子句中包含子查询
- `from`中包含多的个表
- 视图列中包含计算列
- 基表中存在非空约束，则不能进行`insert`

### 2. 函数

和其他语言的函数相同，存在一个返回值

语法：

```mysql
# 创建函数
create function 函数名(参数列表) returns 数据类型
begin
	sql ;
	return ;
end ;

#调用函数
select 函数名

#查看函数的创建语句
show create function 函数名
#查看所有的函数
show function status like '' ;

#删除函数
drop function 函数名
```



### 3. 存储过程

一组可编程的函数，为了完成一段特定功能的SQL语句集，经编译创建并保存在数据库中，用户可以通过存储过程的名字来调用。

通常会配合`DELIMITER `来使用。`DELIMITER `的作用是改变SQL语句的结束符号。默认为`;`

```mysql

#创建存储过程,传入参数，IN代表输入参数，OUt代表输出参数。
DELIMITER $
create procedure 名字(IN a int , IN b int,out sum int)
begin
	#声明一个变量C
	declare c int ;
	if a is null then set a = 0 ;
	end if
	if b is null then set b = 0 ;
	end if ;
	set sum = a+b ;
END
$
DELIMITER ;
```

### 4. 触发器

表示某一个表发生一个事件（写操作），然后自动的执行预先编译好的SQL语句，执行相关的操作。触发器事件跟触发器中的SQL语句的操作是原子性的。

可以用来对数据预处理

```mysql
create trigger 触发器名称
{after|before} #触发时间
{insert|update|delete}#触发事件
for each row #固定写法，无论哪一行数据发生变化均会触发该触发器
begin 
	SQL
end ;	

#查看触发器
show triggers from databaseName;

#删除触发器
drop trigger if exists 
```

- 插入数据

```mysql
#生成一个随机的字符串,n代表位数
create function rand_string(n int) returns varchar(255)
begin 
    declare chars_str varchar(100) default 'abcdefghijklmnopqrstuvwyzABCDEFGHIJKLMNOPQRSTUVWXYZ'; 
    declare return_str varchar(255) default ''; 
    declare i int default 0 ; 
    while i< n do 
        set 	return_str=concat(return_str,substring(chars_str,floor(1+rand()*52),1));
        set i = i +1 ; 
    end while ; 
    return return_str ; 
end$$

#生成一个随机的数字
create function rand_num() returns int(5)
begin  
    declare i int default 0 ; 
    set i = floor(100+rand()*10); 
    return i ; 
end$$

#创建插入数据的存储过程
crate procedure insert_emp(IN start int(10),IN max_num int(10)) 
begin 
    declare i int default 0 ; 
    set autocommit = 0 ; 
    repeat 
        set i = i +1 ; 
        insert into emp(name,age) values(rand_string(6),(start+i)); 
        until i = max_num 
    end repeat; 
    commit; 
end$$

call insert_emp(100,5000000);
```



### 5.用户管理

```mysql
#创建用户
create user 用户名 identified by 密码
# 查看MySQL中所有的用户
select host,user from mysql.user;
# 修改当前用户的密码
set password = password(密码)
# 修改其他用户的密码
update mysql.user set password=password(密码) where user="用户名"
# 修改用户名
update mysql.user set user = user where user = "用户名"
# 删除用户
drop user 用户名
```

- user表中的字段。可以直接通过修改这个表中的字段来为用户赋予权限

```tex
Select_priv。确定用户是否可以通过SELECT命令选择数据。

Insert_priv。确定用户是否可以通过INSERT命令插入数据。

Update_priv。确定用户是否可以通过UPDATE命令修改现有数据。

Delete_priv。确定用户是否可以通过DELETE命令删除现有数据。

Create_priv。确定用户是否可以创建新的数据库和表。

Drop_priv。确定用户是否可以删除现有数据库和表。

Reload_priv。确定用户是否可以执行刷新和重新加载MySQL所用各种内部缓存的特定命令，包括日志、权限、主机、查询和表。

Shutdown_priv。确定用户是否可以关闭MySQL服务器。在将此权限提供给root账户之外的任何用户时，都应当非常谨慎。

Process_priv。确定用户是否可以通过SHOW PROCESSLIST命令查看其他用户的进程。

File_priv。确定用户是否可以执行SELECT INTO OUTFILE和LOAD DATA INFILE命令。

Grant_priv。确定用户是否可以将已经授予给该用户自己的权限再授予其他用户。例如，如果用户可以插入、选择和删除foo数据库中的信息，并且授予了GRANT权限，则该用户就可以将其任何或全部权限授予系统中的任何其他用户。

References_priv。目前只是某些未来功能的占位符；现在没有作用。

Index_priv。确定用户是否可以创建和删除表索引。

Alter_priv。确定用户是否可以重命名和修改表结构。

Show_db_priv。确定用户是否可以查看服务器上所有数据库的名字，包括用户拥有足够访问权限的数据库。可以考虑对所有用户禁用这个权限，除非有特别不可抗拒的原因。

Super_priv。确定用户是否可以执行某些强大的管理功能，例如通过KILL命令删除用户进程，使用SET
GLOBAL修改全局MySQL变量，执行关于复制和日志的各种命令。

Create_tmp_table_priv。确定用户是否可以创建临时表。

Lock_tables_priv。确定用户是否可以使用LOCK TABLES命令阻止对表的访问/修改。

Execute_priv。确定用户是否可以执行存储过程。此权限只在MySQL 5.0及更高版本中有意义。

Repl_slave_priv。确定用户是否可以读取用于维护复制数据库环境的二进制日志文件。此用户位于主系统中，有利于主机和客户机之间的通信。

Repl_client_priv。确定用户是否可以确定复制从服务器和主服务器的位置。

Create_view_priv。确定用户是否可以创建视图。此权限只在MySQL 5.0及更高版本中有意义。关于视图的更多信息，参见第34章。

Show_view_priv。确定用户是否可以查看视图或了解视图如何执行。此权限只在MySQL 5.0及更高版本中有意义。关于视图的更多信息，参见第34章。

Create_routine_priv。确定用户是否可以更改或放弃存储过程和函数。此权限是在MySQL 5.0中引入的。

Alter_routine_priv。确定用户是否可以修改或删除存储函数及函数。此权限是在MySQL 5.0中引入的。

Create_user_priv。确定用户是否可以执行CREATE USER命令，这个命令用于创建新的MySQL账户。

Event_priv。确定用户能否创建、修改和删除事件。这个权限是MySQL 5.1.6新增的。

Trigger_priv。确定用户能否创建和删除触发器，这个权限是MySQL 5.1.6新增的。
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200317205337.png)



#### 5.1 查看当前用户权限

```mysql
show grants for root@'%'
%代表所有IP可访问

select * from mysql.user where user='用户名'
```

####  5.2 赋予用户权限

- 直接修改`mysql.user`表。

```mysql
#赋予权限
update mysql.user set Select_priv='Y' , Insert_priv = 'Y',Update_priv ='Y' ,Delete_priv='Y',Create_priv='Y' where user='stack';
update mysql.user set reload_priv='Y', process_priv='Y',index_priv='Y', alter_priv='Y',lock_tables_priv='Y',execute_priv='Y' where user='stack';
update mysql.user set create_view_priv='Y',show_view_priv='Y',create_routine_priv='Y',alter_routine_priv='Y' where user='stack';
#刷新权限
flush privileges;
```

