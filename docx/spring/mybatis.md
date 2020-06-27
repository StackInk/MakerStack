## Mybatis架构解读



### 1. 架构图

![](https://gitee.com/onlyzl/blogImage/raw/master/img/数据库设计 (4).png)

如题，这就是`MyBatis`的执行架构图。

**解释一下：**我们在使用`MyBatis`的`CRUD`操作的时候，一般有两种方式，一、直接调用`sqlSession`的`crud`方法；二、通过调用`getMapper`获取到接口代理的实现类，然后在代理方法中调用了`crud`方法。**可以看到，本质相同，最终调用的都是`sqlSession`的方法，上图就是`CRUD`执行的流程**

### 2. 执行流程图

我们先来看一下我们执行一个`MyBatis`的查询，需要做什么。

```java
//加载一个配置文件
InputStream resourceAsStream = Resources.getResourceAsStream("main.xml");
SqlSessionFactoryBuilder sqlSessionFactoryBuilder 
            = new SqlSessionFactoryBuilder();
SqlSessionFactory build = sqlSessionFactoryBuilder.build(resourceAsStream);
SqlSession sqlSession = build.openSession();
UserMapper mapper = sqlSession.getMapper(UserMapper.class);//代理模式创建了一个实现类
List<User> all = mapper.findAll(1);
all.forEach(System.out::println);
```

这就是一个最简单的查询过程。下面我们来分析一下他们每一步做了什么事情。

#### 2.1 Resources.getResourceAsSteam

很简单，读取了一个配置文件。可能有的小伙伴这个样子干过，**直接将通过本类的类加载器拿到资源路径，然后直接获取这个主配置文件，但提示未找到。**

看一下他的源码，他直接拿了一个系统类加载器。

```java
public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
    InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
    if (in == null) {
      throw new IOException("Could not find resource " + resource);
    }
    return in;
  }

ClassLoaderWrapper() {
    try {
      systemClassLoader = ClassLoader.getSystemClassLoader();
    } catch (SecurityException ignored) {
      // AccessControlException on Google App Engine
    }
  }
```

这个时候，我们自己使用`ClassLoader`获取系统类加载器加载资源， 这个时候也是可以成功获取的。于是想到了一个方法**我比较了一下本类类加载器和系统类加载的类别，发现都是通过`ApplicationClassLoader`加载的，但系统类加载器无法加载**

后来了解到的原因就是由于`Maven`插件的原因，在插件的地方指定一个`Resource`的映射路径即可，不过建议直接使用`MyBatis`的加载方式，简单一点。

#### 2.2 new SqlSessionFactoryBuilder.build

创建了一个`SqlSessionFactoryBuilder`构建者对象，**构建者模式**

然后通过`build`方法加载配置文件的资源。配置文件包括：主配置文件、`Mapper`文件或者注解。

**来看一下我们的主配置文件**

```java
<configuration>
    <typeAliases>
        <package name="com.bywlstudio.bean"/>
    </typeAliases>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <……………………>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper class="com.bywlstudio.mapper.UserMapper"></mapper>
    </mappers>
</configuration>
```

`XML`文件，`MyBatis`通过`XPath`语法进行解析，首先拿了一个`Configuration`节点，然后再解析内部的节点，每一个节点对应一个方法。**看一下源码**

```java
private void parseConfiguration(XNode root) {
    try {
      //issue #117 read properties first
      propertiesElement(root.evalNode("properties"));
      Properties settings = settingsAsProperties(root.evalNode("settings"));
      loadCustomVfs(settings);
      loadCustomLogImpl(settings);
      typeAliasesElement(root.evalNode("typeAliases"));
      pluginElement(root.evalNode("plugins"));
      objectFactoryElement(root.evalNode("objectFactory"));
      objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
      reflectorFactoryElement(root.evalNode("reflectorFactory"));
      settingsElement(settings);
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode("environments"));
      databaseIdProviderElement(root.evalNode("databaseIdProvider"));
      typeHandlerElement(root.evalNode("typeHandlers"));
      mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }
```

接下来要做的事情，就比较清晰了，**解析每一个`XML`标签中的节点、文本和属性值，为对应的对象进行封装**

我们主要看一下`environments`解析做了什么。

**要看他做了什么，得先看它有什么。**

它内部有多个`environment`元素，还有最关键的信息，**事务管理者和数据源**

所以在这个方法中他封装了一个`Environment`对象，内部存放了一个事务工厂和一个数据源对象。看一下`Environment`类信息

```java
public final class Environment {
  private final String id;
  private final TransactionFactory transactionFactory;
  private final DataSource dataSource;
```

**接下来再看重头戏`Mappers`的解析**

`Mappers`中可以存在四种映射方式：**面试题**

- `package`。指定一个需要扫描的包
- `resource`。指定一个本地的`mapper`映射文件
- `url`。指定一个`url`可以为网络的`mapper`映射文件
- `class`。指定一个类作为一个需要被代理的`mapper`

接下来我们看一下他的处理方式：

```java
 private void mapperElement(XNode parent) throws Exception {
    if (parent != null) {
      for (XNode child : parent.getChildren()) {
          //子节点是否为package
        if ("package".equals(child.getName())) {
          String mapperPackage = child.getStringAttribute("name");
          configuration.addMappers(mapperPackage);
        } else {
            
          String resource = child.getStringAttribute("resource");
          String url = child.getStringAttribute("url");
          String mapperClass = child.getStringAttribute("class");
            //属性是否为resource
          if (resource != null && url == null && mapperClass == null) {
            ErrorContext.instance().resource(resource);
            InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
              //xml方式处理
            mapperParser.parse();
              //属性是否为url
          } else if (resource == null && url != null && mapperClass == null) {
            ErrorContext.instance().resource(url);
            InputStream inputStream = Resources.getUrlAsStream(url);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
            mapperParser.parse();
              //属性是否为class
          } else if (resource == null && url == null && mapperClass != null) {
            Class<?> mapperInterface = Resources.classForName(mapperClass);
              //注解的方式处理
            configuration.addMapper(mapperInterface);
          } else {
            throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
          }
        }
      }
    }
  }
```

##### 2.2.1 xml方式

先来聊一下`xml`的处理方式

首先拿到对应的`mapper`文件，之后创建了一个解析该资源的类`XMLMapperBuilder`。解析子标签`mapper`等等属性，逻辑和之前一样，最后将所有的信息添加到了`Configutation`类中。

##### 2.2.2 注解方式

一个核心方法`org.apache.ibatis.binding.MapperRegistry#addMapper`。**有一个点，当你的Mapper不是一个接口的时候，他直接不处理了**

```java
public <T> void addMapper(Class<T> type) {
    //是否为接口
    if (type.isInterface()) {
      if (hasMapper(type)) {
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
          //将Mapper的信息，封装到一个MapperProxyFactory工厂中
        knownMappers.put(type, new MapperProxyFactory<>(type));
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
          //具体解析
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
```

**这个方法做的最重要的一件事情：**

- 将所有的mapper信息存放到了`MapperRegistry#knownMappers`集合中

具体的解析过程中，他还设置了**StatementType=PREPARED**;

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200613134915.png)

