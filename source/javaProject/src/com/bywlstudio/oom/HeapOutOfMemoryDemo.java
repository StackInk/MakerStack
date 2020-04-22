package com.bywlstudio.oom;

/**
 * 堆溢出模拟
 */
public class HeapOutOfMemoryDemo {
    public static void main(String[] args) {
        byte[] bytes = new byte[1024*1024*30] ;
    }
}
