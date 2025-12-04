package com.github.java_threading.create_threads;

public class MyLambdaFunction {
    public static void main(String[] args) {
        Thread t1 = new Thread(
                () -> System.out.println(Thread.currentThread().getName() + " Hello World"),
                "t1-thread"
                );
        t1.start();
    }
}
