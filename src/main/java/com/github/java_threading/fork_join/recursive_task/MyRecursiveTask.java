package com.github.java_threading.fork_join.recursive_task;

import java.util.concurrent.RecursiveTask;

public class MyRecursiveTask extends RecursiveTask<Integer> {

    private final int initialLoad;

    public MyRecursiveTask(int initialLoad) {
        this.initialLoad = initialLoad;
    }

    @Override
    protected Integer compute() {

        if (this.initialLoad > 16) {
            System.out.println(Thread.currentThread().getName() + " - Splitting workload - " + this.initialLoad);

            int workload1 = initialLoad / 2;
            int workload2 = initialLoad - workload1;

            MyRecursiveTask subTask1 = new MyRecursiveTask(workload1);
            MyRecursiveTask subTask2 = new MyRecursiveTask(workload2);

            subTask1.fork();
            subTask2.fork();

            int result = 0;
            result += subTask1.join();
            result += subTask1.join();

            return result;

        } else {
            System.out.println(Thread.currentThread().getName() + " - Completed workload");
            return initialLoad * 3;
        }


    }
}
