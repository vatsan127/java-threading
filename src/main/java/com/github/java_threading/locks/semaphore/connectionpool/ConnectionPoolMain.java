package com.github.java_threading.locks.semaphore.connectionpool;

public class ConnectionPoolMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Connection Pool (3 connections, 5 clients) ===\n");

        ConnectionPool pool = new ConnectionPool(3);

        // 5 clients competing for 3 connections
        Thread[] clients = new Thread[5];
        for (int i = 0; i < 5; i++) {
            clients[i] = new Thread(new ClientTask(pool), "Client-" + i);
            clients[i].start();
        }

        for (Thread c : clients) {
            c.join();
        }

        System.out.println("\nAll clients finished!");
    }
}
