package com.lineupreporter.web;

import com.lineupreporter.domain.Team;
import com.lineupreporter.service.LineupEngine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
public class HomeController {

    private final LineupEngine lineupEngine;

    public HomeController(LineupEngine lineupEngine) {
        this.lineupEngine = lineupEngine;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("initialized", lineupEngine.isInitialized());
        if (lineupEngine.isInitialized()) {
            model.addAttribute("teams", lineupEngine.getCompleteListOfTeams());
        }
        return "index";
    }

    @PostMapping("/upload")
    public String uploadReport(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a roster report file.");
            model.addAttribute("initialized", lineupEngine.isInitialized());
            return "index";
        }
        try {
            Path temp = Files.createTempFile("roster", ".prt");
            file.transferTo(temp);
            lineupEngine.initialize(temp.toAbsolutePath().toString());
            Files.deleteIfExists(temp);
            return "redirect:/usage";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("initialized", lineupEngine.isInitialized());
        if (lineupEngine.isInitialized()) {
            model.addAttribute("teams", lineupEngine.getCompleteListOfTeams());
        }
        return "index";
    }

    @GetMapping("/usage")
    public String usage() {
        return "usage";
    }

    @GetMapping("/opponents")
    public String opponents() {
        return "opponents";
    }
}
