package com.odontologia.formatos.util;

import com.odontologia.formatos.db.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta una operación de negocio en una transacción atómica
 * (checklist 7.4). Abre una sola conexión, confirma al terminar y
 * revierte ante cualquier error.
 */
public final class TransaccionBD {

    @FunctionalInterface
    public interface Operacion {
        void ejecutar(Connection con) throws SQLException;
    }

    @FunctionalInterface
    public interface OperacionConResultado<T> {
        T ejecutar(Connection con) throws SQLException;
    }

    private TransaccionBD() {
    }

    public static void ejecutar(Operacion operacion) throws SQLException {
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                operacion.ejecutar(con);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static <T> T ejecutarConResultado(OperacionConResultado<T> operacion) throws SQLException {
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
