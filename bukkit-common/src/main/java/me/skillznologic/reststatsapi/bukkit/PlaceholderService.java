package me.skillznologic.reststatsapi.bukkit;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

final class PlaceholderService {

    // Read once at startup instead of walking the config for every player on every refresh.
    private final Map<String, String> placeholders = new LinkedHashMap<>();

    PlaceholderService(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("placeholders");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                placeholders.put(key, section.getString(key));
            }
        }
    }

    Map<String, String> resolve(OfflinePlayer player) {
        Map<String, String> stats = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            stats.put(e.getKey(), PlaceholderAPI.setPlaceholders(player, e.getValue()));
        }
        return stats;
    }
}
