package com.bywlstudio.util;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: zl
 * @Date: Create in 2020/4/10 10:32
 * @Description:
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for (int i = 0; i < 6; i++) {
            final int tempInt = i ;
             new Thread(()->{
                 System.out.println(Thread.currentThread().getName()+"\t离开教室");
                 countDownLatch.countDown();
             },"线程"+ i).start();
        }
        countDownLatch.await();;
        System.out.println(Thread.currentThread().getName() + "\t班长离开教室,main线程是班长");
    }
}
