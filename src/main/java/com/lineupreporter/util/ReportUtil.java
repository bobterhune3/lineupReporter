package com.lineupreporter.util;

/**
 * Shared math/formatting utilities for reports (ported from somReportUtils).
 */
public final class ReportUtil {

    public static double roundToSignificantDigits(double d, int digits) {
        if (d == 0.0) return 0.0;
        double leftSideNumbers = Math.floor(Math.log10(Math.abs(d))) + 1;
        double scale = Math.pow(10, leftSideNumbers);
        double result = scale * Math.round(d / scale * Math.pow(10, digits)) / Math.pow(10, digits);
        if ((int) leftSideNumbers >= digits) {
            return Math.round(result);
        } else {
            return Math.round(result * Math.pow(10, digits - (int) leftSideNumbers))
                / Math.pow(10, digits - (int) leftSideNumbers);
        }
    }

    private ReportUtil() {}
}
