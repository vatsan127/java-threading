package com.github.java_threading.locks.semaphore.connectionpool;

import java.util.concurrent.Semaphore;

/**
 * Simple Connection Pool using Semaphore
 *
 * Semaphore limits how many threads can access the pool at once.
 */
public class ConnectionPool {

    private final Semaphore semaphore;
    private int connectionCounter = 0;

    public ConnectionPool(int maxConnections) {
        this.semaphore = new Semaphore(maxConnections);
    }

    public int acquire() throws InterruptedException {
        semaphore.acquire();
        return ++connectionCounter;
    }

    public void release() {
        semaphore.release();
    }

    public int availablePermits() {
        return semaphore.availablePermits();
    }
}
