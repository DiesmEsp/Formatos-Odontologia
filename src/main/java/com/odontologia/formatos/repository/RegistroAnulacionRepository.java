package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.RegistroAnulacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegistroAnulacionRepository {

    public int insert(RegistroAnulacion r) throws SQLException {
        return insert(null, r);
    }

    public int insert(Connection con, RegistroAnulacion r) throws SQLException {
        String sql = "INSERT INTO RegistroAnulacion (TablaAfectada, IdRegistroAnulado, Motivo, Usuario, Timestamp) "
                + "VALUES (?, ?, ?, ?, datetime('now','localtime'))";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getTablaAfectada());
            ps.setInt(2, r.getIdRegistroAnulado());
            ps.setString(3, r.getMotivo());
            ps.setString(4, r.getUsuario() != null ? r.getUsuario() : "SYSTEM");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
        throw new SQLException("No se pudo obtener el id generado para RegistroAnulacion");
    }
}