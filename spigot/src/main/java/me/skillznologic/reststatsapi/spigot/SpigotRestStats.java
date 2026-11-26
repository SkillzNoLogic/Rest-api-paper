package me.skillznologic.reststatsapi.spigot;

import me.skillznologic.reststatsapi.bukkit.RestStatsPlugin;

public final class SpigotRestStats extends RestStatsPlugin {

    @Override
    protected String platformName() {
        return "spigot";
    }
}
