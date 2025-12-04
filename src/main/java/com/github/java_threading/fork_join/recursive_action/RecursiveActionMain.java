package com.github.java_threading.fork_join.recursive_action;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class RecursiveActionMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool(2);

        MyRecursiveAction myRecursiveAction = new MyRecursiveAction(80);
        forkJoinPool.invoke(myRecursiveAction);

        TimeUnit.SECONDS.sleep(3);

    }

}
