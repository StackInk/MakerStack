package com.bywlstudio.volatiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 演示可见性
 */

public class VolatileDemo {
    private volatile int num =0 ;

    public void addPlus(){
        num++;
    }

    public static void main(String[] args) {
        VolatileDemo volatileDemo = new VolatileDemo();
        for(int i = 0 ; i < 20 ; i++){
            new Thread(()->{
                for (int j = 0; j < 1000; j++) {
                    volatileDemo.addPlus();
                }
            },"线程为："+i).start();
        }
        //后台默认存在两个线程，一个main，一个GC
        while(Thread.activeCount() > 2){
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName()+"\t finally\t"+volatileDemo.num);


    }

    /**
     * 演示可见性
     */
    private static void see() {
        VolatileDemo volatileDemo = new VolatileDemo();
        new Thread(()->{try {
            TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {e.printStackTrace();}
            volatileDemo.num = 10 ;
        }).start();
        while(volatileDemo.num == 1){

        }
        System.out.println("值已经被修改为:"+volatileDemo.num);
    }
}
