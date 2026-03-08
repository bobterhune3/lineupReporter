package com.lineupreporter.config;

import com.lineupreporter.somreport.config.IConfig;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import com.lineupreporter.util.TeamUtils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AppConfig implements IConfig {

    public static final String CONFIG_FILE_NAME = "config.properties";
    public static final float DEFAULT_AB_MULTIPLIER = 1f;
    public static final float DEFAULT_IP_MULTIPLIER = 1f;
    public static final int AB_MINIMUM = 50;
    public static final int IP_MINIMUM = 30;

    private final Map<String, String> teamMap = new HashMap<>();
    private float abMultiplier = DEFAULT_AB_MULTIPLIER;
    private float ipMultiplier = DEFAULT_IP_MULTIPLIER;
    private int abMinimum = AB_MINIMUM;
    private int ipMinimum = IP_MINIMUM;
    private int abAddition = 0;

    public int getAbAddition() { return abAddition; }
    public void setAbAddition(int abAddition) { this.abAddition = abAddition; }

    public float getABMultiplier() { return abMultiplier; }
    public void setABMultiplier(float abMultiplier) { this.abMultiplier = abMultiplier; }

    public float getIPMultiplier() { return ipMultiplier; }
    public void setIPMultiplier(float ipMultiplier) { this.ipMultiplier = ipMultiplier; }

    public float getMinABAllowed() { return abMinimum; }
    public void setAbMinimum(int abMinimum) { this.abMinimum = abMinimum; }

    public float getMinIPAllowed() { return ipMinimum; }
    public void setIpMinimum(int ipMinimum) { this.ipMinimum = ipMinimum; }

    public Map<String, String> getTeamAbrvMapping() { return new HashMap<>(teamMap); }
    public void putTeamMapping(String key, String value) { teamMap.put(key, value); }

    @PostConstruct
    public void loadFromFile() {
        try {
            Path path = Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
            if (!Files.exists(path)) return;
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                switch (key) {
                    case "ESTIMATE_AB_MULTIPLIER" -> setABMultiplier(Float.parseFloat(value));
                    case "ESTIMATE_IP_MULTIPLIER" -> setIPMultiplier(Float.parseFloat(value));
                    case "AB_MINIMUM" -> setAbMinimum(Integer.parseInt(value));
                    case "IP_MINIMUM" -> setIpMinimum(Integer.parseInt(value));
                    case "AB_ADDITION" -> setAbAddition(Integer.parseInt(value));
                    default -> teamMap.put(key, value);
                }
            }
        } catch (Exception ignored) {}
        TeamUtils.registerTeamAbvMapping(teamMap);
    }
}
