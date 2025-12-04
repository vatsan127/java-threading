package com.github.java_threading.signaling_threads;

public class SignalingMain {

    private static final Object lock = new Object();

    public static void doWait(Object lock) {
        synchronized (lock) {
            try {
                System.out.println(Thread.currentThread().getName() + " Lock acquired!!!");
                lock.wait();
                System.out.println(Thread.currentThread().getName() + " waiting on object.");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void doNotify(Object lock) {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " Lock acquired!!!");
            lock.notify();
            System.out.println(Thread.currentThread().getName() + " notify() done.");
        }
    }

    public static void main(String[] args) throws InterruptedException {


        Thread t1 = new Thread(() -> doWait(lock), "wait-thread");
        Thread t2 = new Thread(() -> doNotify(lock), "notify-thread");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Main Thread Execution done!!!");

    }
}


