package com.github.java_threading.interrupted_exception;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Demonstrates different patterns for handling InterruptedException.
 *
 * Three main patterns:
 * 1. Propagate - throw the exception to caller
 * 2. Restore and continue - restore flag, handle gracefully
 * 3. Restore and throw RuntimeException - for Runnable/Callable
 */
public class InterruptHandlingPatterns {

    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== InterruptedException Handling Patterns ===\n");

        // Pattern 1: Propagate
        System.out.println("Pattern 1: Propagate Exception");
        System.out.println("-".repeat(40));
        demonstratePropagatePattern();

        Thread.sleep(500);
        System.out.println("\n" + "=".repeat(50) + "\n");

        // Pattern 2: Restore and handle
        System.out.println("Pattern 2: Restore and Handle");
        System.out.println("-".repeat(40));
        demonstrateRestorePattern();

        Thread.sleep(500);
        System.out.println("\n" + "=".repeat(50) + "\n");

        // Pattern 3: Anti-pattern demonstration
        System.out.println("Pattern 3: Anti-Pattern (What NOT to do)");
        System.out.println("-".repeat(40));
        demonstrateAntiPattern();

        System.out.println("\n=== Demo Complete ===");
    }

    // ============================================
    // PATTERN 1: Propagate the Exception
    // ============================================
    // Use this when your method is part of a library or when callers
    // should decide how to handle the interruption

    /**
     * Method that propagates InterruptedException to caller.
     * This is the cleanest approach for library code.
     */
    static String fetchFromQueue() throws InterruptedException {
        // Let the exception propagate - caller handles it
        return queue.take();
    }

    private static void demonstratePropagatePattern() throws InterruptedException {
        Thread fetcher = new Thread(() -> {
            try {
                System.out.println("Fetcher: Waiting for item (will propagate if interrupted)...");
                String item = fetchFromQueue(); // This propagates InterruptedException
                System.out.println("Fetcher: Got item: " + item);
            } catch (InterruptedException e) {
                // Top-level handler in the thread
                Thread.currentThread().interrupt();
                System.out.println("Fetcher: Propagated exception reached top level");
            }
        }, "Fetcher");

        fetcher.start();
        Thread.sleep(500);

        System.out.println("Main: Interrupting fetcher...");
        fetcher.interrupt();
        fetcher.join();
    }

    // ============================================
    // PATTERN 2: Restore Flag and Handle Gracefully
    // ============================================
    // Use this when you must catch the exception but want to preserve
    // the interrupt status for the calling code

    static class GracefulConsumer implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            System.out.println("Consumer: Starting...");

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Blocking operation
                    String item = queue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                    if (item != null) {
                        System.out.println("Consumer: Processed - " + item);
                    }
                } catch (InterruptedException e) {
                    // RESTORE the interrupt flag
                    Thread.currentThread().interrupt();
                    System.out.println("Consumer: Interrupted, shutting down gracefully");
                    // Exit the loop
                    break;
                }
            }

            System.out.println("Consumer: Cleanup complete");
        }

        public void stop() {
            running = false;
        }
    }

    private static void demonstrateRestorePattern() throws InterruptedException {
        // Add some items to process
        queue.put("Item-1");
        queue.put("Item-2");

        GracefulConsumer consumer = new GracefulConsumer();
        Thread consumerThread = new Thread(consumer, "Consumer");

        consumerThread.start();
        Thread.sleep(3000);

        System.out.println("Main: Interrupting consumer...");
        consumerThread.interrupt();
        consumerThread.join();

        System.out.println("Main: Consumer finished");
    }

    // ============================================
    // ANTI-PATTERN: What NOT to do
    // ============================================

    static class BadConsumer implements Runnable {
        @Override
        public void run() {
            System.out.println("BadConsumer: Starting (with BAD interrupt handling)...");

            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                    System.out.println("BadConsumer: Iteration " + (i + 1));
                } catch (InterruptedException e) {
                    // BAD: Swallowing the exception!
                    // The interrupt is lost - callers will never know this thread was interrupted
                    System.out.println("BadConsumer: [BAD] Swallowed InterruptedException!");
                    // Continues running as if nothing happened...
                }
            }

            System.out.println("BadConsumer: Finished all iterations (interrupt was ignored!)");
        }
    }

    private static void demonstrateAntiPattern() throws InterruptedException {
        Thread badThread = new Thread(new BadConsumer(), "BadConsumer");

        badThread.start();
        Thread.sleep(1500);

        System.out.println("Main: Interrupting bad consumer (it will ignore it!)...");
        badThread.interrupt();

        badThread.join();
        System.out.println("Main: Bad consumer finished - notice it didn't stop when interrupted!");
        System.out.println("Main: This is why you should NEVER swallow InterruptedException!");
    }
}
