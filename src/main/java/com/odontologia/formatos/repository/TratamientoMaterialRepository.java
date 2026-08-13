package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.TratamientoMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TratamientoMaterialRepository {

    public int insert(TratamientoMaterial item) throws SQLException {
        return insert(null, item);
    }

    public int insert(Connection con, TratamientoMaterial item) throws SQLException {
        String sql = "INSERT INTO Materiales_List (TratamientoID, MaterialID, Cantidad) VALUES (?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getTratamientoID());
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
        throw new SQLException("No se pudo obtener el id generado para Materiales_List");
    }

    public void update(TratamientoMaterial item) throws SQLException {
        update(null, item);
    }

    public void update(Connection con, TratamientoMaterial item) throws SQLException {
        String sql = "UPDATE Materiales_List SET Cantidad = ? WHERE MaterialesListID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDouble(1, item.getCantidad());
            ps.setInt(2, item.getMaterialesListID());
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void delete(int materialesListID) throws SQLException {
        delete(null, materialesListID);
    }

    public void delete(Connection con, int materialesListID) throws SQLException {
        String sql = "DELETE FROM Materiales_List WHERE MaterialesListID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, materialesListID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void deleteByTratamientoID(Connection con, int tratamientoID) throws SQLException {
        String sql = "DELETE FROM Materiales_List WHERE TratamientoID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public List<TratamientoMaterial> findByTratamientoID(int tratamientoID) throws SQLException {
        List<TratamientoMaterial> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales_List WHERE TratamientoID = ? ORDER BY MaterialesListID";
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

    public TratamientoMaterial findById(int materialesListID) throws SQLException {
        String sql = "SELECT * FROM Materiales_List WHERE MaterialesListID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialesListID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public TratamientoMaterial findByMaterial(int tratamientoID, int materialID) throws SQLException {
        return findByMaterial(null, tratamientoID, materialID);
    }

    public TratamientoMaterial findByMaterial(Connection con, int tratamientoID, int materialID) throws SQLException {
        String sql = "SELECT * FROM Materiales_List WHERE TratamientoID = ? AND MaterialID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            ps.setInt(2, materialID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public List<MaterialConCantidad> findMaterialesConNombre(int tratamientoID) throws SQLException {
        List<MaterialConCantidad> lista = new ArrayList<>();
        String sql = "SELECT ml.MaterialesListID, ml.MaterialID, m.Nombre, m.Unidad, ml.Cantidad "
                + "FROM Materiales_List ml "
                + "JOIN Materiales m ON m.MaterialID = ml.MaterialID "
                + "WHERE ml.TratamientoID = ? ORDER BY m.Nombre";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratamientoID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MaterialConCantidad(
                            rs.getInt("MaterialesListID"),
                            rs.getInt("MaterialID"),
                            rs.getString("Nombre"),
                            rs.getString("Unidad"),
                            rs.getDouble("Cantidad")));
                }
            }
        }
        return lista;
    }

    private TratamientoMaterial rowToModel(ResultSet rs) throws SQLException {
        return new TratamientoMaterial(
                rs.getInt("MaterialesListID"),
                rs.getInt("TratamientoID"),
                rs.getInt("MaterialID"),
                rs.getDouble("Cantidad"));
    }

    public static class MaterialConCantidad {
        private final int materialesListID;
        private final int materialID;
        private final String nombreMaterial;
        private final String unidad;
        private final double cantidad;

        public MaterialConCantidad(int materialesListID, int materialID, String nombreMaterial,
                                   String unidad, double cantidad) {
            this.materialesListID = materialesListID;
            this.materialID = materialID;
            this.nombreMaterial = nombreMaterial;
            this.unidad = unidad;
            this.cantidad = cantidad;
        }

        public int getMaterialesListID() {
            return materialesListID;
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
