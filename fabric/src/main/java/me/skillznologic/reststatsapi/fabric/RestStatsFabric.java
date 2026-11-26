package me.skillznologic.reststatsapi.fabric;

import me.skillznologic.reststatsapi.core.Log;
import me.skillznologic.reststatsapi.vanilla.ModRuntime;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated-server-only entrypoint. Deliberately references no Minecraft classes, so one jar runs on every
 * Minecraft version from 1.20 to 26.x without remapping.
 */
public final class RestStatsFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        Logger logger = LoggerFactory.getLogger("RestStatsAPI");
        FabricLoader loader = FabricLoader.getInstance();
        ModRuntime runtime = ModRuntime.start(loader.getGameDir(), loader.getConfigDir(), new Log() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message, Throwable error) {
                logger.warn(message, error);
            }
        }, "fabric");
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop, "RestStatsAPI-shutdown"));
    }
}
