package com.lineupreporter.somreport;

import com.lineupreporter.domain.Player;
import com.lineupreporter.domain.Team;
import com.lineupreporter.somreport.data.DataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for SOM reports. Ported from somReportUtils Report.
 */
public class Report {

    public static final DataStore DATABASE = new DataStore();

    private final String title;
    protected final List<String> lines = new ArrayList<>();

    public Report(String title) {
        this.title = title;
    }

    public int getLineCount() { return lines.size(); }
    public String getName() { return title; }

    public void addLine(String line) {
        lines.add(line);
    }

    public String getReportType() { return "UNKNOWN TYPE"; }

    public void processReport(int n) {
        throw new UnsupportedOperationException("processReport");
    }

    public Team getTeamDataByName(String name) {
        for (Team team : DATABASE.getTeams()) {
            if (name != null && name.equals(team.getName())) return team;
        }
        return null;
    }

    public List<Team> getTeamsByOwner(String owner) {
        List<Team> matches = new ArrayList<>();
        for (Team team : DATABASE.getTeams()) {
            if (owner != null && owner.equals(team.getOwner())) matches.add(team);
        }
        return matches;
    }

    public Team getTeamByAbbreviation(String abvr) {
        for (Team team : DATABASE.getTeams()) {
            if (abvr != null && abvr.equals(team.getAbrv())) return team;
        }
        System.err.println("Unable to find a team with Abv " + abvr);
        return null;
    }

    /**
     * Save report state to a string map (e.g. for file persistence).
     * Replaces C# PersistentDictionary.
     */
    public Map<String, String> saveReportInformation(String reportName) {
        Map<String, String> dictionary = new HashMap<>();
        for (Team team : DATABASE.getTeams()) {
            dictionary.put(team.getAbrv(), team.buildStorageData());
        }
        for (Player player : DATABASE.getPlayers()) {
            String key = "Usage_" + player.getName() + ":" + (player.getTeam() != null ? player.getTeam().getAbrv() : "");
            dictionary.put(key, player.buildStorageData());
        }
        return dictionary;
    }

    public void loadPreviousStorageInfo(Map<String, String> dictionary) {
        if (dictionary == null) return;
        System.out.println("Read in Previous Save File");
        for (Map.Entry<String, String> e : dictionary.entrySet()) {
            String teamAbrv = e.getKey();
            if (teamAbrv.startsWith("Usage")) continue;
            String data = e.getValue();
            Team team = getTeamByAbbreviation(teamAbrv);
            if (team != null) {
                Map<String, String> teamData = loadStorageString(data);
                team.setWinsPrevious(Integer.parseInt(teamData.getOrDefault("Wins", "0")));
                team.setLosesPrevious(Integer.parseInt(teamData.getOrDefault("Loses", "0")));
                team.setGbPrevious(Double.parseDouble(teamData.getOrDefault("GB", "0")));
                team.setDivisionPositionPrevious(Integer.parseInt(teamData.getOrDefault("DIVPos", "-1")));
                team.setDraftPickPositionPrevious(Integer.parseInt(teamData.getOrDefault("DPickPos", "-1")));
                team.setWildCardPositionPrevious(Integer.parseInt(teamData.getOrDefault("WCardPos", "-1")));
            }
        }
    }

    public Map<String, String> loadStorageString(String s) {
        Map<String, String> data = new HashMap<>();
        for (String row : s.split("\\|")) {
            int eq = row.indexOf('=');
            if (eq > 0) {
                String key = row.substring(0, eq);
                String value = eq < row.length() - 1 ? row.substring(eq + 1) : "";
                data.put(key, value);
            }
        }
        return data;
    }
}
