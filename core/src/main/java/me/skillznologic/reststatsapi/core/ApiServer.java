package me.skillznologic.reststatsapi.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public final class ApiServer {

    private final StatsCache cache;
    private final Log log;
    private final byte[] status;

    private HttpServer server;
    private ExecutorService pool;

    public ApiServer(StatsCache cache, Log log, String platform) {
        this.cache = cache;
        this.log = log;
        this.status = ("{\"status\":\"online\",\"plugin\":\"RestStatsAPI\",\"platform\":" + Json.quote(platform) + "}")
                .getBytes(StandardCharsets.UTF_8);
    }

    public void start(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "RestStatsAPI-HTTP");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(pool);
        server.createContext("/players", ex -> send(ex, cache.json()));
        server.createContext("/", ex -> send(ex, status));

        // HttpServer's dispatcher thread inherits daemon status from the thread that starts it. Starting it from a
        // daemon thread guarantees it can never keep a stopping server's JVM alive.
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        Thread starter = new Thread(() -> {
            try {
                server.start();
            } catch (RuntimeException e) {
                failure.set(e);
            }
        }, "RestStatsAPI-start");
        starter.setDaemon(true);
        starter.start();
        try {
            starter.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (failure.get() != null) throw new IOException("Could not start HTTP server", failure.get());

        log.info("API started on " + host + ":" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (pool != null) {
            pool.shutdownNow();
            pool = null;
        }
    }

    private static void send(HttpExchange exchange, byte[] body) throws IOException {
        try {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");

            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            if ("HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        } finally {
            exchange.close();
        }
    }
}
