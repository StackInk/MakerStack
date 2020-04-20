## JVM介绍

### 1. JVM的体系架构（内存模型）

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200413210115.png)

绿色的为线程私有，橘色的为线程共有

### 2. 类加载器

负责将`.class`文件加载到内存中，并且将该文件中的数据结构转换为方法区中的数据结构，生成一个`Class`对象

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200413210951.png)

#### 2.1 类加载器分类

- 自启动类加载器。`Bootstrap ClassLoader`类加载器。负责加载`jdk`自带的包。
  - `%JAVA_HOME%/lib/rt.jar%`即JDK源码
  - 使用`C++`编写
  - 在程序中直接获取被该加载器加载的类的类加载器会出现`null`
- 扩展类加载器.`Extension ClassLoader`。负责加载`jdk`扩展的包
  - 便于未来扩展
  - `%JAVA_HOME/lib/ext/*.jar%`
- 应用类加载器或系统类加载器。`AppClassLoader或SystemClassLOader`
  - 用于加载自定义类的加载器
  - `CLASSPATH`路径下
- 自定义类加载器
  - 通过实现`ClassLoader`抽象类实现

#### 2.2 双亲委派机制

当应用类加载器获取到一个类加载的请求的时候，不会立即处理这个类加载请求，而是将这个请求委派给他的父加载器加载，如果这个父加载器不能够处理这个类加载请求，便将之传递给子加载器。一级一级传递指导可以加载该类的类加载器。

该机制又称**沙盒安全机制**。防止开发者对`JDK`加载做破坏

![](https://gitee.com/onlyzl/blogImage/raw/master/img/双亲委派机制.png)

#### 2.3 打破双亲委派机制

- 自定义类加载器，重写`loadClass`方法
- 使用线程上下文类加载器

#### 2.4 Java虚拟机的入口文件

`sun.misc.Launcher`类

### 3. Execution Engine

执行引擎负责执行解释命令，交给操作系统进行具体的执行

### 4. 本地接口

#### 4.1 native方法

`native`方法指`Java`层面不能处理的操作，只能通过本地接口调用本地的函数库(`C函数库`)

#### 4.2 Native Interface

一套调用函数库的接口

### 5. 本地方法栈

在加载`native`方法的时候，会将执行的`C`函数库的方法，放在这个栈区域执行

### 6. 程序计数器

每个线程都有程序计数器，主要作用是存储代码指令，就类似于一个执行计划。

内部维护了多个指针，这些指针指向了方法区中的方法字节码。执行引擎从程序计数器中获取下一次要执行的指令。

由于空间很小，他是当前线程执行代码的一个行号指示器/

不会引发OOM

### 7. 方法区

供各线程共享的运行时内存区域，存放了各个类的结构信息(一个Class对象)，包括：字段，方法，构造方法，运行时常量池。

**虽然JVM规范将方法区描述为堆的一个逻辑部分，但它却还有一个别名叫做Non-Heap(非堆)，目的就是要和堆分开**

**主要有：**永久代或者元空间。存在GC

元空间中由于直接使用物理内存的影响，所以默认的最大元空间大小为`1/4`物理内存大小

### 8. Java栈

主要负责执行各种方法，是线程私有的，随线程的消亡而消亡，不存在垃圾回收的问题。八大数据类型和实例引用都是在函数的栈内存中分配内存的。

默认大小为`512~1024K`，通过`-Xss1024k`参数修改

#### 8.1 栈和队列数据结构

栈`FILO`：先进后出

队列`FIFO`：先进先出

#### 8.2 存储的数据

- 本地变量`Local Variable`。包括方法的形参和返回值
- 栈操作`Operand Stack`。包括各种压栈和出栈操作
- 栈帧数据`Frame Data`。就相当于一个个方法。在栈空间中，方法被称为栈帧

#### 8.3 执行流程

栈中执行的单位是栈帧，栈帧就是一个个方法。

- 首先将`main`方法压栈，成为一个栈帧
- 然后调用其他方法，即再次压栈
- 栈帧中存储了这个方法的局部变量表，操作数栈、动态链接、方法出口等
- 栈的大小和JVM的实现有关，通常在`256K~756K`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200413215821.png)

### 9. 方法区，栈，堆的关系

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200413220431.png)

