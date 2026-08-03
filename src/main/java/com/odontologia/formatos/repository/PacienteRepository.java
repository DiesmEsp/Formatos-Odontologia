package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PacienteRepository {

    public int insert(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO Pacientes (Nombres, Apellidos) VALUES (?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Pacientes");
    }

    public void update(Paciente paciente) throws SQLException {
        String sql = "UPDATE Pacientes SET Nombres = ?, Apellidos = ? WHERE PacienteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.setInt(3, paciente.getPacienteID());
            ps.executeUpdate();
        }
    }

    public void delete(int pacienteID) throws SQLException {
        String sql = "DELETE FROM Pacientes WHERE PacienteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteID);
            ps.executeUpdate();
        }
    }

    public Paciente findById(int pacienteID) throws SQLException {
        String sql = "SELECT * FROM Pacientes WHERE PacienteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pacienteID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Paciente> findAll() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pacientes ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    public List<Paciente> buscarPorTexto(String texto) throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pacientes WHERE Nombres LIKE ? OR Apellidos LIKE ? ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    private Paciente rowToModel(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("PacienteID"),
                rs.getString("Nombres"),
                rs.getString("Apellidos"));
    }
}
