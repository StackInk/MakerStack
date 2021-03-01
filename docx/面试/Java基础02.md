## Java常用API面试总结

### 1.谈一下Object类

![](image/object.png)

>Java中所有类的超类，所有的类默认继承Object

- `hashcode()`返回一个哈希码值
- `getClass()`返回此对象的运行时类，即字节码对象
- `toString()`返回对象的字符串表现形式。（一般子类会重写）
- `equals`比较两个对象是否相等
- `clone`克隆一份对象，此时克隆的对象在堆内存中重新创建，并返回了内存地址
  - 在具体使用的时候需要实现`Cloneable`接口否则抛出`CloneNotSuppoertedException`异常
  - `Cloneable`的作用是表示`clone`可以合法的被子类调用
- `toString`返回这个对象的字符串表示。
- `wait` 线程等待
- `notify` 唤醒在此对象监视器上器的单个线程
- `notifyAll` 唤醒在此对象监视器上全部的线程
- `finalize`在JVM执行垃圾 回收之前调用

#### 1.1 衍生的面试题

- 什么是类对象？

Java中类是对一组行为或者特征的描述，对象则为所描述特征和行为的具体实现。而作为概念层次的类，其本身也拥有某些共同的特性，如都具有类名称、由类加载器加载，都具有父类，属性和方法等。于是，Java中专门定义了一个类`Class`去描述其他类 所具有的特性，所以，从这个角度来看类本身就是`Class`类的对象。

- `equals`和`==`的区别 

|          | equals                                          | ==       |
| -------- | ----------------------------------------------- | -------- |
| 基本类型 | 自动转换为包装类，比较具体值(包装类重写了方法)  | 比较数值 |
| 引用类型 | 比较内存地址，如果重写则按照 重写的规则进行比较 | 比较地址 |

- 为什么重写`equals`方法必须就必须重写`hashcode`方法 

`hashcode`方法的作用就是返回一个对象的哈希值。哈希算法强调，同一个对象的哈希值是唯一的，这也就是没有重写`equals`方法之前，判断两个对象是否相等的依据。而重写`equals`方法以后，判断两个对象的依据发生了变化，就有可能出现 ，两个对象的哈希值不想等，但是他们通过`equals`方法执行的结果却是`true`，此时就违反了同一个对象的哈希值是唯一的。

- `equals`和`hashcode`的关系

`equals`相同，但是`hashcode`不一定相同，此时两个对象必然不是同一个对象

`hashcode`相同，`equals`不相同，此时两个对象必然是同一个对象

- 为什么要有`hashcode`方法

一个例子：在向`Set`集合中添加数据的时候，首先需要判断这个集合中是否存在这个元素，不存在才添加，如果没有`hashcode`的话，需要对集合进行遍历，才可以，此时的时间复杂度达到了`O(n)`，而使用`hashcode`计算出要添加对象的哈希值，直接判断Set中是否存在这个hash值就可以了。Set的底层结构是Map。

- `wait`和`sleep`的区别

|             | wait                               | sleep            |
| ----------- | ---------------------------------- | ---------------- |
| 锁          | 会释放锁                           | 不会释放锁       |
| interrupted | 会被中断抛出异常                   | 会被中断抛出异常 |
| 使用范围    | 只能在同步代码块或者同步方法中使用 | 任意地方         |
| 方法类型    | 非静态                             | 静态             |

- `finalize`，`final`，`finally`的区别

`finalize`是`Obejct`类的方法，在JVM回收对象的之前调用

`final`是一个关键字，可以用来修饰类，成员变量，局部变量，成员方法。修饰类，不能被继承；修饰变量，该变量不可以被修改，变为常量；修饰方法，该方法不能被重写

### 2. String,StringBuffer,StringBuilder

#### 2.1 String 类的不可变性

先来了解一下String底层实现：

- `String`底层在`JDK9`以后使用字节数组来存储字符串，在`JDK8`及以下使用`char[]`来存储字符串。并且均被`final`修饰
  - 为什么要修改字符为字节？节省空间，字符占据两字节，字节占用一个字节
  - `final`修饰的作用。`final`修饰以后数组的地址不可以变化，但是这个地址指向的内容是可变的，也就是数组可变。可以直接修改数组值
