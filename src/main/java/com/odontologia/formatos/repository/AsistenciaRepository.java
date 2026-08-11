package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Asistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AsistenciaRepository {

    public int insert(Asistencia asistencia) throws SQLException {
        return insert(null, asistencia);
    }

    public int insert(Connection con, Asistencia asistencia) throws SQLException {
        String sql = "INSERT INTO Asistencia (DocenteID, Fecha, Estado, HoraEntrada) VALUES (?, ?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, asistencia.getDocenteID());
            ps.setString(2, asistencia.getFecha());
            ps.setString(3, asistencia.getEstado());
            ps.setString(4, asistencia.getHoraEntrada());
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
        throw new SQLException("No se pudo obtener el id generado para Asistencia");
    }

    public Asistencia findById(int asistenciaID) throws SQLException {
        String sql = "SELECT * FROM Asistencia WHERE AsistenciaID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public Asistencia findActivoPorDocenteYFecha(int docenteID, String fecha) throws SQLException {
        return findActivoPorDocenteYFecha(null, docenteID, fecha);
    }

    public Asistencia findActivoPorDocenteYFecha(Connection con, int docenteID, String fecha) throws SQLException {
        String sql = "SELECT * FROM Asistencia WHERE DocenteID = ? AND Fecha = ? AND Estado = 'ACTIVO'";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, docenteID);
            ps.setString(2, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void anular(Connection con, int asistenciaID) throws SQLException {
        String sql = "UPDATE Asistencia SET Estado = 'ANULADO' WHERE AsistenciaID = ?";
        Connection conexion = con != null ? con : ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            ps.executeUpdate();
        }
    }

    public void registrarEntrada(Connection con, int asistenciaID, String horaEntrada) throws SQLException {
        String sql = "UPDATE Asistencia SET HoraEntrada = ? WHERE AsistenciaID = ?";
        Connection conexion = con != null ? con : ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, horaEntrada);
            ps.setInt(2, asistenciaID);
            ps.executeUpdate();
        }
    }

    public void registrarSalida(Connection con, int asistenciaID, String horaSalida) throws SQLException {
        String sql = "UPDATE Asistencia SET HoraSalida = ? WHERE AsistenciaID = ?";
        Connection conexion = con != null ? con : ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, horaSalida);
            ps.setInt(2, asistenciaID);
            ps.executeUpdate();
        }
    }

    public void revertirSalida(Connection con, int asistenciaID) throws SQLException {
        String sql = "UPDATE Asistencia SET HoraSalida = NULL WHERE AsistenciaID = ?";
        Connection conexion = con != null ? con : ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            ps.executeUpdate();
        }
    }

    private Asistencia rowToModel(ResultSet rs) throws SQLException {
        return new Asistencia(
                rs.getInt("AsistenciaID"),
                rs.getInt("DocenteID"),
                rs.getString("Fecha"),
                rs.getString("Estado"),
                rs.getString("HoraEntrada"),
                rs.getString("HoraSalida"));
    }
}
