package com.bywlstudio.pool;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 自定义的线程池
 */
public class MyPool {

    private int currentPoolSize ;
    private int corePoolSize ;
    private int maxPoolSize ;
    private int workSize ;
    private List<Runnable> tasks = new CopyOnWriteArrayList<>();

    public MyPool(int corePoolSize, int maxPoolSize, int workSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.workSize = workSize;
    }

    public void sumbit(Runnable runnable){
        //如果当前线程数小于核心线程数
        if(tasks.size()>=workSize){
            System.out.println("任务被丢弃");
        }else{
            tasks.add(runnable);
            this.execTask(runnable);
        }
    }

    public void execTask(Runnable runnable){
        if(currentPoolSize <= corePoolSize){
            new MyThread("线程"+currentPoolSize,tasks).start();
            currentPoolSize++;
        }else if(currentPoolSize < maxPoolSize){
            new MyThread("非核心线程"+currentPoolSize,tasks).start();
            currentPoolSize++;
        }else{
            System.out.println(runnable+"任务被缓存");
        }
    }

}
