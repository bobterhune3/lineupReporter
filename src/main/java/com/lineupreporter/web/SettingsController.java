package com.lineupreporter.web;

import com.lineupreporter.config.AppConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usage")
public class SettingsController {

    private final AppConfig appConfig;

    public SettingsController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsDto> getSettings() {
        SettingsDto dto = new SettingsDto();
        dto.setAbAddition(appConfig.getAbAddition());
        dto.setTargetAtBats(com.lineupreporter.service.CalculateColumnUtil.targetAtBats);
        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/settings", consumes = "application/json")
    public ResponseEntity<Void> saveSettings(@RequestBody SettingsDto dto) {
        if (dto != null) {
            if (dto.getAbAddition() != null) appConfig.setAbAddition(dto.getAbAddition());
            if (dto.getTargetAtBats() != null && dto.getTargetAtBats() > 0) {
                com.lineupreporter.service.CalculateColumnUtil.targetAtBats = dto.getTargetAtBats();
            }
        }
        return ResponseEntity.ok().build();
    }

    public static class SettingsDto {
        private Integer abAddition;
        private Integer targetAtBats;

        public Integer getAbAddition() { return abAddition; }
        public void setAbAddition(Integer abAddition) { this.abAddition = abAddition; }
        public Integer getTargetAtBats() { return targetAtBats; }
        public void setTargetAtBats(Integer targetAtBats) { this.targetAtBats = targetAtBats; }
    }
}
