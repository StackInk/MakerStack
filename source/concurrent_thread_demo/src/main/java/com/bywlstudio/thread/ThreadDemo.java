package com.bywlstudio.thread;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 目前有30张票
 * 两个线程买票
 *
 * 线程 操作  资源
 */
class MyTask{
    private int ticket = 30 ;
    private Lock lock = new ReentrantLock();

    /**
     * 使用synchronized实现
     */
    public synchronized void shopTicket(){

        if(ticket > 0 ){
            System.out.println(Thread.currentThread().getName()+"抢到了\t"+ticket--+"剩余"+ticket+"\t张票");
        }
    }

    /**
     * 使用可重用锁实现
     */
    public void shopTicket2(){
        lock.lock();
        try{
            if(ticket > 0 ){
                System.out.println(Thread.currentThread().getName()+"抢到了\t"+ticket--+"剩余"+ticket+"\t张票");
            }
        }finally {
             lock.unlock();
        }

    }

}


/**
 * 买票案例
 */
public class ThreadDemo {
    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        lamdbaMethod(myTask);

    }

    private static void lamdbaMethod(MyTask myTask) {
        new Thread(()->{
            for (int i = 0; i < 40; i++){
                myTask.shopTicket2();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                 }
            }
        },"A").start();
        new Thread(()->{for (int i = 0; i < 40; i++) myTask.shopTicket2();},"B").start();
        new Thread(()->{for (int i = 0; i < 40; i++) myTask.shopTicket2();},"C").start();
    }

    /**
     * 匿名内部类的实现方式
     * @param myTask
     */
    private static void anonyMethod(MyTask myTask) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 40; i++) {

                    myTask.shopTicket();
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"A").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 40; i++) {
                    myTask.shopTicket();
                }
            }
        },"B").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 40; i++) {
                    myTask.shopTicket();
                }
            }
        },"C").start();
    }
}
