package com.github.java_threading.locks.condition;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition Examples
 *
 * Condition provides more flexible thread signaling than wait()/notify().
 * It's the Lock equivalent of Object's wait/notify/notifyAll.
 *
 * Key methods:
 * - await()      - like wait(), releases lock and waits for signal
 * - signal()     - like notify(), wakes one waiting thread
 * - signalAll()  - like notifyAll(), wakes all waiting threads
 *
 * Advantages over wait/notify:
 * 1. Multiple conditions per lock (e.g., notFull, notEmpty for bounded buffer)
 * 2. More intuitive API
 * 3. Works with Lock objects (more flexible than synchronized)
 * 4. Supports interruptible, non-interruptible, and timed waits
 */
public class ConditionMain {

    public static void main(String[] args) throws InterruptedException {
        // Example 1: Basic condition - simple signal
        System.out.println("=== Example 1: Basic Condition Signaling ===");
        basicConditionExample();

        // Example 2: Bounded buffer with two conditions
        System.out.println("\n=== Example 2: Bounded Buffer (Producer-Consumer) ===");
        boundedBufferExample();

        // Example 3: Multiple conditions on same lock
        System.out.println("\n=== Example 3: Multiple Conditions ===");
        multipleConditionsExample();

        // Example 4: await with timeout
        System.out.println("\n=== Example 4: Await with Timeout ===");
        awaitWithTimeoutExample();

        // Example 5: signalAll vs signal
        System.out.println("\n=== Example 5: signalAll vs signal ===");
        signalAllExample();
    }

    /**
     * Example 1: Basic condition usage
     * Thread waits until another thread signals
     */
    public static void basicConditionExample() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean[] ready = {false};

        // Waiter thread
        Thread waiter = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Waiter: Waiting for signal...");
                while (!ready[0]) {  // Always use while loop (spurious wakeups!)
                    condition.await();  // Releases lock and waits
                }
                System.out.println("Waiter: Got signal! Ready = " + ready[0]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "waiter-thread");

        // Signaler thread
        Thread signaler = new Thread(() -> {
            try {
                Thread.sleep(100);  // Do some work first
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            lock.lock();
            try {
                System.out.println("Signaler: Setting ready = true and signaling");
                ready[0] = true;
                condition.signal();  // Wake up waiter
            } finally {
                lock.unlock();
            }
        }, "signaler-thread");

        waiter.start();
        signaler.start();

        waiter.join();
        signaler.join();
    }

    /**
     * Example 2: Classic bounded buffer using two conditions
     * This is THE canonical example of Condition usage
     */
    public static void boundedBufferExample() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(5);

        // Producer
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    buffer.put(i);
                    System.out.println("Produced: " + i + " | Buffer size: " + buffer.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "producer");

        // Consumer (slower than producer)
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(100);  // Slower consumption
                    int value = buffer.take();
                    System.out.println("Consumed: " + value + " | Buffer size: " + buffer.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }

    /**
     * Example 3: Multiple conditions on the same lock
     * Different threads wait on different conditions
     */
    public static void multipleConditionsExample() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition conditionA = lock.newCondition();
        Condition conditionB = lock.newCondition();

        Thread threadA = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Thread A: waiting on conditionA");
                conditionA.await();
                System.out.println("Thread A: woke up!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "Thread-A");

        Thread threadB = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Thread B: waiting on conditionB");
                conditionB.await();
                System.out.println("Thread B: woke up!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "Thread-B");

        threadA.start();
        threadB.start();
        Thread.sleep(100);  // Let both threads start waiting

        // Signal only conditionA - only Thread A wakes up
        lock.lock();
        try {
            System.out.println("Main: signaling conditionA only");
            conditionA.signal();
        } finally {
            lock.unlock();
        }

        Thread.sleep(100);

        // Signal conditionB - Thread B wakes up
        lock.lock();
        try {
            System.out.println("Main: signaling conditionB");
            conditionB.signal();
        } finally {
            lock.unlock();
        }

        threadA.join();
        threadB.join();
    }

    /**
     * Example 4: await with timeout
     */
    public static void awaitWithTimeoutExample() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Thread waiter = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Waiter: waiting with 500ms timeout...");
                long start = System.currentTimeMillis();

                // await returns remaining time (> 0 if signaled, <= 0 if timeout)
                boolean signaled = condition.await(500, java.util.concurrent.TimeUnit.MILLISECONDS);

                long elapsed = System.currentTimeMillis() - start;
                if (signaled) {
                    System.out.println("Waiter: was signaled after " + elapsed + "ms");
                } else {
                    System.out.println("Waiter: timed out after " + elapsed + "ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "waiter");

        waiter.start();
        waiter.join();

        // Now with actual signal before timeout
        System.out.println("\nNow testing with signal before timeout:");
        Thread waiter2 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("Waiter2: waiting with 500ms timeout...");
                boolean signaled = condition.await(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                System.out.println("Waiter2: signaled = " + signaled);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "waiter2");

        waiter2.start();
        Thread.sleep(100);

        lock.lock();
        try {
            System.out.println("Main: sending signal");
            condition.signal();
        } finally {
            lock.unlock();
        }

        waiter2.join();
    }

    /**
     * Example 5: signalAll vs signal
     * signal() wakes ONE thread, signalAll() wakes ALL threads
     */
    public static void signalAllExample() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Runnable waiterTask = () -> {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ": waiting...");
                condition.await();
                System.out.println(Thread.currentThread().getName() + ": woke up!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        };

        // Start 3 waiters
        Thread w1 = new Thread(waiterTask, "Waiter-1");
        Thread w2 = new Thread(waiterTask, "Waiter-2");
        Thread w3 = new Thread(waiterTask, "Waiter-3");

        w1.start();
        w2.start();
        w3.start();
        Thread.sleep(100);  // Let all start waiting

        // signalAll wakes all of them
        lock.lock();
        try {
            System.out.println("Main: calling signalAll()");
            condition.signalAll();  // All 3 waiters wake up
        } finally {
            lock.unlock();
        }

        w1.join();
        w2.join();
        w3.join();

        System.out.println("\nNote: signal() would wake only ONE thread (randomly chosen)");
        System.out.println("Use signalAll() when state change could satisfy multiple waiters");
    }

    /**
     * Classic bounded buffer implementation using Condition
     * This is the preferred way over wait()/notify()
     */
    static class BoundedBuffer<T> {
        private final Queue<T> queue = new LinkedList<>();
        private final int capacity;
        private final Lock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();   // Producers wait on this
        private final Condition notEmpty = lock.newCondition();  // Consumers wait on this

        public BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        public void put(T item) throws InterruptedException {
            lock.lock();
            try {
                // Wait while buffer is full
                while (queue.size() == capacity) {
                    System.out.println("Buffer full, producer waiting...");
                    notFull.await();  // Wait for space
                }
                queue.add(item);
                notEmpty.signal();  // Signal consumers that data is available
            } finally {
                lock.unlock();
            }
        }

        public T take() throws InterruptedException {
            lock.lock();
            try {
                // Wait while buffer is empty
                while (queue.isEmpty()) {
                    System.out.println("Buffer empty, consumer waiting...");
                    notEmpty.await();  // Wait for data
                }
                T item = queue.remove();
                notFull.signal();  // Signal producers that space is available
                return item;
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            lock.lock();
            try {
                return queue.size();
            } finally {
                lock.unlock();
            }
        }
    }
}
