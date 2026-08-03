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
        String sql = "INSERT INTO Operadores (Nombres, Apellidos, Grado, Tipo, Periodo, Estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, operador.getNombres());
            ps.setString(2, operador.getApellidos());
            ps.setString(3, operador.getGrado());
            ps.setString(4, operador.getTipo());
            ps.setInt(5, operador.getPeriodo());
            ps.setInt(6, operador.getEstado());
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
        String sql = "UPDATE Operadores SET Nombres = ?, Apellidos = ?, Grado = ?, Tipo = ?, Periodo = ?, Estado = ? WHERE OperadorID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, operador.getNombres());
            ps.setString(2, operador.getApellidos());
            ps.setString(3, operador.getGrado());
            ps.setString(4, operador.getTipo());
            ps.setInt(5, operador.getPeriodo());
            ps.setInt(6, operador.getEstado());
            ps.setInt(7, operador.getOperadorID());
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

    public List<Operador> findAll() throws SQLException {
        List<Operador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Operadores ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    public List<Operador> findByPeriodo(int periodo) throws SQLException {
        List<Operador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Operadores WHERE Periodo = ? ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, periodo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public List<Operador> buscarPorTexto(String texto) throws SQLException {
        List<Operador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Operadores WHERE Nombres LIKE ? OR Apellidos LIKE ? ORDER BY Apellidos, Nombres";
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

    private Operador rowToModel(ResultSet rs) throws SQLException {
        return new Operador(
                rs.getInt("OperadorID"),
                rs.getString("Nombres"),
                rs.getString("Apellidos"),
                rs.getString("Grado"),
                rs.getString("Tipo"),
                rs.getInt("Periodo"),
                rs.getInt("Estado"));
    }
}
