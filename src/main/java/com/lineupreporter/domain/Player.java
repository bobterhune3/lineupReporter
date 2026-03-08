package com.lineupreporter.domain;

import java.util.Comparator;

import com.lineupreporter.util.ReportUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Player implements Comparable<Player> {

    private String id;
    private String name;
    private Team team;
    private boolean hitter;
    private String bal;           // balance e.g. "9L", "E", "3R"
    private int actual;           // at-bats (for hitters)
    private String throwsArm;     // "L" or "R"
    private int games;            // SP rating
    private int reliefRating;
    private int closerRating = -1;
    private int ip;               // innings pitched
    private int hits;
    private int bb;
    private int gs;               // games started
    private int save;
    private String powerL;
    private String powerR;
    private Defense def;
    private String primaryPos = "SP"; // SP, RP, CL
    private int replay;
    private int previousReplay;
    private int targetUsage;
    private int teamRank;
    private int usage;


    /** Usage = Replay/Actual (for SOM comparison reports). */
    public double getUsage() {
        int a = actual;
        if (a == 0) a = 1;
        return ReportUtil.roundToSignificantDigits((double) replay / a, 3);
    }

    public String buildStorageData() {
        return "PreviousReplay=" + replay;
    }

    @Override
    public int compareTo(Player o) {
        return Comparator
            .comparing(Player::getTeam, Comparator.nullsFirst(
                Comparator.comparing(Team::getAbrv, Comparator.nullsFirst(String::compareTo))))
            .thenComparing(Player::getName, Comparator.nullsFirst(String::compareTo))
            .compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return java.util.Objects.equals(name, player.name) &&
               team != null && player.team != null && java.util.Objects.equals(team.getAbrv(), player.team.getAbrv()) &&
               hitter == player.hitter;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, team != null ? team.getAbrv() : null, hitter);
    }
}
