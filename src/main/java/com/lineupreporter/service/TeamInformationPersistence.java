package com.lineupreporter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineupreporter.domain.TeamInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class TeamInformationPersistence {

    private static final String FILE_NAME = "teaminfo.json";

    @Value("${lineupreporter.data-dir:}")
    private String dataDir;

    private Path dataPath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        String base = dataDir != null && !dataDir.isEmpty() ? dataDir : System.getProperty("user.dir");
        dataPath = Path.of(base, "lineupreporter-data");
        try {
            Files.createDirectories(dataPath);
        } catch (IOException e) {
            log.warn("Could not create data directory: {}", dataPath, e);
        }
    }

    public TeamInfo loadDatabase() {
        Path file = dataPath.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            return new TeamInfo();
        }
        try {
            String json = Files.readString(file);
            return objectMapper.readValue(json, TeamInfo.class);
        } catch (Exception e) {
            log.warn("Could not load team info from {}", file, e);
            return new TeamInfo();
        }
    }

    public void saveDatabase(TeamInfo teamInfo) {
        if (teamInfo == null) return;
        Path file = dataPath.resolve(FILE_NAME);
        try {
            Files.createDirectories(file.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), teamInfo);
        } catch (IOException e) {
            log.error("Could not save team info to {}", file, e);
        }
    }
}
