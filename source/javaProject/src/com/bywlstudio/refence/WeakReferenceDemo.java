package com.bywlstudio.refence;

import java.lang.ref.WeakReference;

public class WeakReferenceDemo {


    public static void main(String[] args) {
        //创建强引用
        Object o = new Object();
        //创建弱引用
        WeakReference<Object> weakReference = new WeakReference<>(o);
        System.out.println(o);
        System.out.println(weakReference.get());
        //取消强引用
        o=null ;
        System.gc();
        System.out.println("GC以后\t"+weakReference.get());
        System.out.println(o);

    }

    private static void test() {

    }
}
