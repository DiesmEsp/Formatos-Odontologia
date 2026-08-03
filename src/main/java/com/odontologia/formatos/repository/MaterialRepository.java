package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Materiales;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaterialRepository {

    public int insert(Materiales material) throws SQLException {
        String sql = "INSERT INTO Materiales (Nombre, Unidad, Estado) VALUES (?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, material.getNombre());
            ps.setString(2, material.getUnidad());
            ps.setInt(3, material.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Materiales");
    }

    public void update(Materiales material) throws SQLException {
        String sql = "UPDATE Materiales SET Nombre = ?, Unidad = ?, Estado = ? WHERE MaterialID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, material.getNombre());
            ps.setString(2, material.getUnidad());
            ps.setInt(3, material.getEstado());
            ps.setInt(4, material.getMaterialID());
            ps.executeUpdate();
        }
    }

    public void delete(int materialID) throws SQLException {
        String sql = "DELETE FROM Materiales WHERE MaterialID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialID);
            ps.executeUpdate();
        }
    }

    public Materiales findById(int materialID) throws SQLException {
        String sql = "SELECT * FROM Materiales WHERE MaterialID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public Materiales findByNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM Materiales WHERE Nombre = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Materiales> findAll() throws SQLException {
        List<Materiales> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales ORDER BY Nombre";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    public List<Materiales> buscarPorTexto(String texto) throws SQLException {
        List<Materiales> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales WHERE Nombre LIKE ? ORDER BY Nombre";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    private Materiales rowToModel(ResultSet rs) throws SQLException {
        return new Materiales(
                rs.getInt("MaterialID"),
                rs.getString("Nombre"),
                rs.getString("Unidad"),
                rs.getInt("Estado"));
    }
}
