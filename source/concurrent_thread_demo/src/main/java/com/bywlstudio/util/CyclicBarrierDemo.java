package com.bywlstudio.util;

import java.util.concurrent.CyclicBarrier;

/**
 * @Author: zl
 * @Date: Create in 2020/4/10 10:52
 * @Description:
 */
public class CyclicBarrierDemo {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(5,()-> System.out.println("消灭邪剑仙"));
        for (int i = 0; i < 5; i++) {
            final int tempInt = i +1  ;
             new Thread(()->{
                 try{
                     System.out.println(Thread.currentThread().getName()+"\t收集了第"+tempInt+"颗灵珠");
                     cyclicBarrier.await();
                 }catch (Exception e){
                     e.printStackTrace();
                 }finally {

                 }
             },"线程"+ i).start();
        }
    }
}
