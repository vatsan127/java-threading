package com.github.java_threading.locks.semaphore;

import java.util.concurrent.Semaphore;

/**
 * Semaphore Examples
 *
 * A Semaphore controls access to a shared resource through permits.
 * - acquire() - takes a permit (blocks if none available)
 * - release() - returns a permit
 *
 * Unlike locks (which are binary - locked/unlocked), semaphores can have multiple permits.
 *
 * Use cases:
 * 1. Limiting concurrent access (connection pools, rate limiting)
 * 2. Resource pooling
 * 3. Producer-consumer with bounded buffer
 * 4. Binary semaphore as mutex (1 permit)
 */
public class SemaphoreMain {

    public static void main(String[] args) throws InterruptedException {
        // Example 1: Basic semaphore - limiting concurrent access
        System.out.println("=== Example 1: Limiting Concurrent Access ===");
        basicSemaphoreExample();

        // Example 2: Connection pool simulation
        System.out.println("\n=== Example 2: Connection Pool ===");
        connectionPoolExample();

        // Example 3: Binary semaphore as mutex
        System.out.println("\n=== Example 3: Binary Semaphore (Mutex) ===");
        binarySemaphoreExample();

        // Example 4: tryAcquire (non-blocking)
        System.out.println("\n=== Example 4: TryAcquire (Non-blocking) ===");
        tryAcquireExample();

        // Example 5: Fair semaphore
        System.out.println("\n=== Example 5: Fair Semaphore ===");
        fairSemaphoreExample();

        // Example 6: Acquiring multiple permits
        System.out.println("\n=== Example 6: Multiple Permits ===");
        multiplePermitsExample();
    }

    /**
     * Example 1: Basic semaphore - limiting concurrent access
     * Only 3 threads can access the resource at a time
     */
    public static void basicSemaphoreExample() throws InterruptedException {
        // Semaphore with 3 permits
        Semaphore semaphore = new Semaphore(3);

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            try {
                System.out.println(threadName + ": Waiting for permit...");
                semaphore.acquire();  // Blocks if no permits available
                System.out.println(threadName + ": Acquired permit! Available: " + semaphore.availablePermits());

                // Simulate work
                Thread.sleep(200);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                System.out.println(threadName + ": Released permit. Available: " + semaphore.availablePermits());
            }
        };

        // Start 6 threads, but only 3 can run concurrently
        Thread[] threads = new Thread[6];
        for (int i = 0; i < 6; i++) {
            threads[i] = new Thread(task, "Thread-" + i);
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    /**
     * Example 2: Connection pool simulation
     * Classic use case for semaphores
     *
     * See ConnectionPoolMain.java for full example
     */
    public static void connectionPoolExample() {
        System.out.println("Connection pool is a classic semaphore use case.");
        System.out.println("See: ConnectionPoolMain.java for full example");
        System.out.println("\nPattern:");
        System.out.println("- Semaphore permits = pool size");
        System.out.println("- acquire() blocks until connection available");
        System.out.println("- release() returns connection to pool");
    }

    /**
     * Example 3: Binary semaphore as mutex
     * Semaphore with 1 permit acts like a lock (but NOT reentrant!)
     */
    public static void binarySemaphoreExample() throws InterruptedException {
        Semaphore mutex = new Semaphore(1);  // Binary semaphore
        int[] counter = {0};

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    mutex.acquire();
                    counter[0]++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    mutex.release();
                }
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Counter value: " + counter[0] + " (Expected: 2000)");
        System.out.println("Note: Binary semaphore is NOT reentrant (same thread can't acquire twice)");
    }

    /**
     * Example 4: tryAcquire - non-blocking acquisition
     */
    public static void tryAcquireExample() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);

        Thread holder = new Thread(() -> {
            try {
                semaphore.acquire();
                System.out.println("Holder: acquired permit, sleeping...");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                System.out.println("Holder: released permit");
            }
        }, "Holder");

        holder.start();
        Thread.sleep(100);  // Let holder acquire

        // Try without blocking
        System.out.println("Main: trying tryAcquire()...");
        boolean acquired = semaphore.tryAcquire();
        System.out.println("Main: tryAcquire() returned " + acquired);

        // Try with timeout
        System.out.println("Main: trying tryAcquire with 200ms timeout...");
        acquired = semaphore.tryAcquire(200, java.util.concurrent.TimeUnit.MILLISECONDS);
        System.out.println("Main: tryAcquire(timeout) returned " + acquired);

        holder.join();

        // Now should succeed
        System.out.println("Main: trying tryAcquire() after holder released...");
        acquired = semaphore.tryAcquire();
        System.out.println("Main: tryAcquire() returned " + acquired);
        if (acquired) {
            semaphore.release();
        }
    }

    /**
     * Example 5: Fair vs Unfair semaphore
     */
    public static void fairSemaphoreExample() {
        Semaphore unfairSemaphore = new Semaphore(3);          // Unfair (default)
        Semaphore fairSemaphore = new Semaphore(3, true);  // Fair

        System.out.println("Unfair semaphore isFair(): " + unfairSemaphore.isFair());
        System.out.println("Fair semaphore isFair(): " + fairSemaphore.isFair());

        System.out.println("\nFair mode:");
        System.out.println("- Threads acquire permits in FIFO order");
        System.out.println("- Prevents starvation");
        System.out.println("- Lower throughput");

        System.out.println("\nUnfair mode (default):");
        System.out.println("- Better throughput");
        System.out.println("- Permits may be given to waiting or arriving threads");
        System.out.println("- May cause starvation");
    }

    /**
     * Example 6: Acquiring multiple permits at once
     */
    public static void multiplePermitsExample() throws InterruptedException {
        Semaphore semaphore = new Semaphore(10);  // 10 permits

        System.out.println("Starting permits: " + semaphore.availablePermits());

        // Acquire 3 permits at once
        semaphore.acquire(3);
        System.out.println("After acquire(3): " + semaphore.availablePermits());

        // Acquire 5 more
        semaphore.acquire(5);
        System.out.println("After acquire(5): " + semaphore.availablePermits());

        // Release all at once
        semaphore.release(8);
        System.out.println("After release(8): " + semaphore.availablePermits());

        // You can even release MORE than you acquired (increases total permits!)
        System.out.println("\nNote: release() can increase permits beyond initial count!");
        semaphore.release(5);
        System.out.println("After release(5) more: " + semaphore.availablePermits());

        // Drain all permits
        int drained = semaphore.drainPermits();
        System.out.println("Drained " + drained + " permits. Available: " + semaphore.availablePermits());
    }

}
