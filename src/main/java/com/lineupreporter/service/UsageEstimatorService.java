package com.lineupreporter.service;

import com.lineupreporter.config.AppConfig;
import com.lineupreporter.domain.*;
import com.lineupreporter.util.LineupTools;
import com.lineupreporter.web.dto.UsageApiDto;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for the Lineup Usage Estimator: balance stats, estimated AB by lineup, batter info.
 */
@Service
public class UsageEstimatorService {

    private static final String[] POSITION_LABELS = { "C", "1B", "2B", "3B", "SS", "LF", "CF", "RF", "DH" };
    private static final int POSITIONS_PER_LINEUP = 9;

    private final LineupEngine lineupEngine;
    private final AppConfig appConfig;

    public UsageEstimatorService(LineupEngine lineupEngine, AppConfig appConfig) {
        this.lineupEngine = lineupEngine;
        this.appConfig = appConfig;
    }

    /**
     * Build balance usage table for a team: BAL, LH IP, Est. LH AB, RH IP, Est. RH AB.
     */
    public List<BalanceStatsRow> getBalanceStats(String teamAbrv) {
        Team team = findTeam(teamAbrv);
        if (team == null) return List.of();

        SomTeamReportFile report = lineupEngine.getTeamReportFile();
        TeamInfo teamInfo = lineupEngine.getTeamLineupData();
        if (teamInfo.getInDivisionGameCount() == 0 && teamInfo.getOutOfDivisionGameCount() == 0) {
            return buildBalanceStatsFromTeamPitchersOnly(teamAbrv);
        }

        AllPitchersScheduleWeightedCalculator calculator =
            new AllPitchersScheduleWeightedCalculator(report, team, appConfig);
        calculator.setInDivision(teamInfo.getInDivisionGameCount());
        calculator.setOutDivision(teamInfo.getOutOfDivisionGameCount());
        calculator.setTargetAtBats(CalculateColumnUtil.targetAtBats);

        List<Map<Integer, Integer>> balanceAtBats = calculator.calculate((rowCount, type, totalL, estL, totalR, estR) -> {});

        List<BalanceStatsRow> rows = new ArrayList<>();
        String[] types = LineupTools.getBalanceTypes();
        for (int i = 0; i < types.length; i++) {
            int ipL = balanceAtBats.get(0).getOrDefault(i, 0);
            int ipR = balanceAtBats.get(1).getOrDefault(i, 0);
            rows.add(new BalanceStatsRow(types[i], ipL, ipR));
        }
        return rows;
    }

    /** When division games not set, use raw team balance counts and scale to target AB. */
    private List<BalanceStatsRow> buildBalanceStatsFromTeamPitchersOnly(String teamAbrv) {
        SomTeamReportFile report = lineupEngine.getTeamReportFile();
        List<Player> pitchers = report.getTeamPitchers(teamAbrv);
        Map<String, Integer> balanceL = report.getTeamBalanceCount("L", pitchers);
        Map<String, Integer> balanceR = report.getTeamBalanceCount("R", pitchers);
        String[] types = LineupTools.getBalanceTypes();
        int totalL = balanceL.values().stream().mapToInt(Integer::intValue).sum();
        int totalR = balanceR.values().stream().mapToInt(Integer::intValue).sum();
        List<BalanceStatsRow> rows = new ArrayList<>();
        for (String type : types) {
            int ipL = balanceL.getOrDefault(type, 0);
            int ipR = balanceR.getOrDefault(type, 0);
            int estL = totalL > 0 ? (int) Math.ceil((double) ipL / totalL * CalculateColumnUtil.targetAtBats) : 0;
            int estR = totalR > 0 ? (int) Math.ceil((double) ipR / totalR * CalculateColumnUtil.targetAtBats) : 0;
            rows.add(new BalanceStatsRow(type, ipL, estL, ipR, estR));
        }
        return rows;
    }

