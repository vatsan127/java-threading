package com.github.java_threading.locks.read_write_lock.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-Safe Cache using ReadWriteLock
 *
 * - Multiple readers can read simultaneously (shared read lock)
 * - Writers get exclusive access (write lock blocks all readers and writers)
 * - Best for read-heavy workloads where reads vastly outnumber writes
 */
public class ThreadSafeCache<K, V> {

    private final Map<K, V> cache = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Get value from cache (uses read lock - multiple threads can read concurrently)
     */
    public V get(K key) {
        rwLock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Put value into cache (uses write lock - exclusive access)
     */
    public void put(K key, V value) {
        rwLock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Remove value from cache (uses write lock - exclusive access)
     */
    public V remove(K key) {
        rwLock.writeLock().lock();
        try {
            return cache.remove(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Get cache size (uses read lock)
     */
    public int size() {
        rwLock.readLock().lock();
        try {
            return cache.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Get or compute if absent - demonstrates lock downgrading
     *
     * Pattern: Check with read lock, upgrade to write if needed
     * Note: We can't directly upgrade read->write (causes deadlock),
     * so we release read, acquire write, then check again.
     */
    public V getOrCompute(K key, java.util.function.Function<K, V> computeFunction) {
        // First try with read lock
        rwLock.readLock().lock();
        try {
            V value = cache.get(key);
            if (value != null) {
                return value;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // Need to write - acquire write lock
        rwLock.writeLock().lock();
        try {
            // Double-check (another thread might have added it)
            V value = cache.get(key);
            if (value != null) {
                return value;
            }

            // Compute and store
            value = computeFunction.apply(key);
            cache.put(key, value);
            return value;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
