package me.skillznologic.reststatsapi.vanilla;

import me.skillznologic.reststatsapi.core.ApiServer;
import me.skillznologic.reststatsapi.core.Log;
import me.skillznologic.reststatsapi.core.StatsCache;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Everything the Fabric and Forge builds share. Runs on its own daemon threads; never touches the server thread. */
public final class ModRuntime {

    private final ApiServer api;
    private final ScheduledExecutorService scheduler;

    private ModRuntime(ApiServer api, ScheduledExecutorService scheduler) {
        this.api = api;
        this.scheduler = scheduler;
    }

    public static ModRuntime start(Path serverDir, Path configDir, Log log, String platform) {
        ModConfig config;
        try {
            config = ModConfig.load(configDir.resolve("reststatsapi.json"));
        } catch (Exception e) {
            log.warn("Could not read config/reststatsapi.json, using defaults", e);
            config = new ModConfig();
        }

        Map<String, String> wanted = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : config.stats.entrySet()) {
            wanted.put(e.getKey(), VanillaStatsReader.normalize(e.getValue()));
        }

        StatsCache cache = new StatsCache();
        VanillaStatsReader reader = new VanillaStatsReader(serverDir);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RestStatsAPI-refresh");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                cache.publish(reader.read(wanted));
            } catch (Throwable t) {
                log.warn("Failed to refresh player stats", t);
            }
        }, 0, Math.max(1, config.updateInterval), TimeUnit.SECONDS);

        ApiServer api = new ApiServer(cache, log, platform);
        try {
            api.start(config.host, config.port);
        } catch (Exception e) {
            log.warn("Could not start API server!", e);
        }
        log.info("RestStatsAPI enabled (" + platform + ")");
        return new ModRuntime(api, scheduler);
    }

    public void stop() {
        scheduler.shutdownNow();
        api.stop();
    }
}
