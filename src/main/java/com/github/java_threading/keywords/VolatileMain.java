package com.github.java_threading.keywords;

public class VolatileMain {

    /**
     * Volatile applies only to variables and ensures visibility of changes across threads.
     * It does not provide atomicity or mutual exclusion.
     * When a variable is declared volatile, it's stored directly in RAM rather than being cached.
     * Ensuring that all threads see the most recent value.
     */

    private boolean running = true;
//    private volatile boolean running = true;

    public void runner() throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("Worker thread started");
            while (running) {
                System.out.println("Worker Thread running.");
            }
            System.out.println("Worker thread stopped");
        });

        worker.start();
        Thread.sleep(1000);

//        running = false; // Signal worker to stop
        System.out.println("Main thread stopped worker");

        worker.join();
        System.out.println("Program finished");
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileMain obj = new VolatileMain();
        obj.runner();
    }
}

