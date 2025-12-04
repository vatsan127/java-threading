package com.github.java_threading.callable_future;

import java.util.concurrent.*;

public class FutureMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> task = () -> {
            System.out.println(Thread.currentThread().getName() + " - Executing task");
            Thread.sleep(2000);
            return 42;
        };

        Future<Integer> future = executor.submit(task);

        System.out.println("Task submitted, doing other work...");

        // Check if task is done (non-blocking)
        System.out.println("Is done? " + future.isDone());

        // Get result (blocking)
        Integer result = future.get();
        System.out.println("Result: " + result);

        executor.shutdown();
    }
}
