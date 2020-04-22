package com.bywlstudio.util;

import java.util.concurrent.*;

public class CountLanunchDemo {
    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(3);
        for(int i = 0 ; i < 5 ; i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"\t进入执行");
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"\t离开");
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            },"线程为："+i).start();
        }
    }

    private static void cyclicBarriertest() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        for(int i = 0 ; i < 5 ; i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"\t进入");
                try{
                    TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+"\t执行完成");
            },"线程为："+i).start();
        }
    }

    private static void countDownLatchtest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        for(int i = 0 ; i < 3 ; i++){
            new Thread(()->{
                System.out.println("输出");
                countDownLatch.countDown();
            },"线程为："+i).start();

        }
        countDownLatch.await();
        System.out.println("主线程执行");
    }
}
