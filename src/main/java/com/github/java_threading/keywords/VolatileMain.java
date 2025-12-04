    package com.github.java_threading.keywords;

    public class VolatileMain {

        private boolean running = true;
    //    private volatile boolean running = true;

        public void runner() throws InterruptedException {
            Thread worker = new Thread(() -> {
                System.out.println("Worker thread started");
                while (running) {
    //                System.out.println("Worker Thread running.");
                }
                System.out.println("Worker thread stopped");
            });

            worker.start();
            Thread.sleep(1000);

            running = false; // Signal worker to stop
            System.out.println("Main thread stopped worker");

            worker.join();
            System.out.println("Program finished");
        }

        public static void main(String[] args) throws InterruptedException {
            VolatileMain obj = new VolatileMain();
            obj.runner();
        }
    }

