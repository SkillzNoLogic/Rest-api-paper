package me.skillznologic.reststatsapi.vanilla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** config/reststatsapi.json for the Fabric and Forge builds. */
public final class ModConfig {

    public String host = "0.0.0.0";
    public int port = 8080;
    /** How often stats are refreshed, in seconds (minimum 1). */
    public Integer refreshInterval;
    /** Print a console message every time stats are refreshed. */
    public Boolean consoleMessages;
    /** Pre-1.3 name of refreshInterval; still honoured if present. Never written to new configs. */
    public Integer updateInterval;
    /**
     * Output key -> vanilla statistic, written as {@code category/stat}. The {@code minecraft:} namespace is optional,
     * so {@code custom/player_kills} is the same as {@code minecraft:custom/minecraft:player_kills}.
     */
    public Map<String, String> stats = defaults();

    private static Map<String, String> defaults() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("kills", "custom/player_kills");
        m.put("mob_kills", "custom/mob_kills");
        m.put("deaths", "custom/deaths");
        m.put("play_time_ticks", "custom/play_time");
        m.put("jumps", "custom/jump");
        m.put("diamonds_mined", "mined/diamond_ore");
        return m;
    }

    int intervalSeconds() {
        return Math.max(1, refreshInterval);
    }

    static ModConfig load(Path file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        if (Files.exists(file)) {
            ModConfig cfg;
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                cfg = gson.fromJson(r, ModConfig.class);
            }
            if (cfg != null) {
                // Fill in options added in newer versions and write them back so they're visible in the file.
                boolean migrate = cfg.refreshInterval == null || cfg.consoleMessages == null || cfg.updateInterval != null;
                if (cfg.stats == null) cfg.stats = new LinkedHashMap<>();
                if (cfg.refreshInterval == null) cfg.refreshInterval = cfg.updateInterval != null ? cfg.updateInterval : 30;
                if (cfg.consoleMessages == null) cfg.consoleMessages = true;
                if (cfg.host == null) cfg.host = "0.0.0.0";
                cfg.updateInterval = null;
                if (migrate) write(gson, file, cfg);
                return cfg;
            }
        }
        ModConfig cfg = createDefault();
        write(gson, file, cfg);
        return cfg;
    }

    /** No field initializers for these two, so Gson leaves them null when a config file predates them. */
    static ModConfig createDefault() {
        ModConfig cfg = new ModConfig();
        cfg.refreshInterval = 30;
        cfg.consoleMessages = true;
        return cfg;
    }

    private static void write(Gson gson, Path file, ModConfig cfg) throws IOException {
        Files.createDirectories(file.getParent());
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(cfg, w);
        }
    }
}
