## Java集合类实现原理

![](image/集合类继承图.jpg)

### 1.Iterable接口

- 定义了迭代集合的迭代方法

```java
iterator()
forEach() 对1.8的Lambda表达式提供了支持
```

### 2. Collection接口

- 定义了集合添加的通用方法

```java
int size();
boolean isEmpty();
boolean contains();
boolean add()
boolean addAll()
boolean remove()
    	removeAll()
Object[] toArray()
```

### 3.List接口

- 元素被添加到集合中以后，取出的时候是按照放入顺序。
- `List`可以重复。
- 存在下标，可以直接依靠下标取值

```java
E get()
E set()
E indexOf()
int lastIndexOf()
ListIterator listIterator()
```

#### 3.1 ArrayList类

- 底层是一个`Object`数组。

```java
transient Object[] elementData; // non-private to simplify nested class access
```

- 初始容量为10

```java
private static final int DEFAULT_CAPACITY = 10;
```

- 当数组容量不够是自动扩容为以前的1.5倍

```java
private int newCapacity(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
```

- 数组最大容量为`Integer.MAX_VALUE-8`

```java
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

- 线程不安全

#### 3.2.Vector类（不常用）

- 底层是一个`Object`数组

```java
protected Object[] elementData;
```

- 初始容量为10

```java
public Vector() {
        this(10);
    }
```

- 数组容量不够的时候自动扩容为原来的一倍

```java
int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                         capacityIncrement : oldCapacity);
```

- 数组最大容量为

```java
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

- 线程安全

#### 3.3 LinkedList

- 底层是一个列表

```java
/**
     * Pointer to first node.
     */
    transient Node<E> first;

    /**
     * Pointer to last node.
     */
    transient Node<E> last;
```

- 存放节点个数

```java
transient int size = 0;
```

- 默认构造方法增加元素实现原理

```java
//当默认构造的时候,创建集合的时候
public LinkedList() {
    }
//使用添加方法,直接将元素添加到末尾
public boolean add(E e) {
        linkLast(e);
        return true;
    }
//给尾部添加元素
void linkLast(E e) {
    	//获取最后一个元素
        final Node<E> l = last;
    	//新创建一个界面，其尾结点为null
        final Node<E> newNode = new Node<>(l, e, null);
    	//将数组中存储最后一个界面的元素复制
        last = newNode;
    	//如果此时集合为null，则另第一个节点也为该元素，否则就将这个元素的下一个节点设置为该元素节点
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
    //节点数量增加
        size++;
        modCount++;
    }

```

- 指定下标增加元素实现原理

```java

//LinkedList支持指定的索引出增加节点
public void add(int index, E element) {
    	//检查传入的索引是否符合要求
        checkPositionIndex(index);
		//如果这个索引是最后一个节点，则直接添加
        if (index == size)
            linkLast(element);
        else
            //否则
            linkBefore(element, node(index));
    }
//返回了指定下标的Node
Node<E> node(int index) {
        // assert isElementIndex(index);
		//如果此时的下标小于节点的一半，相当于一个二分查找的方法，
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    //将需要插入的元素进行插入
void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
        modCount++;
    }
```



>实现的思想可以归结为：每一次的插入或者移除，都是通过`node()`方法获取指定的`Node`节点，然后通过`linkBefore`或者`linkLast`这些方法来具体进行链表的操作。

### 4.Set接口

- 插入无序
- 元素不能重复
- 底层均为`Map`集合实现

#### 4.1 TreeSet类

先来瞅一眼这个类的继承关系吧

![](image/TreeSet集合继承图.png)

- 实现了`AbstractSet`拥有了`Set`的属性和方法
- 实现了`NavigableSet`，支持一系列导航方法，可以进行精确查找

剖析一下这个类的源码

- 底层实现`TreeMap`结构

```java
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable
{
    /**
     * 存放生成的TreeMap集合
     */
    private transient NavigableMap<E,Object> m;

    // 作为值添加到TreeMap中，即每一个Entry的键不同但值相同，都是一个对象的地址
    private static final Object PRESENT = new Object();
    
    
    public TreeSet() {
        this(new TreeMap<>());
    }
    
    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }

//添加方法
    public boolean add(E e) {
        return m.put(e, PRESENT)==null;
    }
    
```

- 进行了排序。(在HashMap原理进行分析)

#### 4.2 HashSet类

- 底层基于`HashMap`

```java
	//键
	private transient HashMap<E,Object> map;
	
    // 值
    private static final Object PRESENT = new Object();

		//构造
	public HashSet() {
        map = new HashMap<>();
    }
```