### 10. Heap 堆

#### 10.1 堆内存结构

默认初始大小为物理内存的`1/64`，默认最大大小为`1/4`。在实际生产中一般会将这两个值设置为相同，避免垃圾回收器执行完垃圾回收以后还需要进行空间的扩容计算，浪费资源。

**堆外内存：**内存对象分配在Java虚拟机的堆以外的内存，这些内存直接受操作系统管理（而不是虚拟机），这样做的结果就是能够在一定程度上减少垃圾回收对应用程序造成的影响。使用未公开的Unsafe和NIO包下`ByteBuffer`来创建堆外内存。

默认的堆外内存大小为,通过`-XX:MaxDirectMemorySize=`执行堆外内存的大小

##### 10.1.1 JDK1.7

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200413221251.png)

**在逻辑上划分为三个区域：**

- 新生区`Young Generation Space`。
  - 伊甸区`Eden Space`
  - 幸存区`Survivor 0 Space`
  - 幸存区`Survivor 1 Space`
- 养老区`Tenure Generation Space`
- 永久区`Permanent Space`（方法区）

**在物理层面划分为两个区域：**

- 新生区
- 老年区

###### 10.1.1.1 堆内存GC过程

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200413222739.png)

**主要流程有三步：**

- 当`Eden`满了以后出发一次轻GC(`Minor GC`)，没有死亡的对象，年龄`+1`，存放到`from`区域
- 当`Eden`再次满了以后再次触发一次`GC`，没有死亡的对象放置于`to`区域，然后将`from`区域中没有死亡的对象全部置于`to`区域，年龄`+1`。之后每一次GC都会出发一次`from`和`to`的交换，**哪个区域是空的那个区域就是`to`**
- 当`survivor`区域满了以后，再次触发GC，当存在对象的年龄等于`15`的时候，就会将该对象移入老年区
  - `MaxTenuringThreshold`通过这个参数设置当年龄为多少的时候移入
  
- 老年区满了以后触发一次`Full GC`，如果老年区无法再存放对象直接报`OOM`

**注意：每一次GC都会给存活的对象的年龄+1**

##### 10.1.2 JDK1.8

和`1.7`相比，仅仅是将永久代更替为了**元空间**。元空间的存放内置是**物理内存**，而不是`JVM`中。

这样处理，可以使元空间的大小不再受虚拟机内存大小的影响，而是由系统当前可用的空间来控制。

新生区和老年区的大小比例为`1:2`,通过`-XX:NewRatio=n`设置新生代和老年代的比例，n代表老年区所占的比例。

Eden Space和Survivor Space之间的比例默认为`8:1`，通过`-XX:SurvivorRatio`设置伊甸区和幸存者区的比例

**逻辑层面分层：**

- 新生区`Young Generation Space`
  - 伊甸区`Eden Space`
  - 幸存区`Survivor 0 Space`
  - 幸存区`Survivor 1 Space`
- 老年区`Tenure Generation Space`
- 元空间(方法区)

**物理层面分层：**

- 新生区 **他占据堆的1/3**
- 老年区 **他占据堆的2/3**

#### 10.2 堆参数调优

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200414202111.png)

##### 10.2.1 常用堆参数

| 参数                | 作用                                 |
| ------------------- | ------------------------------------ |
| -Xms                | 设置初始堆大小，默认为物理内存的1/64 |
| -Xmx                | 设置最大堆大小，默认为物理内存的1/4  |
| -XX:+PrintGCDetails | 输出详细的GC日志                     |

**模拟OOM**

```java
//设置最大堆内存为10m 
//-Xms10m -Xmx10m -XX:+PrintGCDetails
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418134104.png)

下面我们具体分析一下GC的过程做了什么，GC日志怎么看

**名称:GC以前占用->GC之后占用(总共占用)**

```java
//GC 分配失败
GC (Allocation Failure)
    [PSYoungGen: 1585K->504K(2560K)] 1585K->664K(9728K), 0.0009663 secs] //[新生代，以前占用->线程占用(总共空闲)] 堆使用大小->堆现在大小(总大小)
    [Times: user=0.00 sys=0.00, real=0.00 secs] 
    
    
