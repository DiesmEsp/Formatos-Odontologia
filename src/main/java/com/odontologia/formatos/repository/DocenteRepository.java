package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Docente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocenteRepository {

    public int insert(Docente docente) throws SQLException {
        String sql = "INSERT INTO Docentes (Nombres, Apellidos, Estado) VALUES (?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, docente.getNombres());
            ps.setString(2, docente.getApellidos());
            ps.setInt(3, docente.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Docentes");
    }

    public void update(Docente docente) throws SQLException {
        String sql = "UPDATE Docentes SET Nombres = ?, Apellidos = ?, Estado = ? WHERE DocenteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docente.getNombres());
            ps.setString(2, docente.getApellidos());
            ps.setInt(3, docente.getEstado());
            ps.setInt(4, docente.getDocenteID());
            ps.executeUpdate();
        }
    }

    public void delete(int docenteID) throws SQLException {
        String sql = "DELETE FROM Docentes WHERE DocenteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docenteID);
            ps.executeUpdate();
        }
    }

    public Docente findById(int docenteID) throws SQLException {
        String sql = "SELECT * FROM Docentes WHERE DocenteID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docenteID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<Docente> findAll() throws SQLException {
        List<Docente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Docentes ORDER BY Apellidos, Nombres";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    public List<Docente> buscarPorTexto(String texto) throws SQLException {
        List<Docente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Docentes WHERE Nombres LIKE ? OR Apellidos LIKE ? ORDER BY Apellidos, Nombres";
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

    private Docente rowToModel(ResultSet rs) throws SQLException {
        return new Docente(
                rs.getInt("DocenteID"),
                rs.getString("Nombres"),
                rs.getString("Apellidos"),
                rs.getInt("Estado"));
    }
}
