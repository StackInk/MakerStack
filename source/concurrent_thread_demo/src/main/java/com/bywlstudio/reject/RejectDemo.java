package com.bywlstudio.reject;

import java.util.concurrent.*;

/**
 * @Author: zl
 * @Date: Create in 2020/4/13 11:59
 * @Description: 线程池拒绝策略
 */
public class RejectDemo {
    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(3),
                Executors.defaultThreadFactory()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
        try{
            for (int i = 0; i < 10; i++) {
                final int tempInt = i ;
                threadPoolExecutor.execute(()-> System.out.println("第"+tempInt+"\t个任务"));
            }
        }catch (Exception e){

        }finally {

        threadPoolExecutor.shutdown();
        }
    }
}