- 无序
- 不可重复

#### 4.3 LinkedHashSet类

- 底层基于`LinkedHashMap`实现，通过LinkedHashMap中的方法实现了顺序存值。具体实现可看下面的LinkedHashMap

```java
public LinkedHashSet() {
        super(16, .75f, true);
    }
HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
```



### 5. Map类

- 键值对的形式存放数据
- 定义了通用的方法
- 不可重复

```java
int size()
isEmpty();
containsKey()
containsValue()
get()    
put()
remove()    
keyset()
values()
entrySet()
```

#### 5.1Entry类

- Map类的内部类，用来获取所有的键值

#### 5.2HashMap类

>put的时候，会通过hash算法，计算一个index，这个index就是节点数组的下标，此时这个实体就被存储到这个数组中。但是由于这个hash算法不能保证任何一个key值计算出来的hash值均相同，所以采用链表的方式，挂载相同的index的实体。在1.8以后，当链表的节点数量大于或者等于8的时候且数组的容量大于64的时候，就会将链表转换为红黑树

- 底层实现：数组+链表或者红黑树

```java
//保存的数组，初始化16个
transient Node<K,V>[] table;
//为entrySet和value提供一个缓存
transient Set<Map.Entry<K,V>> entrySet;
//元素的数量
transient int size;
//初始容量
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
//最大容量
static final int MAXIMUM_CAPACITY = 1 << 30;
//数组递增的策略 当size > capacity*loadFacotor的时候递增
final float loadFactor;
```

- `Node`节点的定义(列表)

```java
static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
```

- 初始容量为16的原因

```java
//hash算法，保证哈希值平均分布，只有当为16的时候才可以最大程度的保证平均分布
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

- `put`方法

```java
//创建一个HashMap对象，并且设定它的递增策略为0.75倍
public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

static final float DEFAULT_LOAD_FACTOR = 0.75f;
//执行put方法
public V put(K key, V value) {
    //key通过hash算法计算一个index
        return putVal(hash(key), key, value, false, true);
    }

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
    //第一次进入为null,所以执行初始化容器大小
        if ((tab = table) == null || (n = tab.length) == 0)
            //此时返回的就是初始化容器以后的大小即16
            n = (tab = resize()).length;
    	//计算下标，如果等于null，直接赋值
    	if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
    	else {
            //如果该数组刚好有值，则采用链表或者红黑树的方式添加数据节点
            Node<K,V> e; K k;
            //判断两个节点是否相等
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            //判断当前节点是否属于红黑树节点
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                //如果不是直接进行链表连接
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        //将当前节点的下一个节点设置为新的实体节点
                        p.next = newNode(hash, key, value, null);
                        //如果此时的节点容量为7那么将链表转换为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    //判断新传入的实体和当前绑定节点的子节点是否相同，如果相同直接退出
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    //进入这个子节点
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
    //修改次数
        ++modCount;
    //查看当前容器的容量是否大于threshold ,如果大于增加数组容量为原来的一倍
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
}
    
    //初始化容器大小
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
    	//旧容量为0
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
    	int oldThr = threshold;
    //设置当前容器的递增为0
        int newCap, newThr = 0;
    //此时的oldCap=0 , newThr = 0 直接else执行
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //扩容，将数组的容量和扩容因子变为原来的一倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            //初始化容器为默认16
            newCap = DEFAULT_INITIAL_CAPACITY;
            //初始化阙值
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    	//初始化存储容器数组
        table = newTab;
