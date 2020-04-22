## NIO模型

### 1. 简介

`Java NIO`是从`JDK1.4`版本开始引入的一个新的`IO API`，可以替代标准的`Java IO API`。NIO与原来的IO有同样的作用和目的，但是使用的方式完全不同，NIO支持面向缓存区的、基于通道的IO操作。NIO将以更加高效的方式进行文件的读写操作。

**两者的区别**

| BIO                     | NIO                         |
| ----------------------- | --------------------------- |
| 面向流(Stream Oriented) | 面向缓冲区(Buffer Oriented) |
| 阻塞IO(Blocking IO)     | 非阻塞IO(Non Blocking IO)   |
| 无                      | 选择器（Selectors）         |

### 2. 工作流程

#### 2.1 通道和缓冲区

通道表示打开到IO设备的连接，不存储数据。

缓冲区，对数据进行主要的处理

#### 2.2 使用流程

**思想：**使用前，获取用于连接IO设备的通道以及用于容纳数据的缓冲区。

相较于传统BIO，NIO使用了双向通道，通道不用来传输任何数据，数据通过Buffer的移动来进行IO操作。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200420203928.png)

#### 2.3 底层原理

NIO中引入了四个概念。

`capacity`缓冲区的容量，表示用来进行**IO的最大数据量**

`limit`第一个不应该进行**写入或者读取的索引**

`position`当前索引的位置，相当于一个**读或者写的指针**

`mark和reset指向一个索引。可以通过`remark`回到这个索引处

**Buffer的底层是一个对应类型的数组**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200420210052.png)

**position<limit<capacity  其中的mark必须在0和limit之间**

`Java.nio`包下提供了除了`boolean`类型的缓冲区

```java
	ByteBuffer b ; 
    ShortBuffer s ;
    IntBuffer i1 ;
    LongBuffer l;
    FloatBuffer floatBuffer ;
    DoubleBuffer doubleBuffer ;
    CharBuffer charBuffer ;
```

#### 2.4 直接缓冲区和非直接缓冲区

直接缓冲区是直接创建在物理内存中的。通过`allocate`方法分配

非直接缓冲区是创建在堆中的。通过`allocateDirect()`方法分配

**非直接缓冲区的工作流程：**应用程序读取数据的时候，首先需要将数据加载到内核地址空间，然后再将数据复制到用户地址空间中，这样应用程序才可以读取。

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200421164050.png)

**直接缓冲区工作流程：**应用程序读取数据的时候，直接在物理内存中读取，抛弃了复制的过程

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200421164247.png)



#### 2.5 通道的执行流程

将数据加载到内存中，然后IO接口创建一个通道，用来具体的数据读取

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200421172530.png)

### 3. 简单的API

**通道API**

```java
FileChannel  用于读取、写入、映射和操作文件的通道
DatagramChannel  通过UDP读写网络中的数据通道
SocketChannel   通过TCP读写网络中的数据
ServerSocketChannel 监听新进来的TCP连接，对于每一个进来的连接都会创建一个SocketChannel    
```

**获取通道：**

```java
输入流的getChannel()方法
FileChannel的open方法
Files工具类中的newByteChannel
```

**常见API用法**

- 字符的解码和加码，通过`CharSet`实现
- 直接缓冲区的IO操作。
  - 通过`Buffer`的`allocateDirect`分配
  - 通过`FileChannel`的`open`方法创建

```java
//打开文件，按照只读模式
        try {
            //指定了通道的模式，读取数据
            FileChannel in = FileChannel.open(Paths.get("G:\\BugReport.txt"), StandardOpenOption.READ);
            //写数据，读数据，创建文件
            FileChannel out = FileChannel.open(Paths.get("G:\\BugReport3.txt"),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
            //获取物理映射空间
            MappedByteBuffer inMap = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
            MappedByteBuffer outMap = out.map(FileChannel.MapMode.READ_WRITE, 0, in.size());

            //复制文件，先直接在物理空间中获取，然后再进行写入
            byte[] bytes = new byte[inMap.limit()];
            inMap.get(bytes);
            outMap.put(bytes);

            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
```

### 4. 网络非阻塞IO通信

对于传统的IO来说，都是阻塞的，即线程会一直等待数据，直到数据被读取或者写入。此时会造成大量的CPU资源浪费

而NIO中添加了一个选择器，他可以选择一个数据以及填充完成的通道发送给服务端执行。即当数据没有来之前，线程可以执行其他的事情.

**非阻塞IO通信图**

![](https://gitee.com/onlyzl/blogImage/raw/master/img/20200421212346.png)

**客户端：**

```java
public static void main(String[] args) throws IOException {
        //1. 获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 10001));

        //2. 切换非阻塞模式
        sChannel.configureBlocking(false);

        //3. 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //4. 发送数据给服务端
        Scanner scan = new Scanner(System.in);

        while(scan.hasNext()){
            String str = scan.next();
            buf.put((new Date().toString() + "\n" + str).getBytes());
            buf.flip();
            sChannel.write(buf);
            buf.clear();
        }

        //5. 关闭通道
        sChannel.close();
    }
```

**服务端：**

```java
public static void main(String[] args) throws IOException {
        //1. 获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        //2. 切换非阻塞模式
        ssChannel.configureBlocking(false);

        //3. 绑定连接
        ssChannel.bind(new InetSocketAddress(10001));

        //4. 获取选择器
        Selector selector = Selector.open();

        //5. 将通道注册到选择器上, 并且指定“监听接收事件”
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);

        //6. 轮询式的获取选择器上已经“准备就绪”的事件
        while(selector.select() > 0){

            //7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while(it.hasNext()){
                //8. 获取准备“就绪”的是事件
                SelectionKey sk = it.next();

                //9. 判断具体是什么事件准备就绪
                if(sk.isAcceptable()){
                    //10. 若“接收就绪”，获取客户端连接
                    SocketChannel sChannel = ssChannel.accept();

                    //11. 切换非阻塞模式
                    sChannel.configureBlocking(false);

                    //12. 将该通道注册到选择器上
                    sChannel.register(selector, SelectionKey.OP_READ);
                }else if(sk.isReadable()){
                    //13. 获取当前选择器上“读就绪”状态的通道
                    SocketChannel sChannel = (SocketChannel) sk.channel();

                    //14. 读取数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);

                    int len = 0;
                    while((len = sChannel.read(buf)) > 0 ){
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len));
                        buf.clear();
                    }
                }

                //15. 取消选择键 SelectionKey
                it.remove();
            }
        }


    }
```

>本文源码已经收录到本文开源项目`MakerStack`中，`Github`地址：https://github.com/StackInk/MakerStack

