package com.odontologia.formatos.util;

import com.odontologia.formatos.repository.BaseRepositoryTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedMigrationTest extends BaseRepositoryTest {

    @Test
    void seedCargaMaterialesYUnidadInicial() throws Exception {
        try (Connection con = connectionManager().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Materiales")) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "Debe existir el catálogo básico de materiales");
        }

        try (Connection con = connectionManager().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Unidad")) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) >= 1, "Debe existir al menos una unidad de tratamiento");
        }
    }

    @Test
    void materialesDelSeedSonUnicos() throws Exception {
        try (Connection con = connectionManager().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*), COUNT(DISTINCT Nombre) FROM Materiales")) {
            assertTrue(rs.next());
            assertEquals(rs.getInt(1), rs.getInt(2), "Los nombres de materiales deben ser únicos");
        }
    }

    @Test
    void unicoTratamientoPredefinidoArrojaMensajeClaro() {
        SQLException e = new SQLException("UNIQUE constraint failed: Materiales.Nombre",
                "SQLITE_CONSTRAINT", 2067);
        assertEquals("Ya existe un registro con esos datos. No se permiten duplicados.",
                SqlErrorUtil.mensajeUsuario(e));
    }

    @Test
    void foreignKeyArrojaMensajeDeRelacion() {
        SQLException e = new SQLException("FOREIGN KEY constraint failed",
                "SQLITE_CONSTRAINT", 787);
        assertEquals("No se puede completar la operación porque el registro está relacionado con otros datos.",
                SqlErrorUtil.mensajeUsuario(e));
    }

    @Test
    void errorGenericoDeSql() {
        SQLException e = new SQLException("syntax error", "SQLITE_ERROR", 1);
        assertEquals("Ocurrió un error al acceder a la base de datos.",
                SqlErrorUtil.mensajeUsuario(e));
    }

    @Test
    void exportacionArrojaMensajeDeArchivoOcupado() {
        Throwable e = new RuntimeException("The process cannot access the file because it is being used by another process");
        assertEquals("No se pudo guardar el archivo 'reporte.xlsx' porque está abierto en otro programa. Ciérrelo e inténtelo de nuevo.",
                ExportErrorUtil.mensaje(e, "reporte.xlsx"));
    }

    @Test
    void exportacionArrojaMensajeDePermisos() throws Exception {
        Throwable e = new java.nio.file.AccessDeniedException("reporte.xlsx");
        assertEquals("No se tienen permisos para escribir en la carpeta 'reporte.xlsx'. Elija otra ubicación para guardar el reporte.",
                ExportErrorUtil.mensaje(e, "reporte.xlsx"));
    }

    private com.odontologia.formatos.db.ConnectionManager connectionManager() {
        return com.odontologia.formatos.db.ConnectionManager.getInstance();
    }
}
