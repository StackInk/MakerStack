package com.bywlstudio.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 自定义一个线程
 */
public class MyThread extends Thread{
    private String name ;
    private List<Runnable> tasks ;

    public MyThread(String name, List<Runnable> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    @Override
    public void run() {
        while(tasks.size()>0){
            Runnable remove = tasks.remove(0);
            remove.run();
        }
    }
}
