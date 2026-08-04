package com.odontologia.formatos.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DemoDataLoader {

    private static final Logger LOG = Logger.getLogger(DemoDataLoader.class.getName());

    private DemoDataLoader() {}

    public static void loadIfNeeded() {
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            try (Statement s = con.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) AS cnt FROM Docentes")) {
                int count = rs.next() ? rs.getInt("cnt") : 0;
                if (count > 0) {
                    LOG.info("Demo data ya cargada, omitiendo.");
                    return;
                }
            }

            InputStream is = DemoDataLoader.class.getResourceAsStream("/db/demo/datos_demo.sql");
            if (is == null) {
                LOG.warning("No se encontro /db/demo/datos_demo.sql");
                return;
            }

            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                    sql.append(trimmed).append(' ');
                }
            }

            String[] statements = sql.toString().split(";");
            try (Statement stmt = con.createStatement()) {
                con.setAutoCommit(false);
                for (String st : statements) {
                    String s = st.trim();
                    if (s.isEmpty()) continue;
                    stmt.execute(s);
                }
                con.commit();
                LOG.info("Demo data cargada exitosamente.");
            } catch (Exception e) {
                con.rollback();
                LOG.log(Level.SEVERE, "Error al cargar datos demo", e);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudo cargar demo data (posiblemente BD no inicializada)", e);
        }
    }
}
