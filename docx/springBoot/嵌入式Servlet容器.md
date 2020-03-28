## SpringBoot嵌入式Servlet配置原理

### SpringBoot修改服务器配置

- 配置文件方式方式修改，实际修改的是ServerProperties文件中的值

```properties
server.servlet.context-path=/crud
server.port=8081
```

- `Java`代码方式修改。通过实现`WebServerFactoryCusomizer`接口来获取到达`ConfigurableServletWebServerFactory`的通道，`ConfigurableServletWebServerFactory`中提供了很多的方法用来修改服务器配置。

```java
@Component
public class ServletHandler implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        factory.setPort(8083);
    }
}
```

### SpringBoot使用原生web组件

>在之前的Web项目中，我们会通过web.xml来注册三大组件，在springboot中我们通过提供的类注册三大组件

- Servlet。通过`ServletRegistrationBean`来注册一个`Servlet`

```java
@Bean
    public ServletRegistrationBean myServlet(){
        ServletRegistrationBean registration = new ServletRegistrationBean(new MyServlet(),"/hello");
        return registration;
    }
```

- Filter。通过`FilterRegistrationBean`来祖册`Filter`

```java
@Bean
    public FilterRegistrationBean myFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new MyFilter());
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/hello"));
        return filterRegistrationBean ;
    }
```

- Listener。通过`ServletListenerRegistrationBean`来注册一个监听器

```java
@Bean
    public ServletListenerRegistrationBean myServletListener(){
        ServletListenerRegistrationBean registrationBean = new ServletListenerRegistrationBean();
        registrationBean.setListener(new MyServletContextListener());
        return registrationBean ;
    }
```

### Spring使用其他服务器

>SpringBoot提供了三个服务器工厂，Tomcat，Jetty，Undertow，默认使用了Tomcat

- 使用Jetty。需要排除Tomcat依赖

```xml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                    <groupId>org.springframework.boot</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <artifactId>spring-boot-starter-jetty</artifactId>
            <groupId>org.springframework.boot</groupId>
        </dependency>
```

- 使用`Undertow`服务器。同Jetty一样

```xml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                    <groupId>org.springframework.boot</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <artifactId>spring-boot-starter-undertow</artifactId>
            <groupId>org.springframework.boot</groupId>
        </dependency>
```

### SpringBoot服务器自动配置原理

- `Springboot`通过`WebServerInitializedEvent`来实现服务器自动配置，通过这个类来加载一个`WebServer`

```java
public abstract class WebServerInitializedEvent extends ApplicationEvent {
    protected WebServerInitializedEvent(WebServer webServer) {
        super(webServer);
    }
```

- 通过`WebServer`来创建固定的服务器。

  - `TomcatWebServer`

  - `JettyWebServer`
  - `NettyWebServer`
  - `UndertowWebServer`

```java
public interface WebServer {
    void start() throws WebServerException;

    void stop() throws WebServerException;

    int getPort();
}
```

### SpringBoot启动Tomcat服务器的过程

-  `SpringBoot`启动方法

```java
SpringApplication.run(DemoApplication.class, args)
```

- 调用`SpringAllication.run`方法返回了`ConfigurableApplicationContext`对象

```java
 public ConfigurableApplicationContext run(String... args) {
     context = this.createApplicationContext();//创建了一个Application对象
     this.refreshContext(context);//刷新ApplicationContext
```

```java
protected ConfigurableApplicationContext createApplicationContext() {
        Class<?> contextClass = this.applicationContextClass;
        if (contextClass == null) {
            try {
                switch(this.webApplicationType) {
                case SERVLET:
                    contextClass = Class.forName("org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext");
                    break;
                case REACTIVE:
                    contextClass = Class.forName("org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext");
                    break;
                default:
                    contextClass = Class.forName("org.springframework.context.annotation.AnnotationConfigApplicationContext");
                }
            } catch (ClassNotFoundException var3) {
                throw new IllegalStateException("Unable create a default ApplicationContext, please specify an ApplicationContextClass", var3);
            }
        }

        return (ConfigurableApplicationContext)BeanUtils.instantiateClass(contextClass);
    }
```

- 创建了`AnnotationConfigReactiveWebServerApplicationContext`这个类最终实现了`AbstractApplicationContext`

```java
private void refreshContext(ConfigurableApplicationContext context) {
        this.refresh(context);
        if (this.registerShutdownHook) {
            try {
                context.registerShutdownHook();
            } catch (AccessControlException var3) {
            }
        }

    }
protected void refresh(ApplicationContext applicationContext) {
        Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
        ((AbstractApplicationContext)applicationContext).refresh();
    }
```

```java
public void refresh() throws BeansException, IllegalStateException {
        synchronized(this.startupShutdownMonitor) {
            this.prepareRefresh();
            ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
            this.prepareBeanFactory(beanFactory);

            try {
                this.postProcessBeanFactory(beanFactory);
                this.invokeBeanFactoryPostProcessors(beanFactory);
                this.registerBeanPostProcessors(beanFactory);
                this.initMessageSource();
                this.initApplicationEventMulticaster();
                //调用子类的刷新方法,最终调用的是创建ApplicationContext容器中所选择的容器即ServletWebServerApplicationContext类中的方法
                this.onRefresh();
                this.registerListeners();
                this.finishBeanFactoryInitialization(beanFactory);
                this.finishRefresh();
            } catch (BeansException var9) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
                }

                this.destroyBeans();
                this.cancelRefresh(var9);
                throw var9;
            } finally {
                this.resetCommonCaches();
            }

        }
    }
```

```java
protected void onRefresh() {
        super.onRefresh();

        try {
            //创建了web容器
            this.createWebServer();
        } catch (Throwable var2) {
            throw new ApplicationContextException("Unable to start web server", var2);
        }
    }
```

```java
private void createWebServer() {
        WebServer webServer = this.webServer;
        ServletContext servletContext = this.getServletContext();
    //当容器中没有服务器的时候
        if (webServer == null && servletContext == null) {
            //创建一个web服务器，
            ServletWebServerFactory factory = this.getWebServerFactory();
            this.webServer = factory.getWebServer(new ServletContextInitializer[]{this.getSelfInitializer()});
        } else if (servletContext != null) {
            try {
                this.getSelfInitializer().onStartup(servletContext);
            } catch (ServletException var4) {
                throw new ApplicationContextException("Cannot initialize servlet context", var4);
            }
        }

        this.initPropertySources();
    }
```

```java
protected ServletWebServerFactory getWebServerFactory() {
    //获取了容器中ServletWebServerFactory类型的容器
        String[] beanNames = this.getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
        if (beanNames.length == 0) {
            throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to missing ServletWebServerFactory bean.");
        } else if (beanNames.length > 1) {
            throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to multiple ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
        } else {
            //创建了web服务器
            return (ServletWebServerFactory)this.getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
        }
    }
```



- 通过`this.getWebServerFactory`方法创建了web服务器，通过`this.getBeanFactory()`获取了容器中所存在的类型为`ServletWebServerFactory`类型的容器，然后获取`bean`创建了`Tomcat`对象