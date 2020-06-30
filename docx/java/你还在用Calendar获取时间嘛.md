# Jdk1.8之时间处理

## 1. 传统时间处理的问题

### 1.1 多线程环境下的SimpleDateFormat

当多个线程使用同一个时间处理对象进行对日期的格式化的时候，会出现`java.lang.NumberFormatException: multiple points`。主要原因是由于`SimpleDateFormat`是线程不安全的，当线程共享的时候，会引发这个异常。

#### 1.1.1 代码演示

```java
SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
//线程池中线程共享了SimpleDateFormat，引发线程不安全
Callable<String> callable = () -> simpleDateFormat.parse("20200402").toString();

ExecutorService executorService = Executors.newFixedThreadPool(10);
List<Future<String>> list = new LinkedList<>();

for (int i = 0; i < 10; i++) {
      Future<String> submit = executorService.submit(callable);
      list.add(submit);
}
for (Future<String> stringFuture : list) {
     String s = stringFuture.get();
     System.out.println(s);
}
executorService.shutdown();
```

解决方法：

- 线程不共享变量`SimpleDateFormat`，每一个线程在进行日期格式化的时候都自己创建一个

```java
ExecutorService executorService = Executors.newFixedThreadPool(10);
List<Future<String>> list = new LinkedList<>();
for (int i = 0; i < 10; i++) {
   Future<String> submit = executorService.submit(new MyCallable01("20200403"));
   list.add(submit);
}
for (Future<String> stringFuture : list) {
        String s = stringFuture.get();
        System.out.println(s);
 }
executorService.shutdown();

class MyCallable01 implements Callable<String>{

    private String date ;

    public MyCallable01(String date) {
        this.date = date;
    }

    @Override
        public String call() throws Exception {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            return simpleDateFormat.parse(date).toString();
        }
}
```

- 通过`ThreadLocal`为每一个线程绑定一个`SimpleDateFormate`

```java
Future<String> submit = executorService.submit(() -> ResolveByThreadLocal.converDateStrToDate("20200405"));

public class ResolveByThreadLocal {
    //创建一个绑定每一个变量的ThreadLocal
    private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<>();

    public static String converDateStrToDate(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = threadLocal.get(); ;
        if(simpleDateFormat == null){
            simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            threadLocal.set(simpleDateFormat);
        }
        Date parse = simpleDateFormat.parse(date);
        return parse.toString() ;
    }
}

```

## 2. 1.8时间处理

对于时间的处理，均在`java.time`包及其子包中，且**线程安全**

![](https://gitee.com/onlyzl/image/raw/master/img/20200403100959.png)



- `java.time`包下存放了进行时间处理的各种类
  - `Instant`获取本地时间的时间戳
  - `LocalDate`获取本地时间的日期
  - `LocalTime`获取本地时间的时间
  - `LocalDateTime`获取本地时间的日期和时间
  - `Duration`计算两个日期之间的间隔
  - `Period`计算两个时间的间隔
  - `OffsetDateTime`对日期和时间进行偏移量计算
  - `offsetTime`对时间进行偏移量计算
  - `ZoneId`各种时区代码
  - `ZoneOffset`市区偏移量计算
  - `ZonedDateTime`
- `java.time.chrono`不同地区时间记时方式
- `java.time.temporal`对时间进行一些调整的包
- `java.time.format`对时间进行格式化

### 2.1  LocalDate、LocalTime、LocalDateTime 

三者的使用方式完全相同，输出的结果不同

- `now`获取本地时间

```java
LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        System.out.println(now.getYear());
        System.out.println(now.getMonthValue());//直接获取月份的值
        System.out.println(now.getDayOfMonth());
        System.out.println(now.getHour());
        System.out.println(now.getMinute());
        System.out.println(now.getSecond());

输出:
2020-04-03T10:25:29.906
2020
4
3
10
25
29
```

- `of()`传入指定的日期和时间

![](https://gitee.com/onlyzl/image/raw/master/img/20200403104441.png)

- 对时间进行偏移量**加**计算

![](https://gitee.com/onlyzl/image/raw/master/img/20200403104306.png)

- 对事件进行偏移量**减**运算

![](https://gitee.com/onlyzl/image/raw/master/img/20200403104550.png)

- 当前时间与另一个时间的比较

![](https://gitee.com/onlyzl/image/raw/master/img/20200403104654.png)

- 将月份天数，年份天数，月份等修改为指定的值，返回一个新的`LocalDateTime`对象

![](https://gitee.com/onlyzl/image/raw/master/img/20200403110805.png)

- `get`方法

![](https://gitee.com/onlyzl/image/raw/master/img/20200403110940.png)

- `format(DateTimeFormatter formatter)`对日期进行格式化
- `until`返回两个日期之间的`Period`对象
- `isLeapYear`判断是否为闰年

### 2.2 Instant时间戳

以`Unix`元年(传统设定为`UTC`时区1970年1月1日)开始所经历的描述进行运算

- 获取当前时间的时间戳`toEpochMilli`
- 获取当前时间的秒`getEpochSecond`
- 对时间进行偏移`Instant.now().ofHours(ZoneOffset.ofHours(int hours))`

### 2.3 TemporalAdjuster 时间校正器

主要通过`TemporalAdjusters`工具类获取到`TemporalAdjuster`实例对象

```java
LocalDateTime now = LocalDateTime.now();
        //直接调用JDK提供的时间校正器
        LocalDateTime with = now.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        System.out.println(with);
        //自定义一个时间校正器，计算下一个工作日
        LocalDateTime with2 = now.with(e -> {
            LocalDateTime e1 = (LocalDateTime) e;
            DayOfWeek dayOfWeek = e1.getDayOfWeek();
            if (dayOfWeek.equals(DayOfWeek.FRIDAY)) {
                return e1.plusDays(3);
            } else if (dayOfWeek.equals(DayOfWeek.SATURDAY)) {
                return e1.plusDays(2);
            } else {
                return e1.plusDays(1);
            }
        });
        System.out.println(with2);
```

### 2.4 DateTimeFormatter日期格式化

**三种格式化方法：**

- 预定义的标准格式
- 语言环境相关的格式
- 自定义的格式

#### 2.4.1 预定义的标准格式

JDK提供的格式化格式

![](https://gitee.com/onlyzl/image/raw/master/img/20200403135916.png)

```java
LocalDate localDate = LocalDate.now();
String format = localDate.format(DateTimeFormatter.ISO_DATE);
输出：
	2020-04-03
```

#### 2.4.1 自定义的时间格式

```java
//自定义日期格式化方式，可以通过format和parse对日期进行格式化
DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
String format1 = localDate.format(dateTimeFormatter);
System.out.println(format1);
localDate.parse(format1,dateTimeFormatter);

输出：
    2020年04月03日
    2020-04-03
```

### 2.5 时区处理

#### 2.5.1 ZoneId

- 获取所有的时区信息

```java
 Set<String> availableZoneIds = ZoneId.getAvailableZoneIds();
```

- 获取指定时区信息的`ZoneId`对象

```java
ZoneId of = ZoneId.of("Asia/Chungking");
```

#### 2.5.2 ZonedDateTime

获取一个带时区的日期时间对象

```java
ZonedDateTime now = ZonedDateTime.now();
        System.out.println(now);
//输出
2020-04-03T14:22:54.250+08:00[Asia/Shanghai]
```

其他用法和`LocalDateTime`类相同













