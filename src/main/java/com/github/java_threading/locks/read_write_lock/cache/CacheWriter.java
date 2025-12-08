package com.github.java_threading.locks.read_write_lock.cache;

/**
 * Cache Writer Task
 *
 * Writes to cache. Writers get exclusive access - when a writer holds the lock,
 * no readers or other writers can access the cache.
 */
public class CacheWriter implements Runnable {

    private final ThreadSafeCache<String, String> cache;
    private final String writerName;
    private final String keyPrefix;
    private final int writeCount;
    private final long delayBetweenWrites;

    public CacheWriter(ThreadSafeCache<String, String> cache, String writerName,
                       String keyPrefix, int writeCount, long delayBetweenWrites) {
        this.cache = cache;
        this.writerName = writerName;
        this.keyPrefix = keyPrefix;
        this.writeCount = writeCount;
        this.delayBetweenWrites = delayBetweenWrites;
    }

    @Override
    public void run() {
        for (int i = 1; i <= writeCount; i++) {
            String key = keyPrefix + i;
            String value = "value-" + i + "-by-" + writerName;

            cache.put(key, value);
            System.out.println(writerName + ": Wrote " + key + " = " + value);

            if (delayBetweenWrites > 0) {
                try {
                    Thread.sleep(delayBetweenWrites);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(writerName + ": Interrupted!");
                    return;
                }
            }
        }
        System.out.println(writerName + ": Finished writing");
    }
}
