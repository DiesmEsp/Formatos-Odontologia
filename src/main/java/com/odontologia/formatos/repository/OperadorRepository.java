package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Operador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OperadorRepository {

    public int insert(Operador operador) throws SQLException {
        String sql = "INSERT INTO Operadores (Nombres, Apellidos, DNI, Grado, Tipo, Periodo, Estado, ClinicaID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, operador.getNombres());
            ps.setString(2, operador.getApellidos());
            ps.setString(3, operador.getDni());
            ps.setString(4, operador.getGrado());
            ps.setString(5, operador.getTipo());
            ps.setInt(6, operador.getPeriodo());
            ps.setInt(7, operador.getEstado());
            ps.setInt(8, operador.getClinicaID());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Operadores");
    }

    public void update(Operador operador) throws SQLException {
        String sql = "UPDATE Operadores SET Nombres = ?, Apellidos = ?, DNI = ?, Grado = ?, Tipo = ?, Periodo = ?, Estado = ?, ClinicaID = ? WHERE OperadorID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, operador.getNombres());
            ps.setString(2, operador.getApellidos());
            ps.setString(3, operador.getDni());
            ps.setString(4, operador.getGrado());
            ps.setString(5, operador.getTipo());
            ps.setInt(6, operador.getPeriodo());
            ps.setInt(7, operador.getEstado());
            ps.setInt(8, operador.getClinicaID());
            ps.setInt(9, operador.getOperadorID());
            ps.executeUpdate();
        }
    }

    public void delete(int operadorID) throws SQLException {
        String sql = "DELETE FROM Operadores WHERE OperadorID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operadorID);
            ps.executeUpdate();
        }
    }

    public Operador findById(int operadorID) throws SQLException {
        String sql = "SELECT * FROM Operadores WHERE OperadorID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operadorID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Operador> findAll(int clinicaID) throws SQLException {
        List<Operador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Operadores WHERE ClinicaID = ? ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public List<Operador> findByPeriodo(int periodo, int clinicaID) throws SQLException {
        List<Operador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Operadores WHERE Periodo = ? AND ClinicaID = ? ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, periodo);
            ps.setInt(2, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public List<Operador> buscarPorTexto(String texto, int clinicaID) throws SQLException {
        List<Operador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Operadores WHERE ClinicaID = ? AND (Nombres LIKE ? OR Apellidos LIKE ?) ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setInt(1, clinicaID);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    private Operador rowToModel(ResultSet rs) throws SQLException {
        return new Operador(
                rs.getInt("OperadorID"),
                rs.getString("Nombres"),
                rs.getString("Apellidos"),
                rs.getString("DNI"),
                rs.getString("Grado"),
                rs.getString("Tipo"),
                rs.getInt("Periodo"),
                rs.getInt("Estado"),
                rs.getInt("ClinicaID"));
    }
}
