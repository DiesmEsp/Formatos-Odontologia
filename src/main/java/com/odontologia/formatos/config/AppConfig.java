package com.odontologia.formatos.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPS = new Properties();
    private static final String CARPETA_APP = "FormatosOdontologia";

    static {
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar application.properties", e);
        }
    }

    private AppConfig() {
    }

    public static String dbPath() {
        String override = System.getProperty("db.path");
        return resolver(override != null ? override : PROPS.getProperty("db.path", "clinica.db"));
    }

    public static String carpetaDatos() {
        return appDataDir();
    }

    public static String carpetaInicialReportes() {
        String override = System.getProperty("reportes.carpetaInicial");
        if (override != null && !override.isBlank()) {
            return resolver(override);
        }
        String configurada = PROPS.getProperty("reportes.carpetaInicial");
        if (configurada != null && !configurada.isBlank()) {
            return resolver(configurada);
        }
        return Paths.get(System.getProperty("user.home"), "Documents", "FormatosOdontologia", "Reportes").toString();
    }

    public static int getInt(String key, int defaultValue) {
        String val = PROPS.getProperty(key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String resolver(String valor) {
        return valor.replace("${APPDATA}", appDataDir());
    }

    private static String appDataDir() {
        String env = System.getenv("APPDATA");
        if (env == null || env.isBlank()) {
            return System.getProperty("user.home");
        }
        return env + "\\" + CARPETA_APP;
    }
}
