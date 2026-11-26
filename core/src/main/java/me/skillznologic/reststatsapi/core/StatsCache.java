package me.skillznologic.reststatsapi.core;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Holds the latest player list, already serialized. Serializing once per refresh (instead of once per
 * request) means HTTP requests cost almost nothing no matter how often a website polls.
 */
public final class StatsCache {

    private volatile byte[] json = "[]".getBytes(StandardCharsets.UTF_8);
    private volatile int size;

    public void publish(Collection<PlayerStats> players) {
        json = Json.players(players).getBytes(StandardCharsets.UTF_8);
        size = players.size();
    }

    public byte[] json() {
        return json;
    }

    public int size() {
        return size;
    }
}
