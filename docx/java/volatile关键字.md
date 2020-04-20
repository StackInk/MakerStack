### 1. volatile关键字

JDK提供的一种轻量级的同步机制。保证了**可见性，有序性**，不保证原子性

#### 1.1 什么是可见性

JMM模型，当线程操作主内存中的变量的时候，首先复制一份变量到线程的工作内存中，然后更新结束以后，就将这个变量的值更新到主内存中。

而当多个线程进行操作的时候，可能会出现不同线程之间读取的值不能实时更新。所以需要提供一种机制，当线程对变量进行更新的时候，去通知其他线程该变量的值已经被更新，从主内存中重新获取该变量的值。

而`volatile`关键字就提供了这种机制，当变量被这个关键字修饰以后，变量的值被修改，其他线程就会重新读取这个变量的值。

**代码演示可见性：**

```java
public class VolatileDemo {
    private volatile int num = 1 ;

    public static void main(String[] args) {
        VolatileDemo volatileDemo = new VolatileDemo();
        new Thread(()->{try {
            TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {e.printStackTrace();}
            volatileDemo.num = 10 ;
        }).start();
        while(volatileDemo.num == 1){

        }
        System.out.println("值已经被修改为:"+volatileDemo.num);
    }
}
```

当`num`被修改为10以后主线程跳出循序，打印输出。实现可见性。

#### 1.2 什么是原子性

先来思考一下数据库事务中的原子性。一次事务中的`SQL`只能全部成功或者全部失败。

在`Java`中的原子性，在一个线程中，原子操作不能被阻塞或者中断。

**不保证原子性演示**

```java
public class VolatileDemo {
    private volatile int num =0 ;

    public void addPlus(){
        num++;
    }
    public static void main(String[] args) {
        VolatileDemo volatileDemo = new VolatileDemo();
        for(int i = 0 ; i < 20 ; i++){
            new Thread(()->{
                for (int j = 0; j < 1000; j++) {
                    volatileDemo.addPlus();
                }
            },"线程为："+i).start();
        }
        //后台默认存在两个线程，一个main，一个GC
        while(Thread.activeCount() > 2){
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName()+"\t finally\t"+volatileDemo.num);
    }
    
   //输出 main	 finally	19689
```

**为什么会出现这种情况：**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200416125158.png)

假设现在线程一在执行`num++`，当他执行完++操作以后，需要写入主内存的时候，线程被挂起，线程二执行。

线程二执行完++操作以后写回主内存，正要通知其他线程值要修改的时候，线程一执行，将这个值又修改为了1，导致一次加失效。

**`num++`底层实现**

通过反编译获取

```java
		2: getfield      #2                  // Field num:I
       5: iconst_1
       6: iadd
       7: putfield      #2                  // Field num:I
```

首先获取字段，然后拿到一个常量值1,对字段加运算，写会主内存

在这个步骤中就会出现上面的情况。

**解决不保证原子性**

通过添加锁

**通过原子类实现**

```java
/**
 * 原子类实现原子性
 */
class MyData{
    //默认值为0
    volatile AtomicInteger atomicInteger = new AtomicInteger();

    public void addPlus(){
        //先++
        atomicInteger.incrementAndGet();
        //先获取
//        atomicInteger.getAndIncrement();
    }
}
public class atomicDemo {
    public static void main(String[] args) {
        MyData myData = new MyData();
        for(int i = 0 ; i < 20 ; i++){
            new Thread(()->{
                for (int j = 0; j < 1000; j++) {
                    myData.addPlus();
                }
            },"线程为："+i).start();
        }
        while(Thread.activeCount() > 2){
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName()+"\t value:\t"+myData.atomicInteger.get());
    }
}
```



#### 1.3 什么是有序性

同样，首先回忆一下`MySql`体系架构中存在一个优化器，它会优化开发者自己的`SQL`语句。即`MySql`引擎不会按照开发者自己书写的逻辑进行执行，而是有一个优化的过程。

类似，Java也有这样的机制，当**多个线程同时执行**的时候，`cpu`不一定按照开发者自己书写的顺序执行任务。这个时候代码的执行顺序就会被打乱。此时代码有序性不能保证。

但是在`Java`中指令重排中有一种机制**指令重排中不能存在依赖关系**

**代码解读：**

```java
int x = 11 ;     //1
int y = 12 ;    //2
x = x+5;       //3
y = y*x;      //4
```

代码中的执行顺序可能会出现：

- 1234
- 2134
- 1324
- 1243或者3421这些情况由于指令重排需要遵循规则**数据之间不存在依赖关系**所以这些情况不会出现。

**代码解读**

```java
创建一个对象需要三步
    首先在堆中分配一块内存区域            //1
    在这个内存区域中创建对象              //2
    将这个内存区域的地址赋值给这个对象的变量 //3
```

这个时候如果不添加`volatile`关键字，就会出现执行顺序为`132`的情况(2,3之间没有依赖关系)，所以会出现线程读取的对象地址为`null`的情况。

**这两案例就是由于指令重排而引起的错误**

#### 1.4 volatile怎么实现的有序性和可见性

底层是**内存屏障**。

内存屏障的作用有两个：第一、保证特定操作的执行顺序；第二、保证某些变量的内存可见性

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200415211428.png)

#### 1.5 DCL

DCL(Double Check Lock)双端检查加锁。

**看一下单例模式的实现：**

```java
class MyInstance {
    private static volatile MyInstance myInstance = null ;
    private MyInstance(){};
    public static MyInstance getInstance(){
        if(myInstance ==null){
            synchronized (MyInstance.class){
                if(myInstance ==null){
                    myInstance = new MyInstance();
                }
            }
        }
        return myInstance ;
    }
}
```

两个问题：第一、为什么使用`volatile`关键字；第二、为什么使用双重检查加锁

