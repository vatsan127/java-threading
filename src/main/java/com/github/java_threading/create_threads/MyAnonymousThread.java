package com.github.java_threading.create_threads;


public class MyAnonymousThread {

    public static void main(String[] args) {

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " Hello From Anonymous Thread");
            }
        }, "anonymous-thread"

        );

        t1.start();

    }

}
