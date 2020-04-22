package com.bywlstudio.reIn;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 自旋锁
 */

class MyOwnLock{
    AtomicReference<Thread> atomicReference = new AtomicReference<>();
    public void lock(){
        String name = Thread.currentThread().getName();
        System.out.println(name+"准备获取锁");
        //如果当前地址的引用为Null,则直接获取锁，跳出循环；如果不是则自旋不断获取锁
        while (!atomicReference.compareAndSet(null,Thread.currentThread())){

        }
        System.out.println(name+"获取锁成功++++++");
    }
    public void unlock(){
        String name = Thread.currentThread().getName();
        atomicReference.compareAndSet(Thread.currentThread(),null);
        System.out.println(name+"\t释放锁成功");
    }
}

public class OwnCriDemo {

    public static void main(String[] args) {
        MyOwnLock myOwnLock = new MyOwnLock();
        new Thread(()->{
            myOwnLock.lock();
            try{TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
            myOwnLock.unlock();

        },"A").start();
        new Thread(()->{
            myOwnLock.lock();
            myOwnLock.unlock();
        },"B").start();

    }

}
