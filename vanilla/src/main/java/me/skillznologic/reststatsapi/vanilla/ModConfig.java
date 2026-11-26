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
    /** Seconds between refreshes. */
    public int updateInterval = 30;
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

    static ModConfig load(Path file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                ModConfig cfg = gson.fromJson(r, ModConfig.class);
                if (cfg != null) {
                    if (cfg.stats == null) cfg.stats = new LinkedHashMap<>();
                    return cfg;
                }
            }
        }
        ModConfig cfg = new ModConfig();
        Files.createDirectories(file.getParent());
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(cfg, w);
        }
        return cfg;
    }
}
