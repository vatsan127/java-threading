package com.github.java_threading.concurrent_map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapMain {

    private static final ConcurrentMap<String, String> cmap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Basic Operations
        cmap.put("1", "A");
        cmap.get("1");
        cmap.remove("1");

        // Atomic Operations
        cmap.putIfAbsent("2", "B"); // Adds entry only if key doesn't exist
        cmap.replace("2", "B2"); // Replaces value for an existing key
        cmap.compute("2", (k, v) -> (v + "3")); // compute
        System.out.println(cmap);

        if (cmap.containsKey("3")) {
            String s = cmap.get("3");
            s = s + "4";
            cmap.put("3", s);
        }

        cmap.computeIfPresent("3", (k, v) -> (v + "3"));

    }

}
