package com.lineupreporter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * On application shutdown, creates backups of teaminfo.json and lineups.json
 * in the data directory, keeping a rolling history of 3 backups per file
 * (.bak1 = newest, .bak2, .bak3 = oldest).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShutdownBackupService implements DisposableBean {

    private static final String LINEUPS_FILE = "lineups.json";
    private static final String TEAMINFO_FILE = "teaminfo.json";
    private static final int BACKUP_COUNT = 3;

    private final LineupPersistence lineupPersistence;

    @Override
    public void destroy() {
        Path dataPath = lineupPersistence.getDataPath();
        if (dataPath == null) return;
        backupFile(dataPath, LINEUPS_FILE);
        backupFile(dataPath, TEAMINFO_FILE);
    }

    private void backupFile(Path dataPath, String fileName) {
        Path current = dataPath.resolve(fileName);
        if (!Files.exists(current)) return;
        try {
            // Rotate: .bak2 -> .bak3, .bak1 -> .bak2, then copy current -> .bak1
            for (int i = BACKUP_COUNT - 1; i >= 1; i--) {
                Path older = dataPath.resolve(fileName + ".bak" + i);
                Path newer = dataPath.resolve(fileName + ".bak" + (i + 1));
                if (Files.exists(older)) {
                    Files.move(older, newer, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            Path bak1 = dataPath.resolve(fileName + ".bak1");
            Files.copy(current, bak1, StandardCopyOption.REPLACE_EXISTING);
            log.info("Backed up {} to {}", fileName, bak1.getFileName());
        } catch (IOException e) {
            log.warn("Failed to backup {}: {}", fileName, e.getMessage());
        }
    }
}
