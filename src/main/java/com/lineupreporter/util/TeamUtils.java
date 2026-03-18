package com.lineupreporter.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps full team names from roster report to standard abbreviations.
 * Based on Strat-O-Matic style team names. Supports SOM 12-char prefix mapping.
 */
public final class TeamUtils {

    private static Map<String, String> teamAbrvMap = null;

//    private static final Map<String, String> PRETTY_MAP = new HashMap<>();
//
//    static {
//        // MLB-style abbreviations
//        PRETTY_MAP.put("YANKEES", "NYY");
//        PRETTY_MAP.put("RED SOX", "BOS");
//        PRETTY_MAP.put("ORIOLES", "BAL");
//        PRETTY_MAP.put("BLUE JAYS", "TOR");
//        PRETTY_MAP.put("RAYS", "TB");
//        PRETTY_MAP.put("GUARDIANS", "CLE");
//        PRETTY_MAP.put("WHITE SOX", "CWS");
//        PRETTY_MAP.put("TIGERS", "DET");
//        PRETTY_MAP.put("TWINS", "MIN");
//        PRETTY_MAP.put("ROYALS", "KC");
//        PRETTY_MAP.put("ASTROS", "HOU");
//        PRETTY_MAP.put("ANGELS", "LAA");
//        PRETTY_MAP.put("ATHLETICS", "OAK");
//        PRETTY_MAP.put("MARINERS", "SEA");
//        PRETTY_MAP.put("RANGERS", "TEX");
//        PRETTY_MAP.put("BRAVES", "ATL");
//        PRETTY_MAP.put("MARLINS", "MIA");
//        PRETTY_MAP.put("METS", "NYM");
//        PRETTY_MAP.put("PHILLIES", "PHI");
//        PRETTY_MAP.put("NATIONALS", "WSH");
//        PRETTY_MAP.put("CUBS", "CHC");
//        PRETTY_MAP.put("REDS", "CIN");
//        PRETTY_MAP.put("BREWERS", "MIL");
//        PRETTY_MAP.put("PIRATES", "PIT");
//        PRETTY_MAP.put("CARDINALS", "STL");
//        PRETTY_MAP.put("DIAMONDBACKS", "ARI");
//        PRETTY_MAP.put("ROCKIES", "COL");
//        PRETTY_MAP.put("DODGERS", "LAD");
//        PRETTY_MAP.put("PADRES", "SD");
//        PRETTY_MAP.put("GIANTS", "SF");
//    }

    public static void registerTeamAbvMapping(Map<String, String> map) {
        teamAbrvMap = map == null ? null : new HashMap<>(map);
    }

    /** SOM: map by first 12 chars of team name when a mapping is registered. */
    public static String prettyTeamName(String fullName) {
        if (fullName == null) return "UNK";
        return prettyTeamNoDiceName(fullName);
    }

    /** SOM "No Dice" style: 3-letter abbreviations from truncated team names. */
    public static String prettyTeamNoDiceName(String teamName) {
        if (teamName == null) return "UNK";
        if (teamName.length() == 3) return teamName;
        if (teamName.startsWith("Anaheim Ang")) return "ANM";
        if (teamName.startsWith("Arizona Diam")) return "AZB";
        if (teamName.startsWith("Atlanta Brav")) return "ATM";
        if (teamName.startsWith("Baltimore Or")) return "BLN";
        if (teamName.startsWith("Boston Red S")) return "BSM";
        if (teamName.startsWith("Chicago Cub")) return "CHB";
        if (teamName.startsWith("Pittsburgh P")) return "PTN";
        if (teamName.startsWith("Cincinnati R")) return "CNN";
        if (teamName.startsWith("Cleveland Gu")) return "CLM";
        if (teamName.startsWith("Detroit Tige")) return "DTB";
        if (teamName.startsWith("Houston Astr")) return "HSN";
        if (teamName.startsWith("Kansas City")) return "KCM";
        if (teamName.startsWith("Los Angeles")) return "LAN";
        if (teamName.startsWith("Miami Fins")) return "MIA";
        if (teamName.startsWith("Milwaukee Br")) return "MLG";
        if (teamName.startsWith("Minnesota Tw")) return "MNB";
        if (teamName.startsWith("New York Yan")) return "NYB";
        if (teamName.startsWith("Oakland Athl")) return "OKM";
        if (teamName.startsWith("Philadelphia")) return "PHN";
        if (teamName.startsWith("Pittsburgh")) return "PTN";
        if (teamName.startsWith("San Diego Pa")) return "SDG";
        if (teamName.startsWith("San Francisc")) return "SFN";
        if (teamName.startsWith("Seattle Mari")) return "SEG";
        if (teamName.startsWith("St. Louis Ca")) return "SLB";
        if (teamName.startsWith("Tampa Bay Ra")) return "TBM";
        if (teamName.startsWith("Tampa Bay De")) return "TBM";
        if (teamName.startsWith("Texas Ranger")) return "TXG";
        if (teamName.startsWith("Toronto Blue")) return "TOG";
        if (teamName.startsWith("Washington N")) return "WSG";
        return "UNK";
    }
//
//    /** Legacy: same as prettyTeamName when no mapping is set. */
//    public static String prettyTeamNameLegacy(String fullName) {
//        if (fullName == null) return "UNK";
//        String normalized = fullName.toUpperCase().trim();
//        return PRETTY_MAP.getOrDefault(normalized, normalized.length() >= 3 ? normalized.substring(0, 3) : "UNK");
//    }

    private TeamUtils() {}
}
