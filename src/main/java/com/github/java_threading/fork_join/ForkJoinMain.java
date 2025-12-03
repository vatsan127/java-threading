package com.github.java_threading.fork_join;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ForkJoinMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

        MyRecursiveTask recursiveTask = new MyRecursiveTask(80);
        ForkJoinTask<Integer> result = forkJoinPool.submit(recursiveTask);
        System.out.println("result - " + result.get());
    }

}