```

##### 5.2.1 Hash数据结构

在`java`中所有的数据结构都可以使用数组和指针即引用来实现。而Hash也成散列，就是一个链表加数组实现。

Hash数据结构具有无序的特征。这里的无序指的是存入顺序于取出顺序不一样。

什么是Hash表的负载因子？负载因子代表了Hash表的空间填充度，即负载因子越大其对空间的使用率越高，但这也造成了查询速度慢，而负载因子越小，其查询速度越快，空间填充度越低。所以在使用的过程一般会通过保持一个平衡。如HashMap的负载因子初始化为0.75.保证了两者之间的权衡。

Hash表如何存储数据？Hash表的每一次存储都会先调用一个Hash函数，而这个Hash函数最后运算的值就是所存储数据的下标。即当需要查询数据的时候，仅仅只需要调用Hash函数进行一次计算就可以得出该数据所在的下标。

##### 5.2.2 HashMap中的数据结构实现

下面详细解析一下HashMap中的Hash表的实现

在HashMap初始化的时候，首先会给内部的负载因子赋值为0.75，然后创建对象，注意此时的HashMap内部的Node数组并没有实例化。

开始put数据，此时put方法会调用putVal()方法，但在调用这个putVal方法之前，他首先通过hash算法计算了一次这个key所对应的哈希值，而在putVal()方法中，又将这个哈希值通过和数组的容量-1进行&运算，得出了在这个数组的容量范围内的一个index。此时这个key所需要存储的index正式确定。

确定key以后，需要判断该index下有没有值，如果有，判断新增的这个元素与现有这个元素是否相同，如果相同，替换该值；如果不相同，遍历这个链表，判断这个链表中是否存在和新增元素相同的值，如果不存在则直接添加到链表尾部，如果存在，替换该值；当然如果此时链表中节点的个数大于或者等于8且数组的容量大于64的时候以后就将链表转化为红黑树。

containKey方法的实现，就是直接通过hash方法计算出哈希值，然后通过&运算，获取数组下标，判断这个下标是否为该值，如果不是，则进行遍历链表或者红黑树。

containeValue方法实现，一级一级遍历时间复杂度似乎蛮高的

#### 5.3 LinkedHashMap类

我们所知道的LinkedHashMap类可以顺序的输出用户所输入的数据。下面谈一下他的实现方式

LinkedHashMap中定义了一个Entry类，继承了HashMap.Node节点类，额外定义了两个属性，before和after，还有最重要的一个方法newNode，这个方法被LinkedHashMap重写，确定了顺序性。看到这也就知道这是双向链表的两个值了。LinkedHashMap在每一次put元素之后都要将该元素的上一个节点设置为之前的那个节点。代码说明！！！

- 成员属性

```java
	// 链表的第一个节点，LinkedHashMap会保存链表的最后一个节点的属性，以方便进行节点添加
    transient LinkedHashMap.Entry<K,V> head;

  	// 链表的最后一个节点
    transient LinkedHashMap.Entry<K,V> tail;
```

- 创建对象了

```java
//老方法，new个对象再说（单身狗的呐喊）
public LinkedHashMap() {
        super();
        accessOrder = false;
    }
//直接调用HashMap的put方法
public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    //在putVal中调用了
    afterNodeAccess(e);
    afterNodeInsertion(evict);
  
```

```java
Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
        LinkedHashMap.Entry<K,V> p =
            new LinkedHashMap.Entry<>(hash, key, value, e);
        linkNodeLast(p);
        return p;
    }

// link at the end of list
    private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
        //获取最后一个节点
        LinkedHashMap.Entry<K,V> last = tail;
        //将最后一个节点定义为新增的节点
        tail = p;
        //如果等于null那么说明之前没有元素
        if (last == null)
            head = p;
        else {
            //如果有，将这个元素的上一个节点定义为之前的最后一个元素
            p.before = last;
            //最后一个节点的下一个元素定义为新元素
            last.after = p;
        }
    }
```



```java
//判断这个新的节点是否为最后一个节点，如果不是移动该节点到最后
void afterNodeAccess(Node<K,V> e) { // move node to last
        LinkedHashMap.Entry<K,V> last;
    		//查看当前最后一个节点是否为当前新增的元素
        if (accessOrder && (last = tail) != e) {
            //p为当前元素，a为下一个元素，b为上一个元素
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            //将p的下一个元素定义为null，切断和之前元素的联系
            p.after = null;
            //如果上一个元素为null ，则说明将该节点的下一个节点赋值为头结点
            if (b == null)
                head = a;
            else
                //否则，将上一个节点的下一个节点定义为a，到此，这个新的节点已经被独立出来了
                b.after = a;
            //如果此时a不为null
            if (a != null)
                //则直接赋值
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }

//永远不起作用removeEldestEntry方法永远返回false
void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K,V> first;
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            removeNode(hash(key), key, null, false, true);
        }
    }

protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }
```

#### 5.4 TreeMap类

- 底层实现：红黑树

- 继承了NavigableMap接口，NavigableMap接口继承了SortedMap接口，可支持一系列导航方法即导航操作
- 实现了Cloneable接口，可被克隆
- 自然排序

##### 5.4.1TreeMap创建源码分析

- TreeMap定义的字段

```java
//比较器
private final Comparator<? super K> comparator;
//根节点
    private transient Entry<K,V> root;
//节点数量
    private transient int size = 0;
//修改次数
    private transient int modCount = 0;
//红黑颜色判断
private static final boolean RED   = false;
    private static final boolean BLACK = true;

