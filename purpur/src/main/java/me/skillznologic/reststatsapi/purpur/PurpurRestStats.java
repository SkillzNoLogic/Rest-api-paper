package me.skillznologic.reststatsapi.purpur;

import me.skillznologic.reststatsapi.bukkit.RestStatsPlugin;

public final class PurpurRestStats extends RestStatsPlugin {

    @Override
    protected String platformName() {
        return "purpur";
    }

    /** Paper API: skip placeholder work while the server is running behind (average tick time over 45ms). */
    @Override
    protected boolean shouldPause() {
        return org.bukkit.Bukkit.getAverageTickTime() > 45.0;
    }
}
