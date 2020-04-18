package com.bywlstudio.thread;

import java.util.concurrent.TimeUnit;

class Phone {
    public static synchronized void printMessage(){
        try {TimeUnit.SECONDS.sleep(4);} catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("Print_Message");
    }
    public synchronized void printMS(){
        System.out.println("Print_MS");
    }
    public void printHello(){
        System.out.println("Hello");
    }
}

/**
 *      假定一个场景，A线程比B线程先执行100ms                 答案：
 * 1 标准访问，先打印短信还是邮件                                邮件，拿到this对象锁，其他线程不能访问加锁方法
 *          接下来的行为是在打印电子邮件需要4000ms才能执行的
 * 2 停4秒在短信方法内，先打印短信还是邮件                        邮件，拿到this对象锁，其他线程只能等锁释放以后执行
 * 3 普通的hello方法，是先打短信还是hello                        hello，通过this对象上锁，而执行普通方法不需要锁，可通过Javap指令看普通方法的执行过程
 * 4 现在有两部手机，先打印短信还是邮件                          短信，上锁的this对象不同，所以按照执行时间执行
 * 5 两个静态同步方法，1部手机，先打印短信还是邮件                   邮件，通过Class上锁，两个同步方法锁一样，其他线程岂能等待
 * 6 两个静态同步方法，2部手机，先打印短信还是邮件                    邮件，通过Class上锁，锁一样(类对象仅仅有一个Class类型)
 * 7 1个静态同步方法，1个普通同步方法，1部手机，先打印短信还是邮件       短信。静态使用类锁，普通不同使用对象锁，锁不一样，不影响
 * 8 1个静态同步方法，1个普通同步方法，2部手机，先打印短信还是邮件       短信，锁不相同
 */
public class SynchronizedLock {

    public static void main(String[] args) {
        Phone phone = new Phone();
        Phone phone1 = new Phone();
        new Thread(()->{
            phone.printMessage();
        },"A").start();
        try {TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
        new Thread(()->{
            phone.printMS();
//            phone.printHello();
//            phone1.printMS();
        },"A").start();
        /**
         * java.lang.ArrayIndexOutOfBoundsException: Index 6 out of bounds for length 6
         *   at line 11, Solution.fourSum
         *   at line 57, __DriverSolution__.__helper__
         *   at line 87, __Driver__.main
         */
    }

}
