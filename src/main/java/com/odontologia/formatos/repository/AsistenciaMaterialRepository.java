package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.AsistenciaMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaMaterialRepository {

    public int insert(AsistenciaMaterial item) throws SQLException {
        return insert(null, item);
    }

    public int insert(Connection con, AsistenciaMaterial item) throws SQLException {
        String sql = "INSERT INTO Materiales_Asistencia (AsistenciaID, MaterialesID, Cantidad) VALUES (?, ?, ?)";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getAsistenciaID());
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
        throw new SQLException("No se pudo obtener el id generado para Materiales_Asistencia");
    }

    public void update(AsistenciaMaterial item) throws SQLException {
        update(null, item);
    }

    public void update(Connection con, AsistenciaMaterial item) throws SQLException {
        String sql = "UPDATE Materiales_Asistencia SET Cantidad = ? WHERE MatAsistenciaID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDouble(1, item.getCantidad());
            ps.setInt(2, item.getMatAsistenciaID());
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void delete(int matAsistenciaID) throws SQLException {
        delete(null, matAsistenciaID);
    }

    public void delete(Connection con, int matAsistenciaID) throws SQLException {
        String sql = "DELETE FROM Materiales_Asistencia WHERE MatAsistenciaID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, matAsistenciaID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public void deleteByAsistenciaID(int asistenciaID) throws SQLException {
        deleteByAsistenciaID(null, asistenciaID);
    }

    public void deleteByAsistenciaID(Connection con, int asistenciaID) throws SQLException {
        String sql = "DELETE FROM Materiales_Asistencia WHERE AsistenciaID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            ps.executeUpdate();
        } finally {
            if (cerrarConexion) {
                conexion.close();
            }
        }
    }

    public List<AsistenciaMaterial> findByAsistenciaID(int asistenciaID) throws SQLException {
        List<AsistenciaMaterial> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales_Asistencia WHERE AsistenciaID = ? ORDER BY MatAsistenciaID";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public AsistenciaMaterial findByMaterial(int asistenciaID, int materialID) throws SQLException {
        return findByMaterial(null, asistenciaID, materialID);
    }

    public AsistenciaMaterial findByMaterial(Connection con, int asistenciaID, int materialID) throws SQLException {
        String sql = "SELECT * FROM Materiales_Asistencia WHERE AsistenciaID = ? AND MaterialesID = ?";
        boolean cerrarConexion = con == null;
        Connection conexion = cerrarConexion ? ConnectionManager.getInstance().getConnection() : con;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
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

    public List<MaterialConCantidad> findMaterialesConNombre(int asistenciaID) throws SQLException {
        List<MaterialConCantidad> lista = new ArrayList<>();
        String sql = "SELECT ma.MaterialesID, m.Nombre, m.Unidad, ma.Cantidad "
                + "FROM Materiales_Asistencia ma "
                + "JOIN Materiales m ON m.MaterialID = ma.MaterialesID "
                + "WHERE ma.AsistenciaID = ? ORDER BY m.Nombre";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, asistenciaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MaterialConCantidad(
                            rs.getInt("MaterialesID"),
                            rs.getString("Nombre"),
                            rs.getString("Unidad"),
                            rs.getDouble("Cantidad")));
                }
            }
        }
        return lista;
    }

    private AsistenciaMaterial rowToModel(ResultSet rs) throws SQLException {
        return new AsistenciaMaterial(
                rs.getInt("MatAsistenciaID"),
                rs.getInt("AsistenciaID"),
                rs.getInt("MaterialesID"),
                rs.getDouble("Cantidad"));
    }

    public static class MaterialConCantidad {
        private final int materialID;
        private final String nombre;
        private final String unidad;
        private final double cantidad;

        public MaterialConCantidad(int materialID, String nombre, String unidad, double cantidad) {
            this.materialID = materialID;
            this.nombre = nombre;
            this.unidad = unidad;
            this.cantidad = cantidad;
        }

        public int getMaterialID() {
            return materialID;
        }

        public String getNombre() {
            return nombre;
        }

        public String getUnidad() {
            return unidad;
        }

        public double getCantidad() {
            return cantidad;
        }
    }
}
