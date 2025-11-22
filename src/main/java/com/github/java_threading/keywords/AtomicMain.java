package com.github.java_threading.keywords;

/*
 * Atomic classes provide lock-free, thread-safe operations using compare-and-swap hardware instructions.
 */

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicMain {

    private static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {

        Runnable incrementCounter = () -> {
            for (int i = 0; i < 100; i++) {
                System.out.println(Thread.currentThread().getName() + " counter incremented " + counter.incrementAndGet());
            }
        };

        Thread t1 = new Thread(incrementCounter);
        Thread t2 = new Thread(incrementCounter);

        t1.start();
        t2.start();

        System.out.println(counter);

    }

}