- `String`每一次更改，都会在堆内存中创建一个新的对象。这也就导致了原字符串更改以后仍然不变，因为其 地址发生了变化
- 为什么要将字符串设置 为不可变。这个可以应该是`Sun`公司的设计师想把`String`作为一种数据类型来使用，因此将之设置为了不可变，其他类也无法继承修改。而且设计师没有提供直接修改数组值的方法。

##### 2.1.1 常量池

>我们经常听说，字符串被保存在常量池。而我们常见的常量池有两种

- 静态常量池（堆中）

即`.class`文件中的常量池，在编译时期，编译器会优化所有的常量，将之组合。而`String`类型的变量(直接通过`==`赋值)保存的地址就是字符串在常量池中的地址

- 运行时常量池（方法区）

类加载的时候，会将静态常量池转换为运行时常量池。我们常说的就是运行时常量池。两个常量池最重要的区别是：运行时常量池是动态的，开发者可以通过代码将新的常量放入池中，这种特性被开发人员利用比较多的就是String类的intern()方法。

#### 2.2 StringBuffer和StringBuilder

>我们经常听过String是不可变的。StringBuffer和StringBuilder是可变的。而且，StringBuffer是安全的，StringBuilder是不安全的。那么他们是怎么实现可变和安全的

- `StringBuilder`和`StringBuffer`两个类都是通过继承`AbstractStringBuilder`来存储字符串的

```java
abstract class AbstractStringBuilder implements Appendable, CharSequence {
    /**
     * The value is used for character storage.
     */
    byte[] value;
```

他和`String`的区别就是这个存储字符串的数组没有被锁定即被`final`修饰，而且他提供了修改这个数组的方法，所以他的两个子类就是可变的。

- `StringBuilder`的安全性是如何保证的？

```java
 @Override
    public synchronized StringBuffer append(CharSequence s, int start, int end)
    {
        toStringCache = null;
        super.append(s, start, end);
        return this;
    }
```

通过添加了一个同步方法来实现线程的不可同时操作

- 还有一个特性。这两个类可以自动扩容

```java
public AbstractStringBuilder append(String str) {
        if (str == null) {
            return appendNull();
        }
        int len = str.length();
        ensureCapacityInternal(count + len);//实现扩容
        putStringAt(count, str);
        count += len;
        return this;
    }
```

```java
private void ensureCapacityInternal(int minimumCapacity) {
        // overflow-conscious code
        int oldCapacity = value.length >> coder;
        if (minimumCapacity - oldCapacity > 0) {
            value = Arrays.copyOf(value,
                    newCapacity(minimumCapacity) << coder);
        }
    }
```

### 3. 谈一下包装类的实现

- 基本类型的数据通过各个包装类的静态方法`valueOf`或者构造方法转换为各个包装类类型
- 包装类型通过`intValue(),doubleValue()`等方法将包装类型转换为基本类型

其中有一个点：**高速缓存存储器**

- 它的作用是：缓存了一个字节的数据，节省了创建对象的时间和空间
- 在`-128~127`的区间内，其赋值类似于常量池，修改了值则对其返回一个高速缓存存储器中对应数据的地址

### 4. 什么是自动装箱和自动拆箱

- 自动装箱
  - 当执行方法调用的时候，传递一个基本类型的值给一个方法类型为包装类型的方法中
  - 将一个基本类型的变量赋值给一个包装类型对象

```java
List<Integer> list = new LinkedList<>();
list.add(1);//第一种情况的自动装箱

Integer id = 1 ; //第二种情况的自动装箱
```

- 自动拆箱
  - 将包装类型的数据转换为基本类型

```java
Integer oldId = 12 ;
int newID = oldId ;
```

### 5. 什么时候不适合使用包装类

```java
Long sum = 0L ;
for(long i = 0 ; i < 100 ; i++){
    sum+=i ;
}
```

在这个场景中，`sum`每一次递增都会发生一次自动装箱和自动拆箱，导致其运行速度会变慢

## Java集合

### 1. 谈一下Set，List 集合及其区别

Set集合无序，不可重复，其常用的`HashSet`和`TreeSet`都是基于`Map`集合实现的。HashSet基于HashMap即哈希表实现，有自然顺序；TreeSet基于TreeMap实现即红黑树实现。HashSet的子类LinkedHashSet基于LinkedHashMap实现，保证了有序性，其均为线程不安全。

