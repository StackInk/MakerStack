package com.bywlstudio.volatiles;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 原子类实现原子性
 */
class MyData{
    //默认值为0
    volatile AtomicInteger atomicInteger = new AtomicInteger();

    public void addPlus(){
        //先++
        atomicInteger.incrementAndGet();
        //先获取
//        atomicInteger.getAndIncrement();
    }
}
public class atomicDemo {
    public static void main(String[] args) {
        MyData myData = new MyData();
        for(int i = 0 ; i < 20 ; i++){
            new Thread(()->{
                for (int j = 0; j < 1000; j++) {
                    myData.addPlus();
                }
            },"线程为："+i).start();
        }
        while(Thread.activeCount() > 2){
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName()+"\t value:\t"+myData.atomicInteger.get());
    }
}
