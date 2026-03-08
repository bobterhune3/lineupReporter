package com.lineupreporter.web;

import com.lineupreporter.domain.Team;
import com.lineupreporter.domain.TeamInfo;
import com.lineupreporter.service.LineupEngine;
import com.lineupreporter.service.UsageEstimatorService;
import com.lineupreporter.web.dto.UsageApiDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final LineupEngine lineupEngine;
    private final UsageEstimatorService usageEstimatorService;

    public UsageController(LineupEngine lineupEngine, UsageEstimatorService usageEstimatorService) {
        this.lineupEngine = lineupEngine;
        this.usageEstimatorService = usageEstimatorService;
    }

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> teams() {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(lineupEngine.getCompleteListOfTeams());
    }

    @GetMapping("/balance-stats/{abrv}")
    public ResponseEntity<List<UsageEstimatorService.BalanceStatsRow>> balanceStats(@PathVariable String abrv) {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(usageEstimatorService.getBalanceStats(abrv));
    }

    @GetMapping("/lineups/{abrv}")
    public ResponseEntity<List<UsageApiDto.LineupDataDto>> lineups(@PathVariable String abrv) {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(List.of());
        }
        List<UsageApiDto.LineupDataDto> list = usageEstimatorService.getLineupsWithEstimatedAb(abrv).stream()
            .map(lwe -> UsageApiDto.LineupDataDto.from(lwe.getLineupData(), lwe.getEstimatedAtBats()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping(value = "/lineups/{abrv}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveLineups(@PathVariable String abrv, @RequestBody UsageApiDto.LineupSaveRequest request) {
        if (!lineupEngine.isInitialized() || request == null || request.getLineups() == null) {
            return ResponseEntity.badRequest().build();
        }
        usageEstimatorService.saveLineups(abrv, request.getLineups());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batter-info/{abrv}")
    public ResponseEntity<List<UsageEstimatorService.BatterInfoRow>> batterInfo(@PathVariable String abrv) {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(usageEstimatorService.getBatterInfo(abrv));
    }

    @GetMapping("/eligible-players/{abrv}")
    public ResponseEntity<Map<String, List<UsageApiDto.PlayerOptionDto>>> eligiblePlayers(@PathVariable String abrv) {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(Map.of());
        }
        return ResponseEntity.ok(usageEstimatorService.getEligiblePlayersByPosition(abrv));
    }

    @GetMapping("/grid/{abrv}")
    public ResponseEntity<List<UsageApiDto.GridAssignmentDto>> grid(@PathVariable String abrv) {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(usageEstimatorService.getGridAssignments(abrv));
    }

    @PutMapping(value = "/grid/{abrv}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveGrid(@PathVariable String abrv, @RequestBody UsageApiDto.GridSaveRequest request) {
        if (!lineupEngine.isInitialized() || request == null || request.getAssignments() == null) {
            return ResponseEntity.badRequest().build();
        }
        usageEstimatorService.saveGridAssignments(abrv, request.getAssignments());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/needs-opponents")
    public ResponseEntity<Boolean> needsOpponents() {
        if (!lineupEngine.isInitialized()) {
            return ResponseEntity.ok(false);
        }
        TeamInfo info = lineupEngine.getTeamLineupData();
        return ResponseEntity.ok(info != null && info.hasEmptyData());
    }
}
