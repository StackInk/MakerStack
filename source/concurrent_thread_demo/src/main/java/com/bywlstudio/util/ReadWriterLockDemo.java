package com.bywlstudio.util;


import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: zl
 * @Date: Create in 2020/4/11 17:43
 * @Description:读写锁案例
 */
public class ReadWriterLockDemo {

    private volatile HashMap<String,Integer> hashMap = new HashMap<>();

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    public void addData(String key , Integer value){
        readWriteLock.writeLock().lock();
        try{
            System.out.println(Thread.currentThread().getName()+"\t正在写");
            try {Thread.sleep(300);} catch (InterruptedException e) {e.printStackTrace();}
            Integer put = hashMap.put(key, value);
            System.out.println(Thread.currentThread().getName()+"\t写入成功");
        }finally {
             readWriteLock.writeLock().unlock();
        }

    }

    public void getData(String key ){
        readWriteLock.readLock().lock();
        try{
            System.out.println(Thread.currentThread().getName()+"\t正在读");
            try {Thread.sleep(300);} catch (InterruptedException e) {e.printStackTrace();}
            Integer integer = hashMap.get(key);
            System.out.println(Thread.currentThread().getName()+"\t读取完成"+integer);
        }finally {
             readWriteLock.readLock().unlock();
        }
    }

    public static void main(String[] args) {
        ReadWriterLockDemo readWriterLockDemo = new ReadWriterLockDemo();
        for (int i = 0; i < 5; i++) {
             int  tempInt = i ;
             new Thread(()->{
                readWriterLockDemo.addData("线程"+tempInt,tempInt);
             },"线程"+ i).start();
        }
        for (int i = 0; i < 5; i++) {
            int tempInt = i ;
             new Thread(()->{
                readWriterLockDemo.getData("线程"+tempInt);
             },"线程"+ i).start();
        }
    }

}