**第一个问题：**使用`volatile`关键字，保证多个线程对这个变量可见，防止多次创建同一个变量。同时禁止了指令重排。

**第二个问题：**使用双端检查加锁，防止一个线程执行到判断是否为`null`的时候时候线程被挂起，另一个线程执行创建对象，这个线程唤醒，再次创建对象。所以采用对`new`实例加锁，加锁以后再次进行一次判断。不过这样容易发生指令重排的现象，所以通过对该变量添加`volatile`关键字实现禁止指令重排。

### 2. CAS

什么是CAS(`compareAndSwap`)。比较替换，当对一个元素进行赋值的时候首先查看该元素是不是自己期望的值，如果是则进行修改，如果不是则返回`false`

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200417154030.png)

**`getAndAddInt`源码(自旋+CAS)**

```java
//o1是对象，v1是字段的内存偏移量，v2是需要增加的值
public int getAndAddInt(Object o1 , long v1,int v2){
    int v3 ;
    do{
        //获取指定内存中的值
		v3 = this.getIntVolatile(o1,v1);
        //查看当前值是否还为获取的值（放置其他线程修改）
    }while(this.compareAndSwapInt(o1,v1,v3,v3+v2))
    return v3 ;
}
```

#### 2.1 原子类

原子类就是通过`CAS`+自旋实现.通过直接和内存中的值进行比较,实现原子操作.2.1

##### 2.1 AtomicInteger

**底层实现：**`Unsafe`类中的CAS操作和自旋

**构造方法：**无参，默认为0；有参，传入一个指定值

##### 2.1 AtomicReference

原子引用，用来对自定义类进行原子操作

```java
AtomicReference<User> atomicReference = new AtomicReference<>();
        atomicReference.set(new User("1",1));
        System.out.println(atomicReference.get());
```

####  2.2 CAS缺点

- 循环时间长
- `ABA`问题

### 3. ABA

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200416134513.png)

即一个线程对主内存中的值进行了一系列中间操作，但修改开始和修改结果相同，导致其他线程认为该值没有发生变化。

#### 3.1 解决ABA

通过添加一个版本号实现，类似于数据库中的乐观锁的实现

在java中提供了一个类`AtomicStampedReference`

```java
User user01 = new User("1",1);
        User user02 = new User("2",2);
        //初始化值和初始化一个版本号
        AtomicStampedReference<User> atomicStampedReference = new AtomicStampedReference<>(user01,100);
        boolean b = atomicStampedReference.compareAndSet(user02, user01, 100, 101);
        boolean b1 = atomicStampedReference.compareAndSet(user01, user02, 101, 101);
        boolean b2 = atomicStampedReference.compareAndSet(user01, user02, 100, 101);
        System.out.println(b2);
```

比较两次，一次为值，一次为版本号，这个时候ABA问题就被解决了

### 4. JUC三大线程类

#### 4.1 CountDownLatch

所有线程执行完毕以后，主线程才开始执行。

```java
CountDownLatch countDownLatch = new CountDownLatch(3);
        for(int i = 0 ; i < 3 ; i++){
            new Thread(()->{
                System.out.println("输出");
                countDownLatch.countDown();
            },"线程为："+i).start();

        }
        countDownLatch.await();
        System.out.println("主线程执行");

输出
输出
输出
主线程执行
```

**原理实现：**在创建对象的时候，传入一个需要等待的线程的数量。当线程完成任务以后通过`countDown()`方法减一，当这个值减为0的时候，通过`await()`方法等待的线程被唤醒，执行。

#### 4.2 CyclicBarrier

当指定数量的线程就绪以后，开始执行代码

```java
CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        for(int i = 0 ; i < 5 ; i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"\t进入");
                try{
                    TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+"\t执行完成");
            },"线程为："+i).start();
        }

/**

	
线程为：1	进入
线程为：0	进入
线程为：2	进入
线程为：3	进入
线程为：4	进入
线程为：0	执行完成
线程为：2	执行完成
线程为：4	执行完成

此时线程处于阻塞状态，等待另一个线程的进入，才可以执行任务

*/
```

**实现原理：**构造方法传入一个线程数量代表需要达到这个数量以后才可以执行之后的代码，如果线程数量不够，则线程阻塞等待

#### 4.3 Semaphore

对线程进行限流

```java
Semaphore semaphore = new Semaphore(3);
        for(int i = 0 ; i < 5 ; i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"\t进入执行");
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"\t离开");
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            },"线程为："+i).start();
        }
//输出
线程为：0	进入执行
线程为：2	进入执行
线程为：0	离开
线程为：1	进入执行
线程为：1	离开
线程为：2	离开
线程为：3	进入执行
线程为：3	离开
线程为：4	进入执行
线程为：4	离开
```

### 5. Callable

创建线程任务的另一种方式

#### 5.1 与Runnable的区别

|          | Runnable | Callable |
| -------- | -------- | -------- |
| 返回值   | 无       | 有       |
| 抛出异常 | 无       | 有       |
| 执行方法 | run      | cal      |

#### 5.2 执行

- 我们发现在`Thread`类中没有直接传入这个接口的构造方法

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200417163359.png)

- 通过传入`Runnbale`的实现类实现传入`Callable`接口

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200417163544.png)

```java
Thread thread = new Thread(new FutureTask<Integer>(()->{return 1;}));
```

### 6. Java故障排除

- `jps -a` 打印正在执行的java程序
- `jstack ID号`，查询具体出错的行数

### 7. 线程状态

- `NEW`开始创建线程
- `RUNNABLE`线程进入就绪状态
- `BLOCKED`线程进入阻塞状态
- `WAITING`线程等待
- `TIMED_WAITING`线程超时等待
- `TERMINATED`线程销毁