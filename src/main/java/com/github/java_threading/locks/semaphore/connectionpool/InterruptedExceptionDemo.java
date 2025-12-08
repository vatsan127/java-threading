package com.github.java_threading.locks.semaphore.connectionpool;

import java.util.concurrent.Semaphore;

/**
 * Demonstrates InterruptedException handling with Semaphore
 *
 * Shows what happens when a thread waiting on acquire() gets interrupted.
 */
public class InterruptedExceptionDemo {

    private static final Semaphore semaphore = new Semaphore(1);  // Only 1 permit

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== InterruptedException Demo ===\n");

        // Thread 1: Acquires the only permit and holds it
        Thread holder = new Thread(() -> {
            try {
                semaphore.acquire();
                System.out.println("Holder: Got permit, holding for 5 seconds...");
                Thread.sleep(5000);
                semaphore.release();
                System.out.println("Holder: Released permit");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Holder: Interrupted!");
            }
        }, "Holder");

        // Thread 2: Will wait for permit and get interrupted
        Thread waiter = new Thread(() -> {
            System.out.println("Waiter: Trying to acquire permit...");
            try {
                semaphore.acquire();  // Will block here
                try {
                    System.out.println("Waiter: Got permit!");
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException e) {
                // Restore interrupt flag - this is the correct pattern!
                Thread.currentThread().interrupt();
                System.out.println("Waiter: Interrupted while waiting! (InterruptedException caught)");
                System.out.println("Waiter: Interrupt flag restored: " + Thread.currentThread().isInterrupted());
            }
        }, "Waiter");

        holder.start();
        Thread.sleep(100);  // Let holder acquire first

        waiter.start();
        Thread.sleep(500);  // Let waiter start waiting

        // Interrupt the waiting thread
        System.out.println("\nMain: Interrupting waiter...\n");
        waiter.interrupt();

        waiter.join();
        holder.interrupt();  // Also interrupt holder to speed up demo
        holder.join();

        System.out.println("\n=== Demo Complete ===");
        System.out.println("Available permits: " + semaphore.availablePermits());
    }
}
