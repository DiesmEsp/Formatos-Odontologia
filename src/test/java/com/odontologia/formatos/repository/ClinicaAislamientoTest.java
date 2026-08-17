package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.RegistroAnulacion;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.Unidad;
import com.odontologia.formatos.service.ClinicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el aislamiento multi-clínica: los datos de una clínica no deben
 * filtrarse en las consultas de otra clínica (catálogos, transacciones y
 * anulaciones).
 */
class ClinicaAislamientoTest extends BaseRepositoryTest {

    private final ClinicaService clinicaService = new ClinicaService();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final UnidadRepository unidadRepository = new UnidadRepository();
    private final TratamientoRepository tratamientoRepository = new TratamientoRepository();
    private final RegistroAnulacionRepository anulacionRepository = new RegistroAnulacionRepository();

    private int clinicaA;
    private int clinicaB;

    @BeforeEach
    void sembrarClinicas() throws SQLException {
        clinicaA = clinicaService.crear("Clinica A", null);
        clinicaB = clinicaService.crear("Clinica B", null);
    }

    @Test
    void catalogosNoCruzanClinicas() throws SQLException {
        insertarOperador(clinicaA, "Ana", "Perez");
        insertarOperador(clinicaB, "Beto", "Gomez");
        insertarPaciente(clinicaA, "Juan", "Lopez");
        insertarPaciente(clinicaB, "Maria", "Ruiz");
        insertarDocente(clinicaA, "Carlos", "Rojas");
        insertarDocente(clinicaB, "Luis", "Torres");
        insertarUnidad(clinicaA, 10);
        insertarUnidad(clinicaB, 20);

        List<Operador> operadoresA = operadorRepository.findAll(clinicaA);
        assertEquals(1, operadoresA.size());
        assertEquals("Perez", operadoresA.get(0).getApellidos());
        assertTrue(operadorRepository.findAll(clinicaB).stream().allMatch(o -> o.getClinicaID() == clinicaB));

        List<Paciente> pacientesA = pacienteRepository.findAll(clinicaA);
        assertEquals(1, pacientesA.size());
        assertEquals("Lopez", pacientesA.get(0).getApellidos());
        assertTrue(pacienteRepository.findAll(clinicaB).stream().allMatch(p -> p.getClinicaID() == clinicaB));

        List<Docente> docentesA = docenteRepository.findAll(clinicaA);
        assertEquals(1, docentesA.size());
        assertEquals("Rojas", docentesA.get(0).getApellidos());
        assertTrue(docenteRepository.findAll(clinicaB).stream().allMatch(d -> d.getClinicaID() == clinicaB));

        List<Unidad> unidadesA = unidadRepository.findAll(clinicaA);
        assertTrue(unidadesA.stream().anyMatch(u -> u.getUnidadNro() == 10));
        assertTrue(unidadesA.stream().noneMatch(u -> u.getUnidadNro() == 20));
        List<Unidad> unidadesB = unidadRepository.findAll(clinicaB);
        assertTrue(unidadesB.stream().anyMatch(u -> u.getUnidadNro() == 20));
        assertTrue(unidadesB.stream().noneMatch(u -> u.getUnidadNro() == 10));
    }

    @Test
    void numeracionUnidadesEsIndependientePorClinica() throws SQLException {
        insertarUnidad(clinicaA, 10);
        insertarUnidad(clinicaA, 11);
        insertarUnidad(clinicaB, 10);

        assertEquals(11, unidadRepository.maxUnidadNro(clinicaA));
        assertEquals(10, unidadRepository.maxUnidadNro(clinicaB));
    }

