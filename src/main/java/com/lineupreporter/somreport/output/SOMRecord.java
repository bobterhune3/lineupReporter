package com.lineupreporter.somreport.output;

/**
 * Single record entry for SOM report output. Ported from somReportUtils output.SOMRecord.
 */
public class SOMRecord {

    private final String label;
    private final int recordValue;
    private final String team;
    private final String opponent;
    private final String description;

    public SOMRecord(String label, int recordValue, String team, String opponent) {
        this.label = label;
        this.recordValue = recordValue;
        this.team = team != null ? team : "";
        this.opponent = opponent != null ? opponent : "";
        this.description = "";
    }

    public SOMRecord(String label, int recordValue, String description) {
        this.label = label;
        this.recordValue = recordValue;
        this.team = "";
        this.opponent = "";
        this.description = description != null ? description : "";
    }

    public String getLabel() { return label; }
    public int getRecordValue() { return recordValue; }
    public String getTeam() { return team; }
    public String getOpponent() { return opponent; }
    public String getDescription() { return description; }
}