    /**
     * Get stored lineups for team with estimated at-bats computed from current balance stats.
     */
    public List<LineupWithEstimate> getLineupsWithEstimatedAb(String teamAbrv) {
        Team team = findTeam(teamAbrv);
        if (team == null) return List.of();

        List<BalanceStatsRow> balanceStats = getBalanceStats(teamAbrv);
        Map<Integer, Integer> statsL = new LinkedHashMap<>();
        Map<Integer, Integer> statsR = new LinkedHashMap<>();
        String[] types = LineupTools.getBalanceTypes();
        for (int i = 0; i < types.length; i++) {
            statsL.put(i, balanceStats.get(i).getEstLhAb());
            statsR.put(i, balanceStats.get(i).getEstRhAb());
        }

        TeamLineup teamLineup = LineupPersistence.lookupTeamLineup(lineupEngine.getStoredLineups(), team);
        List<LineupWithEstimate> result = new ArrayList<>();
        for (LineupData ld : teamLineup.getLineups()) {
            int estAb = "L".equals(ld.getPitcherArm())
                ? SomTeamReportFile.calculateAtBatsByLineup(statsL, ld)
                : SomTeamReportFile.calculateAtBatsByLineup(statsR, ld);
            result.add(new LineupWithEstimate(ld, estAb));
        }
        return result;
    }

