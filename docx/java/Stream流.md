## Steam流

![](https://gitee.com/onlyzl/image/raw/master/img/wxhead.png)

### 1. 创建Stream流的方式

- 创建一个Stream流
- 进行数据操作
- 终止操作

#### 1.1 如何创建Stream流

- `Collection`提供的两个方法`stream()`和`parallelStream()`

  - `stream()`流获取的是一个顺序流

  - `parallelstream()`获取一个并行流

```java
Set<String> set = new HashSet<>();
Stream<String> stream = set.stream();
Stream<String> stringStream = set.parallelStream();
```

- `Arrays`数组的`stream()`获取一个数组流

```java
int[] arr = new int[]{1,24,5,6};
        Object[] objects = new Object[10];
        Stream<Object> stream2 = Arrays.stream(objects);
        IntStream stream1 = Arrays.stream(arr);
```

- 通过`Stream`类的静态方法`of`

```java
Stream<String> aaa = Stream.of("aaa", "bbb");
        Stream<? extends Serializable> stream3 = Stream.of(111, 3223, "23432");
```

- 创建一个无限流

```java
//迭代
Stream<Integer> iterate = Stream.iterate(0, (x) -> x+2).limit(10);
iterate.forEach(System.out::println);

//生成
Stream<Double> generate = Stream.generate(Math::random).limit(10);
        generate.forEach(System.out::println);
```

### 2. 对流进行中间操作

#### 2.1 过滤操作

- `filter(Predicate p )`返回满足条件的数据

```java
emps.stream()
    .filter(e->e.getAge()>18)
    .forEach(System.out::println);
```

- `distinct()`。通过对象的HashCode方法和equals方法筛选对象。相当于去重

```java
@Test
    public void test01(){
        emps.stream()
                .filter(e->e.getAge()>18)
                .distinct()
                .forEach(System.out::println);
    }
```

- `limit(long num)`输出指定个数的值

```java
@Test
    public void test01(){
        emps.stream()
                .filter(e->e.getAge()>18)
                .distinct()
                .limit(2L)
                .forEach(System.out::println);
    }
```

- `skip(long length)`跳过`length`个元素。比如length=1，则当遇到满足条件的第一个元素则跳过，从高第二个开始

```java
 @Test
    public void test01(){
        emps.stream()
                .filter(e->e.getAge()>18)
                .distinct()
                .limit(2L)
                .skip(1L)
                .forEach(System.out::println);
    }
```

#### 2.2 映射操作，对数据进行修改

- `map`对传入的数据进行修改，然后返回一个数据

```java
emps.stream()
                .map(e->e.getAge()*3)
                .forEach(System.out::println);
```

- `mapToDouble`返回一个`Double`类型的值

```java
@Test
    public void test02(){
        emps.stream()
                .mapToDouble(e->e.getSalary()*2)
                .forEach(System.out::println);
    }
```

- `mapToInt`返回一个`Int`类型的值
- `mapToLong`返回一个`Long`类型的值
- `flatMap`返回一个流，将传入的参数全部转化为不同的流，最后将这些流连接为一个流

#### 2.3 排序操作

- `sorted`对数据进行自然排序
- `sorted(Compartor)`传入一个比较器，按照比较器中的排序规则进行排序

```java
@Test
    public void test04(){
       emps.stream()
               .sorted((o1,o2)->{
                   if(o1.getName().equals(o2.getName())){
                       return Integer.compare(o1.getAge(),o2.getAge());
                   }else{
                       return o1.getName().compareTo(o2.getName());
                   }
               }).forEach(System.out::println);
    }
```

### 3. 终止流

生成之前对流操作以后的结果

#### 3.1 匹配终止流，返回一个数据类型

- `allMatch()`。查看该流中的所有值是否**都**满足执行的条件

```java
boolean b = emps.stream()
                .allMatch(employee -> employee.getAge() > 18);
        System.out.println(b);
```

- `anyMatch()`。在流中**至少有一个元素**匹配执行的条件

```java
boolean b = emps.stream()
                .anyMatch(employee -> employee.getAge() > 18);
        System.out.println(b);
```

- `noneMatch()`。检查是否没有匹配元素

```java
boolean b = emps.stream()
                .noneMatch(employee -> employee.getAge() > 18);
        System.out.println(b);
```

- `findFirst()`返回一个`Optional`对象

```java
Optional<Employee> first = emps.stream()
                .findFirst();
        System.out.println(first.get());
```

- `findAny`从当前流中返回一个元素

#### 3.2 总括终止流

- `count()`返回当前流中的元素个数

```java
public void test07(){
        long count = emps.stream()
                .count();
        System.out.println(count);
    }
```

- `max`和`min`返回流中的最大值和最小值。需要传入一个比较器

```java
Optional<Employee> max = emps.stream()
                .max((o1, o2) -> {
                    if (o1.getName().equals(o2.getName())) {
                        return Integer.compare(o1.getAge(), o2.getAge());
                    } else {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

        System.out.println(max.get());


Optional<Integer> min = emps.stream()
                .map(employee -> employee.getAge()).min(Integer::compare);
        System.out.println(min.get());
```

- `forEach`遍历流，参数为一个消费型接口

#### 3.3 归约终止流

- `reduce(BinaryOperator<T> accmulator)`对流中的数据进行汇总计算

```java
Optional<Integer> reduce = emps.stream().map(Employee::getAge).reduce((x, y) ->
                x + y
        );
        Integer integer = reduce.get();
        System.out.println(integer);

//Java代码的表现形式
int result = num[0]
for(int i = 1 ; i < num.length ; i++){
	result = accmulator.apply(result,a[i])
}
return result ;
```

- `reduce(int identity,BinaryOperator<T> accmulator)`给定一个初始向量，需要对改初始向量进行计算

```java
Integer reduce = emps.stream().map(Employee::getAge).reduce(2, (x, y) -> {
            System.out.println(x+"......"+y);
            return x + y ;
        });
        System.out.println(reduce);

//Java代码的表现形式
int result = identity ;
for(int i = 0 ; i < num.length ; i++){
	result = accmulator.apply(result,a[i])
}
return result ;
```

- `reduce(U identity,Function<? super T,? extends U > function , BinaryOperator<U> op `对指定的元素进行`op`操作，返回一个`U`类型的值

#### 3.4 收集终止流

- `collect(Collectors c)`对流中的数据执行指定的收集方法

##### 3.4.1 Collector

- `toList`,`toSet`,`toCollection`将流转换为指定的集合类型

```java
emps.stream().map(Employee::getAge).collect(Collectors.toSet()).stream().forEach(System.out::println);
```

- `counting`对流中元素进行个数汇总
- `summingInt`,`summingDouble`,`summingLong`对元素中的整数，浮点数和`long`型数进行求和

```java
emps.stream().collect(Collectors.counting());
emps.stream().collect(Collectors.summingInt(Employee::getAge));
emps.stream().collect(Collectors.averagingInt(Employee::getAge));
```

- `summarizingInt`收集流中`Integer`属性进行对应的操作，比如：求最大值，最小值和平均值等

```java
     IntSummaryStatistics collect3 = emps.stream().collect(Collectors.summarizingInt(Employee::getAge));
        double average = collect3.getAverage();
        System.out.println(average);
```

- `joining`连接流中指定的元素
  - 没有参数，直接进行连接
  - 有参数，则对元素中间进行连接
  - 三个参数，则对元素中间和首尾进行连接

```java
String collect = emps.stream().map(Employee::getName)
                .collect(Collectors.joining(",","[","]"));
        System.out.println(collect);
[李四,张三,赵六,赵六,王五,田七]
```

- `maxBy()`和`minBy`根据传入的比较器返回最大或者最小的值

```java
@Test
    public void test10(){
        Optional<Employee> max = emps.stream().max((o1, o2) -> {
            if (o1.getName().equals(o2.getName())) {
                return Integer.compare(o1.getAge(), o2.getAge());
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        });
        System.out.println(max.get());
    }
```

- `reducing`和`reduce`相同

```java
Integer collect = emps.stream().collect(Collectors.reducing(2, Employee::getAge, Integer::sum));
        System.out.println(collect);
        Double collect1 = emps.stream().collect(Collectors.reducing(2.0, Employee::getSalary, Double::sum));
```

- `collectingAndThen(A,B)`表示在执行完A以后再执行B

```java
Integer collect = emps.stream().collect(Collectors.collectingAndThen(Collectors.toSet(), Set::size));
//将流转换为一个Set集合，然后求集合的大小
```

- `groupBy(Function<? super T, ? extends K> classifier)`传入一个进行分组的标准，改标准作为键，符合条件的元素作为值

```java
Map<Status, List<Employee>> collect = emps.stream().collect(Collectors.groupingBy(Employee::getStatus));
        System.out.println(collect);
```

- 多重分组，分组之后再进行分组

```java
Map<Status, Map<Integer, List<Employee>>> collect = emps.stream().collect(Collectors.groupingBy(Employee::getStatus, Collectors.groupingBy(Employee::getAge)));
        System.out.println(collect);
```

- 根据`true或者false`进行分组

```java
Map<Boolean, List<Employee>> collect = emps.stream().collect(Collectors.partitioningBy(e -> e.getAge() > 18));
        System.out.println(collect);
```

### 4. Optional

#### 4.1 创建Optional实例

- `Optional.of(T t)`。传入一个对象返回一个`Optional<T>`的实例对象。不能传入`null`，否则报空指针异常
- `Optional.empty()。`返回一个空的`Optional`实例对象，内部不包含任何元素
- `Optional.ofNullable(T t)`。如果有值创建对象，如果没有值返回一个空的`Optional`对象

#### 4.2 判断Optional实例对象中的值

- `isPresent()`判断是否包含值，即判断该`Optional`对象是否为空
- `orElse(T t)`如果该`Optional`中包含值，则返回该值，如果没有值则返回这个值
- `orElseGet(Supplier s)`返回`s`获取的值
- `map(Function f)`和Map的作用类似
- `flatMap(Function mapper)`要求返回值必须为`Optional`

#### 4.3 实际开发中的Optional的作用

**主要应用于：**

- 避免空指针
- 如果当前元素会出现`null`则使用该类进行封装





