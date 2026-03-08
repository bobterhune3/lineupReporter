package com.lineupreporter.service;

import com.lineupreporter.config.AppConfig;
import com.lineupreporter.domain.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TeamPitchingProfile {

    private static final int HIGH_WATER_MARK_SP = 975;
    private static final int HIGH_WATER_MARK_RP = 350;

    private final java.util.Map<String, Integer> teamLBalance = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, Integer> teamRBalance = new java.util.LinkedHashMap<>();

    private final List<Player> starters = new ArrayList<>();
    private final List<Player> relievers = new ArrayList<>();
    private Player closer;

    static {
        for (String t : new String[]{"9L","8L","7L","6L","5L","4L","3L","2L","1L","E","1R","2R","3R","4R","5R","6R","7R","8R","9R"}) {
            // used in buildTeamBalanceCount
        }
    }

    public static TeamPitchingProfile generateLikelyUsage(List<Player> opponentPitchers, AppConfig config) {
        TeamPitchingProfile profile = new TeamPitchingProfile();
        float minIP = config.getMinIPAllowed();
        int starterCount = 0, starterIPCount = 0, closerCount = 0;

        for (Player pitcher : opponentPitchers) {
            if (pitcher.getIp() < minIP) continue;
            String pos = pitcher.getPrimaryPos();
            if (pos == null) pos = "SP";
            if ("SP".equals(pos)) {
                if (starterIPCount < HIGH_WATER_MARK_SP && starterCount < 8) {
                    profile.starters.add(pitcher);
                    starterIPCount += pitcher.getIp();
                } else {
                    pitcher.setPrimaryPos("RP");
                    profile.relievers.add(pitcher);
                }
                starterCount++;
            } else if ("CL".equals(pos)) {
                if (closerCount > 0) {
                    pitcher.setPrimaryPos("RP");
                    profile.relievers.add(pitcher);
                } else {
                    profile.closer = pitcher;
                    closerCount++;
                }
            } else {
                profile.relievers.add(pitcher);
            }
        }
        profile.relievers.sort(Comparator.naturalOrder());
        return profile;
    }

    private TeamPitchingProfile() {
        for (String t : new String[]{"9L","8L","7L","6L","5L","4L","3L","2L","1L","E","1R","2R","3R","4R","5R","6R","7R","8R","9R"}) {
            teamLBalance.put(t, 0);
            teamRBalance.put(t, 0);
        }
    }

    public java.util.Map<String, Integer> getLeftyPitcherBalanceData() { return new java.util.LinkedHashMap<>(teamLBalance); }
    public java.util.Map<String, Integer> getRightyPitcherBalanceData() { return new java.util.LinkedHashMap<>(teamRBalance); }

    public void buildTeamBalanceCount(List<Player> pitchers) {
        for (String t : new String[]{"9L","8L","7L","6L","5L","4L","3L","2L","1L","E","1R","2R","3R","4R","5R","6R","7R","8R","9R"}) {
            teamLBalance.put(t, 0);
            teamRBalance.put(t, 0);
        }
        int index = 1, starterIP = 0;
        for (Player pitcher : starters) {
            String bal = pitcher.getBal();
            if (index < 5) {
                incrementInningsPitched(pitcher, pitcher.getIp(), bal);
            } else {
                if (starterIP > HIGH_WATER_MARK_SP) { index++; continue; }
                if (calculateWHIP(pitcher) < 1.5) {
                    int adj = (int) ((double) pitcher.getIp() / (index - 1));
                    incrementInningsPitched(pitcher, adj, bal);
                }
            }
            starterIP += pitcher.getIp();
            index++;
        }
        if (closer != null) incrementInningsPitched(closer, closer.getIp(), closer.getBal());
        index = 0;
        int reliefIP = 0;
        for (Player pitcher : relievers) {
            if (pitcher == null) continue;
            String bal = pitcher.getBal();
            if (index < 4) {
                incrementInningsPitched(pitcher, pitcher.getIp(), bal);
            } else {
                if (calculateWHIP(pitcher) < 1.5 && reliefIP <= HIGH_WATER_MARK_SP) {
                    int adj = (int) ((double) pitcher.getIp() / (index - 1));
                    incrementInningsPitched(pitcher, adj, bal);
                }
            }
            reliefIP += pitcher.getIp();
            index++;
        }
    }

    private void incrementInningsPitched(Player pitcher, int ip, String bal) {
        if ("L".equals(pitcher.getThrowsArm())) {
            teamLBalance.merge(bal, ip, Integer::sum);
        } else {
            teamRBalance.merge(bal, ip, Integer::sum);
        }
    }

    private static double calculateWHIP(Player p) {
        int ip = p.getIp();
        if (ip == 0) return 0;
        return (double) (p.getHits() + p.getBb()) / ip;
    }

    public List<Player> getStarters() { return starters; }
    public List<Player> getRelievers() { return relievers; }
    public Player getCloser() { return closer; }
}
