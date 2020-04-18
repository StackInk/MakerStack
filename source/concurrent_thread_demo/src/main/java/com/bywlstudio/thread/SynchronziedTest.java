package com.bywlstudio.thread;

/**
 * @Author: zl
 * @Date: Create in 2020/4/4 10:13
 * @Description:
 */
public class SynchronziedTest {
    private int num = 10 ;
    public void test(){
        synchronized (this){
            System.out.println("这是一个测试");
        }
    }

    public static void main(String[] args) {
        SynchronziedTest synchronziedTest = new SynchronziedTest();
        new Thread(()->{synchronziedTest.test();},"A").start();
        new Thread(()->{synchronziedTest.test();},"B").start();
    }

}

