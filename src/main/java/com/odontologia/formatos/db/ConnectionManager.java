package com.odontologia.formatos.db;

import com.odontologia.formatos.config.AppConfig;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public final class ConnectionManager {

    private static final Logger LOG = Logger.getLogger(ConnectionManager.class.getName());

    private static ConnectionManager instancia;
    private final String url;

    private ConnectionManager() {
        Path dbPath = Paths.get(AppConfig.dbPath());
        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de datos: " + dbPath.getParent(), e);
        }
        migrarBaseDatosAntigua(dbPath);
        this.url = "jdbc:sqlite:" + dbPath;
        aplicarMigraciones();
    }

    private void migrarBaseDatosAntigua(Path nuevo) {
        Path antiguo = Paths.get(AppConfig.carpetaDatos(), "FormatosOdontologia", "clinica.db");
        if (Files.exists(nuevo) || !Files.exists(antiguo)) {
            return;
        }
        try {
            Files.move(antiguo, nuevo);
            for (String suf : new String[]{"-wal", "-shm", "-journal"}) {
                Path origen = Paths.get(antiguo.toString() + suf);
                if (Files.exists(origen)) {
                    Files.move(origen, Paths.get(nuevo.toString() + suf));
                }
            }
            LOG.info("Base de datos migrada de " + antiguo + " a " + nuevo);
        } catch (IOException e) {
            LOG.warning("No se pudo migrar la base de datos antigua: " + e.getMessage());
        }
    }

    public static synchronized ConnectionManager getInstance() {
        if (instancia == null) {
            instancia = new ConnectionManager();
        }
        return instancia;
    }

    public static synchronized void reset() {
        instancia = null;
    }

    public Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(url);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    private void aplicarMigraciones() {
        Flyway.configure()
                .dataSource(url, null, null)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
