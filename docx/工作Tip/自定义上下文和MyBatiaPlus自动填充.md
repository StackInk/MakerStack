兄弟们好，今天和大家分享一些自己在工作中的一些小的 Tips 。算的上一些小的经验了，这个专栏会持续更新下去。

今天主要有两个重要的点：1，自定义一个上下文，例如 Spring Security 的用户信息上下文；2，MyBatis Plus 自动插入更新能力

### 1. 环境上下文

#### 1.1 什么是上下文？

在生活中，我们处理事情的时候，首先要了解背景，然后才能看事情产生的原因和事情产生的后果，最后得出一个解决方案。这个案例里面我们针对的是事情，即整个思考和处理的过程就是一个环境，而背景和事情后果就是一个上下文。

再比如，在进行语文课文的学习过程中，单独看一个段落无法明白主人公要为什么做这件事情，于是我们需要了解上一个段落的内容。即此时上一段落就是一个上下文内容。

在程序中，线程和进程也存在上下文，这个上下文信息包含了当前线程或者进程在执行的前后信息，方便当 CPU 调度执行的时候恢复线程执行状态。

即**上下文就是一个保存了很多的信息的容器**

#### 1.2 框架中的上下文

>  Spring Security：一个权限控制的框架，集成了登陆、授权和鉴权的功能

Spring Security 中存在一个 `SecurityContextHolder` 对象，它可以通过 `getContext` 方法获取当前线程执行的上下文对象，它的实现很简单，当线程开始处理请求之前，将用户数据写入到 SecurityContextHolder 对象中，之后当前对象对于当前线程的整个生命周期中都存在，而实现这一点就是通过 `ThreadLocal` 实现的（ [ThreadLocal 文章讲解看这里](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484744&idx=1&sn=641c5b2c2261fe7a82bd314f724deada&chksm=fe541ab5c92393a377edbb4be3b80d028f1228589af584892d9bbb850f381e74ddcbdb9a704b&token=799264124&lang=zh_CN#rd)）。当请求结束以后，将写入的这个变量移除即可。这就是整个 Spring Security 实现用户信息上下文的原理。我们来看一下具体的时序图：



在来看一下具体的源码：

- SecurityContext 上下文对象

```java
public interface SecurityContext extends Serializable {
    Authentication getAuthentication();

    void setAuthentication(Authentication var1);
}
```

- 拦截器

```java
// 每一次请求都会访问 userDetailsService 接口的方法，获取用户信息
UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
// 装饰用户信息
TokenBasedAuthentication authentication = new UsernamePasswordAuthenticationToken(userDetails);
// 设置到上下文中
SecurityContextHolder.getContext().setAuthentication(authentication);
```





> MDC：log4j提供的一个日志记录的上下文

企业在做日志收集的时候，往往会通过一个 ID 标识一个请求的所有执行日志信息，方便后期进行日志回溯。这就是一个简单的单机版链路追踪。

log4j 提供了一个全局的上下文 MDC ，我们可以通过过滤器或者拦截器在执行具体的业务代码给 MDC 中写入一些信息，比如请求的用户名和日志 ID 。来标识当前请求。同样在请求结束之前，将对应的信息移除掉。

>自定义上下文

可以对比上面的案例，可以发现，自定义上下文，仅仅需要在线程执行业务之前，将自己需要使用的信息，写入到 ThreadLocal 中即可，当线程访问以后，将 ThreadLocal 中的数据清除即可。

#### Spring 上下文

在进行工具类封装的时候，我们往往会使用到 Spring 中的类，这个时候会存在两种解决方法：

- 将工具类写入到 Spring 容器中
- 通过一个上下文对象，获取指定的对象

第一种方式，违背了制作工具类的初衷，在具体的业务中，往往会通过上下文获取 Spring 中的 Bean 。这样的处理方式也可以让我们在使用工具类的时候直接静态调用。

我们就以上述的全局 Spring 上下文对象来制作一个上下文对象。先来看一下我们需要准备什么

- 获取 Spring 本身的上下文容器对象。通过实现 ApplicationContextAware 接口获取
- 创建一个函数，传入一个 Class 类型或者 ClassName 返回一个 Bean 对象
- 创建一个函数，当 Spring 销毁的时候，自动将上下文中的数据销毁，通过实现 DisposableBean 接口实现

```java
@Component
public class SpringCtx implements ApplicationContextAware, DisposableBean {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void destroy() throws Exception {
        SpringCtx.applicationContext = null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringCtx.applicationContext = applicationContext;
    }


    public <T> T getBean(Class<T> classOf) {
        return applicationContext.getBean(classOf);
    }
    
    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

}
```

当我们使用工具类的时候，可以通过 SpringCtx 对象直接获取 Spring 容器中对象

#### LogId 上下文

链路追踪是微服务体系中重要的一环，当我们需要查看一个请求所有的执行路径就可以采用 LogId 的方式，在请求进入的时候，生成一个 LogId ，之后的日志都将合格 ID 打印，之后查询日志的时候，就可以通过这个 LogId 看到他所有的执行路径。

我们先看一下我们需要准备什么

- 在请求进入的时候，生成一个 ID ，通过 Java 的 UUID 生成
- 将日志 ID 写入到 日志的配置文件中， Log4j 提供了 MDC 可以在业务中写入变量，在日志文件中读取。

```java
```

