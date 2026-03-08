package com.lineupreporter.somreport;

import com.lineupreporter.domain.Player;
import com.lineupreporter.domain.Team;
import com.lineupreporter.somreport.config.IConfig;
import com.lineupreporter.util.RecordIndex;
import com.lineupreporter.util.TeamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SOM primary stats report. Ported from somReportUtils PrimaryStatsReport.
 */
public class PrimaryStatsReport extends TeamReport {

    private static final String REGEX_PRIMARY_HITTER = ".+[0-9]+ +.+[0-9]+ +([0-9]+).+";
    private static final String REGEX_PRIMARY_PITCHER = "^[0-9]?[0-9]?\\.[0-9]+ +[0-9]+ +[0-9]+ +[0-9]?\\.[0-9]+ +[0-9]+ +[0-9]+ +[0-9]+ +[0-9]+ +[0-9]+ +([0-9]+\\.[0-3])";

    private final List<Player> listOfPlayers = new ArrayList<>();
    private final IConfig config;
    private boolean workingOnHitters = true;
    private boolean lastLineSubTotal = false;

    public PrimaryStatsReport(String title, IConfig config) {
        super(title);
        this.config = config;
    }

    public List<Player> getPlayers() {
        return listOfPlayers;
    }

    @Override
    public void processReport(int leagueNameLength) {
        for (Map.Entry<String, List<String>> e : teamLines.entrySet()) {
            List<String> teamLinesList = e.getValue();
            for (String line : teamLinesList) {
                String fixedLine = line.replace("[0]", "");
                collectData(e.getKey(), fixedLine, leagueNameLength);
            }
        }
    }

    public void collectData(String teamName, String line, int leagueNameLength) {
        if (lastLineSubTotal) {
            lastLineSubTotal = false;
            return;
        }
        if (line.startsWith("NAME              BAVG")) {
            workingOnHitters = true;
            return;
        }
        if (line.startsWith("NAME               ERA")) {
            workingOnHitters = false;
            return;
        }
        if (line.startsWith("TEAM")) {
            lastLineSubTotal = true;
            return;
        }
        if (line.startsWith("NAME") || line.trim().isEmpty() || line.startsWith("------") || line.startsWith("Team")) return;

        int idx = line.indexOf("[4]");
        if (idx < 0) return;
        String playerName = line.substring(0, idx).trim();
        if (playerName.isEmpty()) return;

        String data = line.substring(idx + 4).trim();
        Pattern pattern = Pattern.compile(workingOnHitters ? REGEX_PRIMARY_HITTER : REGEX_PRIMARY_PITCHER);
        Matcher m = pattern.matcher(data);
        if (!m.find()) return;

        Player player = new Player();
        player.setHitter(workingOnHitters);
        player.setName(playerName);

        if (workingOnHitters) {
            player.setReplay(Integer.parseInt(m.group(1).trim()));
        } else {
            String v = m.group(1).trim();
            player.setReplay((int) Double.parseDouble(v));
        }

        Team team = Report.DATABASE.getTeam(TeamUtils.prettyTeamName(teamName));
        if (team == null) {
            team = Team.forReport(RecordIndex.getNextId(RecordIndex.IndexType.TeamId), "XX", leagueNameLength);
            team.setAbrv(teamName);
        }
        player.setTeam(team);
        listOfPlayers.add(player);
    }
}
