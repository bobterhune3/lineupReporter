package com.lineupreporter.domain;

import java.util.Objects;

public class LineupBalanceItem implements Comparable<LineupBalanceItem> {

    private long id;
    private int value;        // index 0-18
    private int balanceLevel; // 0=E, 1-9 L/R
    private String balanceArm; // "", "L", "R"

    public LineupBalanceItem() {}

    public LineupBalanceItem(int index, int level, String arm) {
        this.value = index;
        this.balanceLevel = level;
        this.balanceArm = arm != null ? arm : "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public int getBalanceLevel() { return balanceLevel; }
    public void setBalanceLevel(int balanceLevel) { this.balanceLevel = balanceLevel; }

    public String getBalanceArm() { return balanceArm; }
    public void setBalanceArm(String balanceArm) { this.balanceArm = balanceArm != null ? balanceArm : ""; }

    @Override
    public String toString() {
        return (balanceLevel == 0 ? "E" : String.valueOf(balanceLevel)) + (balanceLevel == 0 ? "" : balanceArm);
    }

    public static int comparison(LineupBalanceItem a, LineupBalanceItem b) {
        return Integer.compare(a.value, b.value);
    }

    @Override
    public int compareTo(LineupBalanceItem o) {
        return comparison(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineupBalanceItem that = (LineupBalanceItem) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
