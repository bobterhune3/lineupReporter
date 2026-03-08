package com.lineupreporter.service;

public final class CalculateColumnUtil {

    public static int targetAtBats = 615;

    public static int calculateColumn(int ipForBalance, double percentAdj, int totalIP) {
        if (ipForBalance == 0) return 0;
        double adj = Math.ceil(ipForBalance * percentAdj);
        double multiplier = adj / totalIP;
        return (int) Math.ceil(multiplier * targetAtBats);
    }

    public static int calculateColumn(int ipForBalance, int totalIP) {
        return (int) Math.ceil((double) ipForBalance / totalIP * targetAtBats);
    }
}
