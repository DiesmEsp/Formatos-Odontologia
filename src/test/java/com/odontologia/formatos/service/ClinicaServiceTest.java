package com.odontologia.formatos.service;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Clinica;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClinicaServiceTest extends BaseRepositoryTest {

    private final ClinicaService service = new ClinicaService();

    @Test
    void crearClinicaValida() throws SQLException {
        int id = service.crear("Clínica 5 ODP", "Pos Grado");
        Clinica c = service.buscarPorId(id);
        assertEquals("Clínica 5 ODP", c.getNombre());
        assertEquals("Pos Grado", c.getGrupo());
        assertEquals(1, c.getEstado());
    }

    @Test
    void rechazaNombreObligatorio() {
        assertThrows(NegocioException.class, () -> service.crear("", null));
        assertThrows(NegocioException.class, () -> service.crear(null, null));
    }

    @Test
    void rechazaNombreDuplicado() throws SQLException {
        service.crear("Clínica 5 ODP", null);
        assertThrows(EntidadDuplicadaException.class, () -> service.crear("Clínica 5 ODP", "Pos Grado"));
    }

    @Test
    void actualizarCambiaNombre() throws SQLException {
        int id = service.crear("Antigua", null);
        Clinica c = service.buscarPorId(id);
        c.setNombre("Clínica Renovada");
        service.actualizar(c);
        assertEquals("Clínica Renovada", service.buscarPorId(id).getNombre());
    }

    @Test
    void listarDevuelveSoloActivas() throws SQLException {
        service.crear("Clínica B", null);
        service.crear("Clínica A", null);
        List<Clinica> clinicas = service.listar();
        assertEquals(2, clinicas.size());
        assertEquals("Clínica A", clinicas.get(0).getNombre());
        assertEquals("Clínica B", clinicas.get(1).getNombre());
    }

    @Test
    void eliminarClinicaVacia() throws SQLException {
        service.crear("Clínica Uno", null);
        int id = service.crear("Clínica Vacía", null);
        service.eliminar(id);
        assertEquals(null, service.buscarPorId(id));
    }

    @Test
    void eliminarClinicaConRegistrosBloqueado() throws SQLException {
        int id = service.crear("Clínica Con Data", null);
        insertarOperador(id);
        assertThrows(NegocioException.class, () -> service.eliminar(id));
    }

    private void insertarOperador(int clinicaID) throws SQLException {
        String sql = "INSERT INTO Operadores (Nombres, Apellidos, DNI, Grado, Tipo, Periodo, Estado, ClinicaID) " +
                "VALUES (?, ?, ?, ?, ?, ?, 1, ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "Juan");
            ps.setString(2, "Perez");
            ps.setString(3, "12345678");
            ps.setString(4, "PRE");
            ps.setString(5, "3");
            ps.setInt(6, 2026);
            ps.setInt(7, clinicaID);
            ps.executeUpdate();
        }
    }
}