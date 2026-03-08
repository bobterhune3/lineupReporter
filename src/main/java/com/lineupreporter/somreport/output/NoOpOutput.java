package com.lineupreporter.somreport.output;

import com.lineupreporter.domain.Player;
import com.lineupreporter.domain.Team;

import java.util.List;

/**
 * No-op implementation of IOutput for when output is not required.
 */
public class NoOpOutput implements IOutput {

    @Override public void draftOrderHeader() {}
    @Override public void draftOrderTableHeader() {}
    @Override public void draftOrderTeamLine(int pickNum, int dicPick, Team team, String code) {}
    @Override public void wildCardHeader(String league) {}
    @Override public void wildCardTableHeader() {}
    @Override public void wildCardTeamLine(int rank, Team team, String gamesBehind) {}
    @Override public void spacer() {}
    @Override public void setOutputHeader(String title, int daysPlayed) {}
    @Override public void setOutputFooter() {}
    @Override public void endOfTable() {}
    @Override public void divisionStandingsHeader(String division) {}
    @Override public void divisionStandingsTableHeader() {}
    @Override public void divisionStandingsTeamLine(int rank, Team team) {}
    @Override public void showWhosHotData(String v) {}
    @Override public void showInjuryData(List<String> injuredPlayers, List<String> returningPlayers) {}
    @Override public void recordBookHeader(boolean teamRecord) {}
    @Override public void recordBookItem(SOMRecord rec, int counter, boolean teamRecord) {}
    @Override public void usageHeader(int playerCount) {}
    @Override public void usageFooter() {}
    @Override public boolean usageReportItem(Player player, int counter, boolean inMinors) { return false; }
    @Override public boolean underUsageReportItem(Player hitter, Player pitcher, int counter) { return false; }
    @Override public void underUsageHeader(int playerCount) {}
    @Override public void underUsageFooter() {}
}
