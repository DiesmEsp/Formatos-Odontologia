package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Tratamiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TratamientoRepository {

    public int insert(Tratamiento tratamiento) throws SQLException {
        return insert(null, tratamiento);
    }

    public int insert(Connection con, Tratamiento tratamiento) throws SQLException {
        String sql = "INSERT INTO Tratamiento (OperadorID, PacienteID, UnidadID, Fecha, NombreTratamiento, "
                + "Monto, Tipo, EstadoPago, MontoPagado, Estado, CerradoEn, TratamientoPadreID, MontoAnterior) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tratamiento.getOperadorID());
            ps.setInt(2, tratamiento.getPacienteID());
            if (tratamiento.getUnidadID() != null) {
                ps.setInt(3, tratamiento.getUnidadID());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, tratamiento.getFecha());
            ps.setString(5, tratamiento.getNombreTratamiento());
            ps.setDouble(6, tratamiento.getMonto());
            ps.setString(7, tratamiento.getTipo());
            ps.setString(8, tratamiento.getEstadoPago());
            ps.setDouble(9, tratamiento.getMontoPagado());
            ps.setString(10, tratamiento.getEstado());
            ps.setString(11, tratamiento.getCerradoEn());
            if (tratamiento.getTratamientoPadreID() != null) {
                ps.setInt(12, tratamiento.getTratamientoPadreID());
            } else {
                ps.setNull(12, java.sql.Types.INTEGER);
            }
            if (tratamiento.getMontoAnterior() != null) {
                ps.setDouble(13, tratamiento.getMontoAnterior());
            } else {
                ps.setNull(13, java.sql.Types.REAL);
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
        throw new SQLException("No se pudo obtener el id generado para Tratamiento");
    }

    public void update(Tratamiento tratamiento) throws SQLException {
        update(null, tratamiento);
    }

    public void update(Connection con, Tratamiento tratamiento) throws SQLException {
        String sql = "UPDATE Tratamiento SET OperadorID = ?, PacienteID = ?, UnidadID = ?, Fecha = ?, "
                + "NombreTratamiento = ?, Monto = ?, Tipo = ?, EstadoPago = ?, MontoPagado = ?, "
                + "Estado = ?, CerradoEn = ?, TratamientoPadreID = ?, MontoAnterior = ? WHERE TratamientoID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tratamiento.getOperadorID());
            ps.setInt(2, tratamiento.getPacienteID());
            if (tratamiento.getUnidadID() != null) {
                ps.setInt(3, tratamiento.getUnidadID());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, tratamiento.getFecha());
            ps.setString(5, tratamiento.getNombreTratamiento());
            ps.setDouble(6, tratamiento.getMonto());
            ps.setString(7, tratamiento.getTipo());
            ps.setString(8, tratamiento.getEstadoPago());
            ps.setDouble(9, tratamiento.getMontoPagado());
            ps.setString(10, tratamiento.getEstado());
            ps.setString(11, tratamiento.getCerradoEn());
            if (tratamiento.getTratamientoPadreID() != null) {
                ps.setInt(12, tratamiento.getTratamientoPadreID());
            } else {
                ps.setNull(12, java.sql.Types.INTEGER);
            }
            if (tratamiento.getMontoAnterior() != null) {
                ps.setDouble(13, tratamiento.getMontoAnterior());
            } else {
                ps.setNull(13, java.sql.Types.REAL);
            }
            ps.setInt(14, tratamiento.getTratamientoID());
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public Tratamiento findById(int tratamientoID) throws SQLException {
        String sql = "SELECT * FROM Tratamiento WHERE TratamientoID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Tratamiento> findByEstado(String estado) throws SQLException {
        List<Tratamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento WHERE Estado = ? ORDER BY Fecha DESC, TratamientoID DESC";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public List<Tratamiento> findByUnidad(int unidadID) throws SQLException {
        List<Tratamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento WHERE UnidadID = ? ORDER BY Fecha DESC, TratamientoID DESC";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public boolean existeOtroAbiertoEnUnidad(int unidadID, int tratamientoExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM Tratamiento "
                + "WHERE UnidadID = ? AND Estado = 'ABIERTO' AND TratamientoID != ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            ps.setInt(2, tratamientoExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        }
    }

    public Tratamiento findAbiertoPorUnidad(int unidadID) throws SQLException {
        String sql = "SELECT * FROM Tratamiento WHERE UnidadID = ? AND Estado = 'ABIERTO' "
                + "ORDER BY TratamientoID DESC LIMIT 1";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    private Tratamiento rowToModel(ResultSet rs) throws SQLException {
        Tratamiento t = new Tratamiento();
        t.setTratamientoID(rs.getInt("TratamientoID"));
        t.setOperadorID(rs.getInt("OperadorID"));
        t.setPacienteID(rs.getInt("PacienteID"));
        int unidad = rs.getInt("UnidadID");
        t.setUnidadID(rs.wasNull() ? null : unidad);
        t.setFecha(rs.getString("Fecha"));
        t.setNombreTratamiento(rs.getString("NombreTratamiento"));
        t.setMonto(rs.getDouble("Monto"));
        t.setTipo(rs.getString("Tipo"));
        t.setEstadoPago(rs.getString("EstadoPago"));
        t.setMontoPagado(rs.getDouble("MontoPagado"));
        t.setEstado(rs.getString("Estado"));
        t.setCerradoEn(rs.getString("CerradoEn"));
        int padre = rs.getInt("TratamientoPadreID");
        t.setTratamientoPadreID(rs.wasNull() ? null : padre);
        double montoAnterior = rs.getDouble("MontoAnterior");
        t.setMontoAnterior(rs.wasNull() ? null : montoAnterior);
        return t;
    }

    public List<Tratamiento> findAvances(int tratamientoPadreID) throws SQLException {
        List<Tratamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento WHERE TratamientoPadreID = ? ORDER BY Fecha, TratamientoID";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratamientoPadreID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public List<Tratamiento> findCandidatosPadre(int pacienteID) throws SQLException {
        List<Tratamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento WHERE PacienteID = ? AND Tipo != 'AVANCE' "
                + "AND Estado IN ('ABIERTO', 'CERRADO') ORDER BY Fecha DESC, TratamientoID DESC";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }
}
