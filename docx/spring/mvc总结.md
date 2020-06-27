## MVC总结

### 1. 概述

还是之前的三个套路

#### 1.1 是什么？

`Spring`提供一套视图层的处理框架，他基于`Servlet`实现，可以通过`XML`或者注解进行我们需要的配置。

他提供了拦截器，文件上传，`CORS`等服务。

#### 1.2 为什么用？

原生`Servlet`在大型项目中需要进过多重封装，来避免代码冗余，其次由于不同接口需要的参数不同，我们需要自己在`Servlet`层 封装我们需要的参数，这对于开发者来说是一种重复且枯燥的工作，于是出现了视图层框架，为我们进行参数封装等功能。让开发者的注意力全部放在逻辑架构中，不需要考虑参数封装等问题。

#### 1.3 怎么用

再聊怎么用之前，我们需要了解一下`MVC`的工作原理。

**他基于一个`DispatcherServlet`类实现对各种请求的转发，即前端的所有请求都会来到这个Servlet中，然后这个类进行参数封装和请求转发，执行具体的逻辑。**(第二章我们细聊)

##### 1.3.1 XML

- 根据上面的原理，我们需要一个`DispatcherServlet`来为我们提供基础的`Servlet`服务，我们可以通过`servlet`规范的`web.xml`文件，对该类进行初始化。并且声明该类处理所有的请求，然后通过这个类实现请求转发。
- 另外，我们还需要一个配置文件，用来配置我们需要的相关的`mvc`信息。

**下面来看一个完整的`web.xml`配置**

```xml
<web-app>

    <servlet>
    <servlet-name>dispatchServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:springmvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>dispatchServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>

</web-app>
```

##### 1.3.2 注解

注解方式也是现在主流，`SpringBoot`基于`JavaConfig`实现了自动配置

**实现方式：**

在`Servlet3.0`的时候定义了一个规范`SPI`规范。

`SPI `，全称为 `Service Provider Interface`，是一种服务发现机制。它通过在`ClassPath`路径下的`META-INF/services`文件夹查找文件，自动加载文件里所定义的类。**也就是在服务启动的时候会Servlet会自动加载该文件定义的类**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200614173148.png)

我们看一眼这个文件里的内容。他内部定义了`SpringServletContainerInitializer`容器初始化类，也就是说在`Servlet`启动的时候会自动初始化这个类，这个类也是注解实现的关键。

这个类中存在一个`onStartup`方法，这个也是当容器初始化的时候调用的方法，这个方法有两参数

- `Set<Class<?>> webAppInitializerClasses`他代表了当前我们的`Spring`容器中存在的`web`初始化类。我们自己可以通过实现`WebApplicationInitializer`类来自定义`Servlet`初始化的时候执行的方法。
- `ServletContext servletContex`代表了`Servlet`上下文对象

```shell
org.springframework.web.SpringServletContainerInitializer
```

```java

@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {
    @Override
	public void onStartup(Set<Class<?>> webAppInitializerClasses, 		
 ServletContext servletContext)    throws ServletException {
        //启动逻辑
    }
}
```

**具体看一下注解配置方式：**

```java
public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletCxt) {

        // Load Spring web application configuration
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        //一个配置类，@Configuration
        ac.register(AppConfig.class);
        //spring的那个refresh方法
        ac.refresh();

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(ac);
        ServletRegistration.Dynamic registration = servletCxt.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/app/*");
    }
}
```

通过实现`WebApplicationInitializer`接口，来作为`MVC`的配置类，在加载`SpringServletContainerInitializer`的时候加载这个类。

****

不过在具体的实现中，`Spring`不建议我们这样做，**他建议将`Spring`和`SpringMvc`分开,看个图**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200616160321.png)

**他在Spring之上加了一层Web环境配置。相当于在Spring的外面包装了一层`Servlet`**

看一下此时的代码

```java
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
    //Spring配置文件
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { RootConfig.class };
    }

    //SpringMVC的配置文件
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { App1Config.class };
    }
	
    //指定DispatcherServlet可以拦截的路径
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/app1/*" };
    }
}
```

通过`AbstractAnnotationConfigDispatcherServletInitializer`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200616161213.png)

可以看到他实现了`WebApplicationInitializer`接口，即在`Servlet`初始化的时候会加载这个类。

`AbstractContextLoaderInitializer`类，他初始化了`Spring`

`AbstractDispatcherServletInitializer`类，初始化了`DispatcherServlet`

`AbstractAnnotationConfigDispatcherServletInitializer`类，将两个类整合到一起

### 2. 实现原理

聊这个原理之前，先来聊聊他要干什么？

**需求：请求分发；参数封装；结果返回**

那如果我们自己来实现，该怎么办？(**单说注解，先来看看我们怎么使用`MVC`的**)

- 一个`@Controller`注解，标识当前类为控制层接口，
- 一个`RequestMapping`标识这个方法的`URI`和请求方式等信息
- 一个`@ResponseBody`标识这个方法的返回类型为`JSON`
- 一个`test01`标识这个方法用来处理`/test`请求

```java
@Controller
public class UserController {

    @GetMapping("/test")
    @ResponseBody
    public String test01(){
        return "success" ;

    }
}
```

**接下来，我们通过我们已有的东西，看一下我们自己去处理请求的逻辑**

先来想一下我们的请求过程：

- 前端发送一个`Http`请求，通过不同的`uri`实现不同逻辑的处理
- 而这个`uri`和我们后端的定义的`@RequestMapping`中的`value`值相同
- 即我们可以通过一个`Map`结构，将`value`作为`key`，将`method`的`Class`对象作为一个`value`存到一个`MappingRegister`中
- 请求来了以后，通过`URI`从这个`Map`中获取相应的`Method`执行，如果没有对应的`Method`给一个`404`.

