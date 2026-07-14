package me.skillznologic.reststatsapi.cache;

import java.util.Map;

public class PlayerCache {

    private final String uuid;
    private final String name;

    private final Map<String,String> stats;


    public PlayerCache(
            String uuid,
            String name,
            Map<String,String> stats
    ){

        this.uuid = uuid;
        this.name = name;
        this.stats = stats;

    }


    public String getUuid(){
        return uuid;
    }


    public String getName(){
        return name;
    }


    public Map<String,String> getStats(){
        return stats;
    }

}