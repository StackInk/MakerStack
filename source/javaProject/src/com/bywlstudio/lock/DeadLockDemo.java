package com.bywlstudio.lock;

import java.util.concurrent.TimeUnit;

class MyResrouce implements Runnable{

    private String lock1 ;
    private String lock2 ;

    public MyResrouce(String lock1, String lock2) {
        this.lock1 = lock1;
        this.lock2 = lock2;
    }

    @Override
    public void run() {
        synchronized (lock1){
            System.out.println(Thread.currentThread().getName()+"\t 获取到了"+lock1+"\t 准备获取"+lock2);
            try {Thread.sleep(20);} catch (InterruptedException e) {e.printStackTrace();}
            synchronized (lock2){
            }
        }
    }
}

public class DeadLockDemo {
    public static void main(String[] args) {
        String lock1 = "LockA" ;
        String lock2 = "LockB" ;
        new Thread(new MyResrouce(lock1,lock2),"线程一").start();
        new Thread(new MyResrouce(lock2,lock1),"线程二").start();
    }
}
