package com.github.java_threading;

public class Main {

    private boolean flag = true;

    private void run() {
        while (flag) {

        }
    }


    public static void main(String[] args) throws InterruptedException {
        Main obj = new Main();

        Thread t1 = new Thread(obj::run, "Thread-1");
        Thread t2 = new Thread(() -> {
            obj.flag = false;
//            System.out.println(Thread.currentThread().getName() + " - updated value to false");
        }, "Thread-2");

        t1.start();

//        System.out.println("Starting Thread-2");
        t2.start();

        t1.join();
    }
}
