package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.TratamientoAvance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TratamientoAvanceRepository {

    public int insert(Connection con, TratamientoAvance avance) throws SQLException {
        String sql = "INSERT INTO Tratamiento_Avance (TratamientoID, Numero, Fecha, UnidadID) VALUES (?, "
                + "(SELECT COALESCE(MAX(Numero), 0) + 1 FROM Tratamiento_Avance WHERE TratamientoID = ?), ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, avance.getTratamientoID());
            ps.setInt(2, avance.getTratamientoID());
            ps.setString(3, avance.getFecha());
            if (avance.getUnidadID() != null) {
                ps.setInt(4, avance.getUnidadID());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
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
        throw new SQLException("No se pudo obtener el id generado para Tratamiento_Avance");
    }

    public TratamientoAvance findById(int avanceID) throws SQLException {
        String sql = "SELECT * FROM Tratamiento_Avance WHERE AvanceID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, avanceID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<TratamientoAvance> findByTratamiento(int tratamientoID) throws SQLException {
        List<TratamientoAvance> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento_Avance WHERE TratamientoID = ? ORDER BY Fecha, AvanceID";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public void anular(Connection con, int avanceID) throws SQLException {
        String sql = "UPDATE Tratamiento_Avance SET Estado = 'ANULADO' WHERE AvanceID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, avanceID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void terminar(Connection con, int avanceID) throws SQLException {
        String sql = "UPDATE Tratamiento_Avance SET Estado = 'TERMINADO' WHERE AvanceID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, avanceID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public TratamientoAvance findActivoByUnidad(int unidadID) throws SQLException {
        String sql = "SELECT * FROM Tratamiento_Avance WHERE UnidadID = ? AND Estado = 'ACTIVO' LIMIT 1";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<TratamientoAvance> findActivos() throws SQLException {
        List<TratamientoAvance> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento_Avance WHERE Estado = 'ACTIVO' ORDER BY Fecha, AvanceID";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    private TratamientoAvance rowToModel(ResultSet rs) throws SQLException {
        TratamientoAvance a = new TratamientoAvance();
        a.setAvanceID(rs.getInt("AvanceID"));
        a.setTratamientoID(rs.getInt("TratamientoID"));
        a.setNumero(rs.getInt("Numero"));
        a.setFecha(rs.getString("Fecha"));
        int unidad = rs.getInt("UnidadID");
        a.setUnidadID(rs.wasNull() ? null : unidad);
        a.setEstado(rs.getString("Estado"));
        a.setTimestamp(rs.getString("Timestamp"));
        return a;
    }
}
