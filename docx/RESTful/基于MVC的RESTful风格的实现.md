### 基于`MVC`的`RESTful`风格的实现

#### 1.`RESTful`风格阐述

> `REST`服务是一种`ROA`(Resource-Oriented Architecture,面向资源的架构)应用。主要特点是方法信息存在于`HTTP`协议的方法中（`GET`,`POST`,`PUT`,`DELETE`），作用域存在于`URL`中。例如，在一个获取设备资源列表的`GET`请求中，方法信息是`GET`，作用域信息是URI种包含的对设备资源的过滤、分页和排序等条件
>
> ==良好的`REST API`不需要任何文档==

##### 1.1`REST`风格资源路径

`REST`风格的资源路径设计是面向资源的，==资源的名称==应该是准确描述该资源的==名词==。

> 资源路径概览：`sheme://host:port/path?queryString`
>
> 例：http://localhost:8080/bywlstudio/users/user?username=xiuer

##### 1.2`HTTP`方法

> `GET`用于==读取==、==检索==、==查询==、==过滤==资源
>
> `PSOT`用于==创建==一个资源
>
> `PUT`用于==修改==、==更新==资源、==创建客户端维护主键信息的资源==
>
> `DELETE`用于==删除==资源

**资源地址和`HTTP`方法结合在一起就可以实现对资源的完整定位**

##### 1.3`RESTful`风格`API`设计

*上文讲述了通过HTTP方法和资源路径对服务器的一个资源进行定位的过程*

接下来看一个REST风格`API`的设计

| 功能           | 描述                                                         |
| -------------- | ------------------------------------------------------------ |
| 添加/创建      | `POST/users`<br />`PUT/users{id}`[^创建客户端维护主键信息的资源] |
| 删除           | `DELETE/users/{id}`                                          |
| 修改/更新      | `PUT/users/{id}`                                             |
| 查询全部       | `GET/users`                                                  |
| 主键查询       | `GET/users/{id}`<br />`GET/users?id=26`                      |
| 分页作用域查询 | `GET/users?start=0&size=10`<br />`GET/users?07,2019-07,2020` |

可以看到通过这个`RESTAPI`都是通过对==同一个资源==的操作，所不同的就是通过不同的==HTTP方法==来实现对资源不同的处理。

#### 2.`MVC`对`REST`的支持

##### 1.1主要通过注解来实现

* `@Controller`声名一个处理请求的控制器
* `@RequestMapping`请求映射地址，它存在几个子注解对于实现`REST`风格来说更加具有==语义性==
  * `@GETMapping`         ==GET请求==
  * `@PUTMapping`         ==PUT请求==
  * `@POSTMapping`       ==POST请求==
  * `@DELETEMapping`   ==DELETE请求== 

* `@ResponseBody` 将响应内容转换为`JSON`格式
* `@RequestBody`   请求内容转换为`JSON`格式
* `@PathVariable("id")`用于绑定一个参数
* `@RESTController`  等同于`@Controller`+`@ResponseBody`在类上写了这个注解，标识这个类的所有方法只==返回数据==，而不进行==视图跳转==

##### 1.2返回`HTTP`状态码

**`REST`风格`API`一个最鲜明的特点通过返回对应的`HTTPStatus`来判断客户端的操作是否完成**

==下面是spring中关于`Http`状态码描述的枚举类，本文列举了常见的状态码==（读者若对此感兴趣可以查看`HttpStatus`源码）

~~~java
public enum HttpStatus{
    OK(200, "OK"),//用于服务器有实体响应
    CREATED(201, "Created"),//创建了新实体，响应该实体
    NO_CONTENT(204, "No Content"),//服务器正常响应，但无实体响应
    BAD_REQUEST(400, "Bad Request"),//客户端请求语法错误
    NOT_FOUND(404, "Not Found"),//目标资源不存在
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),//服务器内部错误
    NOT_IMPLEMENTED(501, "Not Implemented"),//服务器不支持当前请求
}
~~~

Spring返回状态码是通过`@ResponseStatus`注解或者`ResponseEntity<?>`类实现的。

==`@ResponseStatus`方式==

