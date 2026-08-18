package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.MaterialAvance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaterialAvanceRepository {

    public int insert(Connection con, MaterialAvance item) throws SQLException {
        String sql = "INSERT INTO Materiales_List_Avance (AvanceID, MaterialID, Cantidad) VALUES (?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getAvanceID());
            ps.setInt(2, item.getMaterialID());
            ps.setDouble(3, item.getCantidad());
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
        throw new SQLException("No se pudo obtener el id generado para Materiales_List_Avance");
    }

    public List<MaterialAvance> findByAvanceID(int avanceID) throws SQLException {
        List<MaterialAvance> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales_List_Avance WHERE AvanceID = ? ORDER BY MaterialesListAvanceID";
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

    private MaterialAvance rowToModel(ResultSet rs) throws SQLException {
        return new MaterialAvance(
                rs.getInt("MaterialesListAvanceID"),
                rs.getInt("AvanceID"),
                rs.getInt("MaterialID"),
                rs.getDouble("Cantidad"));
    }

    public List<MaterialConsolidado> consolidarPorTratamiento(int tratamientoID) throws SQLException {
        List<MaterialConsolidado> lista = new ArrayList<>();
        String sql = "SELECT m.MaterialID, m.Nombre, m.Unidad, SUM(u.cant) AS Cantidad "
                + "FROM ( "
                + "  SELECT ml.MaterialID AS MaterialID, ml.Cantidad AS cant "
                + "  FROM Materiales_List ml WHERE ml.TratamientoID = ? "
                + "  UNION ALL "
                + "  SELECT mla.MaterialID AS MaterialID, mla.Cantidad AS cant "
                + "  FROM Materiales_List_Avance mla "
                + "  JOIN Tratamiento_Avance a ON a.AvanceID = mla.AvanceID "
                + "  WHERE a.TratamientoID = ? AND a.Estado = 'ACTIVO' "
                + ") u "
                + "JOIN Materiales m ON m.MaterialID = u.MaterialID "
                + "GROUP BY m.MaterialID, m.Nombre, m.Unidad "
                + "ORDER BY m.Nombre";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            ps.setInt(2, tratamientoID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MaterialConsolidado(
                            rs.getInt("MaterialID"),
                            rs.getString("Nombre"),
                            rs.getString("Unidad"),
                            rs.getDouble("Cantidad")));
                }
            }
        }
        return lista;
    }

    public static class MaterialConsolidado {
        private final int materialID;
        private final String nombreMaterial;
        private final String unidad;
        private final double cantidad;

        public MaterialConsolidado(int materialID, String nombreMaterial, String unidad, double cantidad) {
            this.materialID = materialID;
            this.nombreMaterial = nombreMaterial;
            this.unidad = unidad;
            this.cantidad = cantidad;
        }

        public int getMaterialID() {
            return materialID;
        }

        public String getNombreMaterial() {
            return nombreMaterial;
        }

        public String getUnidad() {
            return unidad;
        }

        public double getCantidad() {
            return cantidad;
        }
    }
}
