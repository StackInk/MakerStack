package com.bywlstudio.refence;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class PhantomReferenceDemo {
    public static void main(String[] args) {
        Object o = new Object();
        ReferenceQueue referenceQueue = new ReferenceQueue();
        PhantomReference<Object> phantomReference = new PhantomReference<>(o,referenceQueue);
        System.out.println(o);
        System.out.println(phantomReference.get());
        System.out.println(referenceQueue.poll());
        System.out.println("GC后**************");
        o = null ;
        System.gc();
        System.out.println(o);
        System.out.println(phantomReference.get());
        System.out.println(referenceQueue.poll());
    }
}
