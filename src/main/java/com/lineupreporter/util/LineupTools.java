package com.lineupreporter.util;

import com.lineupreporter.domain.LineupBalanceItem;

import java.util.ArrayList;
import java.util.List;

public final class LineupTools {

    private static final String[] BALANCE_TYPES = {
        "9L", "8L", "7L", "6L", "5L", "4L", "3L", "2L", "1L", "E",
        "1R", "2R", "3R", "4R", "5R", "6R", "7R", "8R", "9R"
    };

    public static List<LineupBalanceItem> buildDefaultLineupTypes() {
        List<LineupBalanceItem> items = new ArrayList<>();
        items.add(new LineupBalanceItem(0, 9, "L"));
        items.add(new LineupBalanceItem(1, 8, "L"));
        items.add(new LineupBalanceItem(2, 7, "L"));
        items.add(new LineupBalanceItem(3, 6, "L"));
        items.add(new LineupBalanceItem(4, 5, "L"));
        items.add(new LineupBalanceItem(5, 4, "L"));
        items.add(new LineupBalanceItem(6, 3, "L"));
        items.add(new LineupBalanceItem(7, 2, "L"));
        items.add(new LineupBalanceItem(8, 1, "L"));
        items.add(new LineupBalanceItem(9, 0, ""));
        items.add(new LineupBalanceItem(10, 1, "R"));
        items.add(new LineupBalanceItem(11, 2, "R"));
        items.add(new LineupBalanceItem(12, 3, "R"));
        items.add(new LineupBalanceItem(13, 4, "R"));
        items.add(new LineupBalanceItem(14, 5, "R"));
        items.add(new LineupBalanceItem(15, 6, "R"));
        items.add(new LineupBalanceItem(16, 7, "R"));
        items.add(new LineupBalanceItem(17, 8, "R"));
        items.add(new LineupBalanceItem(18, 9, "R"));
        return items;
    }

    public static String[] getBalanceTypes() {
        return BALANCE_TYPES.clone();
    }

    private LineupTools() {}
}
