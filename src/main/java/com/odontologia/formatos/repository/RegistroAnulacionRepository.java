package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.RegistroAnulacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RegistroAnulacionRepository {

    public int insert(RegistroAnulacion r) throws SQLException {
        return insert(null, r);
    }

    public int insert(Connection con, RegistroAnulacion r) throws SQLException {
        String sql = "INSERT INTO RegistroAnulacion (TablaAfectada, IdRegistroAnulado, Motivo, Usuario, Timestamp) "
                + "VALUES (?, ?, ?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getTablaAfectada());
            ps.setInt(2, r.getIdRegistroAnulado());
            ps.setString(3, r.getMotivo());
            ps.setString(4, r.getUsuario() != null ? r.getUsuario() : "SYSTEM");
            ps.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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

    public List<RegistroAnulacion> findAll() throws SQLException {
        String sql = "SELECT AnulacionID, TablaAfectada, IdRegistroAnulado, Motivo, Usuario, Timestamp "
                + "FROM RegistroAnulacion ORDER BY Timestamp DESC";
        List<RegistroAnulacion> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RegistroAnulacion r = new RegistroAnulacion();
                r.setAnulacionID(rs.getInt("AnulacionID"));
                r.setTablaAfectada(rs.getString("TablaAfectada"));
                r.setIdRegistroAnulado(rs.getInt("IdRegistroAnulado"));
                r.setMotivo(rs.getString("Motivo"));
                r.setUsuario(rs.getString("Usuario"));
                r.setTimestamp(rs.getString("Timestamp"));
                lista.add(r);
            }
        }
        return lista;
    }
}