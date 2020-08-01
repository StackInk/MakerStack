兄弟们，大家好。时隔多天，我，终于来了。今天我们来聊一下让人神魂颠倒的`Synchronized`。

不过呢，在读这篇文章之前，我希望你真正使用过这个东东，或者了解它到底是干嘛用的，不然很难理解这篇文章讲解的东西。

这篇文章的大体顺序是：从**无锁-->偏向锁-->轻量级锁-->重量级锁**讲解，其中会涉及到`CAS`，**对象内存布局**，缓存行等等知识点。也是满满的干货内容。其中也夹杂了个人在面试过程中出现的面试题，各位兄弟慢慢享受。

`Synchronized`在`jdk1.6`做了非常大的优化，避免了很多时候的用户态到内核态的切换，节省了资源的开销，而这一切的前提均来源于`CAS`这个理念。下面我们先来聊一下`CAS`的一些基本理论。

### 1. CAS

`CAS`全称：`CompareAndSwap`，故名思意：比较并交换。他的主要思想就是：**我需要对一个值进行修改，我不会直接修改，而是将当前我认为的值和要修改的值传入，如果此时内存中的确为我认为的值，那么就进行修改，否则修改失败。**他的思想是一种乐观锁的思想。

一张图解释他的工作流程：

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200417154030.png)

知道了它的工作原理，我们来听一个场景：**现在有一个`int`类型的数字它等于1，存在三个线程需要对其进行自增操作。**

