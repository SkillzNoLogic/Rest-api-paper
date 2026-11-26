package me.skillznologic.reststatsapi.http;

import com.sun.net.httpserver.HttpServer;
import me.skillznologic.reststatsapi.RestStatsAPI;
import me.skillznologic.reststatsapi.cache.CacheManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpServerManager {

    private final RestStatsAPI plugin;
    private final CacheManager cacheManager;

    private HttpServer server;

    public HttpServerManager(RestStatsAPI plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    public void start() throws IOException {

        String host = plugin.getConfig().getString("server.host", "0.0.0.0");
        int port = plugin.getConfig().getInt("server.port", 8080);

        server = HttpServer.create(new InetSocketAddress(host, port), 0);

        // Players endpoint
        server.createContext("/players", new PlayersHandler(cacheManager));

        // Root endpoint
        server.createContext("/", exchange -> {

            addCorsHeaders(exchange);

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            String response = """
                    {
                      "status": "online",
                      "plugin": "RestStatsAPI"
                    }
                    """;

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        server.start();

        plugin.getLogger().info("API started on " + host + ":" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void addCorsHeaders(com.sun.net.httpserver.HttpExchange exchange) {

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