package me.skillznologic.reststatsapi.core;

/** Minimal logging bridge so the core works with JUL (Bukkit) and SLF4J (Fabric/Forge). */
public interface Log {

    void info(String message);

    void warn(String message, Throwable error);

    static Log jul(final java.util.logging.Logger logger) {
        return new Log() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message, Throwable error) {
                logger.log(java.util.logging.Level.WARNING, message, error);
            }
        };
    }
}
