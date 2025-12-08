package com.github.java_threading.locks.semaphore;

import java.util.concurrent.Semaphore;

/**
 * Simple Connection Pool using Semaphore
 *
 * Semaphore limits how many threads can access the pool at once.
 */
public class ConnectionPoolMain {

    private static final Semaphore semaphore = new Semaphore(3);  // 3 connections
    private static int connectionCounter = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Connection Pool (3 connections, 5 clients) ===\n");

        // 5 clients competing for 3 connections
        Thread[] clients = new Thread[5];
        for (int i = 0; i < 5; i++) {
            clients[i] = new Thread(new ClientTask(), "Client-" + i);
            clients[i].start();
        }

        for (Thread c : clients) {
            c.join();
        }

        System.out.println("\nAll clients finished!");
    }

    static class ClientTask implements Runnable {
        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            try {
                System.out.println(name + ": Waiting for connection...");

                semaphore.acquire();  // Wait for permit
                int connId = ++connectionCounter;
                System.out.println(name + ": Got Connection-" + connId + " | Available: " + semaphore.availablePermits());

                // Use connection
                Thread.sleep(200);

                semaphore.release();  // Return permit
                System.out.println(name + ": Released connection | Available: " + semaphore.availablePermits());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
