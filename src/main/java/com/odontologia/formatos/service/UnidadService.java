package com.odontologia.formatos.service;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.repository.UnidadRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnidadService {

    private final UnidadRepository repository = new UnidadRepository();

    public int crear() throws SQLException {
        String sql = "INSERT INTO Unidad (UnidadNro) SELECT COALESCE(MAX(UnidadNro), 0) + 1 FROM Unidad";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo crear la unidad.");
    }

    public void eliminar(int unidadID) throws SQLException {
        if (tieneTratamientoAbierto(unidadID)) {
            throw new NegocioException("La unidad tiene un tratamiento en curso (ABIERTO) y no puede eliminarse.");
        }
        repository.delete(unidadID);
    }

    private boolean tieneTratamientoAbierto(int unidadID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tratamiento WHERE UnidadID = ? AND Estado = 'ABIERTO'";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidadID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
