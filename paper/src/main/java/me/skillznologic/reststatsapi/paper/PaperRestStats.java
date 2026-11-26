package me.skillznologic.reststatsapi.paper;

import me.skillznologic.reststatsapi.bukkit.RestStatsPlugin;

public final class PaperRestStats extends RestStatsPlugin {

    @Override
    protected String platformName() {
        return "paper";
    }

    /** Paper API: skip placeholder work while the server is running behind (average tick time over 45ms). */
    @Override
    protected boolean shouldPause() {
        return org.bukkit.Bukkit.getAverageTickTime() > 45.0;
    }
}
