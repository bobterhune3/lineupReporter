package com.lineupreporter.service;

import com.lineupreporter.domain.Team;
import com.lineupreporter.domain.TeamInfo;
import com.lineupreporter.domain.TeamLineup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main engine: loads report file, teams, lineups, and provides balance/usage data.
 */
@Service
public class LineupEngine {

    private List<Team> completeListOfTeams = new ArrayList<>();
    private TeamInfo teamLineupData;
    private Map<String, TeamLineup> storedLineups = new LinkedHashMap<>();
    private SomTeamReportFile teamReportFile;
    private List<Map<Integer, Integer>> balanceAtBats = new ArrayList<>();

    private final SomTeamReportFile somTeamReportFile;
    private final LineupPersistence lineupPersistence;
    private final TeamInformationPersistence teamInformationPersistence;

    public LineupEngine(SomTeamReportFile somTeamReportFile,
                        LineupPersistence lineupPersistence,
                        TeamInformationPersistence teamInformationPersistence) {
        this.somTeamReportFile = somTeamReportFile;
        this.lineupPersistence = lineupPersistence;
        this.teamInformationPersistence = teamInformationPersistence;
    }

    public void initialize(String fileLocation) {
        balanceAtBats = new ArrayList<>();
        storedLineups = lineupPersistence.loadDatabase();
        try {
            somTeamReportFile.parse(fileLocation, storedLineups);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse roster report: " + e.getMessage(), e);
        }
        if (somTeamReportFile.getLastError() != null && !somTeamReportFile.getLastError().isEmpty()) {
            throw new IllegalArgumentException(somTeamReportFile.getLastError());
        }
        completeListOfTeams = somTeamReportFile.getTeams();
        teamReportFile = somTeamReportFile;
        teamLineupData = teamInformationPersistence.loadDatabase();
        applyDivisionToTeams();
    }

    /** Copy division from persisted team info to the loaded teams. */
    private void applyDivisionToTeams() {
        for (Team teamDb : teamLineupData.getTeam()) {
            for (Team teamFile : completeListOfTeams) {
                if (teamDb.getAbrv() != null && teamDb.getAbrv().equals(teamFile.getAbrv())) {
                    teamFile.setDivision(teamDb.getDivision());
                    break;
                }
            }
        }
    }

    public List<Team> getCompleteListOfTeams() {
        return completeListOfTeams;
    }

    public TeamInfo getTeamLineupData() {
        return teamLineupData;
    }

    public Map<String, TeamLineup> getStoredLineups() {
        return storedLineups;
    }

    public SomTeamReportFile getTeamReportFile() {
        return teamReportFile;
    }

    public List<Map<Integer, Integer>> getBalanceAtBats() {
        return balanceAtBats;
    }

    public void saveDatabase() {
        teamInformationPersistence.saveDatabase(teamLineupData);
        lineupPersistence.saveDatabase(storedLineups);
    }

    public boolean isInitialized() {
        return teamReportFile != null && completeListOfTeams != null && !completeListOfTeams.isEmpty();
    }
}
