package com.bywlstudio.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: zl
 * @Date: Create in 2020/4/3 18:51
 * @Description: 消费者和生产者练习
 */
public class CusumeAndSuppDemo {
    public static void main(String[] args) {
        Air air = new Air();
        new Thread(()->{for (int i = 0; i < 10; i++) air.increment();},"A").start();
        new Thread(()->{for (int i = 0; i < 10; i++) air.decrement();},"B").start();
        new Thread(()->{for (int i = 0; i < 10; i++) air.increment();},"C").start();
        new Thread(()->{for (int i = 0; i < 10; i++) air.increment();},"E").start();
        new Thread(()->{for (int i = 0; i < 10; i++) air.decrement();},"D").start();
        new Thread(()->{for (int i = 0; i < 10; i++) air.decrement();},"F").start();
    }
}

/**
 * 需求，一个线程加，一个线程减，最后保证这个数字仍然为0；
 */
class Air{
    private int num = 0 ;

    /**
     * 加的方法
     */
    public synchronized void increment(){
        while(num != 0 ){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        num++;
        System.out.println(num);
        this.notifyAll();
    }

    /***
     * 减的方法
     */
    public synchronized void decrement(){
        while(num == 0 ){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        num--;
        System.out.println(num);
        this.notifyAll();
    }

    /**
     * 使用Lock实现
     */
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    public void increment1(){
        lock.lock();
        try{
            while(num != 0 ){
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            num++;
            System.out.println(num);
            condition.signalAll();
        }finally {
             lock.unlock();
        }
    }

    /***
     * 减的方法
     */
    public synchronized void decrement1(){
        lock.lock();
        try{
            while(num == 0 ){
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            num--;
            System.out.println(num);
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }
}
