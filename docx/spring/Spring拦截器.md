## Spring拦截器

### 1.拦截器与过滤器的区别

#### 1.1 JavaWeb三大组件

>javaWeb有三大组件，分别是：servlet，Filter，Listener

##### 1.1.1 Servlet

**广义：**Servlet是一个运行在web服务器或者应用服务器上的一个应用程序，用来动态处理客户端请求的资源。

**狭义:**Servlet是一个继承了GenericServlet类的子类。该类中的service方法用来处理相关的资源请求。

##### 1.1.2 Listener

在Servlet规范中定义了多种类型的监听器。主要是三种域对象的监听`ServletContext`,`HttpSession`,`HttpRequest`，根据其功能可以划分为两类

- 三个域对象的销毁与创建
  - ServletContextListener
  - HttpServletListener
  - ServletRequestListener
- 域对象中的属性的变化
  - ServletContextAttributeListener
  - HttpServletAttributeListener
  - ServletRequestAttributeListener

#####  1.1.3 Filter

Filter和Servlet类似。Servlet用来处理请求，而Filter用来拦截和放行请求。

作用：

- 在执行请求之前执行一段代码
- 是否让客户端访问目标资源
- 调用目标资源以后执行一段代码(通过生命周期函数完成)

Filter存在四种拦截方式

- REQUESR 默认值，代表直接访问某个servlet
- ERROR 发生错误时进行跳转
- INCLUDE 包含资源时执行filter
- FORWARD 转发时执行filter

#### 1.2 Spring拦截器

spring拦截器是spring Aop的一种应用，在不修改源码的情况下，执行一段代码，以增强现有方法。

**实现方式：**

通过实现`HandlerInterceptor`接口,重写内部的三个方法

- preHandler。在访问Controller之前执行，可以用来拦截请求
- postHandler。在视图跳转或数据返回之前执行(return之前)
- afterCompletion。在视图加载完成或数据返回完成以后执行

#### 1.3区别

| 场景             | Filter                        | Interceptor                        |
| ---------------- | ----------------------------- | ---------------------------------- |
| 执行机制         | 函数回调                      | Java反射                           |
| 执行场景         | 几乎所有的servlet请求都可使用 | 只能使用在spring定义的controller中 |
| 环境依赖         | 依赖servlet环境               | 依赖spring环境                     |
| 是否可以使用Bean | 不可以                        | 可以                               |

#### 1.4过滤器和拦截器的执行步骤

![](https://gitee.com/onlyzl/image/raw/master/img/拦截器执行步骤.png)

## 2.SpringBoot使用拦截器

>SpringBoot对MVC进行了自动配置，当需要使用拦截器的时候，需要在WebMvcConfigurer实现类中添加自定义的拦截器，并定义拦截路径和排除路径

```java
//自定义的拦截器，未登陆的用户直接转发到登陆界面
public class MyInterceptorResolve implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object loginStatus = request.getSession().getAttribute("loginStatus");
        if(loginStatus !=null ){
            return true ;
        }else{
            request.setAttribute("msg","权限不足请先登陆");
            request.getRequestDispatcher("/index").forward(request,response);
            return false ;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
```

添加到容器中

```java
@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyInterceptorResolve()).addPathPatterns("/**").excludePathPatterns("/index","" +
                "/index.html","/login.html","/user/login","/main");
    }
```

























