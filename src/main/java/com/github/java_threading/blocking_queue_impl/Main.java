package com.github.java_threading.blocking_queue_impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class Main {

    /*
     * ToDO:
     *  create a queue with initial capacity
     *  producer - when the queue capacity is full then wait.
     *  consumer - when the queue is empty wait until there is an item added.
     *  producer and consumer should communicate with each other for the events happening
     *  create a producer thread
     *  create 2 consumer thread
     *
     * */


    Queue<String> blockingQueue = new LinkedList<>();
    int capacity = 5;

    private synchronized void producer() throws InterruptedException {

        while (blockingQueue.size() == capacity) {
            wait();
        }

        String element = System.currentTimeMillis() + "";
        blockingQueue.add(element);
        System.out.println(Thread.currentThread().getName() + " - Produced element - " + element);
        notifyAll();

    }

    private synchronized void consumer() throws InterruptedException {
        while (blockingQueue.isEmpty()) {
            wait();
        }

        String poll = blockingQueue.poll();
        System.out.println(Thread.currentThread().getName() + " - Retrieved element - " + poll);
        notifyAll(); // ToDO: why do we need to notify each time?
    }


    public static void main(String[] args) throws InterruptedException {
        Main obj = new Main();
        Thread t1 = new Thread(() -> {
            try {
                while (true) {
                    obj.producer();
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "producer-thread-1");


        Runnable consumerRunnable = () -> {
            try {
                while (true) {
                    obj.consumer();
                    TimeUnit.SECONDS.sleep(1);                 }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        Thread t2 = new Thread(consumerRunnable, "consumer-thread-1");
        Thread t3 = new Thread(consumerRunnable, "consumer-thread-2");

        t1.start();
        t2.start();
        t3.start();


    }


}
