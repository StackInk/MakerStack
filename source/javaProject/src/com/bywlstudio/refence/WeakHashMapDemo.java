package com.bywlstudio.refence;

import java.util.HashMap;
import java.util.WeakHashMap;

public class WeakHashMapDemo {
    public static void identHashMap(){
        HashMap<Integer,String> hashMap = new HashMap();
        Integer integer = new Integer(1);
        hashMap.put(integer,"MakerStack");
        System.out.println(hashMap);
        integer = null ;
        System.gc();
        //取消强引用
        try{

        }finally {
            System.out.println(hashMap);
        }
    }

    public static void weakHashMapTest(){
        WeakHashMap<Integer,String> hashMap = new WeakHashMap();
        Integer integer = new Integer(1);
        hashMap.put(integer,"MakerStack");
        System.out.println(hashMap);
        integer = null ;
        System.gc();
        //取消强引用
        try{

        }finally {
            System.out.println(hashMap);
        }
    }

    public static void main(String[] args) {
        weakHashMapTest();
    }
}
