package me.skillznologic.reststatsapi.cache;

import java.util.Collections;
import java.util.Map;

public class CacheManager {

    // Replaced atomically on each refresh so HTTP threads never see a half-built cache.
    private volatile Map<String, PlayerCache> players = Collections.emptyMap();

    public void replaceAll(Map<String, PlayerCache> newPlayers) {
        players = Collections.unmodifiableMap(newPlayers);
    }

    public Map<String, PlayerCache> getPlayers() {
        return players;
    }

}