#### 2.1 Spring加载

在上面的**怎么用**中提到了，他通过`AbstractContextLoaderInitializer`来加载`Spring`配置文件的。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200616175042.png)

**此时关于Spring的东西已经加载好了，但并未进行初始化**

#### 2.2 MVC加载

同样也是通过`AbstractDispatcherServletInitializer`类实现

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200616180615.png)

##### 2.2.1 DispatcherServlet

接下来我们具体看一下在这个期间，`DispatcherServlet`如何处理请求的

**作用：分发所有的请求**

**类继承结构图**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200614175426.png)

可以看到他继承了`HttpServlet`类，属于一个`Servlet`，而在之前我们配置了这个`Servlet`的拦截路径。他会将所有的请求拦截，然后做一个分发。

**下面这个图各位看官应该非常熟悉：**

![](https://gitee.com/onlyzl/image/raw/master/img/20200627145457.png)

其实`DispatcherServlet`处理所有请求的方式在这个图里完全都体现了。

接下来聊一下他的设计思路吧。

**当一个请求来的时候，进入`doDispatch`方法中，然后处理这个请求，也是返回一个执行链**

`Spring`提供了三种方式的**处理器映射器**来处理不同的请求。

- `BeanNameUrlHandlerMapping`处理单独`Bean`的请求。适用于实现`Controller`和`HttpRequestHandler`接口的类

```java
@Component("/test02")
public class HttpController  implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("HttpController执行");
        return null;
    }
}
```

```java
@Component("/test01")
public class HandlerController implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("handlerRequest");
    }
}

```

- `RequestMappingHandlerMapping`适用于方法类型的处理器映射。

```java
@Controller
public class UserController {

    @GetMapping("/test")
    public String test01(){
        System.out.println("执行了");
        return "success" ;
    }
}
```

- `RouterFunctionMapping`,`MVC`提供的一个处理通过函数式编程定义控制器的一个映射器处理器。需要直接添加到容器中，然后 通过路由一个地址，返回对应的数据

```java
@Configuration
@ComponentScan("com.bywlstudio.controller")
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/pages/",".jsp");
    }

    @Bean
    public RouterFunction<?> routerFunctionA() {
        return RouterFunctions.route()
                .GET("/person/{id}", request1 -> ServerResponse.ok().body("Hello World"))
                .build();
    }

}
```

聊完了处理器映射器，再来聊一下**处理器适配器**

不同的请求方式，需要不同的处理方式，这也是`Spring`为什么要提供一个适配器的原因。

- `RequestMappingHandlerAdapter`用来处理所有的方法请求，即通过`@Controller`注解定义的
- `HandlerFunctionAdapter`用来处理函数式的映射，即通过`RouterFunctionMapping`定义的
- `HttpRequestHandlerAdapter`用来处理实现了`HttpRequestHandler`接口的
- `SimpleControllerHandlerAdapter`用来处理实现了`Controller`接口的请求

通过处理器适配器拿到适合的处理器，来处理对应的请求。

在处理器执行具体的请求的过程，**实际上就是调用我们的方法的过程，于是就会出现返回值**

**通常对于返回值我们有两种方法：**

- `@ResponseBody`直接返回`JSON`数据。
- 或者返回一个视图，该视图会被视图解析器解析。

**对于返回值解析，`MVC`提供了一个接口用于处理所有的返回值，这里我们仅仅谈上面的两种**

- `ModelAndViewMethodReturnValueHandler`用于处理返回视图模型的请求
- `RequestResponseBodyMethodProcessor`用于处理返回`JSON`

在我们拿到方法返回值以后，会调用`this.returnValueHandlers.handleReturnValue`返回值解析器的这个方法，用于对视图模型的返回和`JSON`数据的回显（**直接回显到网页，此时返回的视图对象为null**）

对于视图对象，通过视图解析器直接解析，进行数据模型渲染，然后回显给前端。

##### 2.2.2  MappingRegistry

这个类存放了`method`的映射信息。

```java
class MappingRegistry {

   private final Map<T, MappingRegistration<T>> registry = new HashMap<>();

   private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();

   private final MultiValueMap<String, T> urlLookup = new LinkedMultiValueMap<>();

   private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();

   private final Map<HandlerMethod, CorsConfiguration> corsLookup = new ConcurrentHashMap<>();

   private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
```

`MVC`会从这个类中获取方法和`URL`的引用。相当于`Spring MVC`的容器。

### 3. 面试题

#### 3.1 什么是MVC？什么是MVVM?

答：`MVC`是一个架构模式，它有三个核心

- 视图(View)。用户界面
- 模型(Model)。业务数据
- 控制器(Controller)。接收用户输入，控制模型和视图进行数据交互

`MVVM`也是一种架构模式，它也是三个核心

- 模型(`Model`)。后端数据
- 视图模型(`ViewModel`)。它完成了数据和视图的绑定
- 视图(`View`)。用户界面

它的核心思想是：**通过`ViewModel`将数据和视图绑定，用数据操作视图，常见框架为Vue**

#### 3.2 Spring Mvc执行流程

- 用户发送请求至`DispatcherServlet`
- `DispatcherServelt`收到请求以后调用`HandlerMapping`，找到请求处理器映射器（**三选一**）
- 通过处理器映射器对应`URI`的处理器执行链（包含了拦截器，和处理器对象）
- 调用处理器适配器，找到可以处理该执行链的处理器(**四选一**)
- 处理器具体执行，返回`ModelAndView`对象
  - 如果存在`@ResponseBody`注解，直接进行数据回显
- 将返回的`ModelAndView`对象传给`ViewResove`视图解析器解析，返回视图
- `DispatcherServlet`对`View`进行渲染视图
- 响应用户

