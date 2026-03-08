package com.lineupreporter.domain;

public class LineupData {

    private long id;
    private String pitcherArm;
    private LineupBalanceItem balanceItemTo;
    private LineupBalanceItem balanceItemFrom;
    private int estimatedAtBats;

    public LineupData() {}

    public LineupData(long id, String arm, LineupBalanceItem to, LineupBalanceItem from, int atBats) {
        this.id = id;
        this.pitcherArm = arm;
        this.balanceItemTo = to;
        this.balanceItemFrom = from;
        this.estimatedAtBats = atBats;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPitcherArm() { return pitcherArm; }
    public void setPitcherArm(String pitcherArm) { this.pitcherArm = pitcherArm; }

    public LineupBalanceItem getBalanceItemTo() { return balanceItemTo; }
    public void setBalanceItemTo(LineupBalanceItem balanceItemTo) { this.balanceItemTo = balanceItemTo; }

    public LineupBalanceItem getBalanceItemFrom() { return balanceItemFrom; }
    public void setBalanceItemFrom(LineupBalanceItem balanceItemFrom) { this.balanceItemFrom = balanceItemFrom; }

    public int getEstimatedAtBats() { return estimatedAtBats; }
    public void setEstimatedAtBats(int estimatedAtBats) { this.estimatedAtBats = estimatedAtBats; }

    @Override
    public String toString() {
        if ("X".equals(pitcherArm)) return "";
        return pitcherArm + " " + balanceItemFrom + "-" + balanceItemTo;
    }
}
