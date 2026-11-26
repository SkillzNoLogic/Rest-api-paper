package me.skillznologic.reststatsapi.bukkit;

import me.skillznologic.reststatsapi.core.ApiServer;
import me.skillznologic.reststatsapi.core.Log;
import me.skillznologic.reststatsapi.core.PlayerStats;
import me.skillznologic.reststatsapi.core.StatsCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Shared plugin logic for every Bukkit-based server.
 *
 * <p>Placeholders have to be resolved on the main thread, so instead of resolving every player in a single tick
 * (a lag spike on servers with thousands of players) the refresh is spread over as many ticks as needed, spending at
 * most {@code tick-budget-ms} per tick. Platform subclasses can pause refreshing while the server is struggling.
 */
public abstract class RestStatsPlugin extends JavaPlugin {

    private final StatsCache cache = new StatsCache();
    private ApiServer api;
    private PlaceholderService placeholders;

    private long intervalMillis;
    private long tickBudgetNanos;
    private long nextRefreshAt;
    private Iterator<OfflinePlayer> pending;
    private List<PlayerStats> building;

    /** Name reported by the {@code /} endpoint. */
    protected abstract String platformName();

    /** Return true to postpone placeholder work this tick (e.g. when the server is lagging). */
    protected boolean shouldPause() {
        return false;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        placeholders = new PlaceholderService(getConfig());
        intervalMillis = Math.max(1, getConfig().getLong("update-interval", 30)) * 1000L;
        tickBudgetNanos = (long) (Math.max(0.1, getConfig().getDouble("tick-budget-ms", 2.0)) * 1_000_000L);

        getServer().getScheduler().runTaskTimer(this, this::tick, 1L, 1L);

        api = new ApiServer(cache, Log.jul(getLogger()), platformName());
        try {
            api.start(getConfig().getString("server.host", "0.0.0.0"), getConfig().getInt("server.port", 8080));
        } catch (IOException e) {
            Log.jul(getLogger()).warn("Could not start API server!", e);
        }
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.stop();
            api = null;
        }
    }

    private void tick() {
        if (pending == null) {
            if (System.currentTimeMillis() < nextRefreshAt || shouldPause()) return;
            pending = Arrays.asList(getServer().getOfflinePlayers()).iterator();
            building = new ArrayList<>();
        } else if (shouldPause()) {
            return;
        }

        long deadline = System.nanoTime() + tickBudgetNanos;
        while (pending.hasNext()) {
            OfflinePlayer player = pending.next();
            building.add(new PlayerStats(player.getUniqueId().toString(), player.getName(), placeholders.resolve(player)));
            if (System.nanoTime() >= deadline) return;
        }

        cache.publish(building);
        pending = null;
        building = null;
        nextRefreshAt = System.currentTimeMillis() + intervalMillis;
    }
}