后面解析的过程主要进行注解解析，判断是否存在某某某注解，最后将所有的信息封装到了一个`Configuration`中。

**每一条SQL对应一个`MappedStatement`对象，该对象不可变**

```java
public final class MappedStatement {

  private String resource;
  private Configuration configuration;
  private String id;
  private Integer fetchSize;
  private Integer timeout;
  private StatementType statementType;
  private ResultSetType resultSetType;
  private SqlSource sqlSource;
  private Cache cache;
  private ParameterMap parameterMap;
  private List<ResultMap> resultMaps;
  private boolean flushCacheRequired;
  private boolean useCache;
  private boolean resultOrdered;
  private SqlCommandType sqlCommandType;
  private KeyGenerator keyGenerator;
  private String[] keyProperties;
  private String[] keyColumns;
  private boolean hasNestedResultMaps;
  private String databaseId;
  private Log statementLog;
  private LanguageDriver lang;
  private String[] resultSets;
```

#### 2.3 openSession

本质上创建了一个`DefalutSqlSession`对象。**创建了Executor**

```java
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
      final Environment environment = configuration.getEnvironment();
        //获取之前的事务工厂
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        //创建一个事务
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        //创建一个执行器
      final Executor executor = configuration.newExecutor(tx, execType);
        //创建了一个SQLSession
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```

#### 2.4 getMapper

