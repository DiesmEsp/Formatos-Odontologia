package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.Tratamiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TratamientoRepositoryTest extends BaseRepositoryTest {

    private final TratamientoRepository repository = new TratamientoRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private int operadorID;
    private int pacienteID;

    @BeforeEach
    void crearReferencias() throws SQLException {
        Operador o = new Operador();
        o.setNombres("Ana");
        o.setApellidos("Perez");
        o.setGrado("PRE");
        o.setTipo("4");
        o.setPeriodo(2026);
        o.setEstado(1);
        operadorID = operadorRepository.insert(o);

        Paciente p = new Paciente();
        p.setNombres("Juan");
        p.setApellidos("Lopez");
        pacienteID = pacienteRepository.insert(p);
    }

    @Test
    void insertYFindById() throws SQLException {
        int id = repository.insert(tratamiento(100.0));

        Tratamiento encontrado = repository.findById(id);
        assertNotNull(encontrado);
        assertEquals("ABIERTO", encontrado.getEstado());
        assertEquals("NORMAL", encontrado.getTipo());
        assertEquals("PENDIENTE", encontrado.getEstadoPago());
        assertEquals(100.0, encontrado.getMonto(), 0.001);
    }

    @Test
    void insertConUnidadNull() throws SQLException {
        Tratamiento t = tratamiento(50.0);
        t.setUnidadID(null);

        int id = repository.insert(t);

        Tratamiento encontrado = repository.findById(id);
        assertNull(encontrado.getUnidadID());
    }

    @Test
    void updateActualizaEstadoYCierre() throws SQLException {
        int id = repository.insert(tratamiento(100.0));

        Tratamiento cargado = repository.findById(id);
        cargado.setEstado("CERRADO");
        cargado.setCerradoEn("2026-08-03 10:00:00");
        repository.update(cargado);

        Tratamiento actualizado = repository.findById(id);
        assertEquals("CERRADO", actualizado.getEstado());
        assertEquals("2026-08-03 10:00:00", actualizado.getCerradoEn());
    }

    @Test
    void findAbiertoPorUnidad() throws SQLException {
        Tratamiento t = tratamiento(100.0);
        t.setUnidadID(1);
        int id = repository.insert(t);

        Tratamiento abierto = repository.findAbiertoPorUnidad(1);
        assertNotNull(abierto);
        assertEquals(id, abierto.getTratamientoID());
    }

    private Tratamiento tratamiento(double monto) {
        Tratamiento t = new Tratamiento();
        t.setOperadorID(operadorID);
        t.setPacienteID(pacienteID);
        t.setUnidadID(1);
        t.setFecha("2026-08-03");
        t.setNombreTratamiento("Exodoncia simple");
        t.setMonto(monto);
        t.setTipo("NORMAL");
        t.setEstadoPago("PENDIENTE");
        t.setMontoPagado(0);
        t.setEstado("ABIERTO");
        t.setCerradoEn(null);
        return t;
    }
}
