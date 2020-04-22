package com.bywlstudio.nio;

import java.nio.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 缓存区NIO
 */


public class BufferNioDemo {

    public static void main(String[] args) {

    }


    private static void allocateTest() {
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        allocate.put("abcdef".getBytes());
        allocate.flip();
        byte[] bytes = new byte[allocate.limit()];
        allocate.get(bytes,0,2);
        System.out.println(Arrays.toString(bytes));

        System.out.println(allocate.position());
        System.out.println("对该处指针进行标记");
        allocate.mark();

        allocate.get(bytes,2,2);
        System.out.println(Arrays.toString(bytes));
        System.out.println(allocate.position());

        allocate.reset();
        System.out.println("重新回到标记以后的指针"+allocate.position());
        //判断缓冲区中是否还有数据
        if(allocate.hasRemaining()){
            System.out.println(allocate.remaining());//获取缓冲区中可以被操作的数据的个数
        }
    }

    /**
     * Buffer的基本操作流程
     */
    private static void bufferTest() {
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        System.out.println("=======allocate()=========");
        System.out.println(allocate.position());
        System.out.println(allocate.limit());
        System.out.println(allocate.capacity());
        System.out.println("=======put()=============");
        allocate.put("abcdef".getBytes());
        System.out.println(allocate.position());
        System.out.println(allocate.limit());
        System.out.println(allocate.capacity());
        System.out.println("============flip()==========");
        allocate.flip();
        System.out.println(allocate.position());
        System.out.println(allocate.limit());
        System.out.println(allocate.capacity());
        System.out.println("==========get()==========");
        byte[] bytes = new byte[allocate.limit()];
        allocate.get(bytes);
        System.out.println(Arrays.toString(bytes));
        System.out.println(allocate.position());
        System.out.println(allocate.limit());
        System.out.println(allocate.capacity());
        System.out.println("============rewind()======");
        allocate.rewind();//将当前position指针指向缓冲区开头
        System.out.println(allocate.position());
        System.out.println(allocate.limit());
        System.out.println(allocate.capacity());
        System.out.println("==============allocate()=========");
        allocate.clear();//清空缓冲区，此时limit回到容量值，以前的数据还在，处于被遗忘的状态，直接被覆盖
        System.out.println(allocate.position());
        System.out.println(allocate.limit());
        System.out.println(allocate.capacity());
        System.out.println((char)allocate.get());
    }


}
