package com.odontologia.formatos.util;

import java.sql.SQLException;

/**
 * Traduce SQLException a mensajes entendibles para el usuario (checklist 8.2).
 */
public final class SqlErrorUtil {

    private static final int SQLITE_BUSY = 5;
    private static final int SQLITE_CONSTRAINT_UNIQUE = 2067;
    private static final int SQLITE_CONSTRAINT_FK = 787;
    private static final int SQLITE_CONSTRAINT_NOTNULL = 1299;
    private static final int SQLITE_CONSTRAINT_CHECK = 275;

    private SqlErrorUtil() {
    }

    public static String mensajeUsuario(SQLException e) {
        if (esErrorCodigo(e, SQLITE_CONSTRAINT_UNIQUE)) {
            return "Ya existe un registro con esos datos. No se permiten duplicados.";
        }
        if (esErrorCodigo(e, SQLITE_CONSTRAINT_FK)) {
            return "No se puede completar la operación porque el registro está relacionado con otros datos.";
        }
        if (esErrorCodigo(e, SQLITE_CONSTRAINT_NOTNULL)) {
            return "Faltan datos obligatorios para completar la operación.";
        }
        if (esErrorCodigo(e, SQLITE_CONSTRAINT_CHECK)) {
            return "Alguno de los valores ingresados no es válido.";
        }
        if (esErrorCodigo(e, SQLITE_BUSY) || esErrorCodigo(e, 6)) {
            return "La base de datos está ocupada. Inténtelo de nuevo en unos instantes.";
        }
        String msg = e.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("no such table")) {
                return "La base de datos no está inicializada correctamente.";
            }
            if (lower.contains("database is locked") || lower.contains("database is busy")) {
                return "La base de datos está ocupada. Inténtelo de nuevo en unos instantes.";
            }
        }
        return "Ocurrió un error al acceder a la base de datos.";
    }

    private static boolean esErrorCodigo(SQLException e, int codigo) {
        return e.getErrorCode() == codigo || (e.getMessage() != null
                && e.getMessage().contains("SQLITE_CONSTRAINT") && mensajeContiene(e, codigo));
    }

    private static boolean mensajeContiene(SQLException e, int codigo) {
        String msg = e.getMessage();
        return switch (codigo) {
            case SQLITE_CONSTRAINT_UNIQUE -> msg.contains("UNIQUE");
            case SQLITE_CONSTRAINT_FK -> msg.contains("FOREIGN KEY");
            case SQLITE_CONSTRAINT_NOTNULL -> msg.contains("NOT NULL");
            case SQLITE_CONSTRAINT_CHECK -> msg.contains("CHECK");
            default -> false;
        };
    }
}
