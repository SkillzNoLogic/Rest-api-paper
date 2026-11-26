package me.skillznologic.reststatsapi.bukkit;

import me.skillznologic.reststatsapi.core.ApiServer;
import me.skillznologic.reststatsapi.core.Log;
import me.skillznologic.reststatsapi.core.PlayerStats;
import me.skillznologic.reststatsapi.core.StatsCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
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

    static final int MIN_INTERVAL = 1;

    private final StatsCache cache = new StatsCache();
    private ApiServer api;
    private String apiHost;
    private int apiPort;
    private PlaceholderService placeholders;

    private int intervalSeconds;
    private long tickBudgetNanos;
    private boolean consoleMessages;

    private long nextRefreshAt;
    private Iterator<OfflinePlayer> pending;
    private List<PlayerStats> building;
    private long refreshNanos;
    private int refreshTicks;

    /** Name reported by the {@code /} endpoint. */
    protected abstract String platformName();

    /** Return true to postpone placeholder work this tick (e.g. when the server is lagging). */
    protected boolean shouldPause() {
        return false;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        applyConfig();

        getServer().getScheduler().runTaskTimer(this, this::tick, 1L, 1L);

        RestStatsCommand command = new RestStatsCommand(this);
        PluginCommand cmd = getCommand("reststats");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        stopApi();
    }

    /** Re-reads config.yml and applies it without a restart. Restarts the web server only if host/port changed. */
    void reload() {
        reloadConfig();
        applyConfig();
        refreshNow();
    }

    private void applyConfig() {
        FileConfiguration config = getConfig();
        placeholders = new PlaceholderService(config);
        // "update-interval" is the pre-1.3 name of this option and is still honoured.
        intervalSeconds = Math.max(MIN_INTERVAL, config.getInt("refresh-interval", config.getInt("update-interval", 30)));
        tickBudgetNanos = (long) (Math.max(0.1, config.getDouble("tick-budget-ms", 2.0)) * 1_000_000L);
        consoleMessages = config.getBoolean("console-messages", true);

        String host = config.getString("server.host", "0.0.0.0");
        int port = config.getInt("server.port", 8080);
        if (api == null || !host.equals(apiHost) || port != apiPort) {
            stopApi();
            api = new ApiServer(cache, Log.jul(getLogger()), platformName());
            try {
                api.start(host, port);
                apiHost = host;
                apiPort = port;
            } catch (IOException e) {
                Log.jul(getLogger()).warn("Could not start API server on " + host + ":" + port + "!", e);
                api = null;
            }
        }
    }

    private void stopApi() {
        if (api != null) {
            api.stop();
            api = null;
        }
    }

    // --- used by /reststats ---

    int intervalSeconds() {
        return intervalSeconds;
    }

    boolean consoleMessages() {
        return consoleMessages;
    }

    int cachedPlayers() {
        return cache.size();
    }

    String apiAddress() {
        return api == null ? null : apiHost + ":" + apiPort;
    }

    void setIntervalSeconds(int seconds) {
        intervalSeconds = Math.max(MIN_INTERVAL, seconds);
        getConfig().set("refresh-interval", intervalSeconds);
        getConfig().set("update-interval", null);
        saveConfig();
        // Apply immediately rather than waiting out the old interval.
        nextRefreshAt = Math.min(nextRefreshAt, System.currentTimeMillis() + intervalSeconds * 1000L);
    }

    void setConsoleMessages(boolean enabled) {
        consoleMessages = enabled;
        getConfig().set("console-messages", enabled);
        saveConfig();
    }

    void refreshNow() {
        nextRefreshAt = 0;
    }

    // --- refresh loop ---

    private void tick() {
        if (pending == null) {
            if (System.currentTimeMillis() < nextRefreshAt || shouldPause()) return;
            pending = Arrays.asList(getServer().getOfflinePlayers()).iterator();
            building = new ArrayList<>();
            refreshNanos = 0;
            refreshTicks = 0;
        } else if (shouldPause()) {
            return;
        }

        refreshTicks++;
        long start = System.nanoTime();
        long deadline = start + tickBudgetNanos;
        while (pending.hasNext()) {
            OfflinePlayer player = pending.next();
            building.add(new PlayerStats(player.getUniqueId().toString(), player.getName(), placeholders.resolve(player)));
            if (System.nanoTime() >= deadline) {
                refreshNanos += System.nanoTime() - start;
                return;
            }
        }
        refreshNanos += System.nanoTime() - start;

        cache.publish(building);
        if (consoleMessages) {
            int n = building.size();
            getLogger().info(String.format("Refreshed stats for %d %s (%.1fms server time over %d %s). Next refresh in %ds.",
                    n, n == 1 ? "player" : "players", refreshNanos / 1_000_000.0,
                    refreshTicks, refreshTicks == 1 ? "tick" : "ticks", intervalSeconds));
        }
        pending = null;
        building = null;
        nextRefreshAt = System.currentTimeMillis() + intervalSeconds * 1000L;
    }
}