    /**
     * Get batter info: projected AB from grid assignments, actual+buffer, remaining, positions.
     */
    public List<BatterInfoRow> getBatterInfo(String teamAbrv) {
        Team team = findTeam(teamAbrv);
        if (team == null) return List.of();

        List<Player> batters = lineupEngine.getTeamReportFile().getTeamBatters(teamAbrv);
        List<LineupWithEstimate> lineupsWithAb = getLineupsWithEstimatedAb(teamAbrv);
        TeamLineup teamLineup = LineupPersistence.lookupTeamLineup(lineupEngine.getStoredLineups(), team);
        List<Player> playerByGRID = teamLineup.getPlayerByGRID();

        Map<String, Integer> projectedAbByPlayerId = new HashMap<>();
        Map<String, Map<String, Integer>> positionsByPlayerId = new HashMap<>();

        int lineupCount = teamLineup.getLineups().size();
        for (int col = 0; col < lineupCount; col++) {
            int estimatedAb = col < lineupsWithAb.size() ? lineupsWithAb.get(col).getEstimatedAtBats() : 0;
            for (int pos = 0; pos < POSITIONS_PER_LINEUP; pos++) {
                int idx = col * POSITIONS_PER_LINEUP + pos;
                if (idx >= playerByGRID.size()) break;
                Player p = playerByGRID.get(idx);
                if (p == null || p.getName() == null || p.getName().isEmpty()) continue;
                String pid = p.getId() != null ? p.getId() : "";
                if (pid.isEmpty()) continue;
                projectedAbByPlayerId.merge(pid, estimatedAb, Integer::sum);
                positionsByPlayerId
                    .computeIfAbsent(pid, k -> new LinkedHashMap<>())
                    .merge(POSITION_LABELS[pos], 1, Integer::sum);
            }
        }

        int abAddition = appConfig.getAbAddition();
        List<BatterInfoRow> rows = new ArrayList<>();
        List<Player> sorted = new ArrayList<>(batters);
        sorted.sort(Comparator.comparing(
            (Player p) -> lastNameFrom(p.getName()),
            Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(Player::getName, Comparator.nullsFirst(String::compareTo)));
        for (Player p : sorted) {
            String pid = p.getId() != null ? p.getId() : "";
            int projected = pid.isEmpty() ? 0 : projectedAbByPlayerId.getOrDefault(pid, 0);
            int actual = p.getActual();
            float abAdjustment = abAdjustment(actual, abAddition);
            int totalAllowed = actual + (int) abAdjustment;
            int remaining = totalAllowed - projected;
            String posDisplay = "";
            if (!pid.isEmpty() && positionsByPlayerId.containsKey(pid)) {
                posDisplay = positionsByPlayerId.get(pid).entrySet().stream()
                    .map(e -> e.getValue() > 1 ? e.getKey() + "(" + e.getValue() + ")" : e.getKey())
                    .collect(Collectors.joining(" "));
            }
            String defense = formatDefense(p.getDef());
            rows.add(new BatterInfoRow(p.getName(), defense, projected, actual, (int) abAdjustment, remaining, p.getBal(), posDisplay));
        }
        return rows;
    }

    private static String formatDefense(Defense def) {
        if (def == null) return "";
        List<String> parts = new ArrayList<>();
        addDefPart(parts, "C", def.getCatcher());
        addDefPart(parts, "1B", def.getFirst());
        addDefPart(parts, "2B", def.getSecond());
        addDefPart(parts, "3B", def.getThird());
        addDefPart(parts, "SS", def.getShortStop());
        addDefPart(parts, "LF", def.getLeft());
        addDefPart(parts, "CF", def.getCenter());
        addDefPart(parts, "RF", def.getRight());
        return String.join(" ", parts);
    }

    private static void addDefPart(List<String> parts, String pos, String rating) {
        if (rating == null || rating.isBlank()) return;
        String r = rating.trim();
        if (r.contains("$") && !r.isEmpty() && Character.isDigit(r.charAt(0)) && (r.charAt(0) - '0') > 4) return;
        parts.add(pos + ":" + r);
    }

    private static String lastNameFrom(String name) {
        if (name == null || name.isBlank()) return "";
        int lastSpace = name.trim().lastIndexOf(' ');
        return lastSpace >= 0 ? name.substring(lastSpace + 1).trim() : name.trim();
    }

    private float abAdjustment(int actual, int abAddition) {
        if (actual > 600) return (actual * 0.1f) + abAddition;
        if (actual > 120) return 60f + abAddition;
        return (actual * 0.5f) + abAddition;
    }

    /** Save lineups for a team (replaces existing lineups). */
    public void saveLineups(String teamAbrv, List<UsageApiDto.LineupEntry> entries) {
        Team team = findTeam(teamAbrv);
        if (team == null) return;
        TeamLineup teamLineup = LineupPersistence.lookupTeamLineup(lineupEngine.getStoredLineups(), team);
        List<LineupBalanceItem> balanceItems = LineupTools.buildDefaultLineupTypes();
        List<LineupData> newLineups = new ArrayList<>();
        for (UsageApiDto.LineupEntry e : entries) {
            if (e.getBalanceFromValue() == null || e.getBalanceToValue() == null) continue;
            int fromVal = e.getBalanceFromValue();
            int toVal = e.getBalanceToValue();
            if (fromVal > toVal) continue;
            LineupBalanceItem from = balanceItems.stream().filter(b -> b.getValue() == fromVal).findFirst().orElse(null);
            LineupBalanceItem to = balanceItems.stream().filter(b -> b.getValue() == toVal).findFirst().orElse(null);
            if (from == null || to == null) continue;
            long id = e.getId() != null && e.getId() != 0 ? e.getId() : com.lineupreporter.util.RecordIndex.getNextId(com.lineupreporter.util.RecordIndex.IndexType.LineupDataId);
            String arm = (e.getPitcherArm() != null && e.getPitcherArm().equals("L")) ? "L" : "R";
            LineupData ld = new LineupData(id, arm, to, from, 0);
            newLineups.add(ld);
        }
        teamLineup.setLineups(newLineups);
        teamLineup.setPlayerByGRID(new ArrayList<>());
        lineupEngine.saveDatabase();
    }

    private Team findTeam(String abrv) {
        if (abrv == null) return null;
        return lineupEngine.getCompleteListOfTeams().stream()
            .filter(t -> abrv.equals(t.getAbrv()))
            .findFirst()
            .orElse(null);
    }

    /** Get players eligible for each position (for lineup grid dropdowns). */
    public Map<String, List<UsageApiDto.PlayerOptionDto>> getEligiblePlayersByPosition(String teamAbrv) {
        List<Player> batters = lineupEngine.getTeamReportFile().getTeamBatters(teamAbrv);
        Map<String, List<UsageApiDto.PlayerOptionDto>> result = new LinkedHashMap<>();
        for (String pos : POSITION_LABELS) result.put(pos, new ArrayList<>());
        for (Player p : batters) {
            if (p.getDef() == null) continue;
            Defense d = p.getDef();
            addIfEligible(result, "C", d.getCatcher(), p);
            addIfEligible(result, "1B", d.getFirst(), p);
            addIfEligible(result, "2B", d.getSecond(), p);
            addIfEligible(result, "3B", d.getThird(), p);
            addIfEligible(result, "SS", d.getShortStop(), p);
            addIfEligibleOutfield(result, "LF", d.getLeft(), p);
            addIfEligibleOutfield(result, "CF", d.getCenter(), p);
            addIfEligibleOutfield(result, "RF", d.getRight(), p);
            UsageApiDto.PlayerOptionDto opt = new UsageApiDto.PlayerOptionDto();
            opt.setId(p.getId());
            opt.setName(p.getName());
            opt.setDefRating("*");
            result.get("DH").add(opt);
        }
        return result;
    }

    private void addIfEligible(Map<String, List<UsageApiDto.PlayerOptionDto>> result, String pos, String rating, Player p) {
        if (rating != null && !rating.isBlank()) {
            UsageApiDto.PlayerOptionDto opt = new UsageApiDto.PlayerOptionDto();
            opt.setId(p.getId());
            opt.setName(p.getName());
            opt.setDefRating(rating.trim());
            result.get(pos).add(opt);
        }
    }

    /** Outfield only: add only when rating is present and numeric value is 4 or less. */
    private void addIfEligibleOutfield(Map<String, List<UsageApiDto.PlayerOptionDto>> result, String pos, String rating, Player p) {
        if (rating == null || rating.isBlank()) return;
        String trimmed = rating.trim();
        if (trimmed.isEmpty()) return;
        char first = trimmed.charAt(0);
        if (Character.isDigit(first) && (first - '0') > 4) return;
        UsageApiDto.PlayerOptionDto opt = new UsageApiDto.PlayerOptionDto();
        opt.setId(p.getId());
        opt.setName(p.getName());
        opt.setDefRating(trimmed);
        result.get(pos).add(opt);
    }

    /** Get current grid assignments for a team (lineupIndex, positionIndex, player). */
    public List<UsageApiDto.GridAssignmentDto> getGridAssignments(String teamAbrv) {
        Team team = findTeam(teamAbrv);
        if (team == null) return List.of();
        TeamLineup teamLineup = LineupPersistence.lookupTeamLineup(lineupEngine.getStoredLineups(), team);
        List<Player> grid = teamLineup.getPlayerByGRID();
        List<UsageApiDto.GridAssignmentDto> out = new ArrayList<>();
        int idx = 0;
        for (int col = 0; col < teamLineup.getLineups().size(); col++) {
            for (int pos = 0; pos < POSITIONS_PER_LINEUP; pos++) {
                UsageApiDto.GridAssignmentDto dto = new UsageApiDto.GridAssignmentDto();
                dto.setLineupIndex(col);
                dto.setPositionIndex(pos);
                if (idx < grid.size()) {
                    Player pl = grid.get(idx);
                    if (pl != null && pl.getName() != null && !pl.getName().isEmpty()) {
                        dto.setPlayerId(pl.getId());
                        dto.setPlayerName(pl.getName());
                    }
                }
                out.add(dto);
                idx++;
            }
        }
        return out;
    }

    /** Save grid assignments (playerByGRID). */
    public void saveGridAssignments(String teamAbrv, List<UsageApiDto.GridAssignmentDto> assignments) {
        Team team = findTeam(teamAbrv);
        if (team == null) return;
        TeamLineup teamLineup = LineupPersistence.lookupTeamLineup(lineupEngine.getStoredLineups(), team);
        int lineupCount = teamLineup.getLineups().size();
        int expectedSlots = lineupCount * POSITIONS_PER_LINEUP;
        if (expectedSlots == 0) return;

        List<Player> batters = lineupEngine.getTeamReportFile().getTeamBatters(teamAbrv);
        Map<String, Player> byId = new HashMap<>();
        for (Player p : batters) byId.put(p.getId(), p);

        // Build (lineupIndex, positionIndex) -> assignment map so we can fill in canonical order
        Map<String, UsageApiDto.GridAssignmentDto> assignmentMap = new HashMap<>();
        if (assignments != null) {
            for (var a : assignments) {
                assignmentMap.put(a.getLineupIndex() + "_" + a.getPositionIndex(), a);
            }
        }

        List<Player> newGrid = new ArrayList<>(expectedSlots);
        for (int col = 0; col < lineupCount; col++) {
            for (int pos = 0; pos < POSITIONS_PER_LINEUP; pos++) {
                UsageApiDto.GridAssignmentDto a = assignmentMap.get(col + "_" + pos);
                if (a != null && a.getPlayerId() != null && !a.getPlayerId().isEmpty()) {
                    Player p = byId.get(a.getPlayerId());
                    newGrid.add(p != null ? p : placeholderPlayer(team));
                } else {
                    newGrid.add(placeholderPlayer(team));
                }
            }
        }
        teamLineup.setPlayerByGRID(newGrid);
        lineupEngine.saveDatabase();
    }

    private static Player placeholderPlayer(Team team) {
        Player p = new Player();
        p.setId("");
        p.setName("");
        p.setTeam(team);
        return p;
    }

    // --- DTOs ---

    public static class BalanceStatsRow {
        private final String balance;
        private final int lhIp;
        private final int estLhAb;
        private final int rhIp;
        private final int estRhAb;

        public BalanceStatsRow(String balance, int lhIp, int rhIp) {
            this.balance = balance;
            this.lhIp = lhIp;
            this.estLhAb = lhIp; // simplified when no schedule
            this.rhIp = rhIp;
            this.estRhAb = rhIp;
        }

        public BalanceStatsRow(String balance, int lhIp, int estLhAb, int rhIp, int estRhAb) {
            this.balance = balance;
            this.lhIp = lhIp;
            this.estLhAb = estLhAb;
            this.rhIp = rhIp;
            this.estRhAb = estRhAb;
        }

        public String getBalance() { return balance; }
        public int getLhIp() { return lhIp; }
        public int getEstLhAb() { return estLhAb; }
        public int getRhIp() { return rhIp; }
        public int getEstRhAb() { return estRhAb; }
    }

    public static class LineupWithEstimate {
        private final LineupData lineupData;
        private final int estimatedAtBats;

        public LineupWithEstimate(LineupData lineupData, int estimatedAtBats) {
            this.lineupData = lineupData;
            this.estimatedAtBats = estimatedAtBats;
        }

        public LineupData getLineupData() { return lineupData; }
        public int getEstimatedAtBats() { return estimatedAtBats; }
    }

    public static class BatterInfoRow {
        private final String player;
        private final String defense;
        private final int projectedAb;
        private final int actualAb;
        private final int abAdjustment;
        private final int remaining;
        private final String balance;
        private final String positions;

        public BatterInfoRow(String player, String defense, int projectedAb, int actualAb, int abAdjustment, int remaining, String balance, String positions) {
            this.player = player;
            this.defense = defense != null ? defense : "";
            this.projectedAb = projectedAb;
            this.actualAb = actualAb;
            this.abAdjustment = abAdjustment;
            this.remaining = remaining;
            this.balance = balance != null ? balance : "";
            this.positions = positions != null ? positions : "";
        }

        public String getPlayer() { return player; }
        public String getDefense() { return defense; }
        public int getProjectedAb() { return projectedAb; }
        public int getActualAb() { return actualAb; }
        public int getAbAdjustment() { return abAdjustment; }
        public int getRemaining() { return remaining; }
        public String getBalance() { return balance; }
        public String getPositions() { return positions; }
    }
}
