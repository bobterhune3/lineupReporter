package com.lineupreporter.somreport;

import com.lineupreporter.domain.Player;

import java.util.List;
import java.util.Map;

/**
 * Interface for parsing SOM report files and finding reports by team/name.
 * Ported from somReportUtils ISOMReportFile.
 */
public interface ISOMReportFile {

    /** Find a report by team and name. */
    Report findReport(String team, String name);

    /** Get all reports grouped by name. */
    Map<String, List<Report>> getAllReports();

    /** Read file as lines, optionally cleaning up. */
    List<String> readFileLinesOnly(boolean cleanup);
}
