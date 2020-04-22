package com.bywlstudio.refence;

import java.lang.ref.SoftReference;

public class SoftRefenceDemo {

    /**
     * 模拟软引用,发生OOM，输出此时的引用
     *  -Xms10m -Xmx10m
     */
    public static void test(){
        //创建强引用
        Object o = new Object() ;
        //创建软引用
        SoftReference<Object> softReference = new SoftReference<>(o);
        System.out.println(o);
        System.out.println(softReference.get());
        //取消强引用
        o = null ;
        try {
            //当发生内存不足的时候，软引用被取消，对象被销毁，
            byte[] bytes = new byte[1024*1024*30] ;
        }finally {
            System.out.println(o);
            System.out.println(softReference.get());
        }
    }

    public static void main(String[] args) {
        test();
    }
}
