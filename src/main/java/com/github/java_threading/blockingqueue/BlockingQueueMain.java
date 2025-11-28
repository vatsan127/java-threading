package com.github.java_threading.blockingqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * BlockingQueue is a thread-safe interface that extends the Queue interface.
 * It is designed primarily for producer–consumer scenarios where threads need to safely exchange data.
 * It provides blocking operations that automatically wait when the queue is full (for producers)
 * or empty (for consumers), eliminating the need for manual synchronization.
 * It does not permit null elements, and attempting to insert null results in a NullPointerException.
 * <p>
 * Default implementations:
 * LinkedBlockingQueue – Does not preallocate size; uses separate locks (putLock and takeLock).
 * ArrayBlockingQueue – Pre-allocates size and uses a single ReentrantLock for both operations.
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

    private final BlockingQueue<String> queue;

    public BlockingQueueMain() {
        queue = new LinkedBlockingQueue<>();
        queue.add("" + 0);
    }


    public void producer() {
        for (int i = 1; i < 10; i++) {
            try {
                queue.put("" + i);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void consumer() {
        while (true) {
            String element = queue.poll();
            if (element == null) {
                System.out.println("Queue is empty!!!");
                break;
            }
            System.out.println(Thread.currentThread().getName() + " " + element);
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
