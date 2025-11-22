package com.github.java_threading.signaling;

public class SignalingMain {

    /**
     * The wait(), notify(), and notifyAll() methods cannot be called on an object unless the calling thread holds the synchronization lock on that object.
     * They must be called from within a synchronized block or method. If called without holding the lock, an IllegalMonitorStateException is thrown.
     * When wait() is called, it causes the current thread to release the synchronization lock on that object and enter a waiting state until another thread invokes notify() or notifyAll() on the same object.
     * Calling notify() wakes up a single thread that is waiting on that object's monitor, while notifyAll() wakes up all threads waiting on that object's monitor.
     * With notifyAll() lock will be acquired by one of the awakened threads, but which specific thread acquires it is determined by the thread scheduler and JVM implementation.
     * The awakened thread must re-acquire the lock before resuming execution from the wait() call.
     */

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


