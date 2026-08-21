package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.ConsumoClinica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ConsumoRepository {

    private static final String SELECT_BASE =
            "SELECT c.ConsumoID, c.Fecha, c.MaterialID, m.Nombre AS NombreMaterial, " +
            "m.Unidad, c.Cantidad, c.ClinicaID " +
            "FROM Consumo_Clinica c " +
            "JOIN Materiales m ON m.MaterialID = c.MaterialID ";

    public int insert(ConsumoClinica consumo, Connection con) throws SQLException {
        boolean propia = con == null;
        Connection c = propia ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Consumo_Clinica (Fecha, MaterialID, Cantidad, ClinicaID) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, consumo.getFecha());
            ps.setInt(2, consumo.getMaterialID());
            ps.setDouble(3, consumo.getCantidad());
            ps.setInt(4, consumo.getClinicaID());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    consumo.setConsumoID(id);
                    return id;
                }
            }
        } finally {
            if (propia) c.close();
        }
        throw new SQLException("No se pudo obtener el id generado para Consumo_Clinica");
    }

    /** @return true si se actualizó algún registro de la clínica indicada. */
    public boolean update(ConsumoClinica consumo, Connection con) throws SQLException {
        boolean propia = con == null;
        Connection c = propia ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE Consumo_Clinica SET Fecha = ?, MaterialID = ?, Cantidad = ? " +
                "WHERE ConsumoID = ? AND ClinicaID = ?")) {
            ps.setString(1, consumo.getFecha());
            ps.setInt(2, consumo.getMaterialID());
            ps.setDouble(3, consumo.getCantidad());
            ps.setInt(4, consumo.getConsumoID());
            ps.setInt(5, consumo.getClinicaID());
            return ps.executeUpdate() > 0;
        } finally {
            if (propia) c.close();
        }
    }

    /** @return true si se eliminó algún registro de la clínica indicada. */
    public boolean delete(int consumoID, int clinicaID, Connection con) throws SQLException {
        boolean propia = con == null;
        Connection c = propia ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM Consumo_Clinica WHERE ConsumoID = ? AND ClinicaID = ?")) {
            ps.setInt(1, consumoID);
            ps.setInt(2, clinicaID);
            return ps.executeUpdate() > 0;
        } finally {
            if (propia) c.close();
        }
    }

    public ConsumoClinica findById(int consumoID, int clinicaID) throws SQLException {
        String sql = SELECT_BASE + "WHERE c.ConsumoID = ? AND c.ClinicaID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, consumoID);
            ps.setInt(2, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<ConsumoClinica> findByMes(int anio, int mes, int clinicaID) throws SQLException {
        YearMonth ym = YearMonth.of(anio, mes);
        String desde = ym.atDay(1).toString();
        String hasta = ym.plusMonths(1).atDay(1).toString();
        String sql = SELECT_BASE + "WHERE c.ClinicaID = ? AND c.Fecha >= ? AND c.Fecha < ? " +
                "ORDER BY c.Fecha DESC, c.ConsumoID DESC";
        List<ConsumoClinica> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clinicaID);
            ps.setString(2, desde);
            ps.setString(3, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    private ConsumoClinica rowToModel(ResultSet rs) throws SQLException {
        return new ConsumoClinica(
                rs.getInt("ConsumoID"),
                rs.getString("Fecha"),
                rs.getInt("MaterialID"),
                rs.getString("NombreMaterial"),
                rs.getString("Unidad"),
                rs.getDouble("Cantidad"),
                rs.getInt("ClinicaID"));
    }
}
