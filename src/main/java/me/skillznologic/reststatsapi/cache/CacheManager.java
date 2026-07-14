package me.skillznologic.reststatsapi.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {


    private final Map<String, PlayerCache> players =
            new HashMap<>();


    public void update(PlayerCache player) {

        players.put(
                player.getUuid(),
                player
        );

    }


    public Map<String, PlayerCache> getPlayers() {

        return players;

    }


    public void clear() {

        players.clear();

    }

}