//节点实体
static final class Entry<K,V> implements Map.Entry<K,V> {
        K key;
        V value;
        Entry<K,V> left;
        Entry<K,V> right;
        Entry<K,V> parent;
    //默认颜色为黑色
        boolean color = BLACK;
}
```

- 创建对象

```java
public TreeMap() {
    //默认构造器
        comparator = null;
    }
public TreeMap(Comparator<? super K> comparator) {
    //传入自定义的构造器
        this.comparator = comparator;
    }
public TreeMap(Map<? extends K, ? extends V> m) {
        comparator = null;
        putAll(m);
    }
```

- Put对象

```java
 public V put(K key, V value) {
     //赋值
        Entry<K,V> t = root;
     //如果此时的root为null 
        if (t == null) {
            //检查这个key是否为null
            compare(key, key); // type (and possibly null) check
			//创建根节点
            root = new Entry<>(key, value, null);
            size = 1;//设置节点数量
            modCount++;//修改次数增加
            return null;
        }
     	//定义比较值
        int cmp;
        Entry<K,V> parent;
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
     //如果此时存在自定义比较器，根据比较器规则进行二分比较
        if (cpr != null) {
            do {
                parent = t;
                cmp = cpr.compare(key, t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else
                    //形同替换value值
                    return t.setValue(value);
            } while (t != null);
        }
        else {
            //使用默认的比较器，查找方法一样
            if (key == null)
                throw new NullPointerException();
            @SuppressWarnings("unchecked")
                Comparable<? super K> k = (Comparable<? super K>) key;
            do {
                parent = t;
                cmp = k.compareTo(t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else
                    return t.setValue(value);
            } while (t != null);
        }
     //没有当前节点，则创建该元素的实体节点
        Entry<K,V> e = new Entry<>(key, value, parent);
     //根据比较器规则，添加节点
        if (cmp < 0)
            parent.left = e;
        else
            parent.right = e;
     //红黑树自动平衡算法
        fixAfterInsertion(e);
     //节点数量，修改数量递增
        size++;
        modCount++;
        return null;
    }
```

##### 5.4.2 TreeMap对象增加的过程

创建一个TreeMap，此时可以传入一个比较器，如果不传入按照默认的自然顺序进行比较。

put对象，首先，检查该root节点是否为null，如果为null，检查当前传入key是否为null，不为null，则直接创建一个root节点。如果当前root节点有值，则通过二分查找，寻找当前可以进行添加的父节点，找到以后按照比较器规则进行添加。

添加以后，红黑树进行自动平衡实现。

#### 5.5 HashTable类

HashTable也是基于哈希表实现，和HashMap不同的是HashTable是线程安全的。

- 底层实现：哈希表+链表

```java
 private transient Entry<?,?>[] table;//存储数组
 private transient int count;//容器中数据多少
private int threshold;//容器容量达到次数以后进行修改
private transient int modCount = 0;//修改次数
```

- Hash函数

```java
		int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
```

- 初始化。在构造方法中初始化。初始化指为11

```java
public Hashtable(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load: "+loadFactor);

        if (initialCapacity==0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        table = new Entry<?,?>[initialCapacity];
        threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
    }
```

- put方法

```java
public synchronized V put(K key, V value) {
        // Make sure the value is not null
        if (value == null) {
            throw new NullPointerException();
        }

        // Makes sure the key is not already in the hashtable.
        Entry<?,?> tab[] = table;
    	//hash函数计算一个index
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> entry = (Entry<K,V>)tab[index];
        for(; entry != null ; entry = entry.next) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                entry.value = value;
                return old;
            }
        }

        addEntry(hash, key, value, index);
        return null;
    }
//增加实体
private void addEntry(int hash, K key, V value, int index) {
        Entry<?,?> tab[] = table;
        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();

            tab = table;
            hash = key.hashCode();
            index = (hash & 0x7FFFFFFF) % tab.length;
        }

        // Creates the new entry.
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>) tab[index];
        tab[index] = new Entry<>(hash, key, value, e);
        count++;
        modCount++;
    }
```

##### 5.5.1 HashTable和HashMap的区别

|                  | HashTable                | HashMap                 |
| ---------------- | ------------------------ | ----------------------- |
| 底层时间         | 哈希表+链表              | 哈希表+链表+红黑树      |
| 初始化时间及大小 | 构造方法初始化，大小为11 | put方法初始化，大小为16 |
| 线程安全         | 安全                     | 不安全                  |
| Hash值           | 直接使用了hashcode       | 重新计算                |
| 扩容             | 二倍+1                   | 二倍                    |

#### 5.6 Properties类

- Java配置文件中用的居多
- 可以直接通过load方法加载配置文件，通过store方法存储配置文件
- 泛型锁定，为两个String类型