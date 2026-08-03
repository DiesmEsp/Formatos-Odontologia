package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.TratamientoPredefinido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TratamientoPredefinidoRepository {

    public int insert(TratamientoPredefinido tratamiento) throws SQLException {
        String sql = "INSERT INTO Tratamiento_PRED (NombreTratamiento, MontoSugerido) VALUES (?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tratamiento.getNombreTratamiento());
            if (tratamiento.getMontoSugerido() != null) {
                ps.setDouble(2, tratamiento.getMontoSugerido());
            } else {
                ps.setNull(2, java.sql.Types.REAL);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Tratamiento_PRED");
    }

    public void update(TratamientoPredefinido tratamiento) throws SQLException {
        String sql = "UPDATE Tratamiento_PRED SET NombreTratamiento = ?, MontoSugerido = ? WHERE TratPredID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tratamiento.getNombreTratamiento());
            if (tratamiento.getMontoSugerido() != null) {
                ps.setDouble(2, tratamiento.getMontoSugerido());
            } else {
                ps.setNull(2, java.sql.Types.REAL);
            }
            ps.setInt(3, tratamiento.getTratPredID());
            ps.executeUpdate();
        }
    }

    public void delete(int tratPredID) throws SQLException {
        String sql = "DELETE FROM Tratamiento_PRED WHERE TratPredID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratPredID);
            ps.executeUpdate();
        }
    }

    public TratamientoPredefinido findById(int tratPredID) throws SQLException {
        String sql = "SELECT * FROM Tratamiento_PRED WHERE TratPredID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratPredID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public TratamientoPredefinido findByNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM Tratamiento_PRED WHERE NombreTratamiento = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToModel(rs) : null;
            }
        }
    }

    public List<TratamientoPredefinido> findAll() throws SQLException {
        List<TratamientoPredefinido> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento_PRED ORDER BY NombreTratamiento";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rowToModel(rs));
            }
        }
        return lista;
    }

    public List<TratamientoPredefinido> buscarPorTexto(String texto) throws SQLException {
        List<TratamientoPredefinido> lista = new ArrayList<>();
        String sql = "SELECT * FROM Tratamiento_PRED WHERE NombreTratamiento LIKE ? ORDER BY NombreTratamiento";
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

    private TratamientoPredefinido rowToModel(ResultSet rs) throws SQLException {
        Double monto = rs.getObject("MontoSugerido") != null ? rs.getDouble("MontoSugerido") : null;
        return new TratamientoPredefinido(
                rs.getInt("TratPredID"),
                rs.getString("NombreTratamiento"),
                monto);
    }
}
