package com.lineupreporter.config;

import com.lineupreporter.service.LineupEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * On startup, initializes the application with the rosterReport.PRT file
 * from the project resources if present.
 */
@Component
@Slf4j
public class RosterReportInitializer implements ApplicationRunner {

    private static final String RESOURCE_PATH = "c:\\dev\\LineupReporter\\rosterReport.PRT";

    private final LineupEngine lineupEngine;

    public RosterReportInitializer(LineupEngine lineupEngine) {
        this.lineupEngine = lineupEngine;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (lineupEngine.isInitialized()) {
            return;
        }
        try {
            lineupEngine.initialize(RESOURCE_PATH);
            log.info("Initialized with roster report from resource: {}", RESOURCE_PATH);
        } catch (Exception e) {
            log.warn("Could not initialize with resource {}: {}", RESOURCE_PATH, e.getMessage());
        }
    }
}
