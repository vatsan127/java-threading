package com.github.java_threading.locks.read_write_lock.cache;

/**
 * Cache Reader Task
 *
 * Reads from cache multiple times. Multiple readers can run concurrently
 * because they only acquire the read lock (shared lock).
 */
public class CacheReader implements Runnable {

    private final ThreadSafeCache<String, String> cache;
    private final String readerName;
    private final String keyToRead;
    private final int readCount;
    private final long delayBetweenReads;

    public CacheReader(ThreadSafeCache<String, String> cache, String readerName,
                       String keyToRead, int readCount, long delayBetweenReads) {
        this.cache = cache;
        this.readerName = readerName;
        this.keyToRead = keyToRead;
        this.readCount = readCount;
        this.delayBetweenReads = delayBetweenReads;
    }

    @Override
    public void run() {
        for (int i = 0; i < readCount; i++) {
            String value = cache.get(keyToRead);
            System.out.println(readerName + ": Read " + keyToRead + " = " + value);

            if (delayBetweenReads > 0) {
                try {
                    Thread.sleep(delayBetweenReads);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(readerName + ": Interrupted!");
                    return;
                }
            }
        }
        System.out.println(readerName + ": Finished reading");
    }
}