    @Test
    void tratamientosNoCruzanClinicas() throws SQLException {
        int operadorA = insertarOperador(clinicaA, "Ana", "Perez");
        int pacienteA = insertarPaciente(clinicaA, "Juan", "Lopez");
        int operadorB = insertarOperador(clinicaB, "Beto", "Gomez");
        int pacienteB = insertarPaciente(clinicaB, "Maria", "Ruiz");

        insertarTratamiento(operadorA, pacienteA, clinicaA, "2026-08-01", "Tratamiento de A");
        insertarTratamiento(operadorB, pacienteB, clinicaB, "2026-08-02", "Tratamiento de B");

        List<Tratamiento> tratamientosA = tratamientoRepository.findByEstado("CERRADO", clinicaA);
        assertEquals(1, tratamientosA.size());
        assertEquals("Tratamiento de A", tratamientosA.get(0).getNombreTratamiento());
        assertTrue(tratamientosA.stream().allMatch(t -> t.getClinicaID() == clinicaA));

        List<Tratamiento> tratamientosB = tratamientoRepository.findByEstado("CERRADO", clinicaB);
        assertEquals(1, tratamientosB.size());
        assertEquals("Tratamiento de B", tratamientosB.get(0).getNombreTratamiento());
        assertTrue(tratamientosB.stream().allMatch(t -> t.getClinicaID() == clinicaB));
    }

    @Test
    void anulacionesNoCruzanClinicas() throws SQLException {
        insertarAnulacion(clinicaA, "Tratamiento", 101, "Error de carga");
        insertarAnulacion(clinicaB, "Tratamiento", 202, "Duplicado");

        List<RegistroAnulacion> anulacionesA = anulacionRepository.findAll(clinicaA);
        assertEquals(1, anulacionesA.size());
        assertEquals(101, anulacionesA.get(0).getIdRegistroAnulado());
        assertTrue(anulacionesA.stream().allMatch(r -> r.getClinicaID() == clinicaA));

        List<RegistroAnulacion> anulacionesB = anulacionRepository.findAll(clinicaB);
        assertEquals(1, anulacionesB.size());
        assertEquals(202, anulacionesB.get(0).getIdRegistroAnulado());
    }

    private int insertarOperador(int clinicaID, String nombres, String apellidos) throws SQLException {
        Operador o = new Operador();
        o.setNombres(nombres);
        o.setApellidos(apellidos);
        o.setGrado("PRE");
        o.setTipo("4");
        o.setPeriodo(2026);
        o.setEstado(1);
        o.setClinicaID(clinicaID);
        return operadorRepository.insert(o);
    }

    private int insertarPaciente(int clinicaID, String nombres, String apellidos) throws SQLException {
        Paciente p = new Paciente();
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setClinicaID(clinicaID);
        return pacienteRepository.insert(p);
    }

    private int insertarDocente(int clinicaID, String nombres, String apellidos) throws SQLException {
        Docente d = new Docente();
        d.setNombres(nombres);
        d.setApellidos(apellidos);
        d.setEstado(1);
        d.setClinicaID(clinicaID);
        return docenteRepository.insert(d);
    }

    private void insertarUnidad(int clinicaID, int nro) throws SQLException {
        Unidad u = new Unidad();
        u.setUnidadNro(nro);
        u.setClinicaID(clinicaID);
        unidadRepository.insert(u);
    }

    private void insertarTratamiento(int operadorID, int pacienteID, int clinicaID,
                                     String fecha, String nombre) throws SQLException {
        Tratamiento t = new Tratamiento();
        t.setOperadorID(operadorID);
        t.setPacienteID(pacienteID);
        t.setUnidadID(null);
        t.setFecha(fecha);
        t.setNombreTratamiento(nombre);
        t.setMonto(100.0);
        t.setTipo("NORMAL");
        t.setEstadoPago("PENDIENTE");
        t.setMontoPagado(0);
        t.setEstado("CERRADO");
        t.setCerradoEn("2026-08-10 10:00:00");
        t.setClinicaID(clinicaID);
        tratamientoRepository.insert(t);
    }

    private void insertarAnulacion(int clinicaID, String tabla, int idRegistro, String motivo)
            throws SQLException {
        RegistroAnulacion r = new RegistroAnulacion();
        r.setTablaAfectada(tabla);
        r.setIdRegistroAnulado(idRegistro);
        r.setMotivo(motivo);
        r.setUsuario("test");
        r.setClinicaID(clinicaID);
        anulacionRepository.insert(r);
    }
}