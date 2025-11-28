package com.github.java_threading.compare_and_swap;

import java.util.concurrent.atomic.AtomicBoolean;

public class CASMain {

    AtomicBoolean isLocked = new AtomicBoolean(false);

    private void doLock() {
        // Spin until we successfully acquire the lock (change false to true)
        while (!isLocked.compareAndSet(false, true)) {
            // Keep trying until lock is acquired
        }
        System.out.println("Lock acquired by thread - " + Thread.currentThread().getName());
    }

    private void doUnlock() {
        // Release the lock by setting it back to false
        isLocked.set(false);
        System.out.println("Lock released by thread - " + Thread.currentThread().getName());
    }

    private void criticalSection() {
        doLock();
        try {
            // Simulate critical section work
            System.out.println("Thread " + Thread.currentThread().getName() + " executing critical section");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            doUnlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CASMain obj = new CASMain();

        // Create multiple threads competing for the lock
        Thread t1 = new Thread(obj::criticalSection, "t1");
        Thread t2 = new Thread(obj::criticalSection, "t2");
        Thread t3 = new Thread(obj::criticalSection, "t3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }
}
