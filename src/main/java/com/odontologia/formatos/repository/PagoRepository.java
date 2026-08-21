package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PagoRepository {

    public int insert(Pago pago) throws SQLException {
        return insert(null, pago);
    }

    public int insert(Connection con, Pago pago) throws SQLException {
        String sql = "INSERT INTO Pago (TratamientoID, Fecha, Monto, AvanceID) VALUES (?, ?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getTratamientoID());
            ps.setString(2, pago.getFecha());
            ps.setDouble(3, pago.getMonto());
            if (pago.getAvanceID() != null) {
                ps.setInt(4, pago.getAvanceID());
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
        throw new SQLException("No se pudo obtener el id generado para Pago");
    }

    public void update(Pago pago) throws SQLException {
        update(null, pago);
    }

    public void update(Connection con, Pago pago) throws SQLException {
        String sql = "UPDATE Pago SET Fecha = ?, Monto = ? WHERE PagoID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pago.getFecha());
            ps.setDouble(2, pago.getMonto());
            ps.setInt(3, pago.getPagoID());
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void delete(int pagoID) throws SQLException {
        delete(null, pagoID);
    }

    public void delete(Connection con, int pagoID) throws SQLException {
        String sql = "DELETE FROM Pago WHERE PagoID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, pagoID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void deleteByAvanceID(Connection con, int avanceID) throws SQLException {
        String sql = "DELETE FROM Pago WHERE AvanceID = ?";
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

    public Pago findById(int pagoID) throws SQLException {
        String sql = "SELECT * FROM Pago WHERE PagoID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pagoID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Pago> findByTratamiento(int tratamientoID) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pago WHERE TratamientoID = ? ORDER BY Fecha, PagoID";
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

    public List<Pago> findByAvanceID(int avanceID) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pago WHERE AvanceID = ? ORDER BY Fecha, PagoID";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, avanceID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public double sumByTratamiento(int tratamientoID) throws SQLException {
        return sumByTratamiento(null, tratamientoID);
    }

    public double sumByTratamiento(Connection con, int tratamientoID) throws SQLException {
        String sql = "SELECT COALESCE(SUM(Monto), 0) AS Total FROM Pago WHERE TratamientoID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("Total") : 0;
            }
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    private Pago rowToModel(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setPagoID(rs.getInt("PagoID"));
        p.setTratamientoID(rs.getInt("TratamientoID"));
        int avanceID = rs.getInt("AvanceID");
        p.setAvanceID(rs.wasNull() ? null : avanceID);
        p.setFecha(rs.getString("Fecha"));
        p.setMonto(rs.getDouble("Monto"));
        p.setTimestamp(rs.getString("Timestamp"));
        return p;
    }
}
