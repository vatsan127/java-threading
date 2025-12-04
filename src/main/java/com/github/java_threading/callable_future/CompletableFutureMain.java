package com.github.java_threading.callable_future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // Basic async operation with chaining
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    System.out.println("Stage 1 - " + Thread.currentThread().getName());
                    return "Hello";
                })
                .thenApply(result -> {
                    // Same thread executes this
                    System.out.println("Stage 2 (thenApply) - " + Thread.currentThread().getName());
                    return result + " World";
                })
                .thenApplyAsync(result -> {
                    // Different thread executes this
                    System.out.println("Stage 3 (thenApplyAsync) - " + Thread.currentThread().getName());
                    return result + "!";
                });

        System.out.println("Final result: " + future.get());

        // thenAccept - consumes result, returns void
        CompletableFuture
                .supplyAsync(() -> 100)
                .thenAccept(value -> System.out.println("Received value: " + value));

        // Combining two futures
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 10);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 20);

        CompletableFuture<Integer> combined = future1.thenCombine(future2, Integer::sum);
        System.out.println("Combined result: " + combined.get());

        // Wait for all to complete
        CompletableFuture.allOf(future1, future2).join();
        System.out.println("All futures completed");

        // Exception handling - String
        CompletableFuture<String> withStringException = CompletableFuture.supplyAsync(() -> {
            if (true) throw new RuntimeException("Something went wrong");
            return "Success";
        }).exceptionally(ex -> "Error: " + ex.getMessage());
        System.out.println("String exception handled: " + withStringException.get());

        // Exception handling - Integer (must return same type)
        CompletableFuture<Integer> withIntException = CompletableFuture.supplyAsync(() -> {
            if (true) throw new RuntimeException("Calculation failed");
            return 100;
        }).exceptionally(ex -> {
            System.out.println("Error: " + ex.getMessage());
            return -1;  // Must return Integer, not String
        });
        System.out.println("Integer exception handled: " + withIntException.get());
    }
}
