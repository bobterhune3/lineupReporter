package com.lineupreporter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineupreporter.domain.Team;
import com.lineupreporter.domain.TeamLineup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class LineupPersistence {

    private static final String FILE_NAME = "lineups.json";

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

    public Map<String, TeamLineup> loadDatabase() {
        Path file = dataPath.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            return new LinkedHashMap<>();
        }
        try {
            String json = Files.readString(file);
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, TeamLineup.class));
        } catch (Exception e) {
            log.warn("Could not load lineups from {}", file, e);
            return new LinkedHashMap<>();
        }
    }

    public void saveDatabase(Map<String, TeamLineup> lineups) {
        if (lineups == null) return;
        Path file = dataPath.resolve(FILE_NAME);
        try {
            Files.createDirectories(file.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), lineups);
        } catch (IOException e) {
            log.error("Could not save lineups to {}", file, e);
        }
    }

    /** Get or create TeamLineup for the given team. */
    public static TeamLineup lookupTeamLineup(Map<String, TeamLineup> storedLineups, Team team) {
        if (team == null || team.getAbrv() == null) return new TeamLineup();
        return storedLineups.computeIfAbsent(team.getAbrv(), k -> new TeamLineup());
    }
}
