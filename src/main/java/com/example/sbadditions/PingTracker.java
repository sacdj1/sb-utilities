package com.example.sbadditions;

public class PingTracker {
    private static volatile int measuredPing = -1;

    public static void set(int ms) {
        measuredPing = Math.max(0, ms);
    }

    public static int get() {
        return measuredPing;
    }

    public static void reset() {
        measuredPing = -1;
    }
}
