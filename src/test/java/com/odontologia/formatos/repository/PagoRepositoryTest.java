package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.Pago;
import com.odontologia.formatos.model.Tratamiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PagoRepositoryTest extends BaseRepositoryTest {

    private final PagoRepository pagoRepository = new PagoRepository();
    private final TratamientoRepository tratamientoRepository = new TratamientoRepository();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private int tratamientoID;

    @BeforeEach
    void crearTratamiento() throws SQLException {
        Operador o = new Operador();
        o.setNombres("Ana");
        o.setApellidos("Perez");
        o.setGrado("PRE");
        o.setTipo("4");
        o.setPeriodo(2026);
        o.setEstado(1);
        int operadorID = operadorRepository.insert(o);

        Paciente p = new Paciente();
        p.setNombres("Juan");
        p.setApellidos("Lopez");
        int pacienteID = pacienteRepository.insert(p);

        Tratamiento t = new Tratamiento();
        t.setOperadorID(operadorID);
        t.setPacienteID(pacienteID);
        t.setFecha("2026-08-03");
        t.setNombreTratamiento("Test");
        t.setMonto(100.0);
        t.setTipo("NORMAL");
        t.setEstadoPago("PENDIENTE");
        t.setMontoPagado(0);
        t.setEstado("ABIERTO");
        tratamientoID = tratamientoRepository.insert(t);
    }

    @Test
    void insertYFindById() throws SQLException {
        int id = insertarPago(tratamientoID, "2026-08-10", 40.0);

        Pago p = pagoRepository.findById(id);
        assertNotNull(p);
        assertEquals(tratamientoID, p.getTratamientoID());
        assertEquals("2026-08-10", p.getFecha());
        assertEquals(40.0, p.getMonto(), 0.001);
    }

    @Test
    void updateModificaMontoYFecha() throws SQLException {
        int id = insertarPago(tratamientoID, "2026-08-10", 40.0);

        Pago p = pagoRepository.findById(id);
        p.setMonto(60.0);
        p.setFecha("2026-08-12");
        pagoRepository.update(p);

        Pago actualizado = pagoRepository.findById(id);
        assertEquals(60.0, actualizado.getMonto(), 0.001);
        assertEquals("2026-08-12", actualizado.getFecha());
    }

    @Test
    void deleteEliminaRegistro() throws SQLException {
        int id = insertarPago(tratamientoID, "2026-08-10", 40.0);

        pagoRepository.delete(id);

        assertNull(pagoRepository.findById(id));
    }

    @Test
    void findByTratamientoOrdenaPorFecha() throws SQLException {
        insertarPago(tratamientoID, "2026-08-10", 40.0);
        insertarPago(tratamientoID, "2026-08-05", 10.0);

        List<Pago> pagos = pagoRepository.findByTratamiento(tratamientoID);
        assertEquals(2, pagos.size());
        assertEquals("2026-08-05", pagos.get(0).getFecha());
        assertEquals("2026-08-10", pagos.get(1).getFecha());
    }

    @Test
    void sumByTratamientoAcumulaMontos() throws SQLException {
        insertarPago(tratamientoID, "2026-08-10", 40.0);
        insertarPago(tratamientoID, "2026-08-12", 60.0);

        assertEquals(100.0, pagoRepository.sumByTratamiento(tratamientoID), 0.001);
    }

    @Test
    void sumByTratamientoSinPagosEsCero() throws SQLException {
        assertEquals(0.0, pagoRepository.sumByTratamiento(tratamientoID), 0.001);
    }

    private int insertarPago(int tratamientoID, String fecha, double monto) throws SQLException {
        Pago pago = new Pago();
        pago.setTratamientoID(tratamientoID);
        pago.setFecha(fecha);
        pago.setMonto(monto);
        return pagoRepository.insert(pago);
    }
}
