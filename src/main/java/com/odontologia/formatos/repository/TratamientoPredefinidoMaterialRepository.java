package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TratamientoPredefinidoMaterialRepository {

    public int insert(TratamientoPredefinidoMaterial item) throws SQLException {
        String sql = "INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES (?, ?, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getTratPredID());
            ps.setInt(2, item.getMaterialID());
            ps.setDouble(3, item.getCantidad());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para Materiales_List_PRED");
    }

    public void update(TratamientoPredefinidoMaterial item) throws SQLException {
        String sql = "UPDATE Materiales_List_PRED SET TratPredID = ?, MaterialID = ?, Cantidad = ? WHERE MaterialListPredID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getTratPredID());
            ps.setInt(2, item.getMaterialID());
            ps.setDouble(3, item.getCantidad());
            ps.setInt(4, item.getMaterialListPredID());
            ps.executeUpdate();
        }
    }

    public void delete(int materialListPredID) throws SQLException {
        String sql = "DELETE FROM Materiales_List_PRED WHERE MaterialListPredID = ?";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialListPredID);
            ps.executeUpdate();
        }
    }

    public List<TratamientoPredefinidoMaterial> findByTratPredID(int tratPredID) throws SQLException {
        List<TratamientoPredefinidoMaterial> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales_List_PRED WHERE TratPredID = ? ORDER BY MaterialListPredID";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratPredID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rowToModel(rs));
                }
            }
        }
        return lista;
    }

    public void deleteByTratPredID(int tratPredID) throws SQLException {
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            deleteByTratPredID(con, tratPredID);
        }
    }

    public void deleteByTratPredID(Connection con, int tratPredID) throws SQLException {
        String sql = "DELETE FROM Materiales_List_PRED WHERE TratPredID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratPredID);
            ps.executeUpdate();
        }
    }

    private TratamientoPredefinidoMaterial rowToModel(ResultSet rs) throws SQLException {
        return new TratamientoPredefinidoMaterial(
                rs.getInt("MaterialListPredID"),
                rs.getInt("TratPredID"),
                rs.getInt("MaterialID"),
                rs.getDouble("Cantidad"));
    }
}
