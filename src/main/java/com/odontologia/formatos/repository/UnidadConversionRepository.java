package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.UnidadConversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UnidadConversionRepository {

    public int insert(UnidadConversion conversion) throws SQLException {
        return insert(null, conversion);
    }

    public int insert(Connection con, UnidadConversion conversion) throws SQLException {
        String sql = "INSERT INTO Unidad_Conversion (MaterialID, UnidadBase, UnidadEmpaque, Factor) "
                + "VALUES (?, ?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conversion.getMaterialID());
            ps.setString(2, conversion.getUnidadBase());
            ps.setString(3, conversion.getUnidadEmpaque());
            ps.setDouble(4, conversion.getFactor());
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
        throw new SQLException("No se pudo obtener el id generado para Unidad_Conversion");
    }

    public void update(UnidadConversion conversion) throws SQLException {
        update(null, conversion);
    }

    public void update(Connection con, UnidadConversion conversion) throws SQLException {
        String sql = "UPDATE Unidad_Conversion SET MaterialID = ?, UnidadBase = ?, UnidadEmpaque = ?, "
                + "Factor = ? WHERE ConversionID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, conversion.getMaterialID());
            ps.setString(2, conversion.getUnidadBase());
            ps.setString(3, conversion.getUnidadEmpaque());
            ps.setDouble(4, conversion.getFactor());
            ps.setInt(5, conversion.getConversionID());
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void delete(int conversionID) throws SQLException {
        delete(null, conversionID);
    }

    public void delete(Connection con, int conversionID) throws SQLException {
        String sql = "DELETE FROM Unidad_Conversion WHERE ConversionID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, conversionID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public UnidadConversion findById(int conversionID) throws SQLException {
        String sql = "SELECT * FROM Unidad_Conversion WHERE ConversionID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, conversionID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public UnidadConversion findByMaterialEmpaque(int materialID, String unidadEmpaque) throws SQLException {
        String sql = "SELECT * FROM Unidad_Conversion WHERE MaterialID = ? AND UnidadEmpaque = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialID);
            ps.setString(2, unidadEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<UnidadConversion> findByMaterial(int materialID) throws SQLException {
        List<UnidadConversion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Unidad_Conversion WHERE MaterialID = ? ORDER BY UnidadEmpaque";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public List<UnidadConversion> findAll() throws SQLException {
        List<UnidadConversion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Unidad_Conversion ORDER BY MaterialID, UnidadEmpaque";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    private UnidadConversion rowToModel(ResultSet rs) throws SQLException {
        return new UnidadConversion(
                rs.getInt("ConversionID"),
                rs.getInt("MaterialID"),
                rs.getString("UnidadBase"),
                rs.getString("UnidadEmpaque"),
                rs.getDouble("Factor"));
    }
}
