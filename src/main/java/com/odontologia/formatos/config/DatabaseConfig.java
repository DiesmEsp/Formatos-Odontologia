package com.odontologia.formatos.config;

public final class DatabaseConfig {

    public static final String URL = "jdbc:sqlite:" + AppConfig.dbPath();

    private DatabaseConfig() {
    }
}
