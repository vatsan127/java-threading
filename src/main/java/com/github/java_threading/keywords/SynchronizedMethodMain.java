package com.github.java_threading.keywords;

public class SynchronizedMethodMain {

    public static synchronized void syncMethod() {
        try {
            System.out.println(Thread.currentThread().getName() + " -  Entered Synchronized method");
            Thread.sleep(3000);
            System.out.println(Thread.currentThread().getName() + " -  Exited Synchronized method");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> syncMethod();
        Thread t1 = new Thread(runnable, "Thread-1");
        Thread t2 = new Thread(runnable, "Thread-2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Main Thread Execution is done!");
    }

}
