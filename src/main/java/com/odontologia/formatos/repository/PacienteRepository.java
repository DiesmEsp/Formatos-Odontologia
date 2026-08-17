package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteRepository {

    public int insert(Paciente p) throws SQLException {
        String sql = "INSERT INTO Pacientes (Nombres, Apellidos, ClinicaID) VALUES (?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombres());
            ps.setString(2, p.getApellidos());
            ps.setInt(3, p.getClinicaID());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Pacientes");
    }

    public void update(Paciente p) throws SQLException {
        String sql = "UPDATE Pacientes SET Nombres = ?, Apellidos = ?, Estado = ?, ClinicaID = ? WHERE PacienteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombres());
            ps.setString(2, p.getApellidos());
            ps.setInt(3, p.getEstado());
            ps.setInt(4, p.getClinicaID());
            ps.setInt(5, p.getPacienteID());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Pacientes WHERE PacienteID = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Paciente findById(int id) throws SQLException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM Pacientes WHERE PacienteID = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Paciente> findAll(int clinicaID) throws SQLException {
        List<Paciente> list = new ArrayList<>();
        String sql = "SELECT * FROM Pacientes WHERE ClinicaID = ? ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToModel(rs));
            }
        }
        return list;
    }

    public List<Paciente> buscarPorTexto(String texto, int clinicaID) throws SQLException {
        List<Paciente> list = new ArrayList<>();
        String sql = "SELECT * FROM Pacientes WHERE ClinicaID = ? AND (Nombres LIKE ? OR Apellidos LIKE ?) ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setInt(1, clinicaID);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToModel(rs));
            }
        }
        return list;
    }

    private Paciente rowToModel(ResultSet rs) throws SQLException {
        return new Paciente(rs.getInt("PacienteID"), rs.getString("Nombres"), rs.getString("Apellidos"), rs.getInt("Estado"), rs.getInt("ClinicaID"));
    }
}
