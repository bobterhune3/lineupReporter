package com.lineupreporter.service;

import com.lineupreporter.config.AppConfig;
import com.lineupreporter.domain.Defense;
import com.lineupreporter.domain.Player;
import com.lineupreporter.domain.Team;
import com.lineupreporter.domain.TeamLineup;
import com.lineupreporter.util.RecordIndex;
import com.lineupreporter.util.TeamUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Strat-O-Matic League Roster Report (PRT) files.
 */
@Service
@Slf4j
public class SomTeamReportFile {

    private static final String[] BALANCE_TYPES = {
        "9L", "8L", "7L", "6L", "5L", "4L", "3L", "2L", "1L", "E",
        "1R", "2R", "3R", "4R", "5R", "6R", "7R", "8R", "9R"
    };

    private final AppConfig config;

    private String lastError = "";
    private List<Team> teams = new ArrayList<>();
    private Map<String, List<Player>> pitcherDataByBalance = new LinkedHashMap<>();
    private Map<Team, List<Player>> pitcherDataByTeam = new LinkedHashMap<>();
    private Map<Team, List<Player>> batterDataByTeam = new LinkedHashMap<>();

    private static final Pattern REGEX_TEAM = Pattern.compile("^([0-9]+) (.{1,16}).*");
    private static final Pattern REGEX_PITCHER_LINE = Pattern.compile("^(.{1,22}) [0-9]+ +[0-9]+ +[0-9]+ +([A-Z]+) +([RL]) {1,6}([0-9 ])     ([0-9 ])      ([0-6N])");
    private static final Pattern REGEX_PITCHER_BAL = Pattern.compile("^(.{1,17})[0-9]+ +([0-9ERL]+) +[0-9]+ +[0-9]+ +[0-9.]+ +([0-9]+) +([0-9]+) +([0-9]+) +[0-9]+ +[0-9]+ +([0-9]+) +([0-9]+)");
    private static final Pattern REGEX_BATTER_LINE = Pattern.compile("^(.{1,17}) +[0-9]+ +[0-9] +[0-9]+ +([A-Z]+) +([0-9ERL]+) +([0-9]+)");
    private static final Pattern REGEX_BATTER_BALANCE = Pattern.compile("^(.{1,16}) +[0-9]+ +([NW])/([NW]) +([LRS])");

    private boolean isTeamSorted = false;

    public SomTeamReportFile(AppConfig config) {
        this.config = config;
        for (String type : BALANCE_TYPES) {
            pitcherDataByBalance.put(type, new ArrayList<>());
        }
    }

    public String getLastError() {
        return lastError;
    }

    public void parse(String filePath, Map<String, TeamLineup> storedLineups) throws IOException {
        lastError = "";
        if (filePath == null || !Files.exists(java.nio.file.Path.of(filePath))) {
            lastError = "Unable to find the League Roster Report file at " + filePath + ".\n" +
                "To create the file:\n1. Select any team in your league\n2. From the Team menu select Display Reports\n" +
                "3. Select the Roster Report\n4. Select Each Team\n5. Save the file (Print to File).";
            return;
        }
        List<String> lines = readFileLinesOnly(filePath, true);
        organizeDataByTeam(lines, storedLineups);
        removeNonStartingPitchers(pitcherDataByTeam);
        pitcherDataByBalance = organizePitcherByBalance(pitcherDataByTeam);
    }

    public List<Team> getTeams() {
        if(!isTeamSorted) {
            teams.sort(Comparator.comparing(Team::getName, Comparator.nullsFirst(String::compareTo)));
            isTeamSorted = true;
        }
        return new ArrayList<>(teams);
    }

    public Map<String, List<Player>> getBalanceData() {
        return pitcherDataByBalance;
    }

    public List<Player> getTeamPitchers(String teamAbv) {
        Team team = lookupTeam(teamAbv);
        return team != null ? pitcherDataByTeam.getOrDefault(team, List.of()) : List.of();
    }

    public List<Player> getTeamBatters(String teamAbv) {
        Team team = lookupTeam(teamAbv);
        return team != null ? batterDataByTeam.getOrDefault(team, List.of()) : List.of();
    }

    public int getTotalStarterIP(Team excludeTeam) {
        int total = 0;
        for (List<Player> list : pitcherDataByBalance.values()) {
            for (Player p : list) {
                if (excludeTeam == null || !excludeTeam.getAbrv().equals(p.getTeam().getAbrv())) {
                    if (p.getGs() > 1) total += p.getIp();
                }
            }
        }
        return total;
    }

