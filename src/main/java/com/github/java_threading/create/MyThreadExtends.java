package com.github.java_threading.create;


class MyThread extends Thread {

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }

}

public class MyThreadExtends {
    public static void main(String[] args) {

        Thread t1 = new MyThread();
        t1.start();
    }
}
