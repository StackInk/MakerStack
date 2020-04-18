package com.bywlstudio;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: zl
 * @Date: Create in 2020/4/11 14:32
 * @Description:公平锁的实现
 */
class MyFairTask {
    //true代表公平锁
    private ReentrantLock reentrantLock = new ReentrantLock(false);

    public void lanchLock(){
        reentrantLock.lock();
        try{
            try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
            System.out.println(Thread.currentThread().getName()+"抢占了系统资源");
        }finally {
             reentrantLock.unlock();
        }
    }
}
public class MyFairLock {

    public static void main(String[] args) {
        MyFairTask myFairTask = new MyFairTask();
        for (int i = 0; i < 10; i++) {
             new Thread(()->{
                myFairTask.lanchLock();
             },"线程"+ i).start();
        }
        System.out.println("Main线程执行");
    }


}