[Full GC (Allocation Failure)
 [PSYoungGen: 0K->0K(2560K)] 
 [ParOldGen: 590K->573K(7168K)] 590K->573K(9728K),
 [Metaspace: 3115K->3115K(1056768K)], 0.0049775 secs] 
 [Times: user=0.00 sys=0.00, real=0.01 secs] 
```

### 11. 垃圾回收算法

#### 11.1 垃圾回收类型

- 普通GC(`minor GC`)发生在新生区的，很频繁
- 全局GC`major GC`发生在老年代的垃圾收集动作，出现一次`major GC`经常会伴随至少一次的`Minor GC`

#### 11.2 垃圾回收算法分类

##### 11.2.1 引用计数法

**主要思想：**每存在一个对象引用就给这个对象加一，当这个对象的引用为零的时候，便触发垃圾回收。**一般不使用**

**缺点：**

- 每次新创建对象就需要添加一个计数器，比较浪费
- 循环引用较难处理

##### 11.2.2 复制算法

**主要思想：**将对象直接拷贝一份，放置到其他区域

**优点：**不会产生内存碎片

**缺点：**占用空间比较大

**使用场景：**新生区的复制就是通过复制算法来执行的。当`Minor Gc`以后，就会幸存的对象复制一份放置到`to`区

##### 11.2.3 标记清除算法

**主要思想：**从引用根节点遍历所有的引用，标记出所有需要清理的对象，然后进行清除。**两步完成**

**缺点：**在进行垃圾回收的时候会打断整个代码的运行。会产生内存碎片

##### 11.2.4 标记整理算法

**主要思想：**和标记清除算法一样，最后添加了一个步骤整理，将整理内存碎片。**三步完成**

**缺点：**效率低，需要移动对象。

#### 11.3 各大垃圾回收算法比较

##### 11.3.1 内存效率

复制算法>标记清除法>标记整理法

##### 11.3.2 内存整齐度

复制算法=标记整理法>标记清除法

##### 11.3.3 内存利用率

标记整理法=标记清除法>复制算法

##### 11.3.4 最优算法

通过场景使用不同的算法，来达到最优的目的

年轻代：因为其对象存活时间段，对象死亡率高，所以一般使用复制算法

老年代：区域大，存活率高，一般采用标记清除和标记整理的混合算法。

**老年代一般是由标记清除或者是标记清除与标记整理的混合实现。以hotspot中的CMS回收器为例，CMS是基于Mark-Sweep实现的，对于对像的回收效率很高，而对于碎片问题，CMS采用基于Mark-Compact算法的Serial Old回收器做为补偿措施：当内存回收不佳（碎片导致的Concurrent Mode Failure时），将采用Serial Old执行Full GC以达到对老年代内存的整理。**

##### 11.3.5 GCRoots

上面我们提到标记清除算法的时候，提到了一个名词，**根节点引用**。那么什么叫做根节点引用呢？

根节点引用也成`GCRoots`，他是指垃圾回收算法进行对象遍历的根节点。即从这个对象开始往下遍历，标记需要进行回收的对象。

垃圾回收标记的过程就是：以`GCRoots`对象开始向下搜索，如果一个对象到`GCRoots`没有任何的引用链相连时，说明此对象不可用。

就是从`GCRoots`进行遍历，**可以被遍历到的就不是垃圾，没有被遍历到的就是垃圾，判定死亡**

###### 11.3.5.1 可达性对象和不可达性对象

可达性对象是指，在对象链路引用的顶层是一个`GCRoot`引用

不可达对象是指，在对象链路引用的顶层不是一个`GCRoot`引用

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200418121525.png)

**通俗解释：**可达性对象就是对象有一个归属，这个归属有一个术语名称叫做`GCRoot`，不可达性对象就是这些对象没有归属。

###### 11.3.5.2 什么引用可以作为GCRoots

- 栈内的局部变量引用
- 元空间中的静态属性引用
- 元空间中的常量引用
- 本地方法栈中`native`修饰的方法

**说白了，就是所有暴露给开发者的引用**

### 12. 垃圾回收器

垃圾回收器是基于`GC`算法实现的。

主要有四种垃圾回收器，不过具体有七种使用方式

#### 12.1 四种垃圾回收器

##### 12.1.1 串行垃圾回收器(Serial)

单线程进行垃圾回收，此时其他的线程全部被暂停

通过`-XX:+UseSerialGC`

##### 12.1.2 并行垃圾回收器(Parallel)

多线程进行垃圾回收，此时其他的线程全部被暂停

##### 12.1.3 并发垃圾回收器(CMS)

GC线程和用户线程同时运行

##### 12.1.4 G1垃圾回收器

分区垃圾回收。物理上不区分新生区和养老区，将堆内存划分为`1024`个小的`region`，每一个占据的空间在`2~32M`，每一个`region`都可能是`Eden Space`、`Survivor01 Space`、`Survivor02 Space`和`Old`区。

整体使用了标记整理算法，局部使用了复制算法。通过复制算法将GC后的对象从一个`region`向另一个`region`迁移，至于造成了内存碎片问题，通过整体的标记整理算法，避免了内存碎片的诞生

在进行垃圾回收的时候直接对一个`region`进行回收，保存下来的对象通过复制算法复制到`TO`区或者`Old`区。

逻辑上堆有四个区，每一个区的大小不定，按需分配。分为`Eden Space`，`Survivor01 Space`，`Old`和`Humongous`。其中`Humongous`用来存放大对象，一般是连续存储，当由于连续`region`不足的时候，会触发`Full GC`清理周围的`Region`以存放大对象

**G1堆内存示意**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419200009.png)

**G1垃圾回收**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419200347.png)

**出现大对象，三个region不能存放，进行FullGC**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419200706.png)

**执行流程**

- 初始标记。**GC多线程**，标记`GCRoots`
- 并发标记。**用户线程和GC线程同时进行**。GC线程遍历`GCRoots`的所有的对象，进行标记
- 重新标记。修正被并发标记标记的对象，由于用户程序再次调用，而需要取消标记的对象。**GC线程**
- 筛选回收。清理被标记的对象。**GC线程**
- 用户线程继续运行

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419202001.png)

###### 12.1.4.1 案例

- 初始标记。是通过一个大对象引发的G1

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419202324.png)

- 并发标记

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419202510.png)

- 重新标记、筛选清理和大对象引发的`Full GC`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419202602.png)

###### 12.1.4.2 G1常用参数

```java
-XX:+UseG1GC  开启GC
-XX:G1HeapRegionSize=n : 设置G1区域的大小。值是2的幂，范围是1M到32M。目标是根据最小的Java堆大小划分出约2048个区域
-XX:MaxGCPauseMillis=n : 最大停顿时间，这是个软目标，JVM将尽可能（但不保证）停顿时间小于这个时间
    
