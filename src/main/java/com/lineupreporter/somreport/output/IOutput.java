package com.lineupreporter.somreport.output;

import com.lineupreporter.domain.Player;
import com.lineupreporter.domain.Team;

import java.util.List;

/**
 * Output formatter for SOM reports. Ported from somReportUtils output.IOutput.
 */
public interface IOutput {

    void draftOrderHeader();
    void draftOrderTableHeader();
    void draftOrderTeamLine(int pickNum, int dicPick, Team team, String code);
    void wildCardHeader(String league);
    void wildCardTableHeader();
    void wildCardTeamLine(int rank, Team team, String gamesBehind);
    void spacer();
    void setOutputHeader(String title, int daysPlayed);
    void setOutputFooter();
    void endOfTable();
    void divisionStandingsHeader(String division);
    void divisionStandingsTableHeader();
    void divisionStandingsTeamLine(int rank, Team team);
    void showWhosHotData(String v);
    void showInjuryData(List<String> injuredPlayers, List<String> returningPlayers);
    void recordBookHeader(boolean teamRecord);
    void recordBookItem(SOMRecord rec, int counter, boolean teamRecord);
    void usageHeader(int playerCount);
    void usageFooter();
    boolean usageReportItem(Player player, int counter, boolean inMinors);
    boolean underUsageReportItem(Player hitter, Player pitcher, int counter);
    void underUsageHeader(int playerCount);
    void underUsageFooter();
}
