package com.github.java_threading.interrupted_exception;

/**
 * Demonstrates InterruptedException with Thread.join()
 *
 * Thread.join() blocks the calling thread until the target thread completes.
 * If the calling thread is interrupted while waiting, join() throws InterruptedException.
 */
public class InterruptWithJoin {

    public static void main(String[] args) {
        System.out.println("=== InterruptedException with join() Demo ===\n");

        // Create a long-running worker thread
        Thread longRunningWorker = new Thread(() -> {
            System.out.println("Worker: Starting long task (10 seconds)...");
            try {
                Thread.sleep(10000);
                System.out.println("Worker: Task completed!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Worker: Was interrupted!");
            }
        }, "LongWorker");

        // Create a waiter thread that will join on the worker
        Thread waiter = new Thread(() -> {
            System.out.println("Waiter: Waiting for worker to finish...");
            try {
                longRunningWorker.join(); // This can throw InterruptedException
                System.out.println("Waiter: Worker finished, continuing...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Waiter: Interrupted while waiting for worker!");
                System.out.println("Waiter: Will stop waiting and continue...");
            }
            System.out.println("Waiter: Done");
        }, "Waiter");

        // Main thread will interrupt the waiter
        Thread mainInterrupter = new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait 2 seconds
                System.out.println("\nInterrupter: Interrupting the waiter thread...\n");
                waiter.interrupt();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Interrupter");

        // Start all threads
        longRunningWorker.start();
        waiter.start();
        mainInterrupter.start();

        // Wait for demonstration to complete
        try {
            waiter.join();
            mainInterrupter.join();

            // Clean up: interrupt the worker so it doesn't run for full 10 seconds
            longRunningWorker.interrupt();
            longRunningWorker.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n=== Demo Complete ===");
        System.out.println("Key insight: join() can be interrupted just like sleep()");
    }
}
