package com.lineupreporter.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TeamInfo {

    private List<Team> team = new ArrayList<>();
    private int inDivisionGameCount;
    private int outOfDivisionGameCount;
    private Instant timeLastLoaded;

    public List<Team> getTeam() { return team; }
    public void setTeam(List<Team> team) { this.team = team != null ? team : new ArrayList<>(); }

    public int getInDivisionGameCount() { return inDivisionGameCount; }
    public void setInDivisionGameCount(int inDivisionGameCount) { this.inDivisionGameCount = inDivisionGameCount; }

    public int getOutOfDivisionGameCount() { return outOfDivisionGameCount; }
    public void setOutOfDivisionGameCount(int outOfDivisionGameCount) { this.outOfDivisionGameCount = outOfDivisionGameCount; }

    public Instant getTimeLastLoaded() { return timeLastLoaded; }
    public void setTimeLastLoaded(Instant timeLastLoaded) { this.timeLastLoaded = timeLastLoaded; }

    public boolean hasEmptyData() {
        if (team.isEmpty()) return true;
        for (Team t : team) {
            if (t.getDivision() == null || t.getDivision().isEmpty()) return true;
        }
        return false;
    }
}
