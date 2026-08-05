package com.odontologia.formatos.controller;

import com.odontologia.formatos.config.AppConfig;
import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.db.DemoDataLoader;
import com.odontologia.formatos.util.LogConfig;
import io.javalin.Javalin;

import java.util.Map;

public class Main {

    private static final int DEFAULT_PORT = 7070;

    public static void main(String[] args) {
        LogConfig.configurar();
        ConnectionManager.getInstance();
        DemoDataLoader.loadIfNeeded();

        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.json(Map.of("status", "OK")));

        app.post("/shutdown", ctx -> {
            ctx.json(Map.of("status", "shutting_down"));
            app.stop();
        });

        app.events(event -> {
            event.serverStarted(() ->
                    System.out.println("Javalin iniciado en http://localhost:" + app.port())
            );
            event.serverStopping(() ->
                    System.out.println("Javalin deteniendose...")
            );
        });

        app.start(getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    private static int getPort() {
        String portProp = System.getProperty("server.port");
        if (portProp != null) {
            try {
                return Integer.parseInt(portProp);
            } catch (NumberFormatException e) {
                System.err.println("Puerto invalido: " + portProp + ". Usando " + DEFAULT_PORT);
            }
        }

        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("Puerto invalido en env PORT. Usando " + DEFAULT_PORT);
            }
        }

        return AppConfig.getInt("server.port", DEFAULT_PORT);
    }
}
