package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.PeriodoAusencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PeriodoAusenciaRepository {

    public int insert(PeriodoAusencia ausencia) throws SQLException {
        return insert(null, ausencia);
    }

    public int insert(Connection con, PeriodoAusencia ausencia) throws SQLException {
        String sql = "INSERT INTO PeriodoAusencia (AsistenciaID, HoraInicio, Motivo) VALUES (?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ausencia.getAsistenciaID());
            ps.setString(2, ausencia.getHoraInicio());
            ps.setString(3, ausencia.getMotivo());
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
        throw new SQLException("No se pudo obtener el id generado para PeriodoAusencia");
    }

    public void finalizar(Connection con, int ausenciaID, String horaFin) throws SQLException {
        String sql = "UPDATE PeriodoAusencia SET HoraFin = ? WHERE AusenciaID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, horaFin);
            ps.setInt(2, ausenciaID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void delete(int ausenciaID) throws SQLException {
        delete(null, ausenciaID);
    }

    public void delete(Connection con, int ausenciaID) throws SQLException {
        String sql = "DELETE FROM PeriodoAusencia WHERE AusenciaID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, ausenciaID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public List<PeriodoAusencia> findByAsistenciaID(int asistenciaID) throws SQLException {
        return findByAsistenciaID(null, asistenciaID);
    }

    public List<PeriodoAusencia> findByAsistenciaID(Connection con, int asistenciaID) throws SQLException {
        List<PeriodoAusencia> lista = new ArrayList<>();
        String sql = "SELECT * FROM PeriodoAusencia WHERE AsistenciaID = ? ORDER BY HoraInicio";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
        return lista;
    }

    public PeriodoAusencia findAbierta(Connection con, int asistenciaID) throws SQLException {
        String sql = "SELECT * FROM PeriodoAusencia WHERE AsistenciaID = ? AND HoraFin IS NULL LIMIT 1";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public PeriodoAusencia findById(int ausenciaID) throws SQLException {
        String sql = "SELECT * FROM PeriodoAusencia WHERE AusenciaID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ausenciaID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    private PeriodoAusencia rowToModel(ResultSet rs) throws SQLException {
        return new PeriodoAusencia(
                rs.getInt("AusenciaID"),
                rs.getInt("AsistenciaID"),
                rs.getString("HoraInicio"),
                rs.getString("HoraFin"),
                rs.getString("Motivo"));
    }
}