~~~java
@GetMapping(path = "/user/{id}" , produces = "application/json;charset=utf-8")
@ResponseStatus(HttpStatus.OK)
public User findUserById(@PathVariable("id")Integer id){
    User user = userService.findUserById(id);
    return user ;
}
~~~

==`ResponseEntity<?>`==方式

~~~java
@GetMapping(produces = "application/json;charset=utf-8")
public ResponseEntity<List<User>> findAll(){
    List<User> users = userService.findAll();
    return new ResponseEntity<List<User>>(users , HttpStatus.OK);
}
~~~

##### 1.3由于`MVC`默认不支持`PUT`和`DELETE`方法，所以需要手动开启

*在`tomcat`服务器的`web.xml`文件中开启一下配置*

~~~xml
<servlet>
		<servlet-name>default</servlet-name>
		<servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
		<init-param>
			<param-name>debug</param-name>
			<param-value>0</param-value>
		</init-param>
		<init-param>
			<param-name>listings</param-name>
			<param-value>false</param-value>
		</init-param>
		<init-param>
        <param-name>readonly</param-name>
        <param-value>true</param-value><!--开启这个-->
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
~~~

在项目的`web.xml`中配置

~~~xml
<filter>
    <filter-name>HiddenHttpMethodFilter</filter-name>
    <filter-class>org.springframework.web.filter.HiddenHttpMethodFilter</filter-class>
  </filter>

  <filter-mapping>
    <filter-name>HiddenHttpMethodFilter</filter-name>
    <servlet-name>dispathcherServlet</servlet-name>
  </filter-mapping>
~~~

#### 3.`MVC`实现`REST`代码实现

##### 3.1实例环境

* `JDK1.8`
* `maven3.60`
* `tomcat9`

##### 3.2`API`设计

| URI                   | Description              | Response | HTTPStatus |
| --------------------- | ------------------------ | :------: | ---------- |
| ==GET==/users         | 获取全部用户             |  `JSON`  | 200        |
| ==GET==/users/{id}    | 获取指定主键的用户       |  `JSON`  | 200        |
| ==PUT==/users/{id}    | 修改指定的主键的用户信息 |  `JSON`  | 200/201    |
| ==POST==/users        | 增加一个用户             |  `JSON`  | 201        |
| ==DELETE==/users/{id} | 删除一个用户             |  `void`  | 204        |

##### 3.3控制层代码

~~~java
@RestController
@RequestMapping("/users")
public class UserControler {

    @Autowired
    private IUserService userService ;

    //REST风格实现方法

    /**
     * 查询所有
     * @return
     */
    @GetMapping(produces = "application/json;charset=utf-8")
    public ResponseEntity<List<User>> findAll(){
        List<User> users = userService.findAll();
        return new ResponseEntity<List<User>>(users , HttpStatus.OK);
    }

    /**、
     * 根据ID查询
     * @param id
     * @return
     */

    @GetMapping(path = "/{id}" , produces = "application/json;charset=utf-8")
    @ResponseStatus(HttpStatus.OK)
    public User findUserById(@PathVariable("id")Integer id){
        User user = userService.findUserById(id);
        return user ;
    }
    /**
     * 增加一个用户
     * 返回该用户
     */
    @PostMapping(produces = "application/json;charset=utf-8")
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody User user){
        User newUser = userService.addUser(user);
        return newUser ;
    }

    /**
     * 更新
     * @param user
     */
    @PutMapping(path = "/{id}" ,produces = "application/json;charset=utf-8")
    public ResponseEntity<User> updateUser(@PathVariable("id") Integer id , @RequestBody User user){
        user.setUid(id);
        //资源是否修改
        boolean flag = userService.updateUser(user);
        User deUser = userService.findUserById(id);
        if(flag)
            return new ResponseEntity<User>(deUser,HttpStatus.CREATED);
        return new ResponseEntity<User>(deUser,HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}"  , produces = "application/json;charset=utf-8")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delUser(@PathVariable("id") Integer id){
        User user = userService.findUserById(id);
        userService.delUser(id);
    }
}
~~~

本文项目地址：