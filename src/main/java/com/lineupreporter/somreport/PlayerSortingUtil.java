package com.lineupreporter.somreport;

import com.lineupreporter.domain.Player;

import java.util.Comparator;

/**
 * SOM player ordering: pitchers first (SP, CL, RP), then hitters.
 * Ported from somReportUtils PlayerSortingUtil.
 */
public final class PlayerSortingUtil {

    private static final int PLAYER_IS_BEFORE = -1;
    private static final int EQUAL = 0;
    private static final int PLAYER_IS_AFTER = 1;
    private static final int MINIMUM_TO_BE_SP = 3;
    private static final int MINIMUM_TO_BE_CLOSER = 5;

    public static int calculatePlayerComparedTo(Player player, Player other) {
        if (player.isHitter()) return PLAYER_IS_AFTER;
        if (other.isHitter()) return PLAYER_IS_BEFORE;

        if (player.getIp() < 26) return PLAYER_IS_AFTER;
        if (other.getIp() < 26) return PLAYER_IS_BEFORE;

        if (player.getGs() > MINIMUM_TO_BE_SP && other.getGs() > MINIMUM_TO_BE_SP) {
            return compareTwoStartingPitchers(player, other);
        }
        if (player.getGs() > MINIMUM_TO_BE_SP) return PLAYER_IS_BEFORE;
        if (other.getGs() > MINIMUM_TO_BE_SP) return PLAYER_IS_AFTER;

        if (player.getSave() > MINIMUM_TO_BE_CLOSER && other.getSave() > MINIMUM_TO_BE_CLOSER) {
            return compareTwoClosers(player, other);
        }
        if (player.getSave() > MINIMUM_TO_BE_CLOSER) {
            player.setPrimaryPos("CL");
            return PLAYER_IS_BEFORE;
        }
        if (other.getSave() > MINIMUM_TO_BE_CLOSER) return PLAYER_IS_AFTER;

        return compareTwoReliefPitchers(player, other);
    }

    public static int compareTwoStartingPitchers(Player player, Player other) {
        int pointsPlayer = player.getIp() * 2;
        int pointsOther = other.getIp() * 2;
        pointsPlayer += (int) ((150 - (150 * calculateWHIP(player))) * 3);
        pointsOther += (int) ((150 - (150 * calculateWHIP(other))) * 3);
        pointsPlayer += player.getGs() * 5;
        pointsOther += other.getGs() * 5;
        player.setPrimaryPos("SP");
        other.setPrimaryPos("SP");
        return pointsOther - pointsPlayer;
    }

    public static int compareTwoClosers(Player player, Player other) {
        int pointsPlayer = player.getIp() * 2;
        int pointsOther = other.getIp() * 2;
        pointsPlayer += (int) (150 - (150 * calculateWHIP(player)));
        pointsOther += (int) (150 - (150 * calculateWHIP(other)));
        pointsPlayer += player.getSave() * 5;
        pointsOther += other.getSave() * 5;
        if (pointsPlayer > pointsOther) {
            if (other.getPrimaryPos() == null || "CL".equals(other.getPrimaryPos())) {
                player.setPrimaryPos("CL");
                other.setPrimaryPos("RP");
            } else {
                player.setPrimaryPos("CL");
            }
        }
        return pointsOther - pointsPlayer;
    }

    public static int compareTwoReliefPitchers(Player player, Player other) {
        int pointsPlayer = player.getIp();
        int pointsOther = other.getIp();
        pointsPlayer += (int) (90 - (75 * calculateWHIP(player)));
        pointsOther += (int) (90 - (75 * calculateWHIP(other)));
        player.setPrimaryPos("RP");
        other.setPrimaryPos("RP");
        return pointsOther - pointsPlayer;
    }

    public static double calculateWHIP(Player player) {
        double bb = player.getBb();
        double hit = player.getHits();
        double ip = player.getIp();
        return ip == 0 ? 0 : (bb + hit) / ip;
    }

    /** Comparator for SOM report player order (pitchers first: SP, CL, RP, then hitters). */
    public static Comparator<Player> somOrderComparator() {
        return (a, b) -> calculatePlayerComparedTo(a, b);
    }

    private PlayerSortingUtil() {}
}
