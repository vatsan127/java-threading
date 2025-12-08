package com.github.java_threading.locks.stamped_lock;

import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock Examples (Java 8+)
 *
 * StampedLock is an advanced lock with three modes:
 * 1. Write Lock - Exclusive, like ReentrantLock
 * 2. Read Lock - Shared, like ReadWriteLock's read lock
 * 3. Optimistic Read - Non-blocking, validates data wasn't modified
 *
 * Key differences from ReadWriteLock:
 * - NOT reentrant (same thread cannot acquire twice)
 * - Supports optimistic reading (huge performance benefit)
 * - Uses stamps (long values) to track lock state
 * - Better performance in read-heavy scenarios
 */
public class StampedLockMain {

    private final StampedLock stampedLock = new StampedLock();
    private double x = 0, y = 0;  // Coordinates - our shared state

    public static void main(String[] args) throws InterruptedException {
        StampedLockMain example = new StampedLockMain();

        // Example 1: Basic write and read locks
        System.out.println("=== Example 1: Basic Write and Read Locks ===");
        example.basicWriteReadExample();

        // Example 2: Optimistic reading (the killer feature)
        System.out.println("\n=== Example 2: Optimistic Reading ===");
        example.optimisticReadExample();

        // Example 3: Optimistic read with fallback to read lock
        System.out.println("\n=== Example 3: Optimistic Read with Fallback ===");
        example.optimisticReadWithFallbackExample();

        // Example 4: Lock conversion (upgrade/downgrade)
        System.out.println("\n=== Example 4: Lock Conversion ===");
        example.lockConversionExample();

        // Example 5: tryLock variants
        System.out.println("\n=== Example 5: TryLock Variants ===");
        example.tryLockVariantsExample();
    }

    /**
     * Example 1: Basic write and read locks
     * Similar to ReadWriteLock but uses stamps
     */
    public void basicWriteReadExample() throws InterruptedException {
        StampedLock lock = new StampedLock();
        int[] data = {0};

        // Writer thread
        Thread writer = new Thread(() -> {
            long stamp = lock.writeLock();  // Returns a stamp
            try {
                System.out.println(Thread.currentThread().getName() + ": Acquired write lock (stamp=" + stamp + ")");
                data[0] = 42;
                System.out.println(Thread.currentThread().getName() + ": Set data to " + data[0]);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlockWrite(stamp);  // Must pass the stamp to unlock
                System.out.println(Thread.currentThread().getName() + ": Released write lock");
            }
        }, "writer-thread");

        // Reader thread
        Thread reader = new Thread(() -> {
            long stamp = lock.readLock();  // Returns a stamp
            try {
                System.out.println(Thread.currentThread().getName() + ": Acquired read lock (stamp=" + stamp + ")");
                System.out.println(Thread.currentThread().getName() + ": Read data = " + data[0]);
            } finally {
                lock.unlockRead(stamp);  // Must pass the stamp to unlock
                System.out.println(Thread.currentThread().getName() + ": Released read lock");
            }
        }, "reader-thread");

        writer.start();
        Thread.sleep(10);
        reader.start();

        writer.join();
        reader.join();
    }

