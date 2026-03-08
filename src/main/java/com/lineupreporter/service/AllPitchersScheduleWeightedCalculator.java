package com.lineupreporter.service;

import com.lineupreporter.config.AppConfig;
import com.lineupreporter.domain.Team;
import com.lineupreporter.util.LineupTools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schedule-weighted usage calculator for all pitchers (in/out division).
 */
public class AllPitchersScheduleWeightedCalculator {

    private static final String[] TYPES = LineupTools.getBalanceTypes();

    private final SomTeamReportFile teamReportFile;
    private final Team targetTeam;
    private final AppConfig appConfig;
    private int inDivision;
    private int outDivision;

    public AllPitchersScheduleWeightedCalculator(SomTeamReportFile teamReportFile, Team targetTeam, AppConfig appConfig) {
        this.teamReportFile = teamReportFile;
        this.targetTeam = targetTeam;
        this.appConfig = appConfig;
    }

    public void setInDivision(int inDivision) { this.inDivision = inDivision; }
    public void setOutDivision(int outDivision) { this.outDivision = outDivision; }
    public void setTargetAtBats(int targetAtBats) { CalculateColumnUtil.targetAtBats = targetAtBats; }

    public List<Map<Integer, Integer>> calculate() {
        return calculate(null);
    }

    public List<Map<Integer, Integer>> calculate(RowCallback createRowFunc) {
        if (inDivision == 0 || outDivision == 0) {
            throw new IllegalStateException("In and out division game counts must be defined");
        }

        double teamsInDivision = 0, teamsOutDivision = 0, gamesInDivision = 0, gamesOutDivision = 0, totalGames = 0;
        for (Team opponent : teamReportFile.getTeams()) {
            if (targetTeam.getAbrv().equals(opponent.getAbrv())) continue;
            if (targetTeam.getDivision().equals(opponent.getDivision())) {
                gamesInDivision += inDivision;
                totalGames += inDivision;
                teamsInDivision++;
            } else {
                gamesOutDivision += outDivision;
                totalGames += outDivision;
                teamsOutDivision++;
            }
        }

        double pctInPerTeam = inDivision / totalGames;
        double pctOutPerTeam = outDivision / totalGames;
        double overallPctIn = pctInPerTeam * teamsInDivision;
        double overallPctOut = pctOutPerTeam * teamsOutDivision;

        Map<String, Integer> totalLIn = new LinkedHashMap<>();
        Map<String, Integer> totalRIn = new LinkedHashMap<>();
        Map<String, Integer> totalLOut = new LinkedHashMap<>();
        Map<String, Integer> totalROut = new LinkedHashMap<>();
        for (String t : TYPES) {
            totalLIn.put(t, 0); totalRIn.put(t, 0);
            totalLOut.put(t, 0); totalROut.put(t, 0);
        }

        double totalLeftIPIn = 0, totalRightIPIn = 0, totalLeftIPOut = 0, totalRightIPOut = 0;

        for (Team opponent : teamReportFile.getTeams()) {
            if (opponent.getAbrv().equals(targetTeam.getAbrv())) continue;
            boolean opponentInDivision = opponent.getDivision().equals(targetTeam.getDivision());
            List<com.lineupreporter.domain.Player> opponentPitchers = teamReportFile.getTeamPitchers(opponent.getAbrv());
            TeamPitchingProfile profile = TeamPitchingProfile.generateLikelyUsage(opponentPitchers, appConfig);
            profile.buildTeamBalanceCount(opponentPitchers);
            if (opponentInDivision) {
                totalLeftIPIn += addToTotal(totalLIn, profile.getLeftyPitcherBalanceData());
                totalRightIPIn += addToTotal(totalRIn, profile.getRightyPitcherBalanceData());
            } else {
                totalLeftIPOut += addToTotal(totalLOut, profile.getLeftyPitcherBalanceData());
                totalRightIPOut += addToTotal(totalROut, profile.getRightyPitcherBalanceData());
            }
        }

        int totalStarterIP = (int) ((totalLeftIPIn + totalRightIPIn) * overallPctIn + (totalLeftIPOut + totalRightIPOut) * overallPctOut);

        Map<String, Integer> estLIn = new LinkedHashMap<>();
        Map<String, Integer> estRIn = new LinkedHashMap<>();
        Map<String, Integer> estLOut = new LinkedHashMap<>();
        Map<String, Integer> estROut = new LinkedHashMap<>();
        for (String type : TYPES) {
            estLIn.put(type, CalculateColumnUtil.calculateColumn(totalLIn.getOrDefault(type, 0), overallPctIn, totalStarterIP));
            estRIn.put(type, CalculateColumnUtil.calculateColumn(totalRIn.getOrDefault(type, 0), overallPctIn, totalStarterIP));
            estLOut.put(type, CalculateColumnUtil.calculateColumn(totalLOut.getOrDefault(type, 0), overallPctOut, totalStarterIP));
            estROut.put(type, CalculateColumnUtil.calculateColumn(totalROut.getOrDefault(type, 0), overallPctOut, totalStarterIP));
        }

        Map<Integer, Integer> balanceLefties = new LinkedHashMap<>();
        Map<Integer, Integer> balanceRighties = new LinkedHashMap<>();
        int rowCount = 1;
        for (String type : TYPES) {
            int ipL = estLIn.get(type) + estLOut.get(type);
            int ipR = estRIn.get(type) + estROut.get(type);
            if (createRowFunc != null) {
                createRowFunc.createRow(rowCount, type,
                    totalLIn.get(type) + totalLOut.get(type), ipL,
                    totalRIn.get(type) + totalROut.get(type), ipR);
            }
            balanceLefties.put(rowCount - 1, ipL);
            balanceRighties.put(rowCount - 1, ipR);
            rowCount++;
        }

        List<Map<Integer, Integer>> result = new ArrayList<>();
        result.add(balanceLefties);
        result.add(balanceRighties);
        return result;
    }

    private static int addToTotal(Map<String, Integer> total, Map<String, Integer> teamData) {
        int sum = 0;
        for (Map.Entry<String, Integer> e : teamData.entrySet()) {
            total.merge(e.getKey(), e.getValue(), Integer::sum);
            sum += e.getValue();
        }
        return sum;
    }

    @FunctionalInterface
    public interface RowCallback {
        void createRow(int rowCount, String type, int totalL, int estL, int totalR, int estR);
    }
}
