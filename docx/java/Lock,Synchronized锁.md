## 1.Synchronized锁

底层是`monitor`监视器，每一个对象再创建的时候都会常见一个`monitor`监视器，在使用`synchronized`代码块的时候，会在代码块的前后产生一个`monitorEnter和monitorexit`指令，来标识这是一个同步代码块。

#### 1.1 执行流程

线程遇到同步代码块，给这个对象`monitor`对象加`1`，当线程退出当前代码块以后，给这个对象的`monitor`对象减一，如果`monitor`指令的值为`0`则当前线程释放锁。

#### 1.2 反编译源码

**同步代码块反编译**

```java
public void test01(){
        synchronized (this){
            int num = 1 ;
        }
    }
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200416174306.png)

两次`monitorexit`的作用是避免同步代码块无法跳出，因此存在两种，**正常退出和异常退出**

**同步方法反编译**

```java
public synchronized  void test01(){
            int num = 1 ;
    }
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200416174422.png)

可以发现其没有在同步方法前后添加`monitor`指令，但是在其底层实际上也是通过`monitor`指令实现的，只不过相较于同步代码块来说，他是隐式的。

#### 1.3 锁升级

在`JDK1.5`的时候对于`synchronzied`做了一系列优化操作，增加了诸如：偏向锁，轻量级锁，自旋锁，锁粗化，重量级锁的概念。

##### 1.3.1 偏向锁

在一个线程在执行获取锁的时候，当前线程会在`monitor`对象中存储指向该线程的ID。当线程再次进入的时候，不需要通过CAS的方法再来进行加锁或者解锁，而是检测偏向锁的ID是不是当前要进行的线程，如果是，直接进入。

偏向锁，**适用于一个线程执行任务的情况**

在`JDK1.6`中，默认是开启的。可以通过`-XX:-UseBiasedLocking=false`参数关闭偏向锁

##### 1.3.2 轻量级锁

轻量级锁是指锁为偏向锁的时候，该锁被其他线程尝试获取，此时偏向锁升级为轻量级锁，其他线程会通过自旋的方式尝试获取锁，线程不会阻塞，从而提供性能

升级为轻量级锁的情况有两种：

- 关闭偏向锁
- 有多个线程竞争偏向锁的时候

**具体实现：**

线程进行代码块以后，如果同步对象锁状态为无锁的状态，虚拟机将首先在当前线程的栈帧中创建一个锁记录的空间。这个空间内存储了当前获取锁的对象。

**使用情况：**

两个线程的互相访问

##### 1.3.3 重量级锁

在有超过2个线程访问同一把锁的时候，锁自动升级为重量级锁，也就是传统的`synchronized`，此时其他未获取锁的线程会陷入等待状态，不可被中断。

由于依赖于`monitor`指令，所以其消耗系统资源比较大

**上面的三个阶段就是锁升级的过程**

##### 1.3.4 锁粗化

当在一个循环中，我们多次使用对同一个代码进行加锁，这个时候，JVM会自动实现锁粗化，即在循环外进行添加同步代码块。

**代码案例：**

锁粗化之前：

```java
for (int i = 0; i < 10; i++) {
            synchronized (LockBigDemo.class){
                System.out.println();
            }
        }
```

锁粗化之后：

```java
synchronized (LockBigDemo.class){
            for (int i = 0; i < 10; i++) {
                    System.out.println();
            }
        }
```

**本次关于`synchronized`的底层原理没有以代码的方式展开，之后笔者会出一篇`synchronized`底层原理剖析的文章**

## 2. Lock锁

一个类级别的锁，需要手动释放锁。可以选择性的选择设置为公平锁或者不公平锁。等待线程可以被打断。

底层是基于`AQS`+`AOS`。`AQS`类完成具体的加锁逻辑，`AOS`保存获取锁的线程信息

#### 2.1 ReentrantLock

我们以`ReentrantLock`为例解析一下其加锁的过程。

##### 2.1.1 lock方法

首先通过`ReentrantLock`的构造方法的布尔值判断创建的锁是公平锁还是非公平锁。

假设现在创建的是非公平锁，他首先会判断锁有没有被获取，如果没有被获取，则直接获取锁；

如果锁已经被获取，执行一次自旋，尝试获取锁。

如果锁已经被获取，则将当前线程封装为`AQS`队列的一个节点，然后判断当前节点的前驱节点是不是`HEAD`节点，如果是，尝试获取锁；如果不是。则寻找一个安全点（线程状态位`SIGNAL=-1`的节点）。

开始不断自旋。判断前节点是不是`HEAD`节点，如果是获取锁，如果不是挂起。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200417215300.png)

**源码解读：**

- 非公平锁`lock`

```java
final void lock() {
    //判断是否存在锁
            if (compareAndSetState(0, 1))
                //获取锁
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
```

```java
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
```

```java
//非公平锁的自旋逻辑
protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }

final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
    	//获取锁状态
            int c = getState();
    	//如果锁没被获取，获取锁
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
    //当前线程已经获取到了锁
            else if (current == getExclusiveOwnerThread()) {
                //线程进入次数增加
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
```

```java
//将线程封装为一个线程节点，传入锁模式，排他或者共享
private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // 获取尾节点
        Node pred = tail;
    //如果尾节点不为Null，直接将这个线程节点添加到队尾
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
    //为空，自旋设置尾节点
        enq(node);
        return node;
    }

private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            //初始化
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                //将头结点和尾结点都设置为当前节点
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```

```java
//尝试入队
final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                //获取节点的前驱节点，如果前驱节点为head节点，则尝试获取锁
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //如果不是，寻找安全位
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
    //前驱节点已经安全
        if (ws == Node.SIGNAL)
            return true;
    //前驱节点不安全，寻找一个线程状态为`Signal`的节点作为前驱节点
        if (ws > 0) {
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            //否则直接设置这个前驱节点的线程等待状态值
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

//中断线程
private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }
```

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200417215559.png)

##### 2.1.2 unlock方法

**代码解读：**

```java
public void unlock() {
        sync.release(1);
    }
```

```java
public final boolean release(int arg) {
    //尝试释放锁
        if (tryRelease(arg)) {
            //获取队列头元素，唤醒该线程节点，执行任务
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
```

```java
protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
    //判断是否为当前线程拥有锁
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
    //释放成功
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
```

```java
private void unparkSuccessor(Node node) {
    
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);
      
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
    //唤醒下一个节点
        if (s != null)
            LockSupport.unpark(s.thread);
    }
```

##### 2.1.3 Node节点

```java
/** 共享锁，读锁使用 */
        static final Node SHARED = new Node();
        /** 独占锁*/
        static final Node EXCLUSIVE = null;

        /** 不安全线程 */
        static final int CANCELLED =  1;
        /** 需要进行线程唤醒的线程 */
        static final int SIGNAL    = -1;
        /**condition等待中 */
        static final int CONDITION = -2;

		//线程等待状态
		volatile int waitStatus;

        volatile Node prev;

        volatile Node next;

        volatile Thread thread;
        Node nextWaiter;
```

### 3. Lock锁和Synchronized的区别

- `Lock`锁是API层面，`synchronized`是`CPU`源语级别的
- `Lock`锁等待线程可以被中断，`synchronized`等待线程不可以被中断
- `Lock`锁可以指定公平锁和非公平锁，`synchronized`只能为非公平锁
- `Lock`锁需要主动释放锁，`synchronized`执行完代码块以后自动释放锁