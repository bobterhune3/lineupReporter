package com.lineupreporter.web.dto;

import com.lineupreporter.domain.LineupBalanceItem;
import com.lineupreporter.domain.LineupData;

import java.util.List;

/** DTOs for Usage Estimator API. */
public final class UsageApiDto {

    private UsageApiDto() {}

    public static class LineupDataDto {
        private long id;
        private String pitcherArm;
        private String balanceFrom;
        private String balanceTo;
        private int estimatedAtBats;

        public static LineupDataDto from(LineupData ld, int estimatedAtBats) {
            LineupDataDto dto = new LineupDataDto();
            dto.setId(ld.getId());
            dto.setPitcherArm(ld.getPitcherArm());
            dto.setBalanceFrom(ld.getBalanceItemFrom() != null ? ld.getBalanceItemFrom().toString() : "");
            dto.setBalanceTo(ld.getBalanceItemTo() != null ? ld.getBalanceItemTo().toString() : "");
            dto.setEstimatedAtBats(estimatedAtBats);
            return dto;
        }

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getPitcherArm() { return pitcherArm; }
        public void setPitcherArm(String pitcherArm) { this.pitcherArm = pitcherArm; }
        public String getBalanceFrom() { return balanceFrom; }
        public void setBalanceFrom(String balanceFrom) { this.balanceFrom = balanceFrom; }
        public String getBalanceTo() { return balanceTo; }
        public void setBalanceTo(String balanceTo) { this.balanceTo = balanceTo; }
        public int getEstimatedAtBats() { return estimatedAtBats; }
        public void setEstimatedAtBats(int estimatedAtBats) { this.estimatedAtBats = estimatedAtBats; }
    }

    public static class LineupSaveRequest {
        private List<LineupEntry> lineups;

        public List<LineupEntry> getLineups() { return lineups; }
        public void setLineups(List<LineupEntry> lineups) { this.lineups = lineups; }
    }

    public static class LineupEntry {
        private Long id;
        private String pitcherArm;
        private Integer balanceFromValue;
        private Integer balanceToValue;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPitcherArm() { return pitcherArm; }
        public void setPitcherArm(String pitcherArm) { this.pitcherArm = pitcherArm; }
        public Integer getBalanceFromValue() { return balanceFromValue; }
        public void setBalanceFromValue(Integer balanceFromValue) { this.balanceFromValue = balanceFromValue; }
        public Integer getBalanceToValue() { return balanceToValue; }
        public void setBalanceToValue(Integer balanceToValue) { this.balanceToValue = balanceToValue; }
    }

    public static class GridAssignmentDto {
        private int lineupIndex;
        private int positionIndex;
        private String playerId;
        private String playerName;

        public int getLineupIndex() { return lineupIndex; }
        public void setLineupIndex(int lineupIndex) { this.lineupIndex = lineupIndex; }
        public int getPositionIndex() { return positionIndex; }
        public void setPositionIndex(int positionIndex) { this.positionIndex = positionIndex; }
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
    }

    public static class GridSaveRequest {
        private List<GridAssignmentDto> assignments;

        public List<GridAssignmentDto> getAssignments() { return assignments; }
        public void setAssignments(List<GridAssignmentDto> assignments) { this.assignments = assignments; }
    }

    public static class PlayerOptionDto {
        private String id;
        private String name;
        private String defRating;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDefRating() { return defRating; }
        public void setDefRating(String defRating) { this.defRating = defRating; }
    }
}
