package me.skillznologic.reststatsapi.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.skillznologic.reststatsapi.cache.CacheManager;
import me.skillznologic.reststatsapi.cache.PlayerCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

public class PlayersHandler implements HttpHandler {

    private final CacheManager cacheManager;
    private final Gson gson = new Gson();

    public PlayersHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Adding CORS headers");

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        Collection<PlayerCache> players =
                cacheManager.getPlayers().values();

        String response =
                gson.toJson(new ArrayList<>(players));

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, bytes.length);

        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "*"
        );

    }
}