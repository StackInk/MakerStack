package com.bywlstudio.cas;


import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicStampRefenceDemo {
    public static void main(String[] args) {
        User user01 = new User("1",1);
        User user02 = new User("2",2);
        //初始化值和初始化一个版本号
        AtomicStampedReference<User> atomicStampedReference = new AtomicStampedReference<>(user01,100);
        boolean b = atomicStampedReference.compareAndSet(user02, user01, 100, 101);
        boolean b1 = atomicStampedReference.compareAndSet(user01, user02, 101, 101);
        boolean b2 = atomicStampedReference.compareAndSet(user01, user02, 100, 101);
        System.out.println(b2);

    }
}
