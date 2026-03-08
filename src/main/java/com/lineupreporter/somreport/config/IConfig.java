package com.lineupreporter.somreport.config;

/**
 * Configuration for SOM report parsing (AB/IP multipliers and minimums).
 * Ported from somReportUtils IConfig.
 */
public interface IConfig {
    float getABMultiplier();
    float getIPMultiplier();
    float getMinABAllowed();
    float getMinIPAllowed();
}
