package com.github.java_threading.locks.read_write_lock.cache;

/**
 * ReadWriteLock Cache Demo
 *
 * Demonstrates a thread-safe cache where:
 * - Multiple readers can read concurrently (shared read lock)
 * - Writers get exclusive access (write lock blocks everyone)
 *
 * This is ideal for read-heavy workloads.
 */
public class CacheMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ReadWriteLock Cache Demo ===\n");

        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();

        // Pre-populate some data
        cache.put("key1", "initial-value-1");
        cache.put("key2", "initial-value-2");
        System.out.println("Cache initialized with key1 and key2\n");

        // Create writer - will update key1 multiple times
        Thread writer = new Thread(
                new CacheWriter(cache, "Writer", "key", 3, 100),
                "Writer"
        );

        // Create multiple readers - will read key1 concurrently
        Thread reader1 = new Thread(
                new CacheReader(cache, "Reader-1", "key1", 5, 50),
                "Reader-1"
        );
        Thread reader2 = new Thread(
                new CacheReader(cache, "Reader-2", "key1", 5, 50),
                "Reader-2"
        );
        Thread reader3 = new Thread(
                new CacheReader(cache, "Reader-3", "key2", 5, 50),
                "Reader-3"
        );

        // Start all threads
        writer.start();
        reader1.start();
        reader2.start();
        reader3.start();

        // Wait for completion
        writer.join();
        reader1.join();
        reader2.join();
        reader3.join();

        System.out.println("\n=== Final Cache State ===");
        System.out.println("Cache size: " + cache.size());
        System.out.println("key1 = " + cache.get("key1"));
        System.out.println("key2 = " + cache.get("key2"));
        System.out.println("key3 = " + cache.get("key3"));

        // Demonstrate getOrCompute
        System.out.println("\n=== getOrCompute Demo ===");
        String computed = cache.getOrCompute("newKey", k -> {
            System.out.println("Computing value for: " + k);
            return "computed-value-for-" + k;
        });
        System.out.println("getOrCompute returned: " + computed);

        // Second call should use cached value
        String cached = cache.getOrCompute("newKey", k -> {
            System.out.println("This should NOT print - value is cached");
            return "should-not-see-this";
        });
        System.out.println("Second getOrCompute returned: " + cached);

        System.out.println("\n=== Demo Complete ===");
    }
}
