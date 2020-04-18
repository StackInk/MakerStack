package com.bywlstudio.queue;

import java.sql.Time;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zl
 * @Date: Create in 2020/4/11 18:33
 * @Description:
 */
public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue(3);
        offerAndPoll(blockingQueue);
    }

    private static void offerAndPoll(BlockingQueue<Integer> blockingQueue) throws InterruptedException {
        blockingQueue.offer(1);
        blockingQueue.offer(1);
        blockingQueue.offer(1);
        System.out.println(blockingQueue.element());
        System.out.println(blockingQueue.peek());
//        for (int i = 0; i < 4; i++) {
//            System.out.println(blockingQueue.poll(3, TimeUnit.SECONDS));
//        }
    }

    private static void putAndtake(BlockingQueue<Integer> blockingQueue) throws InterruptedException {
        blockingQueue.put(1);
        blockingQueue.put(2);
        blockingQueue.put(3);
        for (int i = 0; i < 4; i++) {
            System.out.println(blockingQueue.take());
        }
    }

    private static void offerAndpoll(BlockingQueue<Integer> blockingQueue) {
        blockingQueue.offer(1);
        blockingQueue.offer(1);
        blockingQueue.offer(1);
        for (int i = 0; i < 4; i++) {
            System.out.println(blockingQueue.poll());
        }
    }

    private static void addAndremove(BlockingQueue<Integer> blockingQueue) {
        blockingQueue.add(1);
        blockingQueue.add(2);
        blockingQueue.add(3);
        for (int i = 0; i < 4; i++) {
            System.out.println(blockingQueue.remove());
        }
        //        blockingQueue.add(3);
    }

}