还记得在`build`中，`Mybatis`将`mapper`信息封装为一个`MapperProxyFactory`添加到了一个`List`中，而现在的`GetMapper`就从里面拿到对应的`Mapper`代理工厂信息，然后创建对应的`Mapper`代理对象，最后返回

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
```

我们来看一下我们的代理对象

```java
 @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
      //这个就是我们的代理对象，也就是实现了代理接口的类
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
```

看一下这个里面有什么

```java
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;
    //执行增强的具体的方法
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
```

#### 2.5 具体的CRUD

在`getMapper`中，我们知道了此时返回的是一个该接口的代理对象，当我们执行具体的方法的时候，就走了其代理方法。

**主要执行逻辑是**：判断`Sql`的操作类型，然后执行对应的方法，如果是查询，则从缓存中查询，如果没有，则查询数据库，查到以后，将查询到的信息进行封装，封装以后，将这个信息添加的缓存中，然后返回。

他首先判断了该方法的类信息是不是`object`，然后判断是不是默认方法，如果是分别执行。**最后他给本类的methodCache中添加了一个方法映射**

```java
@Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }
```

接下来再看一下具体的执行方法。

```java
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
    	Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.update(command.getName(), param));
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.delete(command.getName(), param));
        break;
      }
      case SELECT:
            //返回值是否为null
        if (method.returnsVoid() && method.hasResultHandler()) {
          executeWithResultHandler(sqlSession, args);
          result = null;
            //返回值是否为多个(List)
        } else if (method.returnsMany()) {
          result = executeForMany(sqlSession, args);
        } else if (method.returnsMap()) {
            //返回值是否为键值
          result = executeForMap(sqlSession, args);
        } else if (method.returnsCursor()) {
          result = executeForCursor(sqlSession, args);
        } else {
            //返回值为一个
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
          if (method.returnsOptional() &&
              (result == null || !method.getReturnType().equals(result.getClass()))) {
            result = Optional.ofNullable(result);
          }
        }
        break;
      case FLUSH:
        result = sqlSession.flushStatements();
        break;
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    return result;
  }
```

**这个时候就回归到了SqlSession的API调用了**

#### 2.6 SqlSession具体调用

拿`Select`为例

首先他生成了一个`cacheKey`，拿这个key从缓存中找，如果没有查询数据库，查到以后将对应的结果放到缓存中，然后返回给用户

```java
//调用函数，
executor.query(MappedStatement
               , parameter,  
               RowBounds.DEFAULT, 
               Executor.NO_RESULT_HANDLER);

public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException { 

    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      queryStack++;
        //查询缓存
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list != null) {
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      queryStack--;
    }
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
        clearLocalCache();
      }
    }
    return list;
  }
```

- 查询以后放置到缓存并且返回的操作

```java
private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
      list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
      localCache.removeObject(key);
    }
    //缓存添加
    localCache.putObject(key, list);
    if (ms.getStatementType() == StatementType.CALLABLE) {
      localOutputParameterCache.putObject(key, parameter);
    }
    //返回
    return list;
  }
```

- 重头戏来了，接下来将会创建架构图里的第二个内容`StatementHandler`

```java
@Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();
        //创建了一个StatementHandler
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }
```

在创建的时候，他将所有的`StatementHandler`拦截器都执行了一遍。

```java
public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    //创建了一个StatementHandler
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    //执行所有的Statement拦截器（所有）
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }
```

还记得在`build`的时候，指定了一个`StatementType.PREPARED`类型吗？这个时候这个东西就开始起作用了。在创建`RoutingStatementHandler`这个类的时候，他根据`StatementType`类型创建了一个子类，而现在创建的就是`PreparedStatementHandler`，而在这个类的父类里创建了`ParameterHandler`和`ResultSetHandler`。

```java
public class RoutingStatementHandler implements StatementHandler {

  private final StatementHandler delegate;

  public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

    switch (ms.getStatementType()) {
      case STATEMENT:
        delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      case PREPARED:
        delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      case CALLABLE:
        delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      default:
        throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
    }

  }
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200612181854.png)

他们在创建的时候又将对应的所有拦截器执行了一遍。

**到了这里，架构图里的东西已经全部出来了。**

接下来就是执行`SQL`了

#### 2.7 总结

我们来看一下`Configuration`类。**一家人整整齐齐，图上的东西都在这里了**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200612182143.png)

整体执行逻辑就是：

- 创建一个`Executor`对象，将事务和数据源放进去
- 创建`StatementHandler`实现类，将其对应的拦截器执行了
- 在创建实现类的时候又创建了`ParameterHandler`实现类，并且将其拦截器执行了
- 同时也创建了`ResultSetHandler`，并且将其拦截器执行了
- 之后通过这个结果集映射做了一次对象封装，将数据存到缓存里，然后返回了。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/mybatis架构图 (1).png)

### 3. 面试题

>面试题整理自网络，方面复习

#### 3.1 #{}和${}的区别

- ${}是`Properties`文件中的变量占位符，可以应用于标签属性值和SQL内部，属于静态替换
- #{}是SQL参数占位符，`MyBatis`会将`SQL`中的#{}替换为`?`，在SQL执行前通过`PreparedStatement`的参数设置方法，设置具体的参数值。

#### 3.2 XML映射文件中，除了select|insert|update|delete还有哪些标签

答：

- `<resultMap>`。自定义结果集映射
- `<cache>`。定义当前命名空间中的缓存配置策略
- `<cache-ref>`。引用其他命名空间的缓存配置
- `<sql>`。定义一个SQL语句块，可以被引用
- 动态SQL
  - `<include>`引用一个SQL块
  - `<foreach>`
  - `<if>`
  - `<where>`
  - `<trim>`

