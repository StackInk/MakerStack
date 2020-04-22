package com.bywlstudio.queue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阻塞队列实现生产者消费者
 */
public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        test();
    }

    private static void testBlock(){
        BlockingQueue<Integer> blockingQueue = new SynchronousQueue<>();
        new Thread(()->{
            try {
                blockingQueue.take();
                System.out.println("执行了");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"A").start();
        new Thread(()->{
            try {
                try{
                    TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
                blockingQueue.put(2);
                System.out.println("执行了==");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"B").start();
    }

    private static void testSychron() {
        BlockingQueue<Integer> blockingQueue = new SynchronousQueue<>();
        try {
            blockingQueue.put(2);
            System.out.println("11111");
            try{TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            try {
                blockingQueue.put(1);
                System.out.println("执行了");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"A").start();
        new Thread(()->{
            try {
                try{
                    TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
                blockingQueue.take();
                System.out.println("执行了==");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"B").start();
    }

    private static void test() {
        MyData myData = new MyData(new ArrayBlockingQueue(10));
            new Thread(()->{
                for (int j = 0; j < 5; j++) {
                    myData.increment();
                }
            },"线程一").start();
        try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
        new Thread(()->{
            for (int j = 0; j < 5; j++) {
                myData.decrement();
            }
        },"线程二").start();
        try{TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
        myData.stop();
    }
}

class MyData{
    private volatile boolean flag = true ;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private BlockingQueue<Integer> blockingQueue ;

    public MyData(BlockingQueue blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    public void increment(){
        while(flag){
            try {
                if(blockingQueue.offer(atomicInteger.getAndIncrement(),2,TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread().getName()+"\t 插入成功\t"+atomicInteger.get());
                }else{
                    System.out.println(Thread.currentThread().getName()+"\t 插入失败");
                }
                try{TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void decrement(){
        while (flag){
            try {
                Integer poll = blockingQueue.poll(2, TimeUnit.SECONDS);
                if(poll == null ){
                    flag = false ;
                    System.out.println(Thread.currentThread().getName()+"\t不再等待");
                    return ;
                }
                System.out.println(Thread.currentThread().getName()+"\t 获取成功\t"+ atomicInteger.decrementAndGet());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void stop(){
        flag = false ;
        System.out.println("主线程停止");
    }

}