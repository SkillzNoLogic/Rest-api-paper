package me.skillznologic.reststatsapi;

import me.skillznologic.reststatsapi.http.HttpServerManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import me.skillznologic.reststatsapi.cache.CacheManager;

import me.skillznologic.reststatsapi.api.PlaceholderService;
import me.skillznologic.reststatsapi.cache.PlayerCache;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class RestStatsAPI extends JavaPlugin {

    private HttpServerManager httpServer;
    private CacheManager cacheManager;
    private PlaceholderService placeholderService;


    @Override
    public void onEnable() {

        saveDefaultConfig();
        cacheManager = new CacheManager();
        placeholderService = new PlaceholderService(this);
        getServer()
                .getScheduler()
                .runTaskTimer(
                        this,
                        () -> {

                            getLogger().info(
                                    "Updating player cache..."
                            );


                            cacheManager.clear();


                            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {


                                PlayerCache cachedPlayer =
                                        new PlayerCache(
                                                player.getUniqueId().toString(),
                                                player.getName(),
                                                placeholderService.getStats(player)
                                        );


                                cacheManager.update(cachedPlayer);


                            }


                            getLogger().info(
                                    "Cached "
                                            + cacheManager.getPlayers().size()
                                            + " players"
                            );

                        },
                        0L,
                        600L
                );

        getLogger().info("RestStatsAPI enabled!");

        httpServer = new HttpServerManager(
                this,
                cacheManager
        );

        try {

            httpServer.start();

        } catch (IOException e) {

            getLogger().severe("Could not start API server!");
            e.printStackTrace();

        }

    }


    @Override
    public void onDisable() {

        if (httpServer != null) {

            httpServer.stop();

        }

    }

}