package com.lineupreporter.util;

import java.util.concurrent.atomic.AtomicLong;

public final class RecordIndex {

    public enum IndexType { TeamId, PlayerId, LineupItemId, LineupDataId, DefenseId, TestTeamId, TestDefenseId, TestLineupDataId }

    private static final AtomicLong teamId = new AtomicLong(1);
    private static final AtomicLong playerId = new AtomicLong(1);
    private static final AtomicLong defenseId = new AtomicLong(1);
    private static final AtomicLong lineupItemId = new AtomicLong(1);
    private static final AtomicLong lineupDataId = new AtomicLong(1);
    private static final AtomicLong testTeamId = new AtomicLong(1);
    private static final AtomicLong testDefenseId = new AtomicLong(1);
    private static final AtomicLong testLineupDataId = new AtomicLong(1);

    public static long getNextId(IndexType type) {
        return switch (type) {
            case TeamId -> teamId.incrementAndGet();
            case PlayerId -> playerId.incrementAndGet();
            case DefenseId -> defenseId.incrementAndGet();
            case LineupItemId -> lineupItemId.incrementAndGet();
            case LineupDataId -> lineupDataId.incrementAndGet();
            case TestTeamId -> testTeamId.incrementAndGet();
            case TestDefenseId -> testDefenseId.incrementAndGet();
            case TestLineupDataId -> testLineupDataId.incrementAndGet();
        };
    }

    public static void resetIndex(IndexType type) {
        switch (type) {
            case TeamId -> teamId.set(1);
            case PlayerId -> playerId.set(1);
            case DefenseId -> defenseId.set(1);
            case LineupItemId -> lineupItemId.set(1);
            case LineupDataId -> lineupDataId.set(1);
            case TestTeamId -> testTeamId.set(1);
            case TestDefenseId -> testDefenseId.set(1);
            case TestLineupDataId -> testLineupDataId.set(1);
        }
    }

    public static void resetAllIndexes() {
        for (IndexType t : IndexType.values()) resetIndex(t);
    }

    private RecordIndex() {}
}
