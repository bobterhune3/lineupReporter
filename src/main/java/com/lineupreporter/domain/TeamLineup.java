package com.lineupreporter.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TeamLineup {

    private List<LineupData> lineups = new ArrayList<>();
    private List<Player> playerByGRID = new ArrayList<>();
    private Instant timeLastLoaded;

    public List<LineupData> getLineups() { return lineups; }
    public void setLineups(List<LineupData> lineups) { this.lineups = lineups != null ? lineups : new ArrayList<>(); }

    public List<Player> getPlayerByGRID() { return playerByGRID; }
    public void setPlayerByGRID(List<Player> playerByGRID) { this.playerByGRID = playerByGRID != null ? playerByGRID : new ArrayList<>(); }

    public Instant getTimeLastLoaded() { return timeLastLoaded; }
    public void setTimeLastLoaded(Instant timeLastLoaded) { this.timeLastLoaded = timeLastLoaded; }
}
