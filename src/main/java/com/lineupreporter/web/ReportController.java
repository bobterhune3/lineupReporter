package com.lineupreporter.web;

import com.lineupreporter.domain.Team;
import com.lineupreporter.domain.Player;
import com.lineupreporter.service.LineupEngine;
import com.lineupreporter.service.SomTeamReportFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class ReportController {

    private final LineupEngine lineupEngine;

    public ReportController(LineupEngine lineupEngine) {
        this.lineupEngine = lineupEngine;
    }

    @GetMapping("/team")
    public String teamReport(@RequestParam String abrv, Model model) {
        if (!lineupEngine.isInitialized()) {
            return "redirect:/";
        }
        SomTeamReportFile report = lineupEngine.getTeamReportFile();
        Team team = report.getTeams().stream()
            .filter(t -> abrv.equals(t.getAbrv()))
            .findFirst()
            .orElse(null);
        if (team == null) {
            model.addAttribute("error", "Team not found: " + abrv);
            return "index";
        }
        List<Player> pitchers = report.getTeamPitchers(abrv);
        List<Player> batters = report.getTeamBatters(abrv);
        Map<String, Integer> balanceL = report.getTeamBalanceCount("L", pitchers);
        Map<String, Integer> balanceR = report.getTeamBalanceCount("R", pitchers);

        model.addAttribute("team", team);
        model.addAttribute("pitchers", pitchers);
        model.addAttribute("batters", batters);
        model.addAttribute("balanceL", balanceL);
        model.addAttribute("balanceR", balanceR);
        model.addAttribute("teams", lineupEngine.getCompleteListOfTeams());
        return "team";
    }

    @GetMapping("/balance")
    public String balanceReport(@RequestParam(required = false) String abrv,
                                @RequestParam(defaultValue = "19") int inDiv,
                                @RequestParam(defaultValue = "6") int outDiv,
                                Model model) {
        if (!lineupEngine.isInitialized()) {
            return "redirect:/";
        }
        List<Team> teams = lineupEngine.getCompleteListOfTeams();
        model.addAttribute("teams", teams);
        model.addAttribute("inDiv", inDiv);
        model.addAttribute("outDiv", outDiv);
        if (abrv == null || abrv.isEmpty()) {
            return "balance";
        }
        Team target = teams.stream().filter(t -> abrv.equals(t.getAbrv())).findFirst().orElse(null);
        if (target == null) {
            return "balance";
        }
        // Use calculator for schedule-weighted balance (simplified: just show team balance counts for now)
        SomTeamReportFile report = lineupEngine.getTeamReportFile();
        List<Player> pitchers = report.getTeamPitchers(abrv);
        Map<String, Integer> balanceL = report.getTeamBalanceCount("L", pitchers);
        Map<String, Integer> balanceR = report.getTeamBalanceCount("R", pitchers);
        model.addAttribute("selectedTeam", target);
        model.addAttribute("balanceL", balanceL);
        model.addAttribute("balanceR", balanceR);
        return "balance";
    }
}
