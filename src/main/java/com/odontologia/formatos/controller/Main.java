package com.odontologia.formatos.controller;

import com.odontologia.formatos.config.AppConfig;
import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.db.DemoDataLoader;
import com.odontologia.formatos.service.EntidadDuplicadaException;
import com.odontologia.formatos.service.NegocioException;
import com.odontologia.formatos.util.LogConfig;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;
import io.javalin.http.ContentType;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final int DEFAULT_PORT = 7070;
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LogConfig.configurar();
        ConnectionManager.getInstance();
        DemoDataLoader.loadIfNeeded();

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = ContentType.JSON;
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.reflectClientOrigin = true;
                    it.allowCredentials = true;
                });
            });
        });

        app.exception(NegocioException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });

        app.exception(ControllerUtil.ValidationException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });

        app.exception(EntidadDuplicadaException.class, (e, ctx) -> {
            ctx.status(409).json(Map.of("error", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            LOG.log(Level.SEVERE, "Error no manejado: " + e.getMessage(), e);
            ctx.status(500).json(Map.of("error", "Error interno del servidor"));
        });

        app.get("/health", ctx -> ctx.json(Map.of("status", "OK")));

        if (!"production".equals(System.getProperty("app.env", "development"))) {
            app.post("/shutdown", ctx -> {
                ctx.json(Map.of("status", "shutting_down"));
                app.stop();
            });
        }

        new TratamientoController().register(app);
        new AsistenciaController().register(app);
        new CatalogoController().register(app);
        new ReporteController().register(app);
        new DashboardController().register(app);
        new UnidadController().register(app);

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
                LOG.warning("Puerto invalido: " + portProp + ". Usando " + DEFAULT_PORT);
            }
        }

        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                LOG.warning("Puerto invalido en env PORT. Usando " + DEFAULT_PORT);
            }
        }

        return AppConfig.getInt("server.port", DEFAULT_PORT);
    }
}
