## Spring Boot入门

## 一、第一个Spring Boot程序

### 1.导入springBoot 依赖

```xml
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.9.RELEASE</version>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
```

### 2.创建包,控制器和主程序入口

![](https://gitee.com/onlyzl/image/raw/master/img/1577496649481.png)

```java
@SpringBootApplication
public class MainSpringBoot {
    public static void main(String[] args) {
        SpringApplication.run(MainSpringBoot.class,args);
    }
}
```

### 3.运行这个`main`方法访问控制层映射

***第一个SpringBoot程序结束***

## 二、解析入门程序

### 1.POM文件解析

#### 1.1 **spring Boot 依赖存在父依赖**

```xml
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.9.RELEASE</version>
</parent>
<!--上面的父项目又依赖下面的父项目-->
<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-dependencies</artifactId>
		<version>1.5.9.RELEASE</version>
		<relativePath>../../spring-boot-dependencies</relativePath>
</parent>
```

​		第一个父项目用来加载Spring Boot启动需要加载的插件和资源文件

​		第二个父项目用来加载所有的依赖版本，所以每次导入依赖的时候不需要书写版本号，spring boot默认会导入这个父项目中的版本。如果当前依赖在spring boot中没有进行依赖版本的控制，则需要书写版本号

#### 1.2spring boot启动器

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
</dependencies>
<!--上面的依赖依赖下面的父项目-->
<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starters</artifactId>
		<version>1.5.9.RELEASE</version>
</parent>
```

* `spring-boot-starter-web`导入了一系列的web应用可以使用到的依赖

* `spring-boot-starters`顾名思义，starters使用了复数，即为springboot启动器的合集
* 这个启动器的合集中包含了所有可能需要用到的启动器，而启动器内部配置了所有的该工程下可能需要的所有依赖
  * 这个启动器依赖了`spring-boot-starter-parent`然后通过这个依赖获取了所有可能使用到的依赖的版本

### 2.解析主启动类

#### 2.1 @SpringBootApplication注解

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
```

##### 2.1.1@SpringBootConfiguration注解（声名这个类是一个springBoot配置类）

* 基于Spring的注解@Configuration实现

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
```

##### 2.1.2@EnableAutoConfiguration注解（实现自动配置）

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({EnableAutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
```

* @AutoConfigurationPackage注解（实现自动包导入）

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({Registrar.class})
public @interface AutoConfigurationPackage {
```

类`Registrar`是抽象类`AutoConfigurationPackages`的静态内部类，在`Registrar`中调用`registerBeanDefinitions`，该方法内部又调用`AutoConfigurationPackages`的另一个静态内部类`PackageImport`执行包导入。

debug模式下的运行结果为：我们自己的主程序入口的所在包，即**springboot会扫描当前主程序所在包及其子包的所有文件**

![](https://gitee.com/onlyzl/image/raw/master/img/1577502122131.png)

* @Import({EnableAutoConfigurationImportSelector.class})
  * `EnableAutoConfigurationImportSelector`继承`AutoConfigurationImportSelector`类，其父类存在一个`selectImports`方法。
  * 该方法通过执行`this.getCandidateConfigurations(annotationMetadata, attributes);`获得相应的配置信息
  * 在`getCandidateConfigurations`方法内部，通过`SpringFactoriesLoader.loadFactoryNames`加载配置文件，最后作为一个list集合返回。具体加载的配置文件为`META-INF/spring.factories`

![](https://gitee.com/onlyzl/image/raw/master/img/1577503348805.png)

这是所有的自动导入的配置。有了这些自动导入，所以我们在使用的时候就不需要自己去配置，直接使用相应的功能即可。

***

> ​	最新版本的springboot的@EnableAutoConfiguration注解发生了一些变化，不过和之前版本几乎相同。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({AutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
```

***直接导入了`AutoConfigurationImportSelector.class`类，并且内部导入的组件从96增加到了124个***

#### 2.2`Spring-boot-autoconfigure`jar包

该包下的`META-INF/maven/spring.factories`文件中存放了所有的自动导入的配置

## -三、SpringBoot配置文件

### 1.YML格式配置文件

>YML通过使用空格和缩进来决定层级关系。相比于XML和JSON格式来说可以更好的作为配置文件

```yml
person:
	name: 张三
	sex: 男
```

等价于

~~~xml
<person>
	<name>张三</name>
    <sex>男</sex>
</person>
~~~

#### 1.1YML语法格式

`属性:空格值`

下面是YML对应的多种数据类型的写法

```yml
person:
	name: 张三
	age: 12
	accounts: [a1,a2,a3]
	tels: 
		- 123456
		- 456789
		- 789798
	dogs: {frist:'12\n12',second:"12\n12"}
	cats:
		frist: 12
		second: 13
	brithday: 2018/12/8
	sex: true
```

**注：**

- 单引号里面的\会被转义，所以按照特殊的字符进行输出。即输出为frist=12\n12
- 双引号里面的\不会被转一，所以按照原来的字符输出。即输出为second=12换行12
- 对象，Map都是键值的方式，所在在YML配置文件中的写法相同
- 数组，List，Set的写法相同

### 2.在springboot中注入YML配置文件

通过使用`@ConfigationProperties(prefix='属性名')`注解，参数指定需要注入的属性名。即上面YML数据的person

**注：使用`@ConfigationProperties`注解的时候需要指定该Bean为容器中的组件，否则该注解无作用。**

### 3.Properties和YML配置文件的区别

| 使用场景       | @ConfigrationProperties | @Value |
| -------------- | ----------------------- | ------ |
| 松散绑定       | 支持                    | 不支持 |
| SPEL           | 不支持                  | 支持   |
| JSR303数据校验 | 支持                    | 不支持 |
| 复杂类型注入   | 支持                    | 不支持 |

- 松散绑定指定是，frist-name(配置文件中的属性)和fristName(Bean中属性)。这种对应关系中，只有YML配置文件才可以读取到配置信息，properties文件读取不到
- SpEL指SpringEL表达式
- JSR303数据校验在springboot中，通过`@Validated`注解实现，指定该类需要进行数据校验，具体实现通过`@Email`,`@Min`,`@Max`等注解实现
- 复杂类型指List，Set，Map等类型的注入

注：两者的使用场景，YML适用于批量注入数据；Properties适用于单个数据的注入



### 4.`@PropertiesSource`和`@ImportResource`注解

#### 4.1@PropertiesSource注解

**出现背景：**由于默认的springboot项目仅仅可以存在两个配置文件（application.properties和application.yml）,而为了可读性，所以会将不同功能的配置文件分开。

**作用：**导入一个或多个配置文件

**使用方法：**@PropertiesSource(value={"classpath:person.properties","classpth:person.yml"})，之后通过@Value或者@ConfigationProperties注解注入对应值

#### 4.2@ImportResource注解

**出现背景：**在之前的SSM项目整合的时候通常会使用xml进行配置，而springboot默认不读取xml文件，所以如果想要编写xml文件并且在springboot项目中使用的话，就需要使用@ImportResource注解

**作用：**导入一个或多个配置文件，将在xml中配置的信息导入到spring组件中

**使用方式：**@ImportResource（{"classpath:bean.xml"}）

### 5.配置文件占位符

#### 5.1随机数

```yml
${random.uuid},${random.int},${random.int(10,20)}
```

#### 5.1获取之前配置文件的值

```properties
person.frist-name=123
person.last-name=123
person.sex=${person.frist-name:1}
```

注：通过$(frist-name:默认值)来为这个属性指定一个默认值

### 6.Profiles文件

>在实际开发中，不同的生产环境所需要的配置也不相同，所以需要多种配置文件去对不同的环境进行适应

#### 6.1properties文件

profiles文件的格式：application-profiles.properties

例：application-dev.properties或者application-pro.properties等

在主配置文件中，通过spring.profiles.active=dev来激活对应的配置

#### 6.2yml文件

yml文件支持多文档，通过`---`来分割不同的文档

```yml
server:
  port: 8082
  servlet:
    context-path: /demo2
spring:
  profiles:
    active: dev
---
spring:
  profiles: dev

---
spring:
  profiles: pro
```

#### 6.3激活对应的配置文件

* 在主配置文件中激活
* 在命令行参数中激活 `java -jar spring…….jar --spring.profiles.active=dev`此时的配置环境即为dev环境
* 添加虚拟机参数，`-Dspring.profiles.active=dev`

### 7.配置文件加载顺序

> ​	配置文件的加载存在四种加载方式，可以通过不同的配置文件的加载时间，来对项目进行不同环境的配置和升级

* 当前项目的根目录下的conig文件夹
* 当前项目的根目录下
* 当前项目的资源文件下的config文件夹
* 当前项目的资源文件下

以上的顺序即为springboot 加载配置文件的顺序

#### 7.1加载外部的配置文件

在命令行中通过`java -jar spring…….jar --spring.config.loation=盘符`

#### 7.2外部配置文件加载顺序

1. 命令行参数方式。

   java -jar spring……jar --server.port=8082 --server.servlet.context-path=/abc --spring.config.location=盘符

2. 来自java:comp/env的JNDI属性

3. Java系统属性

4. 操作系统环境变量

5. RandomValuePropertySource配置的random.*属性值

6. **jar包外部的application-{profile}.properties或者application.yml(带spring.profile)配置文件**

   *和jar包在同一路径下的application.properties文件*

7. **jar包内部的application-{profile}.properties或application.yml(带spring.profile)配置文件**

8. **jar包外部的application.properties或application.yml(不带spring.profile)配置文件**

9. **jar包内部的application.properties或application.yml(不带spring.profile)配置文件**

10. @Configuration注解类上的@PropertySource

### 8.配置文件书写方式

>在上面讲解`springBoot`主配置类的时候，提到了`@EnableAutoConfiguration`注解。这个注解给spring容器中导入了一些组件，这些组件来源于`spring-boot-autoconfigure-2.2.2.RELEASE.jar!\META-INF\spring.factories`文件，总共有124个

#### 8.1自动配置原理

现在我们刨析一个组件的源码，看一下springboot是如何进行配置的。

```java
@Configuration(
    proxyBeanMethods = false
)
@EnableConfigurationProperties({HttpProperties.class})
@ConditionalOnWebApplication(
    type = Type.SERVLET
)
@ConditionalOnClass({CharacterEncodingFilter.class})
@ConditionalOnProperty(
    prefix = "spring.http.encoding",
    value = {"enabled"},
    matchIfMissing = true
)
public class HttpEncodingAutoConfiguration {
```

- `@EnableConfigurationProperties`注解导入了一个`HttpProperties`类

```java
@ConfigurationProperties(
    prefix = "spring.http"
)
public class HttpProperties {
    public static class Encoding {
        public static final Charset DEFAULT_CHARSET;
        private Charset charset;
        private Boolean force;
        private Boolean forceRequest;
        private Boolean forceResponse;
        private Map<Locale, Charset> mapping;
```

这个类导入了配置文件spring.http，也就是说我们在自己的配置文件中可以通过spring.http为HttpProperties中的属性赋值，所以为Encoding中的属性赋值的时候需要通过，spring.http.encoding.charset=UTF-8的方式，来进行赋值。

- `@ConditionalOnClass`注解都是由`@Conditional`注解演变过来的

| @Conditional扩展注解            | 作用（判断是否满足当前指定条件）                 |
| ------------------------------- | ------------------------------------------------ |
| @ConditionalOnJava              | 系统的java版本是否符合要求                       |
| @ConditionalOnBean              | 容器中存在指定Bean；                             |
| @ConditionalOnMissingBean       | 容器中不存在指定Bean；                           |
| @ConditionalOnExpression        | 满足SpEL表达式指定                               |
| @ConditionalOnClass             | 系统中有指定的类                                 |
| @ConditionalOnMissingClass      | 系统中没有指定的类                               |
| @ConditionalOnSingleCandidate   | 容器中只有一个指定的Bean，或者这个Bean是首选Bean |
| @ConditionalOnProperty          | 系统中指定的属性是否有指定的值                   |
| @ConditionalOnResource          | 类路径下是否存在指定资源文件                     |
| @ConditionalOnWebApplication    | 当前是web环境                                    |
| @ConditionalOnNotWebApplication | 当前不是web环境                                  |
| @ConditionalOnJndi              | JNDI存在指定项                                   |
|                                 |                                                  |

- **自动配置类会在一定的条件下才能生效**
  - 在自己的配置文件中添加***`debug=true`***属性；可以在控制台中打印自动配置报告，这样我们就可以知道哪些配置类生效了。

```java
Positive matches: //生效的自动配置类
-----------------

   AopAutoConfiguration matched:
      - @ConditionalOnProperty (spring.aop.auto=true) matched (OnPropertyCondition)

   AopAutoConfiguration.ClassProxyingConfiguration matched:
      - @ConditionalOnMissingClass did not find unwanted class 'org.aspectj.weaver.Advice' (OnClassCondition)
      - @ConditionalOnProperty (spring.aop.proxy-target-class=true) matched (OnPropertyCondition)

   DispatcherServletAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.web.servlet.DispatcherServlet' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)
          
Negative matches: //没有自动配置的
-----------------

   ActiveMQAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.jms.ConnectionFactory' (OnClassCondition)

   AopAutoConfiguration.AspectJAutoProxyingConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.aspectj.weaver.Advice' (OnClassCondition)

   ArtemisAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.jms.ConnectionFactory' (OnClassCondition)
```

## 四、Springboot日志

### 1.常见的日志框架

> ​	现在日志框架都变成了抽象层到具体实现，即现在的日志框架都提供了一层接口和一部分实现。

| 抽象层                  | 具体实现                   |
| ----------------------- | -------------------------- |
| JCL,SLF4j,jboss-logging | Log4j , JUL,Log4j2,Logback |
|                         |                            |

本次选用的日志框架为：**抽象层——SLF4J，具体实现——Logback**

**Spring**框架底层使用的日志框架是：**JCL**

**SpringBoot**框架底层使用的日志框架是：**SLF4和LogBack**

### 2.SLF4J框架的使用

#### 2.1使用方式

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorld {
  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger(HelloWorld.class);
    logger.info("Hello World");
  }
}
```

**创建日志类，传入本类的字节码文件，打印对应的日志信息**

#### 2.2各类日志框架对SLF4J的实现方式

> ​	刚刚提到目前的日志框架均采用了接口的定义方式，所以SLF4J作为接口，各个日志框架的实现均提供了实现SLF4J的中间件，在提供了[SLF4J官方网站](slf4j.org/manual.html)中，提供了一系列关于实现的具体解释

![](https://gitee.com/onlyzl/image/raw/master/img/slf4j01.png)



上图展示了，SLF4J绑定logBack,log4j,java.util.logging……等实现的具体方法，除了logBack是直接实现SLF4J框架以外，其他框架都是通过中间件的方式来实现对SLF4J的实现。

#### 2.3将非logback日志框架转换使用logback框架的方式

>之前提到，spring底层使用了JCL框架，而springBoot使用了logback框架，所以需要将JCL框架的内容调用改为logback框架

![](https://gitee.com/onlyzl/image/raw/master/img/slf4j02.png)

可以看到，图中对commons logging API , log4j等框架的处理，通过jcl-over-slf4j.jar , log4j-over-slf4j.jar 等框架来调用SLFj-api.jar ,然后调用logback框架的内容。具体可看[SLF4J官方网站](http://www.slf4j.org/legacy.html)

将系统中所有的日志框架都统一到SLF4J中。

- 将系统中其他日志框架先排除出去
- 用中间包来替换原有的日志框架
- 引入slf4j的其他实现

### 3.SpringBoot日志关系

```xml
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-logging</artifactId>
      <version>2.2.2.RELEASE</version>
      <scope>compile</scope>
    </dependency>
```

SpringBoot通过这个jar包来实现日志功能

![](https://gitee.com/onlyzl/image/raw/master/img/log01.png)

- logback-classic，log4j-to-slf4j和jul-to-slf4j均被spring-boot-starter-logging调用
- 他们三个中间件又分别调用logback-core，log4j，执行对应的日志方法
- 而logback-classic，log4j-to-slf4j和jul-to-slf4j又均实现了slf4j-api所以他们的实现相当于遵守了slf4j-api中的标准

**总结：**

1. SpringBoot底层使用的是SLF4J+logback的方式来进行记录日志
2. SpringBoot也将其他的日志转换为了SLF4J
3. 当使用其他框架的时候，需要将该框架所使用的日志框架进行移除

### 4.使用其他日志框架

> ​	上文提到，springboot的日志使用是通过`spring-boot-starter-logging`启动器来管理的，所以我们可以将spring-boot-starter-logging剔除掉

然后根据上面的2.2中图示的方法，将logback替换为Log4j，导入slf4j-log4j12的jar包，这个包依赖log4j。此时slf4j的日志实现更换为了log4j



## 五、WEB开发

### 1.简介

我们在springboot入门（一）中提到了springboot 的自动配置原理，springboot将我们所可能使用的功能场景设置了启动器，我们在使用的时候只需要导入启动器的依赖就可以使用对应的功能。



自动配置的原理：

​	spring boot提供了很多的XXXAutoConfiguration和XXXProperties来实现自动配置。

**这个自动配置也是我们使用springboot进行开发的根本方式**

### 2.Web开发配置详解

springboot进行web开发的时候，使用的自动配置类为WebMvcAutoConfigration和ResourceProperties类

#### 2.1springboot静态资源映射解析

查看`ResourceProperties`类可知，springboot默认会在一下四个路径下寻找对应的静态资源，也就是说我们在web开发的时候，可以将自己的静态资源放置在这四个路径下。

```java
classpath:/META-INF/resources/, 
classpath:/resources/, 
classpath:/static/, 
classpath:/public/
```

如何引入第三方js库文件呢？

springBoot提供了一个增加资源文件的映射器

```java
public void addResourceHandlers(ResourceHandlerRegistry registry) {
            if (!this.resourceProperties.isAddMappings()) {
                logger.debug("Default resource handling disabled");
            } else {
                Duration cachePeriod = this.resourceProperties.getCache().getPeriod();
                CacheControl cacheControl = this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
                if (!registry.hasMappingForPattern("/webjars/**")) {
                    this.customizeResourceHandlerRegistration(registry.addResourceHandler(new String[]{"/webjars/**"}).addResourceLocations(new String[]{"classpath:/META-INF/resources/webjars/"}).setCachePeriod(this.getSeconds(cachePeriod)).setCacheControl(cacheControl));
                }
                String staticPathPattern = this.mvcProperties.getStaticPathPattern();
                //静态资源映射
                if (!registry.hasMappingForPattern(staticPathPattern)) {
                    this.customizeResourceHandlerRegistration(registry.addResourceHandler(new String[]{staticPathPattern}).addResourceLocations(WebMvcAutoConfiguration.getResourceLocations(this.resourceProperties.getStaticLocations())).setCachePeriod(this.getSeconds(cachePeriod)).setCacheControl(cacheControl));
                }

            }
        }
```

springboot默认会加载资源文件下的webjars的所有文件，可以使用[webjars](https://www.webjars.org/)网站中提供的maven依赖将自己需要的库文件添加到自己的项目中去，可以看导入的库文件，其自动在/META_INF/resources文件夹下添加了webjars文件夹，并且这个文件夹下存在我们需要的库文件。



**对于我们自己的文件，我们可以将其放入springboot默认处理的静态资源文件夹下**



上面提到，既然springboot会默认访问类路径下的resources,public,static,META_INF/resources，文件夹，那么我们可以通过我们的url访问到这些资源。

> ​	localhost:8080/webjars/jquery/……  可以访问到我们导入的库文件

#### 2.2设置欢迎界面，就是index.html

```java
@Bean
public WelcomePageHandlerMapping welcomePageHandlerMapping(
    ApplicationContext applicationContext, 
    FormattingConversionService mvcConversionService, 
    ResourceUrlProvider mvcResourceUrlProvider) 
{
            WelcomePageHandlerMapping welcomePageHandlerMapping = new WelcomePageHandlerMapping(new TemplateAvailabilityProviders(applicationContext), applicationContext, this.getWelcomePage(), this.mvcProperties.getStaticPathPattern());
            welcomePageHandlerMapping.setInterceptors(this.getInterceptors(mvcConversionService, mvcResourceUrlProvider));
            return welcomePageHandlerMapping;
        }
=================================================
private Optional<Resource> getWelcomePage() {
            String[] locations = WebMvcAutoConfiguration.getResourceLocations(this.resourceProperties.getStaticLocations());//获取资源路径
            return Arrays.stream(locations).map(this::getIndexHtml).filter(this::isReadable).findFirst();
        }
=================================================
private Resource getIndexHtml(String location) {
            return this.resourceLoader.getResource(location + "index.html");
        }
```

通过getWelcomePage()方法获取对应的静态资源路径，再通过方法绑定获取改资源路径下的inder.html文件。

四个位置的优先级顺序为：

- META-INF文件夹下的Resources文件夹下的index.html文件
- Resources文件夹下的index.html文件
- static文件夹下的Index.html文件
- public文件夹下的Index.html文件

#### 2.3设置网站的图标

在上文提到的springboot默认寻找静态资源的文件夹里放置ico文件，springboot会自动读取

### 3.模板引擎

#### 3.1常见的模板引擎

JSP,ThyemLeaf,FreeMaker。这些模板引擎的原理和前端的框架实现原理相同，只不过将Vue系列的框架进行了简化，都是通过数据驱动视图更新

> springBoot推荐使用ThyemLeaf

笔者写文章的时候springboot默认对ThymeLeaf支持到了2.4.1）,如果想要使用最新版本的ThyemLeaf,需要在pom文件的Properties中修改版本

```xml
<properties>
		<thymeleaf.version>3.0.9.RELEASE</thymeleaf.version>
</properties>
```

### 3.2ThyemLeaf的使用

[ThyemLeaf官方文档](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)

```java
@ConfigurationProperties(
    prefix = "spring.thymeleaf"
)
public class ThymeleafProperties {
    private static final Charset DEFAULT_ENCODING;
    public static final String DEFAULT_PREFIX = "classpath:/templates/";
    public static final String DEFAULT_SUFFIX = ".html";
    private boolean checkTemplate = true;
    private boolean checkTemplateLocation = true;
    private String prefix = "classpath:/templates/";
    private String suffix = ".html";
    private String mode = "HTML";
```

* springboot默认使用thymeleaf来编译类路径下的template文件夹的文件，且文件后缀为html
* 通过引入thymeleaf的命名空间来得到代码提示

```html
<html lang = "en" xmlns:th="http://www.thymeleaf.org">
```

* 通过`spring.thymeleaf`命令来修改默认springboot的对thymeLeaf的配置

##### 3.2.1 JSP与ThymeLeaf的比较

***语法比较***

| JSP语法      | ThymeLeaf语法                                 |
| ------------ | --------------------------------------------- |
| jsp:include  | th:insert,th:replace                          |
| c:forEach    | th:each                                       |
| c:if         | th:if,th:unless,th:switch,th:case             |
| c:set        | th:object,th:with                             |
| 修改标签内容 | th:text(转义特殊字符)th:utext(不转义特殊字符) |

***表达式比较***

```html
- ${}表达式 : 可以用来获取对象的属性，调用方法，使用内置的域对象，使用一些工具及对象
- *{}表达式：它的功能和${}表达式相同，它是为了配合th:object="${session.user}"
		例子：
			<div th:object="session.user">
                <p th:text="*{name}"></pp>
		直接获取th:object所指向对象 的属性，不需要再写对象变量.用发
- #{}：获取国际化的内容
- @{}:定义URL
		如： @{/user/1}
```

在上述的表达式中均可以书写一系列的文本操作，三元运算，布尔运算

### 4.SpringMVC自动配置

#### 4.1 springBoot对mvc的配置

[SpringBoot对MVC 的自动配置官方文档](https://docs.spring.io/spring-boot/docs/2.2.3.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-json-gson)

- SpringBoot自动配置`ContentNegotiatingViewResolver`和`BeanNameViewResolver`两个视图解析器，相当于配置好了视图解析器

  - ContentNegotiationgViewResolver将所有的视图解析器组合到了一起，我们自定义的视图解析器也被添加到了ContentNegotiationViewResolver内部的集合中

- springboot配置了webjars

- springboot支持静态资源的获取(static/resource/public/**META-INF/resource**)

- springboot支持了`Converter`，`GenericConverter`，`Formatter`。

  - Converter 类型转化器（下图为时间转换器）

  ![](https://gitee.com/onlyzl/image/raw/master/img/dateConverter.png)

  - 可以通过查看`org.springframework.core.convert.converter.Converter`接口的实现类查看springboot提供的各种转化器支持
  - `GenericConverter`spring提供的类型转化器
  - `Formatter`提供的格式化器

  ![](https://gitee.com/onlyzl/image/raw/master/img/fomater.png)

- springboot提供了favicon.ico支持，可以直接修改网站的默认图标

- 支持配置`HttpMessageConverters`，转换Http请求和响应的

  - 用户可以通过自定义`HttpMessageConverter`来实现自己对Http请求和响应的控制
  - springboot默认将对象转化为json对象
  - 用户可以创建自己的HttpMessageConverter来自定义Http请求响应的转换，通过HttpMessageConverters的构造方法将自定义的转换类添加到容器中

```java
public HttpMessageConverters(HttpMessageConverter<?>... additionalConverters) {
        this((Collection)Arrays.asList(additionalConverters));
    }
```

#### 4.2扩展Spring MVC

1. 如果仍然想要使用springboot对mvc的默认配置，只是想扩展使用mvc的其他功能。如：拦截器，格式化器，视图转发等，可以通过构造自己的配置类，实现WebMvcConfigurer接口，重写里面的方法。但是该配置类不能被@EnableWebMvc注解修饰

   **SpringMVC中，使用xml配置视图转发,拦截器等**

   ```xml
   <mvc:view-controller path="/index" view-name="index"/>
       <mvc:interceptors>
           <mvc:interceptor>
               <mvc:mapping path="/hello"/>
               <bean></bean>
           </mvc:interceptor>
       </mvc:interceptors>
   ```

   **SpringBoot通过实现WebMvcConfigurer接口，重写内部的方法来实现**

   ```java
   //视图控制
   public void addViewControllers(ViewControllerRegistry registry) {
       registry.addRedirectViewController("/","/index");//界面跳转
       registry.addViewController("/index").setViewName("success");//视图渲染
       
       
      //拦截器
   public void addInterceptors(InterceptorRegistry registry) {
   ```

2. 保留springboot对mvc的默认配置扩展使用RequestMappingHandlerMapping, RequestMappingHandlerAdapter, or ExceptionHandlerExceptionResolver,组件，可以使用接口WebMvcRegistrations来自定义组件解析

   ```java
   public class MappingConfigurater implements WebMvcRegistrations {
       @Override
       public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
           return null;
       }
   
       @Override
       public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
           return null;
       }
   
       @Override
       public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
           return null;
       }
   }
   ```

   

3. 如果你想要自己完全控制springmvc ，你必须创建自己的配置文件，并且该主配置类被@EnableWebMvc修饰，才可以使用

>SpringBoot存在很多XXXConfigurter然用户进行扩展配置
>
>存在XXXCustomizer自定义配置

### 5.RestFulCRUD实现

#### 5.1配置主页面显示

```java
public class Redirectconfigurer implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index").setViewName("login");
        registry.addViewController("/*").setViewName("login");
    }
}
```

#### 5.2国际化处理

>为了在不同得语言环境下使用不同得语言提示，所以引入国际化处理

SpringBoot提供了国际化处理的类

```java
public class MessageSourceAutoConfiguration {
    @Bean
    public MessageSource messageSource(MessageSourceProperties properties) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        if (StringUtils.hasText(properties.getBasename())) {
            messageSource.setBasenames(StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
        }

        if (properties.getEncoding() != null) {
            messageSource.setDefaultEncoding(properties.getEncoding().name());
        }
        
        
```

上面的代码是springboot获取用户国际化配置的代码，springboot会读取默认配置文件，查看是否含有国际化路径配置，如果没有则使用默认的路径。默认路径为根路径下的message。

```java
public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String basename = context.getEnvironment().getProperty("spring.messages.basename", "messages");
            ConditionOutcome outcome = (ConditionOutcome)cache.get(basename);
            if (outcome == null) {
                outcome = this.getMatchOutcomeForBasename(context, basename);
                cache.put(basename, outcome);
            }

            return outcome;
        }
```

springboot对国际化的配置文件存在默认的读取路径，则说明我们可以直接在根路径下创建国际化配置，这样springboot也可以读取。但是一般情况下不会这样做，为了更好的语义化，我们会提供不同界面不同的国际化配置路径。

##### 5.2.1根据浏览器的头信息改变界面语言环境

在springboot的主配置文件里，配置`spring.messages.basename=i18n.login`（il8n是文件夹名字,login是国际化配置的文件名的前缀，对应的中文为:login_zh_CN.properties，英文为:login_en_US.properties）

此时便可以在界面中引用对应的国际化配置了，当浏览器的语言环境切换的时候，界面的语言也会随之改变

---

##### 5.2.2 主动改变界面语言环境

>在当前的界面中，会提供切换语言的按钮。

我们为切换语言的按钮提供一个区域参数，来决定要使用什么语言（使用了thymeleaf模板引擎）

```html
<a class="btn btn-sm" th:href="@{/index.html(l='zh_CN')}">中文</a>
<a class="btn btn-sm" th:href="@{/index.html(l='en_US')}">English</a>
```

接下来，我们需要提供一个自定义的区域信息解析器，并将这个组件添加到容器中，让springboot来加载

```java
public class MyLocaleResover implements LocaleResolver {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        String l = httpServletRequest.getParameter("l");
        logger.info(l);
        //获取操作系统默认的区域信息
        Locale locale = Locale.getDefault();
        if(!StringUtils.isEmpty(l)){
            String[] s = l.split("_");
            locale = new Locale(s[0],s[1]);
        }
        return locale;
    }
```

具体的实现逻辑：获取传入的区域信息，判断是否为空，如果不为空，将 传入的区域信息封装为Locale对象进行返回，如果为空使用系统默认的区域信息或者获取浏览器默认的请求头。

```java
httpServletRequest.getHeader("Accept-Language");获取浏览器默认请求头
zh-CN,zh;q=0.9,en;q=0.8  === 这是结果，需要进行切割，然后传入
```

最后将这个区域解析器添加到容器中

```java
@Bean("localeResolver")
    public LocaleResolver getLocaleResolver(){
        return new MyLocaleResover();
    }
```

注：需要指定这个bean 的id为localResolver，不然springboot无法解析这个组件

### 5.3 ThymeLeaf公共界面抽取

```html
声名该代码块是可以被抽取：<th:fragment = "topbar">或者定义一个id属性

