package com.github.java_threading.locks.read_write_lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
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

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Map<String, String> cache = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        ReadWriteLockMain example = new ReadWriteLockMain();

        // Example 1: Basic read-write separation
        System.out.println("=== Example 1: Basic Read-Write Separation ===");
        example.basicReadWriteExample();

        // Example 2: Thread-safe cache implementation
        System.out.println("\n=== Example 2: Thread-Safe Cache ===");
        example.cacheExample();

        // Example 3: Multiple concurrent readers
        System.out.println("\n=== Example 3: Multiple Concurrent Readers ===");
        example.multipleConcurrentReadersExample();

        // Example 4: Lock downgrading (write -> read)
        System.out.println("\n=== Example 4: Lock Downgrading ===");
        example.lockDowngradingExample();

        // Example 5: Fair vs Unfair lock
        System.out.println("\n=== Example 5: Fair Lock ===");
        example.fairLockExample();
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
     * Example 2: Thread-safe cache using ReadWriteLock
     * Classic use case: many reads, few writes
     */
    public void cacheExample() throws InterruptedException {
        cache.clear();

        // Writer thread - populates the cache
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                put("key" + i, "value" + i);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "cache-writer");

        // Multiple reader threads - read from cache
        Thread reader1 = new Thread(() -> readCache("reader-1"), "reader-1");
        Thread reader2 = new Thread(() -> readCache("reader-2"), "reader-2");

        writer.start();
        Thread.sleep(25); // Let some writes happen
        reader1.start();
        reader2.start();

        writer.join();
        reader1.join();
        reader2.join();
    }

    private void put(String key, String value) {
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + ": Writing " + key + "=" + value);
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private String get(String key) {
        rwLock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void readCache(String readerName) {
        for (int i = 1; i <= 5; i++) {
            String value = get("key1");
            System.out.println(readerName + ": Read key1 = " + value);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Example 3: Multiple concurrent readers
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
     * Example 4: Lock downgrading (write lock -> read lock)
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

    /**
     * Example 5: Fair lock demonstration
     * Fair mode ensures FIFO ordering of lock acquisition
     */
    public void fairLockExample() {
        ReentrantReadWriteLock unfairLock = new ReentrantReadWriteLock(false); // default
        ReentrantReadWriteLock fairLock = new ReentrantReadWriteLock(true);

        System.out.println("Unfair lock isFair(): " + unfairLock.isFair());
        System.out.println("Fair lock isFair(): " + fairLock.isFair());

        System.out.println("\nFair mode:");
        System.out.println("- Threads acquire locks in FIFO order");
        System.out.println("- Prevents thread starvation");
        System.out.println("- Lower throughput due to context switching");

        System.out.println("\nUnfair mode (default):");
        System.out.println("- Better throughput");
        System.out.println("- May cause thread starvation");
        System.out.println("- Allows barging (thread can acquire lock before waiting threads)");
    }
}
