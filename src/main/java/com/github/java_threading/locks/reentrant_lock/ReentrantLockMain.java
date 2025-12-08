package com.github.java_threading.locks.reentrant_lock;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockMain {

    private final ReentrantLock lock = new ReentrantLock();
    private int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockMain example = new ReentrantLockMain();

        // Example 1: Basic lock usage
        System.out.println("=== Example 1: Basic Lock Usage ===");
        example.basicLockExample();

        // Example 2: Try lock (non-blocking)
        System.out.println("\n=== Example 2: Try Lock (Non-Blocking) ===");
        example.tryLockExample();

        // Example 3: Reentrant behavior
        System.out.println("\n=== Example 3: Reentrant Behavior ===");
        example.reentrantExample();

        // Example 4: Lock with timeout
        System.out.println("\n=== Example 4: Lock with Timeout ===");
        example.tryLockWithTimeoutExample();

        // Example 5: Fair lock
        System.out.println("\n=== Example 5: Fair Lock ===");
        example.fairLockExample();

        // Example 6: Lock information methods
        System.out.println("\n=== Example 6: Lock Information ===");
        example.lockInfoExample();
    }

    /**
     * Example 1: Basic lock/unlock pattern
     * Always use try-finally to ensure unlock happens
     */
    public void basicLockExample() throws InterruptedException {
        counter = 0;

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                lock.lock();  // Acquire lock
                try {
                    counter++;  // Critical section
                } finally {
                    lock.unlock();  // Always unlock in finally block
                }
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Counter value: " + counter + " (Expected: 2000)");
    }

    /**
     * Example 2: tryLock() - Non-blocking lock acquisition
     * Returns immediately if lock is not available
     */
    public void tryLockExample() throws InterruptedException {
        ReentrantLock tryLock = new ReentrantLock();

        Runnable task = () -> {
            if (tryLock.tryLock()) {  // Try to acquire without blocking
                try {
                    System.out.println(Thread.currentThread().getName() + " acquired the lock");
                    Thread.sleep(100);  // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    tryLock.unlock();
                }
            } else {
                System.out.println(Thread.currentThread().getName() + " could NOT acquire the lock, doing other work...");
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        Thread.sleep(10);  // Small delay to ensure t1 gets lock first
        t2.start();

        t1.join();
        t2.join();
    }

    /**
     * Example 3: Reentrant behavior - same thread can acquire lock multiple times
     */
    public void reentrantExample() {
        ReentrantLock reentrantLock = new ReentrantLock();

        reentrantLock.lock();
        System.out.println("First lock acquired. Hold count: " + reentrantLock.getHoldCount());

        reentrantLock.lock();  // Same thread can lock again
        System.out.println("Second lock acquired. Hold count: " + reentrantLock.getHoldCount());

        reentrantLock.lock();  // And again
        System.out.println("Third lock acquired. Hold count: " + reentrantLock.getHoldCount());

        // Must unlock same number of times
        reentrantLock.unlock();
        System.out.println("First unlock. Hold count: " + reentrantLock.getHoldCount());

        reentrantLock.unlock();
        System.out.println("Second unlock. Hold count: " + reentrantLock.getHoldCount());

        reentrantLock.unlock();
        System.out.println("Third unlock. Hold count: " + reentrantLock.getHoldCount());

        System.out.println("Lock is now free: " + !reentrantLock.isLocked());
    }

    /**
     * Example 4: tryLock with timeout
     * Waits for specified time before giving up
     */
    public void tryLockWithTimeoutExample() throws InterruptedException {
        ReentrantLock timeoutLock = new ReentrantLock();

        Thread holder = new Thread(() -> {
            timeoutLock.lock();
            try {
                System.out.println("Holder: acquired lock, holding for 2 seconds...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                timeoutLock.unlock();
                System.out.println("Holder: released lock");
            }
        }, "Holder");

        Thread waiter = new Thread(() -> {
            try {
                System.out.println("Waiter: trying to acquire lock with 500ms timeout...");
                boolean acquired = timeoutLock.tryLock(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (acquired) {
                    try {
                        System.out.println("Waiter: acquired lock!");
                    } finally {
                        timeoutLock.unlock();
                    }
                } else {
                    System.out.println("Waiter: timeout! Could not acquire lock in 500ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Waiter");

        holder.start();
        Thread.sleep(100);  // Ensure holder gets lock first
        waiter.start();

        holder.join();
        waiter.join();
    }

    /**
     * Example 5: Fair lock - threads acquire lock in FIFO order
     * Prevents thread starvation but has lower throughput
     */
    public void fairLockExample() throws InterruptedException {
        ReentrantLock fairLock = new ReentrantLock(true);  // true = fair lock
        ReentrantLock unfairLock = new ReentrantLock(false);  // false = unfair (default)

        System.out.println("Fair lock is fair: " + fairLock.isFair());
        System.out.println("Unfair lock is fair: " + unfairLock.isFair());

        // In a fair lock, if multiple threads are waiting,
        // the one that has been waiting longest gets the lock next
        System.out.println("\nFair locks guarantee FIFO ordering but have lower throughput");
        System.out.println("Unfair locks have better throughput but may cause starvation");
    }

    /**
     * Example 6: Lock information and diagnostic methods
     */
    public void lockInfoExample() throws InterruptedException {
        ReentrantLock infoLock = new ReentrantLock();

        Thread holder = new Thread(() -> {
            infoLock.lock();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                infoLock.unlock();
            }
        }, "Holder");

        Thread waiter = new Thread(() -> {
            infoLock.lock();
            try {
                System.out.println("Waiter finally got the lock!");
            } finally {
                infoLock.unlock();
            }
        }, "Waiter");

        holder.start();
        Thread.sleep(100);  // Let holder acquire lock
        waiter.start();
        Thread.sleep(100);  // Let waiter start waiting

        // Diagnostic information
        System.out.println("Is locked: " + infoLock.isLocked());
        System.out.println("Is held by current thread: " + infoLock.isHeldByCurrentThread());
        System.out.println("Has queued threads: " + infoLock.hasQueuedThreads());
        System.out.println("Queue length: " + infoLock.getQueueLength());

        holder.join();
        waiter.join();
    }
}
