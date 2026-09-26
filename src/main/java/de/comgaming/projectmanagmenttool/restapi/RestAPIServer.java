package de.comgaming.projectmanagmenttool.restapi;

import com.sun.net.httpserver.HttpServer;
import dev.comgaming.framework.Framework;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RestAPIServer {

    private final HttpServer server;

    public RestAPIServer() throws IOException {
        RestAPIConfig config = new RestAPIConfig();
        server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        server.createContext("/api", new APIHandler());
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() {
        server.start();
        Framework.getLogger().info("Restapi","REST API gestartet auf Port " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }
}
