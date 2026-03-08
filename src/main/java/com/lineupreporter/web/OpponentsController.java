package com.lineupreporter.web;

import com.lineupreporter.domain.Team;
import com.lineupreporter.domain.TeamInfo;
import com.lineupreporter.service.LineupEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
public class OpponentsController {

    private final LineupEngine lineupEngine;

    public OpponentsController(LineupEngine lineupEngine) {
        this.lineupEngine = lineupEngine;
    }

    @GetMapping("/opponents")
    public ResponseEntity<OpponentsDto> getOpponents() {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(new OpponentsDto(List.of(), 0, 0));
        }
        TeamInfo info = lineupEngine.getTeamLineupData();
        List<TeamDivisionDto> teams = lineupEngine.getCompleteListOfTeams().stream()
            .map(t -> new TeamDivisionDto(t.getAbrv(), t.getName(), t.getDivision() != null ? t.getDivision() : ""))
            .toList();
        return ResponseEntity.ok(new OpponentsDto(teams,
            info != null ? info.getInDivisionGameCount() : 0,
            info != null ? info.getOutOfDivisionGameCount() : 0));
    }

    @PutMapping(value = "/opponents", consumes = "application/json")
    public ResponseEntity<Void> saveOpponents(@RequestBody OpponentsDto dto) {
        if (!lineupEngine.isInitialized() || dto == null) {
            return ResponseEntity.badRequest().build();
        }
        TeamInfo info = lineupEngine.getTeamLineupData();
        if (info == null) return ResponseEntity.badRequest().build();
        Map<String, String> divisionByAbrv = new java.util.HashMap<>();
        if (dto.getTeams() != null) {
            for (TeamDivisionDto t : dto.getTeams()) {
                if (t.getAbrv() != null) {
                    divisionByAbrv.put(t.getAbrv(), t.getDivision() != null ? t.getDivision() : "");
                }
            }
        }
        for (Team team : lineupEngine.getCompleteListOfTeams()) {
            if (team.getAbrv() != null && divisionByAbrv.containsKey(team.getAbrv())) {
                team.setDivision(divisionByAbrv.get(team.getAbrv()));
            }
        }
        info.setTeam(new java.util.ArrayList<>(lineupEngine.getCompleteListOfTeams()));
        info.setInDivisionGameCount(dto.getInDivisionGameCount());
        info.setOutOfDivisionGameCount(dto.getOutOfDivisionGameCount());
        lineupEngine.saveDatabase();
        return ResponseEntity.ok().build();
    }

    public static class OpponentsDto {
        private List<TeamDivisionDto> teams;
        private int inDivisionGameCount;
        private int outOfDivisionGameCount;

        public OpponentsDto() {}

        public OpponentsDto(List<TeamDivisionDto> teams, int inDivisionGameCount, int outOfDivisionGameCount) {
            this.teams = teams;
            this.inDivisionGameCount = inDivisionGameCount;
            this.outOfDivisionGameCount = outOfDivisionGameCount;
        }

        public List<TeamDivisionDto> getTeams() { return teams; }
        public void setTeams(List<TeamDivisionDto> teams) { this.teams = teams; }
        public int getInDivisionGameCount() { return inDivisionGameCount; }
        public void setInDivisionGameCount(int inDivisionGameCount) { this.inDivisionGameCount = inDivisionGameCount; }
        public int getOutOfDivisionGameCount() { return outOfDivisionGameCount; }
        public void setOutOfDivisionGameCount(int outOfDivisionGameCount) { this.outOfDivisionGameCount = outOfDivisionGameCount; }
    }

    public static class TeamDivisionDto {
        private String abrv;
        private String name;
        private String division;

        public TeamDivisionDto() {}

        public TeamDivisionDto(String abrv, String name, String division) {
            this.abrv = abrv;
            this.name = name;
            this.division = division;
        }

        public String getAbrv() { return abrv; }
        public void setAbrv(String abrv) { this.abrv = abrv; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDivision() { return division; }
        public void setDivision(String division) { this.division = division; }
    }
}
