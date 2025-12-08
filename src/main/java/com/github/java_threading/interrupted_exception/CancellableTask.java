package com.github.java_threading.interrupted_exception;

/**
 * Demonstrates a properly cancellable task using interrupt mechanism.
 *
 * This is the recommended pattern for long-running tasks that need
 * to be cancellable:
 * 1. Check isInterrupted() in loops
 * 2. Restore interrupt flag when catching InterruptedException
 * 3. Clean up resources before exiting
 */
public class CancellableTask implements Runnable {

    private final String taskName;
    private final int totalItems;

    public CancellableTask(String taskName, int totalItems) {
        this.taskName = taskName;
        this.totalItems = totalItems;
    }

    @Override
    public void run() {
        System.out.println(taskName + ": Starting to process " + totalItems + " items");

        int processedCount = 0;

        // Pattern: Check interrupt flag in loop condition
        while (!Thread.currentThread().isInterrupted() && processedCount < totalItems) {
            try {
                processItem(processedCount);
                processedCount++;

                // Simulate work with sleep (this is where InterruptedException can be thrown)
                Thread.sleep(500);

            } catch (InterruptedException e) {
                // CRITICAL: Restore the interrupt flag
                Thread.currentThread().interrupt();

                System.out.println(taskName + ": Received interrupt signal");
                System.out.println(taskName + ": Processed " + processedCount + "/" + totalItems +
                        " items before cancellation");

                // Break out of loop (the while condition will also be false now)
                break;
            }
        }

        // Cleanup - always runs whether interrupted or completed normally
        cleanup(processedCount);

        if (Thread.currentThread().isInterrupted()) {
            System.out.println(taskName + ": Task was cancelled");
        } else {
            System.out.println(taskName + ": Task completed successfully");
        }
    }

    private void processItem(int itemNumber) {
        System.out.println(taskName + ": Processing item " + (itemNumber + 1));
    }

    private void cleanup(int processedCount) {
        System.out.println(taskName + ": Cleanup - saving progress (" + processedCount + " items processed)");
    }

    // Main method to demonstrate the cancellable task
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Cancellable Task Demo ===\n");

        CancellableTask task = new CancellableTask("DataProcessor", 20);
        Thread workerThread = new Thread(task, "Worker");

        workerThread.start();

        // Let it process some items
        Thread.sleep(2500);

        // Cancel the task by interrupting
        System.out.println("\nMain: Cancelling task...\n");
        workerThread.interrupt();

        // Wait for thread to finish cleanup
        workerThread.join();

        System.out.println("\nMain: Worker thread has finished");
    }
}
