package me.skillznologic.reststatsapi.bukkit;

import me.skillznologic.reststatsapi.bukkit.RestStatsPlugin;

public final class BukkitRestStats extends RestStatsPlugin {

    @Override
    protected String platformName() {
        return "bukkit";
    }
}
