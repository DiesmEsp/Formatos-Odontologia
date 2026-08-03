package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Unidad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UnidadRepository {

    public int insert(Unidad unidad) throws SQLException {
        String sql = "INSERT INTO Unidad (UnidadNro) VALUES (?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, unidad.getUnidadNro());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Unidad");
    }

    public void delete(int unidadID) throws SQLException {
        String sql = "DELETE FROM Unidad WHERE UnidadID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            ps.executeUpdate();
        }
    }

    public Unidad findById(int unidadID) throws SQLException {
        String sql = "SELECT * FROM Unidad WHERE UnidadID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Unidad> findAll() throws SQLException {
        List<Unidad> lista = new ArrayList<>();
        String sql = "SELECT * FROM Unidad ORDER BY UnidadNro";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    public int maxUnidadNro() throws SQLException {
        String sql = "SELECT COALESCE(MAX(UnidadNro), 0) FROM Unidad";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Unidad rowToModel(ResultSet rs) throws SQLException {
        return new Unidad(
                rs.getInt("UnidadID"),
                rs.getInt("UnidadNro"));
    }
}
