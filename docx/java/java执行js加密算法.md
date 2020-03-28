## Java执行js加密算法

>今日需求：在后端执行一段加密算法，算法是js写的

明白需求以后疯狂百度。最后发现`JDK`提供了各种脚本的支持(怪笔者学艺不精，第一次见识到这个库，留下不学无术的泪水)，正题开始，Java如何执行一段加密算法呢？



`java`对脚本的支持全部存放于`javax.script`包下

```properties
接口：
    Bindlings
    Compilable 
    Invocable 
    ScriptContext 
    ScriptEngine 
	ScriptEngineFactory 
类：
    AbstractScriptEngine 
    CompiledScript 
    ScriptEngineManager 
    SimpleBindings 
    SimpleScriptContext 
异常：
	ScriptException 
```

我们本次使用到的有`ScriptEngineManager`，`ScriptEngine`，`Invocable`

> `ScriptEngineManager` 为 `ScriptEngine`  类实现一个发现和实例化机制，还维护一个键/值对集合来存储所有 Manager 创建的引擎所共享的状态。此类使用[服务提供者](../../../technotes/guides/jar/jar.html#Service Provider)机制枚举所有的  `ScriptEngineFactory` 实现。
>
> `ScriptEngineManager`  提供了一个方法，可以返回一个所有工厂实现和基于语言名称、文件扩展名和 mime 类型查找工厂的实用方法所组成的数组。  
>
> 键/值对的 `Bindings`（即由管理器维护的 "Global Scope"）对于  `ScriptEngineManager` 创建的所有 `ScriptEngine`  实例都是可用的。`Bindings` 中的值通常公开于所有脚本中。 
>
> ​																												JDK官方解释

我们通过`ScriptEngineManager`获取指定脚本语言的执行引擎（`ScriptEngine`），然后调用`eval()`执行脚本代码，再加该脚本引擎转换为`Invocable`接口，该接口由`NashornScriptEngine`实现，并且这个实现类中定义了`invokeFunction`方法来执行这段代码。同时它也定义了`getInterface`方法来将js代码作为传入接口的实现，来调用这个方法。

```java
public static String playJS(String str){
        //获取脚本引擎管理器
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        //获取指定脚本的引擎管理器
        ScriptEngine js = scriptEngineManager.getEngineByName("js");
    //conwork.js文件是一个js的加密算法
        InputStream resource = PlayJS.class.getClassLoader().getResourceAsStream("conwork.js");
        try {
            Reader reader = new BufferedReader(new InputStreamReader(resource,"utf-8"));
            //执行脚本
            js.eval(reader);
            if(js instanceof Invocable){
                //将脚本引擎转换Invocable
                Invocable invocable = (Invocable) js;
                // 将js代码转换为该接口的实现，Method是自定义的接口，用来存放加密算法
                Methods executeMethod = invocable.getInterface(Methods.class);
                // 执行指定的js方法
                return executeMethod.encodeInp(str);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null ;
    }
```

Methods接口

```java
public interface Methods {
    public String encodeInp(String input);
}
```

此时，调用`playJS`方法传入的数据，就是加密以后的数据



### 接下来玩一个更好玩的，JavaScript代码中执行Java代码

1. 定义一个静态方法

```java
static String fun1(String name){
    System.out.format("this is java code , %s"+name);
    return "thank you";
}
```

2. 在JavaScript代码中导入类

```js
//通过Java.type(),这个代码相当于Java代码的import导入类
var myTestClass = Java.ype("com.bywlstuido.MyTestClass");
//有了Java类的原型，就可以调用类中的静态方法了
var result = myTestClass.fun1("JS invoke");
print(result);

//this is java code ,JS invoke
//thank you 
```

执行完毕

