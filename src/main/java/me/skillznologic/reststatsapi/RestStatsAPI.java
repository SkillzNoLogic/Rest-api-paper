package me.skillznologic.reststatsapi;

import me.skillznologic.reststatsapi.api.PlaceholderService;
import me.skillznologic.reststatsapi.cache.CacheManager;
import me.skillznologic.reststatsapi.cache.PlayerCache;
import me.skillznologic.reststatsapi.http.HttpServerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class RestStatsAPI extends JavaPlugin {

    private HttpServerManager httpServer;
    private CacheManager cacheManager;
    private PlaceholderService placeholderService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        cacheManager = new CacheManager();
        placeholderService = new PlaceholderService(this);

        long intervalTicks = Math.max(1, getConfig().getLong("update-interval", 30)) * 20L;
        getServer().getScheduler().runTaskTimer(this, this::refreshCache, 0L, intervalTicks);

        httpServer = new HttpServerManager(this, cacheManager);
        try {
            httpServer.start();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not start API server!", e);
        }

        getLogger().info("RestStatsAPI enabled!");
    }

    private void refreshCache() {
        Map<String, PlayerCache> fresh = new HashMap<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String uuid = player.getUniqueId().toString();
            fresh.put(uuid, new PlayerCache(uuid, player.getName(), placeholderService.getStats(player)));
        }

        cacheManager.replaceAll(fresh);
        getLogger().fine("Cached " + fresh.size() + " players");
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }

}
