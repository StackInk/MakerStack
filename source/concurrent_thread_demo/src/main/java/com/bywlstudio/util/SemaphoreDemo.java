package com.bywlstudio.util;

import java.util.concurrent.Semaphore;

/**
 * @Author: zl
 * @Date: Create in 2020/4/10 11:10
 * @Description:
 */
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 4; i++) {
             new Thread(()->{
                try{
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"\t抢到了热巴");
                    try {Thread.sleep(300);} catch (InterruptedException e) {e.printStackTrace();}
                    System.out.println(Thread.currentThread().getName()+"\t热巴拒绝了他");
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
             },"线程"+ i).start();
        }
    }
}
