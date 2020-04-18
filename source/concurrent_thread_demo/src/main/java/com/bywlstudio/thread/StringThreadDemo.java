package com.bywlstudio.thread;

import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: zl
 * @Date: Create in 2020/4/3 19:09
 * @Description:
 */
public class StringThreadDemo {
    public static void main(String[] args) {
        MyPrint myPrint = new MyPrint();
        //现在存在两个数组，间隔打印
       new Thread(()->{myPrint.printInt();},"A").start();
       new Thread(()->{myPrint.printChar();},"B").start();

    }
}
class MyPrint{
    int[] arr01 = new int[]{1,2,3};
    char[] arr02 = new char[]{'a','b','c'};
    boolean flag = true ;
//    public synchronized void printInt()  {
//        int i = 0 ;
//        while (flag && i < arr01.length){
//            System.out.println(arr01[i++]);
//            flag = false ;
//            this.notifyAll();
//            try {
//                this.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    public synchronized void printChar()  {
//        int i = 0 ;
//        while (!flag && i < arr02.length){
//            System.out.println(arr02[i++]);
//            flag = true ;
//            this.notifyAll();
//            try {
//                this.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
    private Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
public void printInt()  {
    lock.lock();
    try{
        int i = 0 ;
        while (flag && i < arr01.length){
            System.out.println(arr01[i++]);
            flag = false ;
            condition.signalAll();
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }finally {
         lock.unlock();
    }
}
    public  void printChar()  {
        lock.lock();
        try{
            int i = 0 ;
            while (!flag && i < arr02.length){
                System.out.println(arr02[i++]);
                flag = true ;
                condition.signalAll();
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            lock.unlock();
        }


    }


}