一般来说，我们认为的操作步骤是这样：线程从主内存中读取这个变量，到自己的工作空间中，然后执行变量自增，然后回写主内存，但这样在多线程状态下会存在安全问题。**而如果我们保证变量的安全性，常用的做法是`ThreadLocal`或者直接加锁。**（对`ThreadLocal`不了解的兄弟，看我这篇文章[一文读懂ThreadLocal设计思想](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484744&idx=1&sn=641c5b2c2261fe7a82bd314f724deada&chksm=fe541ab5c92393a377edbb4be3b80d028f1228589af584892d9bbb850f381e74ddcbdb9a704b&token=1308274375&lang=zh_CN#rd)）

这个时候我们思考一下，如果使用我们上面的`CAS`进行对值的修改，我们需要如何操作。

**首先，我们需要将当前线程认为的值传入，然后将想要修改的值传入。如果此时内存中的值和我们的期望值相等，进行修改，否则修改失败。这样是不是解决了一个多线程修改的问题，而且它没有使用到操作系统提供的锁。**

上面的流程其实就是类`AtomicInteger`执行自增操作的底层实现，它保证了一个操作的原子性。我们来看一下源码。

```java
public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }

public final int getAndAddInt(Object var1, long var2, int var4) {
        int var5;
        do {
            //从内存中读取最新值
            var5 = this.getIntVolatile(var1, var2);
            //修改
        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

        return var5;
 }
```

实现`CAS`使用到了`Unsafe`类，看它的名字就知道不安全，所以`JDK`不建议我们使用。对比我们上面多个线程执行一个变量的修改流程，这个类的操作仅仅增加了一个自旋，它在不断获取内存中的最新值，然后执行自增操作。

可能有兄弟说了，那`getIntVolatile`和`compareAndSwapInt`操作如何保证原子性。

对于`getIntVolatile`来说，读取内存中的地址，本来就一部操作，原子性显而易见。

对于`compareAndSwapInt`来说，它的原子性由`CPU`保证，通过一系列的`CPU`指令实现，其`C++`底层是依赖于`Atomic::cmpxchg_ptr`实现的

到这里`CAS`讲完了，不过其中还有一个`ABA`问题，有兴趣可以去了解我的这篇文章[多线程知识点小节](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484523&idx=1&sn=099d662b6a1758036f10dd77d3931c71&chksm=fe541b96c9239280e9eed5842bd27b21a374dcd9ce6bce1bb64324f685f3dd9e32413d333d31&token=1308274375&lang=zh_CN#rd)。里面有详细的讲解。

我们通过`CAS`可以保证了操作的原子性，那么我们需要考虑一个东西，锁是怎么实现的。对比生活中的`case`，我们通过一组密码或者一把钥匙实现了一把锁，同样在计算机中也通过一个钥匙即`synchronized`代码块使用的锁对象。

那其他线程如何判断当前资源已经被占有了呢？

**在计算机中的实现，往往是通过对一个变量的判断来实现，无锁状态为`0`，有锁状态为`1`等等来判断这个资源是否被加锁了，当一个线程释放锁时仅仅需要将这个变量值更改为0，代表无锁。**

**我们仅仅需要保证在进行变量修改时的原子性即可，而刚刚的`CAS`刚好可以解决这个问题**

**至于那个锁变量存储在哪里这个问题，就是下面的内容了，对象的内存布局**

### 2. 内存布局

各位兄弟们，应该都清楚，我们创建的对象都是被存放到堆中的，最后我们获得到的是一个对象的引用指针。那么有一个问题就会诞生了，`JVM`创建的对象的时候，开辟了一块空间，那这个空间里都有什么东西？这个就是我们这个点的内容。

先来结论：**`Java`中存在两种类型的对象，一种是普通对象，另一种是数组**

**对象内存布局**

![](https://gitee.com/onlyzl/image/raw/master/img/20200725135023.png)

我们来一个一个解释其含义。

**白话版：**对象头中包含又两个字段，`Mark Word`主要存储改对象的锁信息，`GC`信息等等(锁升级的实现)。而其中的`Klass Point`代表的是一个类指针，它指向了方法区中类的定义和结构信息。而`Instance Data`代表的就是类的成员变量。在我们刚刚学习`Java`基础的时候，都听过老师讲过，对象的非静态成员属性都会被存放在堆中，这个就是对象的`Instance Data`。相对于对象而言，数组额外添加了一个数组长度的属性

**最后一个对其数据是什么？**

我们拿一个场景来展示这个原因：**想像一下，你和女朋友周末打算出去玩，女朋友让你给她带上口红，那么这个时候你仅仅会带上口红嘛？当然不是，而是将所有的必用品统统带上，以防刚一出门就得回家拿东西！！！**这种行为叫啥？**未雨绸缪，没错，暖男行为**。还不懂？再来一个案例。**你准备创业了，资金非常充足，你需要注册一个域名，你仅仅注册一个嘛？不,而是将所有相关的都注册了，防止以后大价钱买域名**。一个道理。

而对于`CPU`而言，它在进行计算处理数据的时候，不可能需要什么拿什么吧，那对其性能损耗非常严重。所以有一个协议，**`CPU`在读取数据的时候，不仅仅只拿需要的数据，而是获取一行的数据，这就是缓存行，而一行是64个字节**。

所以呢？通过这个特性可以玩一些诡异的花样，比如下面的代码。

```java
public class CacheLine {
    private volatile Long l1 , l2;
}
```

我们给一个场景：两个线程`t1和t2`分别操作`l1`和`l2`，那么当`t1`对`l1`做了修改以后，`l2`需不需要重新读取主内存种值。答案是一定，根据我们上面对于缓存行的理解，`l1和l2`必然位于同一个缓存行中，根据缓存一致性协议，当数据被修改以后，其他`CPU`需要重新重主内存中读取数据。**这就引发了伪共享的问题**

**那么为什么对象头要求会存在一个对其数据呢？**

`HotSpot`虚拟机要求每一个对象的内存大小必须保证为8字节的整数倍，所以对于不是8字节的进行了对其补充。其原因也是因为缓存行的原因

**对象=对象头+实例数据**

### 3. 无锁

我们在前面聊了一下，计算机中的锁的实现思路和对象在内存中的布局，接下来我们来聊一下它的具体锁实现，为对象加锁使用的是对象内存模型中的对象头，**通过对其锁标志位和偏向锁标志位的修改实现对资源的独占即加锁操作**。接下来我们看一下它的内存结构图。

![](https://gitee.com/onlyzl/image/raw/master/img/20200725151042.png)

上图就是对象头在内存中的表现(64位)，`JVM`通过对对象头中的锁标志位和偏向锁位的修改实现“无锁”。

对于无锁这个概念来说，在`1.6`之前，即所有的对象，被创建了以后都处于无锁状态，而在`1.6`之后，偏向锁被开启，对象在经历过几秒的时候(4~5s)以后，自动升级为当前线程的偏向锁。(无论经没经过`synchronized`)。

我们来验证一下，通过`jol-core`工具打印其内存布局。**注：该工具打印出来的数据信息是反的，即最后几位在前面，通过下面的案例可以看到**

场景：创建两个对象，一个在刚开始的时候就创建，另一个在`5`秒之后创建，进行对比其内存布局

```java
Object object = new Object();
System.out.println(ClassLayout.parseInstance(object).toPrintable());//此时处于无锁态
try {TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
Object o = new Object();
System.out.println("偏向锁开启");
System.out.println(ClassLayout.parseInstance(o).toPrintable());//五秒以后偏向锁开启
```

![](https://gitee.com/onlyzl/image/raw/master/img/20200725152033.png)

我们可以看到，线程已开启创建的对象处于无锁态，而在5秒以后创建的线程处于偏向锁状态。

同样，当我们遇到`synchronized`块的时候，也会自动升级为偏向锁，而不是和操作系统申请锁。

说完这个，提一嘴一个面试题吧。**解释一下什么是无锁。**

从对象内存结构的角度来说，是一个锁标志位的体现；从其语义来说，无锁这个比较抽象了，因为在以前锁的概念往往是与操作系统的锁息息相关，所以新出现的基于`CAS`的偏向锁，轻量级锁等等也被成为无锁。而在`synchronized`升级的起点----无锁。这个东西就比较难以解释，只能说它没加锁。不过面试的过程中从对象内存模型中理解可能会更加舒服一点。

### 4. 偏向锁

在实际开发中，往往资源的竞争比较少，于是出现了偏向锁，故名思意，当前资源偏向于该线程，认为将来的一切操作均来自于改线程。下面我们从对象的内存布局下看看偏向锁

**对象头描述：偏向锁标志位通过CAS修改为1，并且存储该线程的线程指针**

![](https://gitee.com/onlyzl/image/raw/master/img/20200725151042.png)

当发生了锁竞争，其实也不算锁竞争，就是当这个资源被多个线程使用的时候，偏向锁就会升级。

在升级的期间有一个点-----**全局安全点**，只有处在这个点的时候，才会撤销偏向锁。

全局安全点-----类似于`CMS`的`stop the world`，保证这个时候没有任何线程在操作这个资源，这个时间点就叫做全局安全点。

可以通过`XX:BiasedLockingStartupDelay=0 `关闭偏向锁的延迟，使其立即生效。

通过`XX:-UseBiasedLocking=false `关闭偏向锁。

### 5.轻量级锁

在聊轻量级锁的时候，我们需要搞明白这几个问题。**什么是轻量级锁，什么重量级锁？，为什么就重量了，为什么就轻量了？**

**轻量级和重量级的标准是依靠于操作系统作为标准判断的，在进行操作的时候你有没有调用过操作系统的锁资源，如果有就是重量级，如果没有就是轻量级**

接下来我们看一下轻量级锁的实现。

- 线程获取锁，判断当前线程是否处于无锁或者偏向锁的状态，如果是，通过`CAS`复制当前对象的对象头到`Lock Recoder`放置到当前栈帧中(对于`JVM`内存模型不清楚的兄弟，看这里[入门JVM看这一篇就够了](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484527&idx=1&sn=e0b1896ffc6167b270c750d9407c0003&chksm=fe541b92c92392848d0e3670661c9b6aab86d412a6d1733cba755239d52b912d0302a7638814&token=1308274375&lang=zh_CN#rd)
- 通过`CAS`将当前对象的对象头设置为栈帧中的`Lock Recoder`,并且将锁标志位设置为`00`
- 如果修改失败，则判断当前栈帧中的线程是否为自己，如果是自己直接获取锁，如果不是升级为重量级锁，后面的线程阻塞

我们在上面提到了一个`Lock Recoder`，这个东东是用来保存当前对象的对象头中的数据的，并且此时在该对象的对象头中保存的数据成为了当前`Lock Recoder`的指针

![](https://gitee.com/onlyzl/image/raw/master/img/20200726150730.png)

我们看一个代码模拟案例，

```java
public class QingLock {
    public static void main(String[] args) {
        try {
            //睡觉5秒，开启偏向锁，可以使用JVM参数
            TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        A o = new A();
        //让线程交替执行
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(()->{
            o.test();
            countDownLatch.countDown();
        },"1").start();

        new Thread(()->{
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            o.test();
        },"2").start();


    }
}

class A{
    private Object object = new Object();
    public void test(){
        System.out.println("为进入同步代码块*****");
        System.out.println(ClassLayout.parseInstance(object).toPrintable());
        System.out.println("进入同步代码块******");
        for (int i = 0; i < 5; i++) {
            synchronized (object){
                System.out.println(ClassLayout.parseInstance(object).toPrintable());
            }
        }
    }
}
```

**运行结果为两个线程交替前后**

![](https://gitee.com/onlyzl/image/raw/master/img/20200726151602.png)

**轻量级锁强调的是线程交替使用资源，无论线程的个数有几个，只要没有同时使用就不会升级为重量级锁**

在上面的关于轻量级锁加锁步骤的讲解中，如果线程`CAS`修改失败，则判断栈帧中的`owner`是不是自己，如果不是就失败升级为重量级锁，而在实际中，`JDK`加入了一种机制**自旋锁**，即修改失败以后不会立即升级而是进行自旋，在`JDK1.6`之前自旋次数为`10`次，而在`1.6`又做了优化，改为了**自适应自旋锁**，由虚拟机判断是否需要进行自旋，判断原因有：**当前线程之前是否获取到过锁，如果没有，则认为获取锁的几率不大，直接升级，如果有则进行自旋获取锁。**



### 6. 重量级锁

前面我们谈到了无锁-->偏向锁-->轻量级锁，现在最后我们来聊一下重量级锁。

这个锁在我们开发过程中很常见，线程抢占资源大部分都是同时的，所以`synchronized`会直接升级为重量级锁。我们来代码模拟看一下它的对象头的状况。

**代码模拟**

```java
public class WeightLock {
    public static void main(String[] args) {
        A a = new A();
        for (int i = 0; i < 2; i++) {
             new Thread(()->{
                a.test();
             },"线程"+ i).start();
        }
    }
}
```

**未进入代码块之前,两者均为无锁状态**

![](https://gitee.com/onlyzl/image/raw/master/img/20200726152708.png)

**开始执行循环，进入代码块**

![](https://gitee.com/onlyzl/image/raw/master/img/20200726152817.png)

**在看一眼，对象头锁标志位**

![](https://gitee.com/onlyzl/image/raw/master/img/20200725151042.png)

对比上图，可以发现，在线程竞争的时候锁，已经变为了重量级锁。接下来我们来看一下重量级锁的实现

#### 6.1 Java汇编码分析

我们先从`Java`字节码分析`synchronzied`的底层实现，它的主要实现逻辑是依赖于一个`monitor`对象，当前线程执行遇到`monitorenter`以后，给当前对象的一个属性`recursions`加一（下面会详细讲解），当遇到`monitorexit`以后该属性减一，代表释放锁。

**代码**

```java
Object o = new Object();
synchronized (o){

}
```

**汇编码**

![](https://gitee.com/onlyzl/image/raw/master/img/20200726153344.png)

上图就是上面的四行代码的汇编码，我们可以看到`synchronized`的底层是两个汇编指令

- `monitoreneter`代表`synchronized`块开始
- `monitorexit`代表`synchronized`块结束

有兄弟要说了**为什么会有两个`monitorexit`?这也是我曾经遇到的一个面试题**

第一个`monitorexit`代表了`synchronized`块正常退出

第二个`monitorexit`代表了`synchronized`块异常退出

很好理解，当在`synchronized`块中出现了异常以后，不能当前线程一直拿着锁不让其他线程使用吧。所以出现了两个`monitorexit`

**同步代码块理解了，我们再来看一下同步方法。**

代码

```java
public static void main(String[] args) {

}

public synchronized void test01(){

}
```

**汇编码**

![](https://gitee.com/onlyzl/image/raw/master/img/20200726154106.png)

我们可以看到，同步方法增加了一个`ACC_SYNCHRONIZED`标志，它会在同步方法执行之前调用`monitorenter`，结束以后调用`monitorexit`指令。

#### 6.2 C++代码

在`Java`汇编码的讲解中，我们提到了两个指令`monitorenter`和`monitorexit`，其实他们是来源于一个`C++`对象`monitor`，在`Java`中每创建一个对象的时候都会有一个`monitor`对象被隐式创建，他们和当前对象绑定，用于监视当前对象的状态。其实说绑定也不算正确，其实际流程为：**线程本身维护了两个`MonitorList`列表，分别为空闲(free)和已经使用(used)，当线程遇到同步代码块或者同步方法的时候，会从空闲列表中申请一个`monitor`使用，如果当先线程已经没有空闲的了，则直接从全局(`JVM`)获取一个`monitor`使用**

我们来看一下`C++`对这个对象的描述

```c++
ObjectMonitor() {
    _header       = NULL;
    _count        = 0;
    _waiters      = 0,
    _recursions   = 0; // 重入次数
    _object       = NULL; //存储该Monitor对象
    _owner        = NULL; //拥有该Monitor对象的对象
    _WaitSet      = NULL; //线程等待集合(Waiting)
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ; //多线程竞争时的单向链表
    FreeNext      = NULL ;
    _EntryList    = NULL ; //阻塞链表(Block)
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }
```

**线程加锁模型**

![](https://gitee.com/onlyzl/image/raw/master/img/20200726173501.png)

**加锁流程：**

- 最新进入的线程会进入`_cxp`栈中，尝试获取锁，如果当前线程获得锁就执行代码，如果没有获取到锁则添加到`EntryList`阻塞队列中
- 如果在执行的过程的当前线程被挂起(`wait`)则被添加到`WaitSet`等待队列中，等待被唤醒继续执行
- 当同步代码块执行完毕以后，从`_cxp`或者`EntryList`中获取一个线程执行

**`monitorenter`加锁实现**

- `CAS`修改当前`monitor`对象的`_owner`为当前线程，如果修改成功，执行操作；
- 如果修改失败，判断`_owner`对象是否为当前线程，如果是则令`_recursions`重入次数加一
- 如果当前实现是第一次获取到锁，则将`_recursions`设置为一
- 等待锁释放

**阻塞和获取锁实现**

- 将当前线程封装为一个`node`节点，状态设置为`ObjectWaiter::TS_CXQ`
- 将之添加到`_cxp`栈中，尝试获取锁，如果获取失败，则将当前线程挂起，等待唤醒
- 唤醒以后，从挂起点执行剩下的代码

**`monitorexit`释放锁实现**

- 让当前线程的`_recursions`重入次数减一，如果当前重入次数为0，则直接退出，唤醒其他线程

参考资料：

马士兵多线程技术详解书籍

HotSpot源码

往期推荐：

[一文带你了解Spring MVC的架构思路](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484676&idx=1&sn=4eaf632c4452b9f1124351be165e2a55&chksm=fe541af9c92393ef97f43b2914e5389990f88220b6e0ce55305a8fffe9f2aa4fce1f32552c6c&token=1308274375&lang=zh_CN#rd)

[Mybatis你只会CRUD嘛](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484608&idx=1&sn=c5f5ebe27e85657a9a1d4ea4842012a5&chksm=fe541b3dc923922bc7b0b96808efa49976e5bdf0c2af78ee8bf83c0591735aa6da0be4aac51d&token=1308274375&lang=zh_CN#rd)

[IOC的架构你了解嘛](https://mp.weixin.qq.com/s?__biz=MzU5NzMxNDE5NA==&mid=2247484651&idx=1&sn=5e041c6f17d4c0f2dba9722c7b263006&chksm=fe541b16c9239200066d6b25fa28fd79e773fe4e26571f0a9def2023c7cbfa0e0003deb9dc6f&token=1308274375&lang=zh_CN#rd)



