package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Clinica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClinicaRepository {

    public int insert(Clinica clinica) throws SQLException {
        String sql = "INSERT INTO Clinica (Nombre, Grupo, Estado) VALUES (?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, clinica.getNombre());
            ps.setString(2, clinica.getGrupo());
            ps.setInt(3, clinica.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Clinica");
    }

    public void update(Clinica clinica) throws SQLException {
        String sql = "UPDATE Clinica SET Nombre = ?, Grupo = ?, Estado = ? WHERE ClinicaID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clinica.getNombre());
            ps.setString(2, clinica.getGrupo());
            ps.setInt(3, clinica.getEstado());
            ps.setInt(4, clinica.getClinicaID());
            ps.executeUpdate();
        }
    }

    public void delete(int clinicaID) throws SQLException {
        String sql = "DELETE FROM Clinica WHERE ClinicaID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clinicaID);
            ps.executeUpdate();
        }
    }

    public Clinica findById(int clinicaID) throws SQLException {
        String sql = "SELECT * FROM Clinica WHERE ClinicaID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public Clinica findByNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM Clinica WHERE Nombre = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public Clinica findByNombreActiva(String nombre) throws SQLException {
        String sql = "SELECT * FROM Clinica WHERE Nombre = ? AND Estado = 1";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Clinica> findAll() throws SQLException {
        List<Clinica> lista = new ArrayList<>();
        String sql = "SELECT * FROM Clinica WHERE Estado = 1 ORDER BY Nombre";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    private Clinica rowToModel(ResultSet rs) throws SQLException {
        return new Clinica(
                rs.getInt("ClinicaID"),
                rs.getString("Nombre"),
                rs.getString("Grupo"),
                rs.getInt("Estado"));
    }
}