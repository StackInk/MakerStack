package com.bywlstudio.gc;

/**
 * 串行化GCDemo
 * 通过设置 -XX:+UseSerialGC
 */
public class SerialNewDemo {
    public static void main(String[] args) {
        byte[] bytes = new byte[1024*1024*30];
    }
}
