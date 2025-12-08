package com.github.java_threading.interrupted_exception;

/**
 * Demonstrates InterruptedException with Object.wait()
 *
 * wait() causes a thread to wait until another thread calls notify()/notifyAll()
 * on the same object. If interrupted while waiting, it throws InterruptedException.
 *
 * Important: wait() must be called from a synchronized block and when
 * InterruptedException is thrown, the thread re-acquires the lock before
 * the exception is thrown.
 */
public class InterruptWithWait {

    private static final Object lock = new Object();
    private static boolean dataReady = false;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== InterruptedException with wait() Demo ===\n");

        // Consumer thread that waits for data
        Thread consumer = new Thread(() -> {
            System.out.println("Consumer: Waiting for data...");

            synchronized (lock) {
                while (!dataReady) {
                    try {
                        System.out.println("Consumer: Entering wait state (holding lock)");
                        lock.wait(); // Releases lock and waits
                        System.out.println("Consumer: Woke up from wait");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Consumer: Interrupted while waiting!");
                        System.out.println("Consumer: Note - lock was re-acquired before exception");
                        System.out.println("Consumer: isInterrupted = " +
                                Thread.currentThread().isInterrupted());
                        return; // Exit the method
                    }
                }
                System.out.println("Consumer: Data is ready, processing...");
            }
        }, "Consumer");

        // Thread to demonstrate interrupt
        Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("\nInterrupter: Interrupting consumer...\n");
                consumer.interrupt();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Interrupter");

        consumer.start();
        interrupter.start();

        consumer.join();
        interrupter.join();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Now demonstrating normal notify (no interrupt):\n");

        // Reset state
        dataReady = false;

        // New consumer
        Thread consumer2 = new Thread(() -> {
            synchronized (lock) {
                while (!dataReady) {
                    try {
                        System.out.println("Consumer2: Waiting for data...");
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println("Consumer2: Got data! Processing complete.");
            }
        }, "Consumer2");

        // Producer that provides data
        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(1000);
                synchronized (lock) {
                    System.out.println("Producer: Setting data ready and notifying...");
                    dataReady = true;
                    lock.notify();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        consumer2.start();
        producer.start();

        consumer2.join();
        producer.join();

        System.out.println("\n=== Demo Complete ===");
    }
}
