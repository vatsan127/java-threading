package com.github.java_threading.blocking_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueueMain {

    private final BlockingQueue<String> queue;

    public BlockingQueueMain() {
        queue = new LinkedBlockingQueue<>();
        queue.add("" + 0);
    }


    public void producer() {
        for (int i = 1; i < 10; i++) {
            try {
                queue.put("" + i);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void consumer() {
        while (true) {
            String element = queue.poll();
            if (element == null) {
                System.out.println("Queue is empty!!!");
                break;
            }
            System.out.println(Thread.currentThread().getName() + " " + element);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        BlockingQueueMain obj = new BlockingQueueMain();

        Thread t1 = new Thread(obj::producer, "producer-thread");
        Thread t2 = new Thread(obj::consumer, "consumer-thread");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

    }

}
