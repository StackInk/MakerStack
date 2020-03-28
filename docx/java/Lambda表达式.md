## 一文读懂Lambda表达式

>Lambda表达式是JDK1.8的新特性，主要为了解决匿名内部类过于冗长的问题。不过笔者认为其最大的作用就是对于策略模式更好的书写。
>
>这个表达式有点类似于ES6中的箭头函数，可以根据此进行理解

### 1. 案例

```java
@Test
    public void test01(){
        //匿名内部类的写法
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        //Lambda表达式写法
        Thread thread1 = new Thread(()->{

        });
    }
    @Test
    public void test02(){
        //匿名内部类写法
        Set<String> set  = new TreeSet(new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return Integer.compare(o1.length(),o2.length());
            }
        });
        //Lambda表达式写法
        Comparator<String> comparator = (x,y)->Integer.compare(x.length(),y.length());
        Set<String> set1 = new TreeSet(comparator);
    }
```

可以看到`Lambda`表达式简化了代码的书写，不过其底层仍然为匿名内部类，所以其就是一个语法糖。

### 2. 函数式接口

- 接口中唯一仅有一个函数的接口
- 被`@FunctionalInterface`修饰

### 3. Lamdba表达式语法

左侧为参数列表，右侧为执行的功能，为`Lambda`体

**无返回值，无参数**

- `()->sout("Hello World")`
- 当`Lambda`体中仅仅存在一条语句则不需要书写大括号

**无返回值，有参数**

- `o->sout("Hello World")`
- 当`Lambda`参数列表仅仅只有一个参数的时候，不需要书写括号

**有返回值，有两参数**

```java
(x,y)->{
    sout("Hello World");
    return Integer.compare(x,y);
}
```

**有返回值，有参数**

```java
(x,y)-> Interger.compare(x,y);
```

- 如果仅仅有一条语句，则不需要书写`return`，会默认将该语句的值返回

可以发现，在书写`Lambda`的时候，其参数列表没有书写数据类型，是因为JVM存在一个类型推断的机制，会通过上下文的数据类型判断参数的数据类型。所以数据类型可写可不写。

### 4. Java提供的四大函数式接口

- `Consumer<T>`消费型接口。消费一个参数，没有返回
- `Supplier<T>`供给型接口。不需要参数，返回一个T类型
- `Function<T,R>`函数型接口。消费一个T，返回一个R
- `Predicate<T>`断言型接口。给一个参数，对该参数进行操作判断，返回一个布尔

**看一下接口中的方法：**

```java
Consumer<T> 消费型
    void accept(T t);

Supplier<T> 供给型
    T get();

Function<T,R> 函数型
    R apply(T t);

Predicate<T> 断言型
    boolean test(T t) ;
```

#### 4.1 Predicate使用

- 主要思想还是使用了策略模式，将需要对数据处理的策略传入即可

```java
	@Test
    public void test03(){
        List<String> list = Arrays.asList("Hello", "bywlstudio", "Lambda", "www", "MakerStack");
        List<String> newList = this.filterList(list,s -> s.length()>5);
        for (String s : newList) {
            System.out.println(s);
        }
    }

    //需求：将满足条件的字符串，放入集合中
    public List<String> filterList(List<String> list , Predicate<String> predicate){
        List<String> list1 = new ArrayList<>();
        for (String s : list) {
            boolean test = predicate.test(s);
            if(test){
                list1.add(s);
            }
        }
        return list1 ;
    }
```

#### 4.2 Function使用

```java
@Test
    public void test04(){
        String s = "\tStack  " ;
        String s2 = this.stringHandler(s, s1 -> s1.trim());
        System.out.println(s2);
    }
    //需求：用于处理字符串
    public String stringHandler(String s , Function<String,String> function){
        return function.apply(s);
    }
```

#### 4.3 Supplier使用

```java
@Test
    public void test05(){
        List<Integer> anInt = getInt(5, () -> new Random().nextInt(100));
        for (Integer integer : anInt) {
            System.out.println(integer);
        }
    }
    //需求：产生指定个数的整数，并放入集合中
    public List<Integer> getInt(int num , Supplier<Integer> supplier){
        List<Integer> list = new ArrayList<>();
        for(int i = 0 ; i < num ; i++){
            list.add(supplier.get());
        }
        return list ;
    }
```

#### 4.4 Consumer

```java
@Test
    public void test06(){
        this.consumers(564.23f,t-> System.out.println("您已经消费了"+t));
    }

    public void consumers(double money , Consumer<Double> consumer){
        consumer.accept(money);
    }
```

#### 4.5 其他接口

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200321141952.png)

### 5. 方法引用

**主要有三种形式：**

- `实例::普通方法`。针对于普通方法。
- `类名::静态方法`。针对于静态方法。
- `类名::普通方法`。针对于普通方法。这是是针对于传入的两个参数：**一个参数调用的方法的参数刚好是另一个参数如：String类的equals**

**使用前提：**

- 接收这些调用的方法的函数式接口内部的方法的返回值类型和方法参数列表必须与调用的方法相同

```java
interface BiFunction<T,U,R>{
    R apply(T t , U u);
}
class MyTest{
    Boolean compareString(String s1 , String s2){
        return s1.equals(s2);
    }
}

//此时可以调用
MyTest mytest = new MyTest();
//此时方法的参数列表和返回值均相同
BiFunction<String,String,Boolean> bi = mytest::compareString;
bi.apply("my","Method");

```

#### 5.1 实例::普通方法

```java
@FunctionalInterface
public interface Consumer<T> {

    void accept(T t);	
}

public StringBuffer{
    @Override
    public synchronized void setLength(int newLength) {
        toStringCache = null;
        super.setLength(newLength);
    }
}

StringBuffer str = new StringBuffer();
Consumer<String> consumer = str::setLength;
consumer.accept(13);
```

#### 5.2 类名::静态方法

```java
class Integer{
    @HotSpotIntrinsicCandidate
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }
}

	Function<String,Integer> function = Integer::valueOf;
    Integer apply = function.apply("12");
    System.out.println(apply);
```

#### 5.3 类名:普通方法

```java
BiPredicate<String,String> biFunction = String::equals;
System.out.println(biFunction.test("my", "test"));
```

### 6. 构造引用

- 这个只要保证参数列表相同就可以

```java
class Emp{
    int id ;
    String name ;
    public Emp(){

    }
Supplier<Emp> supp = Emp::new ;
System.out.println(supp.get());    
   
Consumer<Emp> consumer = Emp::new ;
consumer.accept(1);
```

### 7. 数组引用

```java
Function<Integer,String[]> function = String[]::new;
        String[] apply = function.apply(10);
        System.out.println(apply.length);
```























