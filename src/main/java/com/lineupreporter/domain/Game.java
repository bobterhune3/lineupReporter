package com.lineupreporter.domain;

/**
 * Single game line score (ported from somReportUtils Team.Game).
 */
public class Game {
    private final int scoreUs;
    private final String opponentAbv;
    private final int scoreThem;
    private final boolean homeTeam;

    public Game(int scoreUs, String opponentAbv, int scoreThem, boolean homeTeam) {
        this.scoreUs = scoreUs;
        this.opponentAbv = opponentAbv;
        this.scoreThem = scoreThem;
        this.homeTeam = homeTeam;
    }

    public boolean isWon() { return scoreUs > scoreThem; }
    public String getOpponent() { return opponentAbv; }
    public boolean isHomeTeam() { return homeTeam; }
    public String getDisplayOpponent() { return (homeTeam ? "" : "@") + opponentAbv; }
}
