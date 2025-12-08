package com.github.java_threading.locks.semaphore.connectionpool;

public class ClientTask implements Runnable {

    private final ConnectionPool pool;

    public ClientTask(ConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        try {
            System.out.println(name + ": Waiting for connection...");

            int connId = pool.acquire();
            System.out.println(name + ": Got Connection-" + connId + " | Available: " + pool.availablePermits());

            // Use connection
            Thread.sleep(200);

            pool.release();
            System.out.println(name + ": Released connection | Available: " + pool.availablePermits());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
