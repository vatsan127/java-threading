package com.github.java_threading.blockingqueue;

import java.sql.Time;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * BlockingQueue is a thread-safe interface that extends the Queue interface.
 * It is designed primarily for producer–consumer scenarios where threads need to safely exchange data.
 * It provides blocking operations that automatically wait when the queue is full (for producers)
 * or empty (for consumers), eliminating the need for manual synchronization.
 * It does not permit null elements, and attempting to insert null results in a NullPointerException.
 * <p>
 * Default implementations:
 * LinkedBlockingQueue – Does not preallocate size; uses separate locks (putLock and takeLock).
 * ArrayBlockingQueue – Preallocates size and uses a single ReentrantLock for both operations.
 * <p>
 * Write operations:
 * add     – Throws IllegalStateException if the queue is full.
 * put     – Inserts an element, waiting indefinitely if necessary for space to become available.
 * offer   – Returns false immediately if the queue is full (does not block).
 * <p>
 * Read operations:
 * take    – Retrieves and removes the head element, blocking until an element becomes available.
 * poll    – Returns null immediately if the queue is empty.
 */

public class BlockingQueueMain {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void producer() {
        while (true){
            try {
                queue.put(""+System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void consumer() {
        while (true){
            try {
                String element = queue.take();
                System.out.println(Thread.currentThread().getName()+" "+element);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {


        BlockingQueueMain obj = new BlockingQueueMain();

        Thread t1 = new Thread(() -> obj.producer(), "producer-thread");
        Thread t2 = new Thread(() -> obj.consumer(), "consumer-thread");

        t1.start();
        t2.start();

        t1.join();
        t2.join();


    }

}
