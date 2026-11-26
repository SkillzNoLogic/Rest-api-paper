package me.skillznologic.reststatsapi.forge;

import me.skillznologic.reststatsapi.core.Log;
import me.skillznologic.reststatsapi.vanilla.ModRuntime;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Forge entrypoint. Only uses the {@code @Mod} annotation, which hasn't changed since 1.13, so the same jar loads
 * on Forge for Minecraft 1.20 through 26.x. Does nothing on clients.
 */
@Mod("reststatsapi")
public final class RestStatsForge {

    public RestStatsForge() {
        if (!isDedicatedServer()) return;

        Logger logger = LoggerFactory.getLogger("RestStatsAPI");
        Path serverDir = Paths.get("").toAbsolutePath();
        ModRuntime runtime = ModRuntime.start(serverDir, serverDir.resolve("config"), new Log() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message, Throwable error) {
                logger.warn(message, error);
            }
        }, "forge");
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop, "RestStatsAPI-shutdown"));
    }

    private static boolean isDedicatedServer() {
        try {
            Object dist = Class.forName("net.minecraftforge.fml.loading.FMLEnvironment").getField("dist").get(null);
            return "DEDICATED_SERVER".equals(String.valueOf(dist));
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }
}
