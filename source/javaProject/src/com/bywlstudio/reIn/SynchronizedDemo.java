package com.bywlstudio.reIn;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sycn 可重入锁实现
 */

class MyData implements Runnable{
    private Lock lock = new ReentrantLock();

    public synchronized void printName(){
        System.out.println(Thread.currentThread().getName()+"\t打印名字");
        int i = printAge();
        System.out.println("名字结束"+i);
    }

    @Override
    public void run() {
        lock.lock();
        try{
            printID();
        }finally {
            lock.unlock();
         }
    }

    private void printID() {
        lock.lock();
        try{
            System.out.println(Thread.currentThread().getName()+"\t打印ID");
        }finally {
            lock.unlock();
         }
    }

    public  int printAge(){
        synchronized (MyData.class){
            System.out.println(Thread.currentThread().getName() + "\t打印年龄***********");
            return 0 ;
        }
    }



}

public class SynchronizedDemo {
    public static void main(String[] args) {
        MyData myData = new MyData();
        new Thread(()->{myData.printName();},"线程一").start();
        new Thread(()->{myData.printName();},"线程二").start();

        try {TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("----------------------------------------------------------");

        new Thread(myData,"线程三").start();
        new Thread(myData,"线程四").start();

    }
}
