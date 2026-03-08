package com.lineupreporter.domain;

import java.util.Objects;

import com.lineupreporter.util.ReportUtil;

public class Team {

    private long id;
    private String name;
    private String abrv;
    private String division = "";
    private String league = "";
    private String fullDiv = "";
    private String owner = "";
    private int wins;
    private int loses;
    private int winsPrevious;
    private int losesPrevious;
    private double gb;
    private double gbPrevious;
    private int runsScored;
    private int runsAllowed;
    private double average;
    private double era;
    private double ip;
    private double homeruns;
    private double homerunsAllowed;
    private double pythagoreanTheorem;
    private int divisionPositionCurrent = -1;
    private int divisionPositionPrevious = -1;
    private int draftPickPositionCurrent = -1;
    private int draftPickPositionPrevious = -1;
    private int wildCardPositionCurrent = -1;
    private int wildCardPositionPrevious = -1;

//    private final List<Game> lineScores = new ArrayList<>();

    public static final String BASELINE_TEAM = "DTB";

    public Team() {}

    public Team(long id, String name, int wins) {
        this.id = id;
        this.name = name;
        this.wins = wins;
    }

    /**
     * Creates a team for SOM report parsing. div is "LE division" or division only;
     * if leagueNameLength &gt; 0, league is parsed from div.
     */
    public static Team forReport(long id, String div, int leagueNameLength) {
        Team t = new Team();
        t.setId(id);
        if (leagueNameLength > 0 && div != null) {
            String leagueVal = div.length() >= 2 ? div.substring(0, 2) : "";
            if ("NE".equals(leagueVal)) leagueVal = "NL";
            t.setLeague(leagueVal);
            t.setDivision(div.length() > 3 ? div.substring(3) : "");
        } else {
            t.setDivision(div != null ? div : "");
        }
        t.setFullDiv(div != null ? div : "");
        return t;
    }

//    public void addLineScore(int scoreUs, String opponentAbv, int scoreThem, boolean homeTeam) {
//        lineScores.add(0, new Game(scoreUs, opponentAbv, scoreThem, homeTeam));
//    }
//
//    public List<Game> getLineScores() { return lineScores; }
//
//    public List<Game> getLastGames(int gamesToReport) {
//        int n = Math.min(gamesToReport, lineScores.size());
//        return new ArrayList<>(lineScores.subList(0, n));
//    }
//
//    public int getGamesPlayed() { return wins + loses; }
//
//    public double getWpct() {
//        int w = wins, l = loses;
//        if (w + l == 0) return 0.0;
//        return ReportUtil.roundToSignificantDigits((double) w / (w + l), 3);
//    }
//
//    public String getRecordLastRun() {
//        int w = wins - winsPrevious;
//        int l = loses - losesPrevious;
//        return w + "-" + l;
//    }

    public void calculatePythagoreanTheorem() {
        if (runsAllowed > 0 && runsScored > 0) {
            pythagoreanTheorem = (double) (runsScored * runsScored)
                / (runsScored * runsScored + runsAllowed * runsAllowed);
        }
    }

//    public double calculateGamesBehind(Team leader) {
//        double a1 = (double) leader.getWins() - this.getWins();
//        double a2 = (double) this.getLoses() - leader.getLoses();
//        return (a1 + a2) / 2.0;
//    }

    public String buildStorageData() {
        return String.format("Wins=%d|Loses=%d|GB=%s|DIVPos=%d|DPickPos=%d|WCardPos=%d",
            wins, loses, Double.toString(gb), divisionPositionCurrent, draftPickPositionCurrent, wildCardPositionCurrent);
    }

//    public String getManager() {
//        return (abrv != null && abrv.length() >= 3) ? abrv.substring(2, 3) : "";
//    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        if (name != null && name.contains("  "))
            this.name = name.substring(0, name.indexOf("  "));
    }

    public String getAbrv() { return abrv; }
    public void setAbrv(String abrv) {
        this.abrv = abrv;
        if (abrv != null && abrv.length() >= 3) owner = abrv.substring(2, 3);
    }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division != null ? division : ""; }

    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league != null ? league : ""; }

    public String getFullDiv() { return fullDiv; }
    public void setFullDiv(String fullDiv) { this.fullDiv = fullDiv != null ? fullDiv : ""; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLoses() { return loses; }
    public void setLoses(int loses) { this.loses = loses; }

    public int getWinsPrevious() { return winsPrevious; }
    public void setWinsPrevious(int winsPrevious) { this.winsPrevious = winsPrevious; }

    public int getLosesPrevious() { return losesPrevious; }
    public void setLosesPrevious(int losesPrevious) { this.losesPrevious = losesPrevious; }

    public double getGb() { return gb; }
    public void setGb(double gb) { this.gb = gb; }

    public double getGbPrevious() { return gbPrevious; }
    public void setGbPrevious(double gbPrevious) { this.gbPrevious = gbPrevious; }

    public int getRunsScored() { return runsScored; }
    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
        calculatePythagoreanTheorem();
    }

    public int getRunsAllowed() { return runsAllowed; }
    public void setRunsAllowed(int runsAllowed) {
        this.runsAllowed = runsAllowed;
        calculatePythagoreanTheorem();
    }

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }

    public double getEra() { return era; }
    public void setEra(double era) { this.era = era; }

    public double getIp() { return ip; }
    public void setIp(double ip) { this.ip = ip; }

    public double getHomeruns() { return homeruns; }
    public void setHomeruns(double homeruns) { this.homeruns = homeruns; }

    public double getHomerunsAllowed() { return homerunsAllowed; }
    public void setHomerunsAllowed(double homerunsAllowed) { this.homerunsAllowed = homerunsAllowed; }

    public double getPythagoreanTheorem() {
        return ReportUtil.roundToSignificantDigits(pythagoreanTheorem, 3);
    }

    public int getDivisionPositionCurrent() { return divisionPositionCurrent; }
    public void setDivisionPositionCurrent(int v) { this.divisionPositionCurrent = v; }

    public int getDivisionPositionPrevious() { return divisionPositionPrevious; }
    public void setDivisionPositionPrevious(int v) { this.divisionPositionPrevious = v; }

    public int getDraftPickPositionCurrent() { return draftPickPositionCurrent; }
    public void setDraftPickPositionCurrent(int v) { this.draftPickPositionCurrent = v; }

    public int getDraftPickPositionPrevious() { return draftPickPositionPrevious; }
    public void setDraftPickPositionPrevious(int v) { this.draftPickPositionPrevious = v; }

    public int getWildCardPositionCurrent() { return wildCardPositionCurrent; }
    public void setWildCardPositionCurrent(int v) { this.wildCardPositionCurrent = v; }

    public int getWildCardPositionPrevious() { return wildCardPositionPrevious; }
    public void setWildCardPositionPrevious(int v) { this.wildCardPositionPrevious = v; }

    @Override
    public String toString() {
        return abrv != null ? abrv : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(abrv, team.abrv);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(abrv);
    }
}