List结合有序，可重复。常用的集合为ArrayList，LinkedList和Vector，其中ArrayList基于数组实现，默认长度为10   ，每次递增的长度为原来的1.5倍。Vector基于数组实现，默认长度为10，线程安全；LinkedList,基于链表实现，线程不安全。

### 2. ArrayList和LinkedList的区别

- 都不安全，线程不同步
- ArrayList底层是Object数组初始容量为10，递增策略为1.5倍；LinkedList基于双向链表实现，本身存储了一个节点的值。
- ArrayList查找和修改快，LinkedList插入和删除快
- ArrayList支持随机访问，通过RandomAccess接口实现。随机访问是指通过下标获取对象
- 内存空间占用。ArrayList的空间用主要体现在数组尾部的空间不能完全占用；LinkedList的空间占用主要体现在每一个节点中都添加了前节点和后节点

### 3. ArrayList实现RandomAccess接口的作用？为什么LinkedList没有实现

RandomAccess接口仅仅只是一个声明，声明实现该接口的类具有随机访问的能力，同时实现该接口的类一般都会采用for循环来进行遍历（性能高）。随机访问指：是否可以通过下标直接访问到对象。

4. ### Array和ArrayList有何区别？什么时候更适合使用Array？

- Array是一个可以容纳基本类型的对象，而ArrayList仅仅只能容纳对象
- Array指定大小，而ArrayList大小可以指定可以动态增加

### 5.HashMap 的实现原理/底层数据结构？JDK1.7 和 JDK1.8

Jdk1.8 HashMap基于哈希表，链表和红黑树实现；新元素在链表的添加方式改为尾部添加

Jdk1.7 HashMap基于哈希表，链表实现。新元素在链表中的添加方式为头部添加

### 6. HashMap 的 put 方法的执行过程？

当Put元素的时候，首先会检查当前table是否存有值，如果没有值则通过resize方法创建一个初始容量为16的数组，进行添加。如果此时hash算法计算出来的下标数组位有值，则比较当前新添加的元素和该值是否相同，如果相同，直接替换；如果不同，则检查当前节点是不是红黑树的节点，如果是红黑树的节点，则进行红黑树节点添加；如果不是则进行链表添加，循环遍历链表中的值，如果出现和新添加的元素相同的key则直接替换，如果没有，则添加到尾部。当添加的时候，发现链表的长度大于或者等于8了，则进行转换为红黑树。在转换红黑树的方法中，首先判断了一次该数组的容量是否大于64，如果大于64，则将链表转换为红黑树。

### 7. HashMap 的 get 方法的执行过程？

当get方法的时候，传入一个key，首先通过hash算法，计算出这哥key所对应的的哈希值，然后通过和数组的容量-1进行&运算，得出下标。然后遍历这个位的链表，比较其key值，返回对应节点的value。如果是红黑树，也是相同的道理。

### 8.HashMap 的 resize 方法的执行过程？

当数组实际承载容量>负载因子*数组容量时扩容为原来的2倍

### 9.HashMap 的 size 为什么必须是 2 的整数次方？

hash算法的本质就是将每一个key尽可能的均匀分配值，即此时的计算得到的哈希值已经是一个哈希值非常平均的数字了，所以最好获取index的方法就是在不影响原哈希值的基础上进行对应容量的下标计算，而2的整数次方均为进制位为1的二进制数，所以可以更好的避免哈希碰撞。

### 10. JDK1.7的HashMap并发情况下为什么会出现死锁？

现在假设有一个容量为2的哈希表，第一个节点存储A->B->C，第二个节点为D->E。此时线程T1需要新添加元素，并且该元素的哈希值与第一个节点和第二个节点的哈希值都不同，此时需要扩容数组(为了方便，不考虑负载因子先。原理一样)

T1线程refresh数组容量为4.在这个时候，线程T2进来，T1阻塞，T2线程put元素，与T1相同也需要扩容数组。在扩容以后，T2重新计算所有节点的下标，但A和C出现了哈希冲突，于是构建链表，根据遍历的结构可知，A首先进来，C之后进来，所以在JDK1.7中会将C置于A的前面即C.next = A

T2线程执行完毕，T1线程重新被调度，继续refresh，此时的链表结构为C->A;于是线程T2会将A的下一个节点设置为C.next.这时出现环形链表。当get的时候出现死锁。
