package com.bywlstudio.pool;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.util.concurrent.*;

/**
 * 自定义线程池测试类
 */
public class MyThreadPollTest {
    public static void main(String[] args) throws Exception{
        Thread thread = new Thread(new FutureTask<Integer>(()->{return 1;}));

    }
}
