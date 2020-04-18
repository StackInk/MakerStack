package com.bywlstudio.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/***
 * 单方法实现
 */
class MyPrintln{
    private int num = 1 ;
    private Lock lock = new ReentrantLock();
    private Condition condition01 = lock.newCondition();
    private Condition condition02 = lock.newCondition();
    private Condition condition03 = lock.newCondition();

    public void print(int length){
        lock.lock();
        try{
            String name = Thread.currentThread().getName();
            justPrint(length, name, "A", condition01, condition02);
            justPrint(length, name, "B", condition02, condition03);
            justPrint(length, name , "C" ,condition03,condition01);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
             lock.unlock();
        }
    }

    private void justPrint(int length, String name, String b, Condition condition002, Condition condition003) throws InterruptedException {
        while (name.equals(b)) {
            if ("A".equals(name)&&num != 1) {
                condition002.await();
                return;
            }else if("B".equals(name)&&num != 2){
                condition002.await();
                return;
            }else if("C".equals(name)&&num != 3){
                condition002.await();
                return;
            }
            for (int i = 0; i < length; i++) {
                System.out.println(name + "\t" + (i+1));
            }
            if("C".equals(name)){
                num = 1;
            }else if("B".equals(name)) {
                num = 3;
            }else if("A".equals(name)){
                num = 2 ;
            }
            condition003.signal();
        }
    }

}

/**
 * @Author: zl
 * @Date: Create in 2020/4/4 17:49
 * @Description: 线程A 打印5次 - > 线程B 打印10次 ->线程C 打印15次.执行10次
 *                  思想：引入一个变量num A 1 , B 2 , C 3
 */
public class ThreadOrderAccess {
    public static void main(String[] args) {
        singleMethod();

    }

    /**
     * 多个方法执行调用
     */
    private static void multiMethod() {
        MyPrintln02 myPrintln02 = new MyPrintln02();
        int num = 10 ;
        new Thread(()->{
            for (int i = 0; i < num; i++)
            myPrintln02.printA();
        },"A").start();
        new Thread(()->{
            for (int i = 0; i < num; i++)
            myPrintln02.printB();
        },"B").start();
        new Thread(()->{
            for (int i = 0; i < num; i++)
            myPrintln02.printC();
        },"C").start();
    }

    private static void singleMethod() {
        MyPrintln myPrintln = new MyPrintln();
        int num = 100 ;
        new Thread(()->{
            for (int i = 0; i < num; i++)
                myPrintln.print(5);
        },"A").start();
        new Thread(()->{
            for (int i = 0; i < num; i++)
                myPrintln.print(10);
        },"B").start();
        new Thread(()->{
            for (int i = 0; i < num; i++)
                myPrintln.print(15);
        },"C").start();
    }
}

/**
 * 三方法实现
 */
class MyPrintln02{
    private int num = 1 ;
    private Lock lock = new ReentrantLock();
    private Condition condition01 = lock.newCondition();
    private Condition condition02 = lock.newCondition();
    private Condition condition03 = lock.newCondition();

    public void printA(){
        lock.lock();
        try{
            while(num !=1){
                condition01.await();
            }
            String name = Thread.currentThread().getName();
            for (int i = 0; i < 5; i++) {
                System.out.println(name+"\t"+(i+1));
            }
            num = 2 ;
            condition02.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
             lock.unlock();
        }
    }
    public void printB(){
        lock.lock();
        try{
            while(num !=2){
                condition02.await();
            }
            String name = Thread.currentThread().getName();
            for (int i = 0; i < 10; i++) {
                System.out.println(name+"\t"+(i+1));
            }
            num = 3 ;
            condition03.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void printC(){
        lock.lock();
        try{
            while(num !=3){
                condition03.await();
            }
            String name = Thread.currentThread().getName();
            for (int i = 0; i < 15; i++) {
                System.out.println(name+"\t"+(i+1));
            }
            num = 1 ;
            condition01.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
