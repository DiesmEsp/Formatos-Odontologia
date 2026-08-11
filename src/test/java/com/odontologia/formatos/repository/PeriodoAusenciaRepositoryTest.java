package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.model.PeriodoAusencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoAusenciaRepositoryTest extends BaseRepositoryTest {

    private final PeriodoAusenciaRepository repository = new PeriodoAusenciaRepository();
    private final AsistenciaRepository asistenciaRepository = new AsistenciaRepository();
    private int asistenciaID;

    @BeforeEach
    void crearAsistencia() throws SQLException {
        DocenteRepository docenteRepository = new DocenteRepository();
        Docente docente = new Docente();
        docente.setNombres("Carlos");
        docente.setApellidos("Lopez");
        docente.setEstado(1);
        int docenteID = docenteRepository.insert(docente);

        Asistencia a = new Asistencia();
        a.setDocenteID(docenteID);
        a.setFecha("2026-08-03");
        a.setEstado("ACTIVO");
        a.setHoraEntrada("08:00:00");
        asistenciaID = asistenciaRepository.insert(a);
    }

    @Test
    void insertYFindById() throws SQLException {
        PeriodoAusencia pa = new PeriodoAusencia();
        pa.setAsistenciaID(asistenciaID);
        pa.setHoraInicio("10:00:00");
        pa.setMotivo("Reunion");
        int id = repository.insert(pa);

        PeriodoAusencia encontrado = repository.findById(id);
        assertNotNull(encontrado);
        assertEquals(asistenciaID, encontrado.getAsistenciaID());
        assertEquals("10:00:00", encontrado.getHoraInicio());
        assertEquals("Reunion", encontrado.getMotivo());
        assertNull(encontrado.getHoraFin());
    }

    @Test
    void findByAsistenciaID() throws SQLException {
        insertarAusencia("09:00:00", null);
        insertarAusencia("12:00:00", null);

        List<PeriodoAusencia> ausencias = repository.findByAsistenciaID(asistenciaID);
        assertEquals(2, ausencias.size());
        assertEquals("09:00:00", ausencias.get(0).getHoraInicio());
        assertEquals("12:00:00", ausencias.get(1).getHoraInicio());
    }

    @Test
    void findAbiertaEncuentraSinHoraFin() throws SQLException {
        insertarAusencia("10:00:00", null);
        insertarAusencia("11:00:00", "11:30:00");

        PeriodoAusencia abierta = repository.findAbierta(null, asistenciaID);
        assertNotNull(abierta);
        assertEquals("10:00:00", abierta.getHoraInicio());
        assertNull(abierta.getHoraFin());
    }

    @Test
    void findAbiertaRetornaNullSiTodasCerradas() throws SQLException {
        PeriodoAusencia pa = insertarAusencia("10:00:00", null);
        repository.finalizar(null, pa.getAusenciaID(), "11:00:00");

        PeriodoAusencia abierta = repository.findAbierta(null, asistenciaID);
        assertNull(abierta);
    }

    @Test
    void finalizarActualizaHoraFin() throws SQLException {
        PeriodoAusencia pa = insertarAusencia("10:00:00", null);
        repository.finalizar(null, pa.getAusenciaID(), "11:00:00");

        PeriodoAusencia actualizado = repository.findById(pa.getAusenciaID());
        assertEquals("11:00:00", actualizado.getHoraFin());
    }

    @Test
    void deleteEliminaRegistro() throws SQLException {
        PeriodoAusencia pa = insertarAusencia("10:00:00", null);
        repository.delete(pa.getAusenciaID());

        assertNull(repository.findById(pa.getAusenciaID()));
    }

    @Test
    void insertSinMotivoGuardaNull() throws SQLException {
        PeriodoAusencia pa = new PeriodoAusencia();
        pa.setAsistenciaID(asistenciaID);
        pa.setHoraInicio("10:00:00");
        int id = repository.insert(pa);

        PeriodoAusencia encontrado = repository.findById(id);
        assertNull(encontrado.getMotivo());
    }

    @Test
    void findByAsistenciaIDOrdenadoPorHoraInicio() throws SQLException {
        insertarAusencia("14:00:00", null);
        insertarAusencia("09:00:00", null);
        insertarAusencia("11:30:00", null);

        List<PeriodoAusencia> ausencias = repository.findByAsistenciaID(asistenciaID);
        assertEquals(3, ausencias.size());
        assertEquals("09:00:00", ausencias.get(0).getHoraInicio());
        assertEquals("11:30:00", ausencias.get(1).getHoraInicio());
        assertEquals("14:00:00", ausencias.get(2).getHoraInicio());
    }

    @Test
    void findByAsistenciaIDSinRegistrosRetornaListaVacia() throws SQLException {
        List<PeriodoAusencia> ausencias = repository.findByAsistenciaID(asistenciaID);
        assertTrue(ausencias.isEmpty());
    }

    private PeriodoAusencia insertarAusencia(String horaInicio, String horaFin) throws SQLException {
        PeriodoAusencia pa = new PeriodoAusencia();
        pa.setAsistenciaID(asistenciaID);
        pa.setHoraInicio(horaInicio);
        pa.setHoraFin(horaFin);
        int id = repository.insert(pa);
        pa.setAusenciaID(id);
        return pa;
    }
}
