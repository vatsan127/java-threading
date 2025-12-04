package com.github.java_threading.fork_join.recursive_action;

import java.util.concurrent.RecursiveAction;

public class MyRecursiveAction extends RecursiveAction {

    private final int initialWorkLoad;

    public MyRecursiveAction(int initialWorkLoad) {
        this.initialWorkLoad = initialWorkLoad;
    }

    @Override
    protected void compute() {
        if (initialWorkLoad > 16) {
            int workLoad1 = initialWorkLoad / 2;
            int workLoad2 = initialWorkLoad - workLoad1;

            MyRecursiveAction subTask1 = new MyRecursiveAction(workLoad1);
            MyRecursiveAction subTask2 = new MyRecursiveAction(workLoad2);

            subTask1.fork();
            subTask2.fork();

        } else {
            System.out.println(Thread.currentThread().getName() + " workload done - " + initialWorkLoad);
        }
    }
}