    public int getTotalPitcherIP() {
        int total = 0;
        for (List<Player> list : pitcherDataByBalance.values()) {
            for (Player p : list) total += p.getIp();
        }
        return total;
    }

    public Map<String, Integer> getTeamBalanceCount(String pitcherArm, List<Player> pitchers) {
        Map<String, Integer> workingBalance = new LinkedHashMap<>();
        for (String type : BALANCE_TYPES) workingBalance.put(type, 0);
        for (Player pitcher : pitchers) {
            if (pitcher.getThrowsArm() != null && pitcher.getThrowsArm().equals(pitcherArm) && pitcher.getGs() > 3) {
                String bal = pitcher.getBal();
                workingBalance.merge(bal, pitcher.getIp(), Integer::sum);
            }
        }
        return workingBalance;
    }

    public static int calculateAtBatsByLineup(Map<Integer, Integer> stats, com.lineupreporter.domain.LineupData lineup) {
        if (stats == null || stats.isEmpty()) return 0;
        int fromLevel = lineup.getBalanceItemFrom().getValue();
        int toLevel = lineup.getBalanceItemTo().getValue();
        int total = 0;
        for (int i = fromLevel; i <= toLevel; i++) {
            total += stats.getOrDefault(i, 0);
        }
        return total;
    }

