package com.github.java_threading.locks.read_write_lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLock / ReentrantReadWriteLock Examples
 *
 * ReadWriteLock maintains a pair of locks:
 * - Read lock: Can be held by multiple threads simultaneously (shared lock)
 * - Write lock: Exclusive access - only one thread can hold it
 *
 * Best for: Read-heavy workloads where reads vastly outnumber writes
 */
public class ReadWriteLockMain {

    public static void main(String[] args) throws InterruptedException {
        ReadWriteLockMain example = new ReadWriteLockMain();

        // Example 1: Basic read-write separation
        System.out.println("=== Example 1: Basic Read-Write Separation ===");
        example.basicReadWriteExample();

        // Example 2: Multiple concurrent readers
        System.out.println("\n=== Example 2: Multiple Concurrent Readers ===");
        example.multipleConcurrentReadersExample();

        // Example 3: Lock downgrading (write -> read)
        System.out.println("\n=== Example 3: Lock Downgrading ===");
        example.lockDowngradingExample();
    }

    /**
     * Example 1: Basic read-write separation
     * Demonstrates how read and write locks work
     */
    public void basicReadWriteExample() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        int[] sharedData = {0};

        // Writer thread - needs exclusive access
        Thread writer = new Thread(() -> {
            lock.writeLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + ": Acquired WRITE lock");
                sharedData[0] = 42;
                System.out.println(Thread.currentThread().getName() + ": Updated value to " + sharedData[0]);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
                System.out.println(Thread.currentThread().getName() + ": Released WRITE lock");
            }
        }, "writer-thread");

        // Reader thread - can share with other readers
        Thread reader = new Thread(() -> {
            lock.readLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + ": Acquired READ lock");
                System.out.println(Thread.currentThread().getName() + ": Read value = " + sharedData[0]);
            } finally {
                lock.readLock().unlock();
                System.out.println(Thread.currentThread().getName() + ": Released READ lock");
            }
        }, "reader-thread");

        writer.start();
        Thread.sleep(10); // Let writer start first
        reader.start();

        writer.join();
        reader.join();
    }

    /**
     * Example 2: Multiple concurrent readers
     * Demonstrates that multiple readers can hold the lock simultaneously
     */
    public void multipleConcurrentReadersExample() throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        String[] sharedData = {"Hello, Concurrent World!"};

        Runnable readerTask = () -> {
            lock.readLock().lock();
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": Acquired read lock");
                System.out.println(threadName + ": Reading data = " + sharedData[0]);
                System.out.println(threadName + ": Read lock count = " + lock.getReadLockCount());
                Thread.sleep(200); // Hold the lock for a while
                System.out.println(threadName + ": Done reading");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.readLock().unlock();
                System.out.println(Thread.currentThread().getName() + ": Released read lock");
            }
        };

        // Start 3 readers simultaneously
        Thread r1 = new Thread(readerTask, "reader-1");
        Thread r2 = new Thread(readerTask, "reader-2");
        Thread r3 = new Thread(readerTask, "reader-3");

        r1.start();
        r2.start();
        r3.start();

        r1.join();
        r2.join();
        r3.join();

        System.out.println("All readers completed - they ran concurrently!");
    }

    /**
     * Example 3: Lock downgrading (write lock -> read lock)
     * You CAN downgrade: acquire write lock, then read lock, then release write lock
     * You CANNOT upgrade: acquiring read lock first, then trying to get write lock causes deadlock
     */
    public void lockDowngradingExample() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        int[] data = {0};

        // Lock downgrading pattern
        lock.writeLock().lock();  // 1. Acquire write lock
        try {
            data[0] = 100;
            System.out.println("Write lock acquired, updated data to: " + data[0]);

            lock.readLock().lock();  // 2. Acquire read lock (while holding write)
            System.out.println("Read lock also acquired (downgrading)");
        } finally {
            lock.writeLock().unlock();  // 3. Release write lock (still holding read)
            System.out.println("Write lock released, but still holding read lock");
        }

        // Now we only hold the read lock
        try {
            System.out.println("Reading data with read lock: " + data[0]);
            System.out.println("Is write locked: " + lock.isWriteLocked());
            System.out.println("Read lock count: " + lock.getReadLockCount());
        } finally {
            lock.readLock().unlock();  // 4. Release read lock
            System.out.println("Read lock released");
        }
    }

}