#### 3.3  最佳实践中，通常一个Xml映射文件，都会写一个Dao接口与之对应，请问，这个Dao接口的工作原理是什么？Dao接口里的方法，参数不同时，方法能重载吗？

答：在`MyBaits`中，每一个命名空间的方法都拥有一个唯一标识。**接口全限定类名.方法名**，所以参数不同不能重载。其工作原理是通过JDK动态代理实现的。真正执行的是`MapperProxy`

#### 3.4 MyBatis是如何进行分页的？分页插件的原理是什么？

`Mybatis`使用`RowBounds`对象进行分页，它是针对`ResultSet`结果集执行的内存分页，而非物理分页。可以在sql内直接书写带有物理分页的参数来完成物理分页功能，也可以使用分页插件来完成物理分页。

分页插件的基本原理是使用`Mybatis`提供的插件接口，实现自定义插件，在插件的拦截方法内拦截待执行的sql，然后重写`sql`

#### 3.5 简述Mybatis的插件运行原理，以及如何编写一个插件。

答：Mybatis仅可以编写针对`ParameterHandler`、`ResultSetHandler`、`StatementHandler`、`Executor`这4种接口的插件，`Mybatis`使用JDK的动态代理，为需要拦截的接口生成代理对象以实现接口方法拦截功能，每当执行这4种接口对象的方法时，就会进入拦截方法，具体就是InvocationHandler的invoke()方法，当然，只会拦截那些你指定需要拦截的方法。

实现`Mybatis`的`Interceptor`接口并重写`intercept()`方法，然后在给插件编写注解，指定要拦截哪一个接口的哪些方法即可，记住，别忘了在配置文件中配置你编写的插件。

#### 3.6 Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？

答：第一种是使用`<resultMap>`标签，逐一定义列名和对象属性名之间的映射关系。第二种是使用sql列的别名功能，将列别名书写为对象属性名，比如T_NAME AS NAME，对象属性名一般是name，小写，但是列名不区分大小写，Mybatis会忽略列名大小写，智能找到与之对应对象属性名，你甚至可以写成T_NAME AS NaMe，Mybatis一样可以正常工作。

　　有了列名与属性名的映射关系后，Mybatis通过反射创建对象，同时使用反射给对象的属性逐一赋值并返回，那些找不到映射关系的属性，是无法完成赋值的。

#### 3.7 Mybatis能执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。

答：在`Mybatis`的`ResultMap`中可以通过`Result`标签或者注解指定需要映射的表。通过内部的`one`和`many`实现具体的一对一或者一对多映射关系。

比如可以通过`Result`中的`one`实现一对一映射。内部的`select`指定另一个查询语句，fetchType用于指定是否使用懒加载

```java
@Results({
            @Result(id = true , column = "id" , property = "id"),
            @Result(column = "nickName" , property = "nickName"),
            @Result(column = "gender" , property = "gender"),
            @Result(column = "city" , property = "city"),
            @Result(column = "province" , property = "province"),
            @Result(column = "wid" , property = "wxuser" ,one = @One(select = "com.bywlstudio.dao.IWXUserDao.findWXUserById" ,fetchType = FetchType.EAGER)),
    })
```

#### 3.8 懒加载实现原理

答：通过代理方法创建代理对象以后，在真正获取数据的时候到达拦截器的方法之后，拦截器方法首先判断当前值是否为null，如果为null，则通过预先的SQL查询并且set，最后get查询。

#### 3.9 myBatis如何执行批处理

答：通过`BatchExecutor`完成批处理

#### 3.10 MyBatis有哪些Executor执行，以及他们之间的区别

答：

- `SimpleExecutor`，执行一次`update`或者`select`就开启一个`statement`，用完立刻关闭
- `ReuseExecutor`，执行`update`或者`select`，以`SQL`作为`key`查找`Statement`对象，存在就使用，不存在就创建，用完以后，添加到`Map<String,Statement>`中
- `BatchExecutor`，执行`update`，将所有的`Sql`添加到批处理中，等待统一执行，缓存了多个`Statement`对象。

#### 3.11 Mybatis中如何指定使用哪一种Executor执行器？

在Mybatis配置文件中，可以指定默认的ExecutorType执行器类型，也可以手动给DefaultSqlSessionFactory的创建SqlSession的方法传递ExecutorType类型参数。

#### 3.12 Mybatis是否可以映射Enum枚举类？

Mybatis可以映射枚举类，不单可以映射枚举类，Mybatis可以映射任何对象到表的一列上。映射方式为自定义一个TypeHandler，实现TypeHandler的setParameter()和getResult()接口方法。TypeHandler有两个作用，一是完成从javaType至jdbcType的转换，二是完成jdbcType至javaType的转换，体现为setParameter()和getResult()两个方法，分别代表设置sql问号占位符参数和获取列查询结果。













































