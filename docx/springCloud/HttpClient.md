## HttpClient介绍和使用

>今天有一个需求：后台访问一个接口，获取返回的数据。于是找到了`HttpClient`

### 1.介绍

>`SpringCloud`中服务和服务之间的调用全部是使用`HttpClient`，还有前面使用`SolrJ`中就封装了`HttpClient`，在调用`SolrTemplate`的`saveBean`方法时就调用`HttpClient`技术。

笔者在查找`HttpClient`的使用方法的时候，发现很多文章都使用了这句话，在这里引用一下。

`HttpClient`提供的主要功能：

- 实现了所有的Http方法(Get，Post，Put，Delete)
- 支持自动转向(自动重定向)
- 支持HTTPS协议
- 支持代码服务器等

### 2.使用流程

1. 创建一个HttpClient对象
2. 创建请求方法的实例对象，并且传入需要请求的URL
3. 设置请求头，请求体等内容
4. 通过HttpClient执行请求方法
5. 获取响应结果。获取响应的响应头，响应码，响应体
6. 关闭HttpClient，和响应结果

### 3.代码实现

笔者对`HttpClent`进行了简单封装

#### 3.1导入坐标

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.3</version>
</dependency>
```

#### 3.2Get方式请求

```java
	//HttpClient客户端
    private static CloseableHttpClient httpClient ;
    //响应
    private static CloseableHttpResponse httpResponse ;

    /**
     * 返回Get方式请求得实体
     * @param url 需要通过Get方式请求的URL
     * @param headers 需要添加的请求头
     * @return 返回响应的实体
     */
    public static HttpReturn doGet(String url, Header[] headers) throws IOException {
        //创建一个httpClient对象，相当于创建了一个浏览器，用来访问URL链接
        httpClient = HttpClients.createDefault();
        //创建Get请求
        HttpGet httpGet = new HttpGet(url);
        //设置请求头
        httpGet.setHeaders(headers);
        //访问这个Get链接
        httpResponse = httpClient.execute(httpGet);
        //获取响应的状态码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        //获取响应头
        Header[] allHeaders = httpResponse.getAllHeaders();
        //获取响应体，并进行封装
        HttpEntity entity = httpResponse.getEntity();
        //将响应的实体按照utf-8转换程字符串
        String s = EntityUtils.toString(entity, "utf-8");
        //创建自定义Http请求返回结果
        HttpReturn httpReturn = new HttpReturn(statusCode,s,allHeaders);
        //关闭使用的流
        HttpClientResult.destoryResourcec();
        return httpReturn ;
    }
```

代码解读：这是一个笔者自定义的工具类，传入需要访问的`url`和需要添加的请求头。`HttpReturn`是笔者自定义的实体，用来存放返回的信息。

```java
public class HttpReturn {
    //Http请求状态码
    private int httpStatusCode ;
    //Http请求返回的实体
    private String httpEntity ;
    //Http请求头
    private Header[] headers ;

```

`destoryResourcec`是笔者自定义的一个关闭流的静态方法

```java
public static void destoryResourcec(){
        if(httpResponse != null){
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(httpClient != null){
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
```

其他的代码的解释注释中已经全部注明。

#### 3.3Post方式请求

```java
/**
     * Post方式请求的方式
     * @param url 需要进行请求的URL
     * @param headers 需要添加的请求头
     * @param entity 需要携带的请求体
     * @return 返回响应的实体
     */
    public static HttpReturn doPost(String url , Header[] headers , HttpEntity entity) throws IOException {
        //创建一个httpClient对象，相当于创建了一个浏览器，用来访问URL链接
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        //设置请求头
        httpPost.setHeaders(headers);
        //添加请求体
        httpPost.setEntity(entity);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        //将状态码，实体，头信息放在了一起
        HttpReturn httpReturn = new HttpReturn(httpResponse.getStatusLine().getStatusCode(),EntityUtils.toString(httpResponse.getEntity(),"utf-8"),httpResponse.getAllHeaders()) ;
        HttpClientResult.destoryResourcec();
        return httpReturn ;
    }
```

相对于Get方式，Post方式多了一个实体

![](https://gitee.com/onlyzl/image/raw/master/img/1.png)

这是`HttpEntity`的接口的部分实现类，对应了各种实体的创建方式，笔者使用了`UrlEncodedFormEntity`相当于`x-www-form-urlencoded`的表单提交方式

下面看一下添加实体的代码

```java
//封装URL
String url = "http://111.177.117.104:8080/admin/sys!chaxun.action";
// 设置请求头消息User-Agent
Header[] headers = new Header[2];
//设置表单的提交方式
headers[0] = new BasicHeader("Content-Type","application/x-www-form-urlencoded") ;
//设置为为浏览器访问方式，现在有一些网站不允许非浏览器访问
headers[1] = new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
//创建实体键值对的集合
List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
//创建最基本的键值
NameValuePair nameValuePair = new BasicNameValuePair("fjmc",roomNum);
parameters.add(nameValuePair);
try {
    //将声名的实体写入
    HttpEntity httpEntity = new UrlEncodedFormEntity(parameters);
    HttpReturn httpReturn = HttpClientResult.doPost(url, headers, httpEntity);
} catch (UnsupportedEncodingException e) {
  e.printStackTrace();
} catch (IOException e) {
  e.rintStackTrace();
}
```

上面代码中的`List<NameValuePair>`就是用来存放表单信息的键值的

`NameValuePair`是一个键值的接口，使用其实现类`BasicNameValuePair`,通过其构造方法，将键值传入

```java
public class BasicNameValuePair implements NameValuePair, Cloneable, Serializable {
    private static final long serialVersionUID = -6437800749411518984L;
    private final String name;
    private final String value;
```

然后将这个对象添加到List集合中，再将实体放入`UrlEncodedFormEntity`中即可，此时一个`HttpEntity`构造完成。

#### 3.4Put方式请求

`Put`方式的请求和`Post`方式 相同，只是请求方式变化，仍然使用`body`添加数据

#### 3.5Delete方式请求

`Delete`方式和`Get`方式相同



### 最后推荐一个解析`html`的工具， `jsoup`

坐标

```xml
<dependency>
      <!-- jsoup HTML parser library @ https://jsoup.org/ -->
      <groupId>org.jsoup</groupId>
      <artifactId>jsoup</artifactId>
      <version>1.12.1</version>
</dependency>
```

中文版网站[https://www.open-open.com/jsoup/dom-navigation.htm](https://www.open-open.com/jsoup/dom-navigation.htm)有兴趣可以了解一下