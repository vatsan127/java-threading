package com.github.java_threading.create_threads;

class ThreadImpl implements Runnable {

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + ": Implementing Runnable Interface");
    }
}

public class MyThreadImplements {

    public static void main(String[] args) {
        ThreadImpl impl = new ThreadImpl();
        Thread t1 = new Thread(impl);
        t1.start();
    }

}
