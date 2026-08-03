package com.odontologia.formatos.util;

import com.odontologia.formatos.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configura java.util.logging (JUL) para escribir a un archivo en la carpeta
 * de datos de la aplicación (checklist 8.4).
 */
public final class LogConfig {

    private static final Logger LOGGER = Logger.getLogger("com.odontologia.formatos");
    private static volatile boolean configurado = false;

    private LogConfig() {
    }

    public static synchronized void configurar() {
        if (configurado) {
            return;
        }
        Path carpeta = Paths.get(AppConfig.carpetaDatos());
        Path archivoLog = carpeta.resolve("formatos_odontologia.log");
        try {
            Files.createDirectories(carpeta);
            FileHandler handler = new FileHandler(archivoLog.toString(), 2 * 1024 * 1024, 3, true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(handler);
            LOGGER.setLevel(Level.INFO);
            configurado = true;
            LOGGER.info("Logging inicializado en " + archivoLog);
        } catch (IOException e) {
            System.err.println("No se pudo configurar el archivo de log: " + e.getMessage());
        }
    }
}
