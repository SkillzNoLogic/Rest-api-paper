package me.skillznologic.reststatsapi.api;

import me.clip.placeholderapi.PlaceholderAPI;
import me.skillznologic.reststatsapi.RestStatsAPI;
import org.bukkit.OfflinePlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlaceholderService {


    private final RestStatsAPI plugin;


    public PlaceholderService(RestStatsAPI plugin) {

        this.plugin = plugin;

    }



    public Map<String, String> getStats(OfflinePlayer player) {


        Map<String, String> stats =
                new LinkedHashMap<>();


        if(plugin.getConfig().isConfigurationSection("placeholders")) {


            for(String key :
                    plugin.getConfig()
                            .getConfigurationSection("placeholders")
                            .getKeys(false)) {


                String placeholder =
                        plugin.getConfig()
                                .getString(
                                        "placeholders." + key
                                );


                String value =
                        PlaceholderAPI.setPlaceholders(
                                player,
                                placeholder
                        );


                stats.put(
                        key,
                        value
                );

            }

        }


        return stats;

    }

}