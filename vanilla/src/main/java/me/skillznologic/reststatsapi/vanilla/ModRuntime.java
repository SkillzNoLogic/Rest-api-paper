package me.skillznologic.reststatsapi.vanilla;

import me.skillznologic.reststatsapi.core.ApiServer;
import me.skillznologic.reststatsapi.core.Log;
import me.skillznologic.reststatsapi.core.PlayerStats;
import me.skillznologic.reststatsapi.core.StatsCache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Everything the Fabric and Forge builds share. Runs on its own daemon threads; never touches the server thread.
 *
 * <p>config/reststatsapi.json is watched: edits are applied within a couple of seconds, no restart needed.
 */
public final class ModRuntime {

    private final Path configFile;
    private final Log log;
    private final String platform;
    private final StatsCache cache = new StatsCache();
    private final VanillaStatsReader reader;
    private final ScheduledExecutorService scheduler;

    private ApiServer api;
    private String apiHost;
    private int apiPort;

    private ModConfig config;
    private Map<String, String> wanted;
    private long configModified;
    private long nextRefreshAt;

    private ModRuntime(Path serverDir, Path configDir, Log log, String platform) {
        this.configFile = configDir.resolve("reststatsapi.json");
        this.log = log;
        this.platform = platform;
        this.reader = new VanillaStatsReader(serverDir);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RestStatsAPI-refresh");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
    }

    public static ModRuntime start(Path serverDir, Path configDir, Log log, String platform) {
        ModRuntime runtime = new ModRuntime(serverDir, configDir, log, platform);
        runtime.loadConfig(false);
        runtime.scheduler.scheduleWithFixedDelay(runtime::tick, 0, 1, TimeUnit.SECONDS);
        log.info("RestStatsAPI enabled (" + platform + "). Refreshing every " + runtime.config.intervalSeconds()
                + "s; edit config/reststatsapi.json to change settings (applied automatically).");
        return runtime;
    }

    public void stop() {
        scheduler.shutdownNow();
        if (api != null) api.stop();
    }

    private void tick() {
        try {
            long modified = Files.exists(configFile) ? Files.getLastModifiedTime(configFile).toMillis() : 0;
            if (modified != configModified) loadConfig(true);

            if (System.currentTimeMillis() < nextRefreshAt) return;
            long started = System.nanoTime();
            List<PlayerStats> players = reader.read(wanted);
            cache.publish(players);
            nextRefreshAt = System.currentTimeMillis() + config.intervalSeconds() * 1000L;
            if (config.consoleMessages) {
                int n = players.size();
                log.info(String.format("Refreshed stats for %d %s (%.1fms, background thread). Next refresh in %ds.",
                        n, n == 1 ? "player" : "players", (System.nanoTime() - started) / 1_000_000.0, config.intervalSeconds()));
            }
        } catch (Throwable t) {
            log.warn("Failed to refresh player stats", t);
            nextRefreshAt = System.currentTimeMillis() + 5000L;
        }
    }

    private void loadConfig(boolean reload) {
        ModConfig fresh;
        try {
            fresh = ModConfig.load(configFile);
        } catch (Exception e) {
            if (reload) {
                log.warn("config/reststatsapi.json is invalid, keeping the previous settings", e);
                configModified = modifiedOrZero();
                return;
            }
            log.warn("Could not read config/reststatsapi.json, using defaults", e);
            fresh = ModConfig.createDefault();
        }
        configModified = modifiedOrZero();

        Map<String, String> stats = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : fresh.stats.entrySet()) {
            stats.put(e.getKey(), VanillaStatsReader.normalize(e.getValue()));
        }

        config = fresh;
        wanted = stats;
        nextRefreshAt = 0; // refresh right away with the new settings

        if (api == null || !fresh.host.equals(apiHost) || fresh.port != apiPort) {
            if (api != null) api.stop();
            api = new ApiServer(cache, log, platform);
            try {
                api.start(fresh.host, fresh.port);
                apiHost = fresh.host;
                apiPort = fresh.port;
            } catch (Exception e) {
                log.warn("Could not start API server on " + fresh.host + ":" + fresh.port + "!", e);
                api = null;
                apiHost = null;
            }
        }

        if (reload) {
            log.info("Reloaded config/reststatsapi.json: refresh every " + fresh.intervalSeconds() + "s, console messages "
                    + (fresh.consoleMessages ? "on" : "off") + ".");
        }
    }

    private long modifiedOrZero() {
        try {
            return Files.exists(configFile) ? Files.getLastModifiedTime(configFile).toMillis() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
