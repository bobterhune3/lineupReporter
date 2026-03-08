package com.lineupreporter.somreport.data;

import com.lineupreporter.domain.Player;
import com.lineupreporter.domain.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory store for teams and players used by SOM reports.
 * Ported from somReportUtils DataStore.
 */
public class DataStore {

    private final List<Team> teams = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();

    public void addTeam(Team team) {
        teams.add(team);
    }

    public void addPlayerUsage(Player player) {
        players.add(player);
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Team getTeam(String abbrv) {
        for (Team team : teams) {
            if (abbrv != null && abbrv.equals(team.getAbrv())) return team;
        }
        return null;
    }

    public void reset() {
        teams.clear();
        players.clear();
    }
}