    在其他界面引用对应抽取出来的元素
    <th:insert="~{topbar::topbar}">
```

insert , replace , include的区别

```html
公共元素
<footer th:fragment="copy">
  &copy; 2011 The Good Thymes Virtual Grocery
</footer>

引入：
<body>

  ...

  <div th:insert="footer :: copy"></div>

  <div th:replace="footer :: copy"></div>

  <div th:include="footer :: copy"></div>
  
</body>

结果：
<body>

  ...

  <div>
    <footer>
      &copy; 2011 The Good Thymes Virtual Grocery
    </footer>
  </div>

  <footer>
    &copy; 2011 The Good Thymes Virtual Grocery
  </footer>

  <div>
    &copy; 2011 The Good Thymes Virtual Grocery
  </div>
  
</body>
```

### 5.4错误界面定制

`@ControllerAdivice`定义全局的异常处理

- 通过`@ExceptionHandler(XXXException.class)`执行该方法需要处理什么异常，然后返回什么数据或者视图

```java
	//json数据返回	，处理自定义用户不存在异常
	@ResponseBody
    @ExceptionHandler(UserException.class)
    public Map<String,String> userExceptionMethod(UserException us){
        Map<String,String> map = new HashMap<>();
        map.put("message",us.getMessage());
        return map ;
    }
	//响应一个视图 ，处理所有的异常
    @ExceptionHandler({Exception.class})
    public ModelAndView allException(Exception e){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/login");
        modelAndView.addObject("message","全局异常");
        return modelAndView ;
    }
```



`@ControllerAdvice`定义全局数据

- 通过`@ModelAttribute(Name="key")`执行全局数据的key
- 方法的返回值作为键
- 在`Controller`中通过`Model`获取对应的key的值

```java
@ControllerAdvice
public MyConfig{
	@ModelAttribute(name = "key")
    public Map<String,String> defineAttr(){
        Map<String,String> map = new HashMap<>();
        map.put("message","今天是情人节");
        map.put("update","也会有一个人与你哭述衷肠");
        return map ;
    }
    
@Controller
public UserController{
    @GetMapping("/hello")
    public Map<String, Object> hello(Model model){
        Map<String, Object> asMap = model.asMap();
        System.out.println(asMap);
        //{key={message='今天是情人节',update='也会有一个人与你哭述衷肠'}}
        return asMap ;
    }
}
```



`@ControllerAdvice`处理预处理数据(当需要添加的实体，属性名字相同的时候)

- 在`Controller`的参数中添加`ModelAttribute`作为属性赋值的前缀
- 在`ControllerAdvice`修饰的类中，结合`InitBinder`来绑定对应的属性(该属性为ModelAttribite的value值
- 在`@InitBinder`修饰的方法中通过`WebDataBinder`添加默认的前缀

```java
@Getter@Setter
public class Book {
    private String name ;
    private int age ;
    
@Getter@Setter
public class Music {
    private String name ;
    private String author ;
    
    //这种方式的处理，spring无法判断Name属性给哪个bean赋值，所以需要通过别名的方式来进行赋值
@PostMapping("book")
    public String book(Book book , Music music){
        System.out.println(book);
        System.out.println(music);
        return "404" ;
    }
    //使用以下的方式
@PostMapping("/book")
    public String book(@ModelAttribute("b")Book book , @ModelAttribute("m")Music music){
        System.out.println(book);
        System.out.println(music);
        return "404" ;
    }
    
public MyCOnfiguration{
    @InitBinder("b")
    public void b(WebDataBinder webDataBinder){
        webDataBinder.setFieldDefaultPrefix("b.");
    }
    @InitBinder("m")
    public void m(WebDataBinder webDataBinder){
        webDataBinder.setFieldDefaultPrefix("m.");
    }
}
    
```





