-XX:InitiatingHeapOccupancyPercent=n  堆占用了多少的时候就触发GC，默认是45
-XX:ConcGCThreads=n  并发GC使用的线程数
-XX:G1ReservePercent=n 设置作为空闲空间的预留内存百分比，以降低目标空间溢出的风险，默认值是10%
```

#### 12.2 常用参数

```java
DefNew  	Default New Generation //串行垃圾回收器，新生代叫法
Tenured 	Old  //串行垃圾回收器，老年代叫法
ParNew 		Parallel New Generation //新生代并行垃圾回收器，新生代叫法
PSYongGen 	Parallel Scavenge //新生代和老年代垃圾回收器，叫法
ParOldGen 	Parallel Old Generation //新生代和老年代垃圾回收器，叫法
```

#### 12.3 新生代垃圾回收器

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419163703.png)

上图显示的是新生区和老年区可以使用垃圾回收器的所有种类，我们一个一个来说明

##### 12.3.1 串行GC(Serial/Serial Coping)

**新生代**使用`Serial Coping`垃圾回收器使用**复制算法**

**老年区**默认使用`Serial Old`垃圾回收器，使用**标记清除算法和标记整理算法**

通过`-XX:+UseSerialGC`设置

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419171045.png)

##### 12.3.2 并行GC(ParNew)

**新生区**使用`ParNew`垃圾回收器，使用复制算法

**老年区**使用`Serial Old`垃圾回收器(不推荐这样使用)，使用标记清除算法和标记整理算法

通过`-XX:+UseParNewGC`启动

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419171538.png)

##### 12.3.3 并行回收GC(Parallel/Parallel Scavenge)

**新生代**使用并行垃圾回收

**老年代**使用并行垃圾回收。Java1.8中默认使用的垃圾回收器

**一个问题：Parallel和Parallel Scavenge收集器的区别？**

`Parallel Scavenge`收集器类似于`ParNew`也是一个新生代的垃圾收集器，使用了复制算法，也是一个并行的多线程的垃圾收集器，俗称吞吐量优先收集器。

`parallel Scavenge`是一种自适应的收集器，虚拟机会根据当前系统运行情况收集性能监控信息，动态调整这些参数以提供最合适的提顿时间或者最大吞吐量

**他关注的点是:**

可控制的吞吐量。吞吐量=运行用户代码时间/(运行用户代码时间+垃圾收集时间)，

同时，当新生代选择为`Parallel Scavenge`的时候，会默认激活老年区使用并行垃圾回收

通过`-XX:UseParallelGC或者-XX:UseParallelOldGC`两者会互相激活

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419172007.png)

**`-XX:ParallelGCThreads=n`表示启动多少个GC线程**

`cpu>8时 N=5或者8`

`cpu<8时 N=实际个数` 

#### 12.4 老年代垃圾回收器

##### 12.4.1 串行垃圾回收器(Serial Old/Serial MSC)

`Serial Old`是`Serial`垃圾收集器老年代版本，是一个单线程的收集器，使用**标记整理算法**，运行在`Client`中的年老代垃圾回收算法

与新生代的`Serial GC`相关联

##### 12.4.2 并行回收(Parallel Old/Parallel MSC)

`Parallel Old/`采用**标记整理算法**实现

与新生代的`Parallel Scavenge GC`相关联

##### 12.4.3 并发标记清除GC

`CMS`收集器(`Concurrent Mark Sweep`并发标记清除)：一种以获取最短回收停顿时间为目标的收集器

适合应用在互联网站或者`B/S`系统的服务器上，重视服务器的响应速度

`CMS`非常适合堆内存大、`CPU`核数多的服务端应用，也是`G1`出现之前大型应用的首选收集器

**标记的时候，GC线程运行；清除的时候和用户线程一起运行**

通过`-XX:+UseConcMarkSweepGC`指令开启

配合**新生区的`pallellal New GC`回收器使用**

**当CMS由于CPU压力太大无法使用的时候会使用`SerialGC`作为备用收集器**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419185937.png)

###### 12.4.3.1 CMS执行过程

- 初始标记(`CMS initial mark`)。遍历寻找到所有的`GCRoots`。`GC`线程执行，用户线程暂停
- 并发标记(`CMS concurrent mark`)和用户线程一起遍历`GCRoots`，标记需要清除的对象
- 重新标记(`CMS remark`)。修正标记期间，对因用户程序继续运行而不需要进行回收的对象进行修正
- 并发清除(`CMS concurrent sweep`)和用户线程一起清除所有标记的对象

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419190511.png)

###### 12.4.3.2 优缺点

**优点：**

- 并发收集低停顿

**缺点：**

- 并发执行，对CPU资源压力大
- 采用标记清除算法会导致大量的内存碎片

#### 12.5 垃圾回收器小结

| 参数(-XX:+……)      | 新生代垃圾回收器     | 新生代算法 | 老年代垃圾回收器   | 老年代算法 |
| ------------------ | -------------------- | ---------- | ------------------ | ---------- |
| UseSerialGC        | SerialGC             | 复制算法   | Serial Old GC      | 标整       |
| UseParNewGC        | Parallel New GC      | 复制算法   | Serial Old GC      | 标整       |
| UseParllelGC       | Parallel Scavenge GC | 复制算法   | Parallel GC        | 标整       |
| UseConcMarkSweepGC | Parallel New GC      | 复制算法   | CMS和Serial Old GC | 标清       |
| UseG1GC            | 整体标整             |            | 局部复制           |            |

**垃圾回收算法通用逻辑**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200419191717.png)

#### 12.6 CMS和G1的区别

- G1不会引发内存碎片
- G1对内存的精准控制，可以精准的去收集垃圾。**根据设置的GC处理时间去收集垃圾最多的区域**

### 13. JMM

java内存模型。是一种规范。

**线程在操作变量的时候，首先从物理内存中复制一份到自己的工作内存中(栈内存)，更新以后再写入物理内存中**

**特点：**

- 原子性
- 可见性
- 有序性

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200414212439.png)