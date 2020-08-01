今天呢，和大家聊一下`ThreadLocal`。

### 1. 是什么？

`JDK1.2`提供的的一个线程绑定变量的类。

**他的思想就是：给每一个使用到这个资源的线程都克隆一份，实现了不同线程使用不同的资源，且该资源之间相互独立**

### 2. 为什么用？

**思考一个场景**：数据库连接的时候，我们会创建一个`Connection`连接，让不同的线程使用。这个时候就会出现多个线程争抢同一个资源的情况。

这种多个线程争抢同一个资源的情况，很常见，我们常用的解决办法也就两种：**空间换时间，时间换空间**

没有办法，鱼与熊掌不可兼得也。就如我们的`CAP`理论，也是牺牲其中一项，保证其他两项。

而针对上面的场景我们的解决办法如下：

- 空间换时间：为每一个线程创建一个连接。
  - 直接在线程工作中，创建一个连接。(**重复代码太多**)
  - 使用`ThreadLocal`，为每一个线程绑定一个连接。
- 时间换空间：对当前资源加锁，每一次仅仅存在一个线程可以使用这个连接。

通过`ThreadLocal`为每一个线程绑定一个指定类型的变量，相当于线程私有化

### 3. 怎么用？

```java
ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
threadLocal.get();
threadLocal.set(1);
threadLocal.remove();
```

没错，这四行代码已经把`ThreadLocal`的使用方法表现得明明白白。

- `get`从`ThreadLocal`拿出一个当前线程所拥有得对象
- `set`给当前线程绑定一个对象
- `remove`将当前线程绑定的当前对象移除

**记住在使用的以后，一定要remove,一定要remove,一定要remove**

为什么要`remove`。相信不少小伙伴听到过`ThreadLocal`会导致内存泄漏问题。

没错，所以为了解决这种情况，**所以你懂吧，用完就移除，别浪费空间（渣男欣慰）**

看到这，脑袋上有好多问号出现了（**小朋友你是否有很多问号？**）

**为啥会引发内存泄漏？**

**为啥不remove就内存泄漏了**

**它是怎么讲对象和线程绑定的**

**为啥get的时候拿到的就是当前线程的而不是其他线程的**

**它怎么实现的？？？**

来吧，**开淦，源码来**

### 4. 源码解读

先来说一个思路：**如果我们自己写一个`ThreadLocal`会咋写？**

线程绑定一个对象。**这难道不是我们熟知的`map`映射？**有了`Map`我们就可以以线程为`Key`,对象为`value`添加到一个集合中，然后各种`get,set,remove`操作，想怎么玩就怎么玩，搞定。**😀**

**这个时候，有兄弟说了。你这思路不对啊，你这一个线程仅仅只能存放一个类型的变量，那我想存多个呢？**

摸摸自己充盈的发量，你说出了一句至理名言：**万般问题，皆系于源头和结果之中。**

从结果考虑，让开发者自己搞线程私有（估计被会开发者骂死）

来吧，从源头考虑。现在我们的需求是：**线程可以绑定多个值，而不仅仅是一个**。嗯，没错，兄弟们把你们的想法说出来。

**让线程自己维护一个Map，将这个`ThreadLocal`作为`Key`,对象作为`Value`不就搞定了**

**兄弟，牛掰旮旯四**

---

此时，又有兄弟说了。按照你这样的做法，将`ThreadLocal`扔到线程本身的的Map里，那岂不是这个`ThreadLocal`**一直被线程对象引用，所以在线程销毁之前都是可达的，都无法`GC`呀，有`BUG`啊**？？？

**好，问题。**这样想，既然由于线程和`ThreadLocal`对象存在引用，导致无法`GC`，那我将你和线程之间的引用搞成弱引用或者软引用不就成了。一`GC`你就没了。

**啥，你不知道啥是弱引用和软引用？？？**

前面讲过的东西，算啦再给你们复习一波。

`JDK`中存在四种类型引用，默认是强引用，也就是我们经常干的事情。疯狂`new,new,new`。这个时候创建的对象都是强引用。

- 强引用。直接`new`
- 软引用。通过`SoftReference`创建，在内存空间不足的时候直接销毁，即它可能最后的销毁地点是在老年区
- 弱引用。通过`WeakReference`创建，在`GC`的时候直接销毁。即其销毁地点必定为伊甸区
- 虚引用。通过`PhantomReference`创建，它和不存也一样，**非常虚，只能通过引用队列在进行一些操作，主要用于堆外内存回收**