    /**
     * Example 2: Optimistic reading - THE killer feature of StampedLock
     *
     * Optimistic read doesn't actually acquire a lock!
     * It just gets a stamp and later validates if data changed.
     * If no writes happened, the read is valid - no blocking at all!
     */
    public void optimisticReadExample() throws InterruptedException {
        x = 10;
        y = 20;

        // Optimistic reader
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                long stamp = stampedLock.tryOptimisticRead();  // Non-blocking!
                System.out.println(Thread.currentThread().getName() + ": Got optimistic stamp = " + stamp);

                // Read the data (without holding any lock!)
                double currentX = x;
                double currentY = y;

                // Validate - check if a write happened since we got the stamp
                if (stampedLock.validate(stamp)) {
                    System.out.println(Thread.currentThread().getName() + ": Optimistic read SUCCESS - x=" + currentX + ", y=" + currentY);
                } else {
                    System.out.println(Thread.currentThread().getName() + ": Optimistic read FAILED - data was modified!");
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "optimistic-reader");

        // Writer that occasionally modifies data
        Thread writer = new Thread(() -> {
            try {
                Thread.sleep(75);  // Wait a bit
                long stamp = stampedLock.writeLock();
                try {
                    System.out.println(Thread.currentThread().getName() + ": Writing new values");
                    x = 100;
                    y = 200;
                } finally {
                    stampedLock.unlockWrite(stamp);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "writer-thread");

        reader.start();
        writer.start();

        reader.join();
        writer.join();
    }

    /**
     * Example 3: Optimistic read with fallback to pessimistic read lock
     * This is the recommended pattern for production use
     */
    public void optimisticReadWithFallbackExample() {
        x = 50;
        y = 100;

        // This is the canonical pattern for using optimistic reads
        double[] result = readCoordinatesWithFallback();
        System.out.println("Read coordinates: x=" + result[0] + ", y=" + result[1]);
    }

    private double[] readCoordinatesWithFallback() {
        // Step 1: Try optimistic read first (non-blocking)
        long stamp = stampedLock.tryOptimisticRead();
        double currentX = x;
        double currentY = y;

        // Step 2: Validate the optimistic read
        if (!stampedLock.validate(stamp)) {
            // Optimistic read failed - someone wrote while we were reading
            // Fallback to pessimistic read lock
            System.out.println("Optimistic read failed, falling back to read lock");
            stamp = stampedLock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        } else {
            System.out.println("Optimistic read succeeded - no lock needed!");
        }

        return new double[]{currentX, currentY};
    }

    /**
     * Example 4: Lock conversion (upgrading and downgrading)
     * StampedLock supports converting between lock modes
     */
    public void lockConversionExample() {
        StampedLock lock = new StampedLock();
        int[] counter = {0};

        // Pattern: Read, decide if update needed, upgrade to write if necessary
        long stamp = lock.readLock();
        System.out.println("Acquired read lock (stamp=" + stamp + ")");
        try {
            int currentValue = counter[0];
            System.out.println("Current value: " + currentValue);

            // Decide we need to update
            if (currentValue == 0) {
                // Try to upgrade to write lock
                long writeStamp = lock.tryConvertToWriteLock(stamp);
                if (writeStamp != 0L) {
                    // Upgrade successful!
                    stamp = writeStamp;
                    System.out.println("Upgraded to write lock (stamp=" + stamp + ")");
                    counter[0] = 42;
                    System.out.println("Updated value to: " + counter[0]);
                } else {
                    // Upgrade failed - must release read and acquire write
                    System.out.println("Upgrade failed, releasing read and acquiring write");
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                    counter[0] = 42;
                    System.out.println("Updated value to: " + counter[0]);
                }
            }
        } finally {
            lock.unlock(stamp);  // Generic unlock works with any stamp
            System.out.println("Released lock");
        }

        // Demonstrate downgrading (write -> read)
        System.out.println("\nDemonstrating lock downgrade:");
        stamp = lock.writeLock();
        System.out.println("Acquired write lock");
        try {
            counter[0] = 100;
            System.out.println("Updated value to: " + counter[0]);

            // Downgrade to read lock
            stamp = lock.tryConvertToReadLock(stamp);
            if (stamp != 0L) {
                System.out.println("Downgraded to read lock");
                System.out.println("Reading value: " + counter[0]);
            }
        } finally {
            lock.unlock(stamp);
            System.out.println("Released lock");
        }
    }

    /**
     * Example 5: TryLock variants
     * Non-blocking and timed lock acquisition
     */
    public void tryLockVariantsExample() throws InterruptedException {
        StampedLock lock = new StampedLock();

        // tryWriteLock - non-blocking
        long stamp = lock.tryWriteLock();
        if (stamp != 0L) {
            System.out.println("tryWriteLock succeeded (stamp=" + stamp + ")");
            lock.unlockWrite(stamp);
        } else {
            System.out.println("tryWriteLock failed");
        }

        // tryReadLock - non-blocking
        stamp = lock.tryReadLock();
        if (stamp != 0L) {
            System.out.println("tryReadLock succeeded (stamp=" + stamp + ")");
            lock.unlockRead(stamp);
        } else {
            System.out.println("tryReadLock failed");
        }

        // tryWriteLock with timeout
        System.out.println("\nTrying lock with timeout while another thread holds it:");

        Thread holder = new Thread(() -> {
            long s = lock.writeLock();
            try {
                System.out.println("Holder: acquired write lock, sleeping...");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlockWrite(s);
                System.out.println("Holder: released write lock");
            }
        }, "holder");

        holder.start();
        Thread.sleep(50);  // Let holder acquire lock

        try {
            System.out.println("Main: trying to acquire write lock with 100ms timeout...");
            stamp = lock.tryWriteLock(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (stamp != 0L) {
                System.out.println("Main: got the lock!");
                lock.unlockWrite(stamp);
            } else {
                System.out.println("Main: timeout! Could not acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        holder.join();

        // Important: StampedLock is NOT reentrant
        System.out.println("\nIMPORTANT: StampedLock is NOT reentrant!");
        System.out.println("The same thread CANNOT acquire the lock twice.");
        System.out.println("Attempting to do so will cause deadlock!");
    }
}