    private List<String> readFileLinesOnly(String filePath, boolean cleanup) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String line : Files.readAllLines(java.nio.file.Path.of(filePath))) {
            if (cleanup) line = cleanUpLine(line);
            if (line.trim().isEmpty()) continue;
            lines.add(line);
        }
        return lines;
    }

    private static String cleanUpLine(String line) {
        line = line.replace("[1]", "").replace("[2]", "").replace("[3]", "").replace("[4]", "");
        return line.trim();
    }

    private void organizeDataByTeam(List<String> lines, Map<String, TeamLineup> database) {
        teams = new ArrayList<>();
        pitcherDataByTeam = new LinkedHashMap<>();
        batterDataByTeam = new LinkedHashMap<>();

        boolean inPitcherSection = false, inPitcherBalanceSection = false;
        boolean inBatterSection = false, inBatterBalanceSection = false, inDefenseSection = false;
        int pitcherIndex = 0, batterIndex = 0;
        Team team = null;
        List<Player> pitchers = new ArrayList<>();
        List<Player> batters = new ArrayList<>();

        for (String line : lines) {
            if (REGEX_TEAM.matcher(line).matches()) {
                String teamName = parseTeamNameLine(line);
                if (teamName != null) {
                    inPitcherBalanceSection = false;
                    inPitcherSection = false;
                    if (team != null) {
                        pitcherDataByTeam.put(team, pitchers);
                        batterDataByTeam.put(team, batters);
                    }
                    pitchers = new ArrayList<>();
                    batters = new ArrayList<>();
                    pitcherIndex = 0;
                    batterIndex = 0;
                    team = new Team(RecordIndex.getNextId(RecordIndex.IndexType.TeamId), "", 0);
                    teams.add(team);
                    team.setName(teamName);
                    team.setAbrv(TeamUtils.prettyTeamName(teamName));
                    if ("UNK".equals(team.getAbrv())) {
                        log.warn("Abbreviation mapping for team {} not found.", team.getName());
                    }
                }
            }

            if (!inBatterSection) inBatterSection = line.contains("CODES YEAR TEAM BAL  AB DO TR HR BAVG  BB K'S RBI  OB%");
            if (!inBatterBalanceSection) {
                inBatterBalanceSection = line.contains("LEFT% POWER BAT STEAL BUNT H&R RUN");
                if (inBatterBalanceSection) inBatterSection = false;
            }
            if (!inPitcherBalanceSection) {
                inPitcherBalanceSection = line.contains("LEFT% BAL WON LOST  ERA");
                if (inPitcherBalanceSection) inPitcherSection = false;
            }
            if (!inPitcherSection) {
                inPitcherSection = line.contains("CODES YEAR TEAM  THROWS START RELIEF CLOSER");
                if (inPitcherSection) inDefenseSection = false;
            }
            if (!inDefenseSection) {
                inDefenseSection = line.contains("Catcher       1b   2b   3b   ss   lf   cf   rf   ARM");
                if (inDefenseSection) {
                    batterIndex = 0;
                    inBatterBalanceSection = false;
                }
            }

            if (inBatterSection && team != null) {
                Matcher m = REGEX_BATTER_LINE.matcher(line);
                if (m.find() && m.groupCount() >= 4) {
                    Player player = new Player();
                    player.setName(m.group(1).trim());
                    player.setHitter(true);
                    player.setBal(m.group(3).trim());
                    float val = 0f;
                    try { val = Float.parseFloat(m.group(4).trim()); } catch (NumberFormatException ignored) {}
                    player.setActual((int) (val * config.getABMultiplier()));
                    player.setId(lookupOrBuildPlayerId(player, team, database, true, batters.size()));
                    batters.add(player);
                }
            } else if (inBatterBalanceSection && team != null && batterIndex < batters.size()) {
                Matcher m = REGEX_BATTER_BALANCE.matcher(line);
                if (m.find() && m.groupCount() >= 4) {
                    Player player = batters.get(batterIndex);
                    String name = m.group(1).trim();
                    if (player.getName().length() > 16) {
                        int firstSpace = player.getName().indexOf(' ') + 1;
                        int lastNameLen = player.getName().length() - firstSpace;
                        name = name.substring(0, Math.min(name.length(), lastNameLen));
                    }
                    if (!player.getName().endsWith(name)) {
                        log.warn("Batter name mismatch: {} != {}", player.getName(), name);
                        batterIndex++;
                        continue;
                    }
                    player.setPowerL(m.group(2).trim());
                    player.setPowerR(m.group(3).trim());
                    player.setThrowsArm(m.group(4).trim());
                    player.setId(lookupOrBuildPlayerId(player, team, database, true, batterIndex));
                    batterIndex++;
                }
            } else if (inPitcherSection && team != null) {
                Matcher m = REGEX_PITCHER_LINE.matcher(line);
                if (m.find() && m.groupCount() >= 6) {
                    Player player = new Player();
                    player.setName(m.group(1).trim());
                    if (team.getAbrv() == null || team.getAbrv().isEmpty())
                        team.setAbrv(m.group(2).trim());
                    player.setTeam(team);
                    player.setHitter(false);
                    player.setThrowsArm(m.group(3).trim());
                    String sp = m.group(4).trim();
                    if (!sp.isEmpty()) player.setGames(Integer.parseInt(sp));
                    try { player.setReliefRating(Integer.parseInt(m.group(5).trim())); } catch (NumberFormatException ignored) {}
                    try { player.setCloserRating(Integer.parseInt(m.group(6).trim())); } catch (NumberFormatException e) { player.setCloserRating(-1); }
                    player.setId(lookupOrBuildPlayerId(player, team, database, false, pitchers.size()));
                    pitchers.add(player);
                }
            } else if (inPitcherBalanceSection && team != null && pitcherIndex < pitchers.size()) {
                Matcher m = REGEX_PITCHER_BAL.matcher(line);
                if (m.find() && m.groupCount() >= 7) {
                    Player player = pitchers.get(pitcherIndex);
                    String name = m.group(1).trim();
                    if (!player.getName().endsWith(name)) {
                        log.warn("Pitcher name mismatch: {} != {}", player.getName(), name);
                        pitcherIndex++;
                        continue;
                    }
                    player.setBal(m.group(2).trim());
                    player.setIp(Integer.parseInt(m.group(3).trim()));
                    player.setHits(Integer.parseInt(m.group(4).trim()));
                    player.setBb(Integer.parseInt(m.group(5).trim()));
                    player.setGs(Integer.parseInt(m.group(6).trim()));
                    player.setSave(Integer.parseInt(m.group(7).trim()));
                    pitcherIndex++;
                }
            } else if (inDefenseSection && team != null && !line.startsWith("Catcher")) {
                if (batterIndex < batters.size()) {
                    String name = line.length() >= 16 ? line.substring(0, 16).trim() : "";
                    String catcher = line.length() >= 29 ? line.substring(16, 29).trim() : "";
                    if (catcher.contains("t")) catcher = catcher.substring(0, catcher.indexOf('t'));
                    String first = line.length() >= 34 ? line.substring(30, 34).trim() : "";
                    String second = line.length() >= 39 ? line.substring(35, 39).trim() : "";
                    String third = line.length() >= 44 ? line.substring(40, 44).trim() : "";
                    String ss = line.length() >= 49 ? line.substring(45, 49).trim() : "";
                    String left = line.length() >= 54 ? line.substring(50, 54).trim() : "";
                    String center = line.length() >= 59 ? line.substring(55, 59).trim() : "";
                    String right = line.length() >= 64 ? line.substring(60, 64).trim() : "";
                    String ofArm = line.length() >= 69 ? line.substring(65, 69).trim() : "";
                    Player player = batters.get(batterIndex);
                    if (player.getName().endsWith(name) ||
                        (name.length() > 1 && player.getName().endsWith(name.substring(0, name.length() - 1))) ||
                        (name.length() > 2 && player.getName().endsWith(name.substring(0, name.length() - 2))) ||
                        (name.length() > 3 && player.getName().endsWith(name.substring(0, name.length() - 3))) ||
                        (name.length() > 4 && player.getName().endsWith(name.substring(0, name.length() - 4)))) {
                        Defense def = new Defense(RecordIndex.getNextId(RecordIndex.IndexType.DefenseId),
                            catcher, first, second, third, ss, left, center, right, ofArm);
                        player.setDef(def);
                    } else {
                        log.warn("Defense name mismatch: {} != {}", player.getName(), name);
                    }
                    batterIndex++;
                }
            }
        }
        if (team != null) {
            pitcherDataByTeam.put(team, pitchers);
            batterDataByTeam.put(team, batters);
        }
    }

    private static String parseTeamNameLine(String line) {
        Matcher m = REGEX_TEAM.matcher(line);
        if (m.find() && m.groupCount() >= 2) return m.group(2).trim();
        return null;
    }

    private void removeNonStartingPitchers(Map<Team, List<Player>> data) {
        for (List<Player> pitchers : data.values()) {
            pitchers.removeIf(p -> p.getReliefRating() == 0);
        }
    }

    private static Map<String, List<Player>> organizePitcherByBalance(Map<Team, List<Player>> data) {
        Map<String, List<Player>> balance = new LinkedHashMap<>();
        for (String type : BALANCE_TYPES) balance.put(type, new ArrayList<>());
        for (List<Player> pitchers : data.values()) {
            for (Player p : pitchers) {
                String bal = p.getBal();
                balance.computeIfAbsent(bal, k -> new ArrayList<>()).add(p);
            }
        }
        return balance;
    }

    /** Build a persistent string id: team_name_position_hr_index (e.g. ARI_John_Smith_B_9L_0, ARI_John_Smith_P_R_0). */
    private static String buildBatterId(String teamAbrv, String name, String balance, int index) {
        String t = (teamAbrv != null && !teamAbrv.isEmpty()) ? teamAbrv : "UNK";
        String n = sanitizePlayerName(name != null ? name : "");
        String b = (balance != null && !balance.isEmpty()) ? balance : "E";
        return t + "_" + n + "_B_" + b + "_" + index;
    }

    private static String buildPitcherId(String teamAbrv, String name, String throwsArm, int index) {
        String t = (teamAbrv != null && !teamAbrv.isEmpty()) ? teamAbrv : "UNK";
        String n = sanitizePlayerName(name != null ? name : "");
        String th = (throwsArm != null && !throwsArm.isEmpty()) ? throwsArm : "R";
        return t + "_" + n + "_P_" + th + "_" + index;
    }

    private static String sanitizePlayerName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9_]", "");
    }

    private String lookupOrBuildPlayerId(Player player, Team team, Map<String, TeamLineup> database, boolean hitter, int index) {
        if (database != null && team != null) {
            TeamLineup lineup = database.get(team.getAbrv());
            if (lineup != null && lineup.getPlayerByGRID() != null) {
                for (Player lp : lineup.getPlayerByGRID()) {
                    if (lp.equals(player) && lp.getId() != null && !lp.getId().isEmpty()) return lp.getId();
                }
            }
        }
        String teamAbrv = team != null ? team.getAbrv() : "UNK";
        if (hitter) {
            return buildBatterId(teamAbrv, player.getName(), player.getBal(), index);
        }
        return buildPitcherId(teamAbrv, player.getName(), player.getThrowsArm(), index);
    }

    private Team lookupTeam(String teamAbv) {
        for (Team t : teams) {
            if (t.getAbrv() != null && t.getAbrv().equals(teamAbv)) return t;
        }
        return null;
    }
}
