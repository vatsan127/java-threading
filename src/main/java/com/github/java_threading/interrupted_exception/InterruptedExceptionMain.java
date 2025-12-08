package com.github.java_threading.interrupted_exception;

/**
 * Main class demonstrating InterruptedException concepts.
 *
 * InterruptedException is a checked exception thrown when a thread
 * is interrupted while it's waiting, sleeping, or blocked.
 *
 * Key methods that throw InterruptedException:
 * - Thread.sleep()
 * - Object.wait()
 * - Thread.join()
 * - BlockingQueue.take() / put()
 * - Semaphore.acquire()
 * - Lock.lockInterruptibly()
 */
public class InterruptedExceptionMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== InterruptedException Demonstration ===\n");

        // Demo 1: Basic interruption of a sleeping thread
        basicInterruptDemo();

        Thread.sleep(500);
        System.out.println("\n" + "=".repeat(50) + "\n");

        // Demo 2: Interrupt flag behavior
        interruptFlagDemo();

        Thread.sleep(500);
        System.out.println("\n" + "=".repeat(50) + "\n");

        // Demo 3: Proper handling pattern
        properHandlingDemo();

        System.out.println("\n=== All Demos Complete ===");
    }

    /**
     * Demo 1: Basic interrupt - shows how a sleeping thread responds to interrupt
     */
    private static void basicInterruptDemo() throws InterruptedException {
        System.out.println("Demo 1: Basic Interrupt of Sleeping Thread");
        System.out.println("-".repeat(40));

        Thread sleeper = new Thread(() -> {
            System.out.println("Sleeper: Going to sleep for 10 seconds...");
            try {
                Thread.sleep(10000);
                System.out.println("Sleeper: Woke up normally (this won't print)");
            } catch (InterruptedException e) {
                System.out.println("Sleeper: InterruptedException caught!");
                System.out.println("Sleeper: Was interrupted while sleeping");
                // Note: When InterruptedException is thrown, the interrupt flag is cleared
                System.out.println("Sleeper: Interrupt flag after catch: " +
                        Thread.currentThread().isInterrupted()); // false
            }
        }, "Sleeper-Thread");

        sleeper.start();
        Thread.sleep(1000); // Let sleeper start sleeping

        System.out.println("Main: Interrupting the sleeper thread...");
        sleeper.interrupt();

        sleeper.join();
        System.out.println("Main: Sleeper thread finished");
    }

    /**
     * Demo 2: Shows the difference between isInterrupted() and interrupted()
     */
    private static void interruptFlagDemo() throws InterruptedException {
        System.out.println("Demo 2: Interrupt Flag Behavior");
        System.out.println("-".repeat(40));

        Thread worker = new Thread(() -> {
            // Simulate work without blocking calls
            System.out.println("Worker: Starting work (no blocking calls)...");

            int count = 0;
            // Check interrupt status in loop - this is how non-blocking code should handle interrupts
            while (!Thread.currentThread().isInterrupted() && count < 1000000000) {
                count++;
                if (count % 100000000 == 0) {
                    System.out.println("Worker: Progress - " + (count / 10000000) + "%");
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Worker: Detected interrupt flag!");
                System.out.println("Worker: isInterrupted() = " + Thread.currentThread().isInterrupted()); // true

                // Thread.interrupted() returns status AND clears the flag
                System.out.println("Worker: Thread.interrupted() = " + Thread.interrupted()); // true (clears flag)
                System.out.println("Worker: isInterrupted() after interrupted() = " +
                        Thread.currentThread().isInterrupted()); // false (was cleared)
            } else {
                System.out.println("Worker: Completed without interruption");
            }
        }, "Worker-Thread");

        worker.start();
        Thread.sleep(200); // Let worker start

        System.out.println("Main: Setting interrupt flag on worker...");
        worker.interrupt();

        worker.join();
        System.out.println("Main: Worker thread finished");
    }

    /**
     * Demo 3: Shows proper handling - restore interrupt flag after catching
     */
    private static void properHandlingDemo() throws InterruptedException {
        System.out.println("Demo 3: Proper InterruptedException Handling");
        System.out.println("-".repeat(40));

        Thread properWorker = new Thread(() -> {
            System.out.println("ProperWorker: Starting with proper interrupt handling...");

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Do some work
                    System.out.println("ProperWorker: Working... (will sleep)");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("ProperWorker: Caught InterruptedException");

                    // IMPORTANT: Restore the interrupt flag!
                    // This allows code higher in the call stack to detect the interrupt
                    Thread.currentThread().interrupt();
                    System.out.println("ProperWorker: Restored interrupt flag");
                    System.out.println("ProperWorker: isInterrupted() = " +
                            Thread.currentThread().isInterrupted()); // true

                    // Now the while loop condition will be false, and we exit gracefully
                    System.out.println("ProperWorker: Exiting gracefully...");
                }
            }

            // Cleanup code here
            System.out.println("ProperWorker: Cleanup complete, thread ending");
        }, "ProperWorker-Thread");

        properWorker.start();
        Thread.sleep(500);

        System.out.println("Main: Interrupting proper worker...");
        properWorker.interrupt();

        properWorker.join();
        System.out.println("Main: Proper worker thread finished");
    }
}
