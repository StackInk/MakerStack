package com.bywlstudio.lock;

import java.util.concurrent.locks.Lock;

/**
 * 锁粗化
 */
public class LockBigDemo {
    public static void main(String[] args) {
        synchronized (LockBigDemo.class){
            for (int i = 0; i < 10; i++) {
                    System.out.println();
            }
        }
    }
}
