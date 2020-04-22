package com.bywlstudio.pool;

/**
 * 创建一个任务类
 */
public class MyTask implements Runnable{

    int id  ;

    public MyTask(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        System.out.println(name+"\t即将执行任务"+id);
        try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println(name+"\t完成了任务");
    }

    @Override
    public String toString() {
        return "MyTask{" +
                "id=" + id +
                '}';
    }
}
