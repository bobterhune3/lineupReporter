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
 * Parses SOM comparison report (usage vs actual). Ported from somReportUtils ComparisonReport.
 */
public class ComparisonReport extends TeamReport {

    private static final String REGEX_USAGE_HITTER = ".+[0-9\\-]+ +.+[0-9\\-]+ +([0-9]+) +([0-9]+) +[\\S]+";
    private static final String REGEX_USAGE_PITCHER = "[0-9]?[0-9]?\\.[0-9]+[ ]+[0-9]?[0-9]?\\.[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+([0-9]+)+[ ]+([0-9]+).+";
    private static final String REGEX_USAGE_PITCHER_NOT_USED = "[.0-9][0-9]*\\.[0-9]+ +--- +[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[0-9]+[ ]+([0-9]+) +([0-9]+) +[\\S]+";

    private final List<Player> listOfPlayers = new ArrayList<>();
    private final List<Player> listOfActualPlayers = new ArrayList<>();
    private final IConfig config;
    private boolean workingOnHitters = true;

    public ComparisonReport(String title, IConfig config) {
        super(title);
        this.config = config;
    }

    public List<Player> getPlayers() {
        return listOfPlayers;
    }

    public void setPlayerActualData(List<Player> players) {
        listOfActualPlayers.clear();
        if (players != null) listOfActualPlayers.addAll(players);
    }

    @Override
    public void processReport(int leagueNameLength) {
        for (Map.Entry<String, List<String>> e : teamLines.entrySet()) {
            String teamKey = e.getKey();
            for (String line : e.getValue()) {
                String fixedLine = line.replace("[0]", "");
                collectData(teamKey, fixedLine, leagueNameLength);
            }
        }
    }

    private int findPlayersReplayABIP(Player player) {
        for (Player p : listOfActualPlayers) {
            String shortName = p.getName();
            if (shortName != null && shortName.length() > 11) shortName = shortName.substring(0, 11);
            if (shortName != null && shortName.startsWith(player.getName() != null ? player.getName() : "")
                && player.getTeam() != null && p.getTeam() != null
                && player.getTeam().getAbrv().equals(p.getTeam().getAbrv())) {
                return p.getReplay();
            }
        }
        return 0;
    }

    public void collectData(String teamName, String line, int leagueNameLength) {
        if (line.startsWith("--AVERAGE")) {
            workingOnHitters = true;
            return;
        }
        if (line.startsWith("----ERA")) {
            workingOnHitters = false;
            return;
        }
        if (line.startsWith("NAME") || line.trim().isEmpty()) return;

        int idx = line.indexOf("[4]");
        if (idx < 0) return;
        String playerName = line.substring(0, idx).trim();
        if (playerName.isEmpty()) return;

        String data = line.substring(idx + 4).trim();
        Pattern pattern;
        if (data.contains("---") && !workingOnHitters) {
            pattern = Pattern.compile(REGEX_USAGE_PITCHER_NOT_USED);
        } else {
            pattern = Pattern.compile(workingOnHitters ? REGEX_USAGE_HITTER : REGEX_USAGE_PITCHER);
        }
        Matcher m = pattern.matcher(line);
        if (!m.find()) return;

        Player player = new Player();
        player.setHitter(workingOnHitters);
        player.setName(playerName);

        if (workingOnHitters) {
            float value = 0f;
            try { value = Float.parseFloat(m.group(1).trim()); } catch (NumberFormatException ignored) {}
            player.setActual((int) (value * config.getABMultiplier()));
            if (player.getActual() < config.getMinABAllowed()) return;
            player.setTargetUsage(getPlayerTargetUsage(player.getActual()));
        } else {
            float value = 0f;
            try { value = Float.parseFloat(m.group(1).trim()); } catch (NumberFormatException ignored) {}
            player.setActual((int) (value * config.getIPMultiplier()));
            if (player.getActual() < config.getMinIPAllowed()) return;
            player.setTargetUsage(getPitcherTargetUsage(player.getActual()));
        }

        Team team = Report.DATABASE.getTeam(TeamUtils.prettyTeamName(teamName));
        if (team == null) {
            team = Team.forReport(RecordIndex.getNextId(RecordIndex.IndexType.TeamId), "XX", leagueNameLength);
            team.setAbrv(teamName);
        }
        player.setTeam(team);
        player.setReplay(findPlayersReplayABIP(player));
        listOfPlayers.add(player);
    }

    public static int getPlayerTargetUsage(int actual) {
        if (actual < 120) return Math.round(actual * 1.5f);
        if (actual > 600) return Math.round(actual * 1.1f);
        return actual + 60;
    }

    public static int getPitcherTargetUsage(int actual) {
        if (actual < 60) return Math.round(actual * 1.5f);
        if (actual > 199) return Math.round(actual * 1.15f);
        return actual + 30;
    }
}