好了，回到正题，上面的引用里最适合我们当前的场景的就是弱引用了，**为什么这个样子说：**

在以往我们使用完对象以后等着`GC`清理，但是对于`ThreadLocal`来说，即使我们使用结束，也会因为线程本身存在该对象的引用，处于对象可达状态，垃圾回收器无法回收。这个时候当`ThreadLocal`太多的时候就会出现内存泄漏的问题。

而我们将`ThreadLocal`对象的引用作为弱引用，那么就很好的解决了这个问题。当我们自己使用完`ThreadLocal`以后，**当`GC`的时候就会将我们创建的强引用直接干掉，而这个时候我们完全可以将线程`Map`中的引用干掉，于是使用了弱引用，这个时候大家应该懂了为啥不使用软引用了吧**

**还有一个问题：为什么会引发内存泄漏呢？**

了解`Map`结构的兄弟们应该清楚，内部实际就一个节点数组，对于`ThreadLocalMap`而言，内部是一个`Entity`，它将`Key`作为弱引用，`Value`还是强引用。如果我们在使用完`ThreadLocal`以后，没有对`Entity`进行移除，会引发内存泄漏问题。

`ThreadLocalMap`提供了一个方法`expungeStaleEntry`方法用来排除无效的`Entity`（`Key`为空的实体）

**说到这里，有一个问题我思考了蛮久的，value为啥不搞成弱引用，用完直接扔了多好**

最后思考出来得答案(按照源码推了一下)：

**不设置为弱引用，是因为不清楚这个`Value`除了`map`的引用还是否还存在其他引用，如果不存在其他引用，当`GC`的时候就会直接将这个Value干掉了，而此时我们的`ThreadLocal`还处于使用期间，就会造成Value为null的错误，所以将其设置为强引用。**

而为了解决这个强引用的问题，它提供了一种机制就是上面我们说的将`Key`为`Null`的`Entity`直接清除

**到这里，这个类的设计已经很清楚了。接下来我们看一下源码吧！**

---

需要注意的一个点是：**`ThreadLocalMap`解决哈希冲突的方式是线性探测法。**

**人话就是：如果当前数组位有值，则判断下一个数组位是否有值，如果有值继续向下寻找，直到一个为空的数组位**

**Set方法**

```java
class ThreadLocal	
	public void set(T value) {
    	//拿到当前线程
        Thread t = Thread.currentThread();
    //获取当前线程的ThreadLocalMap
        ThreadLocalMap map = getMap(t);
        if (map != null)
            //如果当前线程的Map已经创建，直接set
            map.set(this, value);
        else
            //没有创建，则创建Map
            createMap(t, value);
    }

	private void set(ThreadLocal<?> key, Object value) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
			//拿到当前数组位，当前数组位是否位null，如果为null，直接赋值，如果不为null，则线性查找一个null，赋值
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
        //清除一些失效的Entity
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }


	ThreadLocalMap getMap(Thread t) {
    //获取当前线程的ThreadLocalMap
        return t.threadLocals;
    }

	void createMap(Thread t, T firstValue) {
        	//当前对象作为Key，和我们的设想一样
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }
```

**Get方法**

```java
	public T get() {
        //获取当前线程
        Thread t = Thread.currentThread();
        //拿到当前线程的Map
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            //获取这个实体
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                //返回
                return result;
            }
        }
        return setInitialValue();
    }

	private Entry getEntry(ThreadLocal<?> key) {
        //计算数组位
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
        //如果当前数组有值，且数组位的key相同，则返回value
            if (e != null && e.get() == key)
                return e;
            else
                //线性探测寻找对应的Key
                return getEntryAfterMiss(key, i, e);
        }

	private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    //排除当前为空的Entity
                    expungeStaleEntry(i);
                else
                    //获取下一个数组位
                    i = nextIndex(i, len);
                e = tab[i];
            }
        //如果没有找到直接返回空
            return null;
        }

```

**remove**

```java
	public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
     }

	private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
        //拿到当前的数组，判断是否为需要的数组位，如果不是线性查找
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    //清空位NUll的实体
                    expungeStaleEntry(i);
                    return;
                }
            }
        }
```

**我们可以看到一个现象：在`set`,`get`,`remove`的时候都调用了`expungeStaleEntry`来将所有失效的`Entity`移除**

看一下这个方法做了什么

```java
private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // 删除实体的Value
            tab[staleSlot].value = null;
    //置空这个数组位
            tab[staleSlot] = null;
    //数量减一
            size--;

            // 重新计算一次哈希，如果当前数组位不为null，线性查找直到一个null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }
```

