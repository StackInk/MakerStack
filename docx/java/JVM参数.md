## JVM参数

### 1. 标配参数

```java
java -version
java -help
java =showversion
```

### 2. -X参数

```java
-Xint  //解释执行
-Xcomp //第一次使用就编译成本地代码
-Xmixed	//混和模式
```

### 3. -XX参数

#### 3.1 Boolean类型

**公式：-XX:+代表增加,-代表剔除某个属性值**

查看某个`Java`进程是否开启了某些JVM参数的方法：

- `jps -l `获取所有后台运行的`Java`进程
- `jinfo -flag JVM参数 进程ID`获取这个`Java`程序对该`JVM`参数的开启状况

**案例：**当前程序是否打印了`GC`收集细节

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418135828.png)

代表当前程序没有开启垃圾回收

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418140145.png)

这是开启以后的

#### 3.2 KV类型

**公式：-XX:属性key=属性值value**

**案例：**

`-XX:MetaspaceSize=128m`修改元空间大小

`-XX:MaxTenuringThreshold`修改对象最大存活年龄

`-XX:InitialHeapSize`初始堆大小

**修改前**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418141016.png)

**修改后**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418141150.png)

**`jinfo -flags`获取所有JVM开启的参数**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418142021.png)

##### 3.2.1 Xms和-Xmx

`Xms`和`Xmx`分别代表堆的标准大小和最大空间。

`Xms`相当于`-XX:InitialHeapSize`

`Xmx`相当于`-XX:MaxHeapSize`

#### 3.3 查看JVM默认参数

`java -XX:+PrintFlagsInitial -version`打印`Java`版本信息和初始默认JVM参数设置

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418151556.png)

`java -XX:+PrintFlagsFinal -version`打印被更新过的JVM参数设置

- 冒号表示由于平台或者开发者的缘故导致该参数被修改

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418152614.png)

`java -XX:+PrintCommandLineFlags`打印一些基本信息，包括使用的垃圾回收器

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418152834.png)

打印了使用的垃圾回收器

### 4. JVM常用参数

```java
-Xms 初始大小为物理内存的1/64  等价于-XX:InitialHeapSize
-Xmx 默认为物理内存的1/4    等价于-XX:MaxHeapSize
-Xss 单个线程栈大小 一般为512~1024k
-Xmn 设置年轻代大小
-XX:MetaspaceSize 设置元空间大小
-XX:+PrintGCDetails 输出GC日志信息
-XX:SurvivorRatio  设置Ende区在新生代中的占比
-XX:NewRatio  设置老年代在堆中的占比
-XX:MaxTenuringThreashold 设置对象的最大年龄
```

**关于新生代中的Eden和Survivor的占比问题：笔者JDK得出的是6:1，但查看官方文档得出的结论是8:1**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418182834.png)

### 5. 对象的引用类型

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418192412.png)

#### 5.1 强引用

`Java`的默认引用类型，所有的对象创建都是强引用，当对象引用变量为`null`的时候被回收

#### 5.2 软引用

通过`SoftReference`设置软引用，当内存空间不足的时候，软引用对象被直接回收

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418203958.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200421112318.png)

#### 5.3 弱引用

通过`WeakReference`设置若引用，当GC运行的时候，就会将这个类型的对象回收

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418204218.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418204246.png)

##### 5.3.1 什么实用实用软引用或者虚引用

本地图片加载的时候，通常我们会将之加载到缓存中。但是当图片数量急剧增加的时候就有可能发生`OOM`，所以我们可以通过虚引用或者软引用保存图片的对象

例如我们可以构建一个`HashMap`对象封装`URL`和图片对象映射，其中的图片对象使用弱引用或者软引用的方式。当内存不足的时候自动回收内存中的图片对象

```java
HashMap<String,SoftReference<Bitmap>> imageCache =
    new HashMap<>();
```

##### 5.3.2 WeakHashMap

存储一个弱引用的`Key`，当一个`key`是无效的时候，该键将被移除

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418204436.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418204454.png)

#### 5.4 虚引用

通过`PhantomReference`设置虚引用，该引用相当于没有引用，通过`get`方法不能获取引用的值,唯一的作用就是配合`ReferenceQueue`使用，在`gC`以后被放入引用队列中，做一些后续操作，比如通知等等。

#### 5.5 引用队列

软引用，弱引用，虚引用被`GC`以后不会立即消失，而是添加到了`ReferenceQueue`引用队列中，可以做后续的操作。

**案例演示**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418210515.png)

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418210539.png)

可以看到在GC以后引用队列中有值了

### 6. OOM

#### 6.1 栈溢出

`java.lang.StackOverflowError`栈溢出，栈的默认空间大小为`512~1024k`当超过这个界限以后就会触发这个错误

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419132629.png)

#### 6.2 堆溢出

`java.lang.OutOfMemoryError:Java heap space`堆溢出。

**常见的场景：大对象（直接超过老年区大小）；强引用对象的不断出现**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419135935.png)

#### 6.3 GC超出资源限制

`java.lang.OutOfMemoryError:overhead limit exceeded`GC占据了系统98%的运行，但是仅仅回收了`2%`不到的内存空间。导致重复GC，但空间没法释放。爆出这个错误

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419143937.png)

#### 6.4 堆外内存溢出(直接内存溢出)

`java.lang.OutOfMemoryError:Direct buffer memory`堆外内存溢出，使用`NIO`的时候会出现

NIO中使用

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419144849.png)

#### 6.5 不能够再创建本地线程

`java.lang.OutOfMemoryError:unable to create new native thread`

默认大小为1024个

#### 6.6 元空间溢出

`java.lang.OutOfMemoryError:Metaspace`

默认大小为`20M`