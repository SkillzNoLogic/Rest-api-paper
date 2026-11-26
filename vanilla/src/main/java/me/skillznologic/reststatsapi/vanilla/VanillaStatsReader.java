package me.skillznologic.reststatsapi.vanilla;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.skillznologic.reststatsapi.core.PlayerStats;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Reads vanilla per-player statistics straight from the world folder, entirely off the server thread.
 *
 * <p>Parsed files are cached by modification time, so each refresh only re-parses players whose stats actually
 * changed since the last one (Minecraft rewrites a player's file on autosave and when they log out).
 * Handles both the pre-26.x layout ({@code world/stats}) and the 26.x layout ({@code world/players/stats}).
 */
final class VanillaStatsReader {

    private final Path serverDir;
    private final Map<Path, Parsed> parsed = new HashMap<>();

    private long userCacheModified = -1;
    private Map<String, String> names = new HashMap<>();

    VanillaStatsReader(Path serverDir) {
        this.serverDir = serverDir;
    }

    List<PlayerStats> read(Map<String, String> wanted) throws IOException {
        Path world = serverDir.resolve(levelName());
        refreshNames();

        List<PlayerStats> players = new ArrayList<>();
        Map<Path, Parsed> seen = new HashMap<>();
        for (Path dir : new Path[]{world.resolve("players").resolve("stats"), world.resolve("stats")}) {
            if (!Files.isDirectory(dir)) continue;
            try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*.json")) {
                for (Path file : files) {
                    String uuid = file.getFileName().toString();
                    uuid = uuid.substring(0, uuid.length() - ".json".length());
                    Parsed p = parse(file);
                    if (p == null) continue;
                    seen.put(file, p);

                    Map<String, String> out = new LinkedHashMap<>();
                    for (Map.Entry<String, String> e : wanted.entrySet()) {
                        Long v = p.values.get(e.getValue());
                        out.put(e.getKey(), String.valueOf(v == null ? 0L : v));
                    }
                    players.add(new PlayerStats(uuid, names.get(uuid), out));
                }
            }
        }
        parsed.keySet().retainAll(seen.keySet());
        return players;
    }

    private Parsed parse(Path file) {
        try {
            long modified = Files.getLastModifiedTime(file).toMillis();
            Parsed cached = parsed.get(file);
            if (cached != null && cached.modified == modified) return cached;

            Map<String, Long> values = new HashMap<>();
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject root = new JsonParser().parse(r).getAsJsonObject();
                JsonObject stats = root.getAsJsonObject("stats");
                if (stats != null) {
                    for (Map.Entry<String, JsonElement> category : stats.entrySet()) {
                        for (Map.Entry<String, JsonElement> stat : category.getValue().getAsJsonObject().entrySet()) {
                            values.put(category.getKey() + "/" + stat.getKey(), stat.getValue().getAsLong());
                        }
                    }
                }
            }
            Parsed p = new Parsed(modified, values);
            parsed.put(file, p);
            return p;
        } catch (Exception e) {
            // Partially-written or corrupt file: keep the previous values if we have them.
            return parsed.get(file);
        }
    }

    private String levelName() {
        Path props = serverDir.resolve("server.properties");
        if (Files.isRegularFile(props)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(props)) {
                p.load(in);
                String name = p.getProperty("level-name");
                if (name != null && !name.trim().isEmpty()) return name.trim();
            } catch (IOException ignored) {
            }
        }
        return "world";
    }

    private void refreshNames() {
        Path file = serverDir.resolve("usercache.json");
        try {
            if (!Files.isRegularFile(file)) return;
            long modified = Files.getLastModifiedTime(file).toMillis();
            if (modified == userCacheModified) return;
            Map<String, String> fresh = new HashMap<>();
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonArray arr = new JsonParser().parse(r).getAsJsonArray();
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    if (o.has("uuid") && o.has("name")) fresh.put(o.get("uuid").getAsString(), o.get("name").getAsString());
                }
            }
            names = fresh;
            userCacheModified = modified;
        } catch (Exception ignored) {
            // Keep the previous names.
        }
    }

    /** Normalizes {@code custom/jump} to {@code minecraft:custom/minecraft:jump}. */
    static String normalize(String stat) {
        String[] parts = stat.trim().split("/", 2);
        if (parts.length != 2) return stat.trim();
        return ns(parts[0]) + "/" + ns(parts[1]);
    }

    private static String ns(String id) {
        id = id.trim();
        return id.indexOf(':') >= 0 ? id : "minecraft:" + id;
    }

    private static final class Parsed {
        final long modified;
        final Map<String, Long> values;

        Parsed(long modified, Map<String, Long> values) {
            this.modified = modified;
            this.values = values;
        }
    }
}
