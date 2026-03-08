package com.lineupreporter.somreport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report that organizes lines by team key. Ported from somReportUtils TeamReport.
 */
public abstract class TeamReport extends Report {

    protected final Map<String, List<String>> teamLines = new LinkedHashMap<>();

    public TeamReport(String title) {
        super(title);
    }

    @Override
    public void processReport(int n) {
        // no-op
    }

    public void addLine(String team, String line) {
        teamLines.computeIfAbsent(team, k -> new ArrayList<>()).add(line);
    }
}
