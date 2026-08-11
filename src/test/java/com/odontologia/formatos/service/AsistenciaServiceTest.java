package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.model.PeriodoAusencia;
import com.odontologia.formatos.repository.AsistenciaMaterialRepository;
import com.odontologia.formatos.repository.AsistenciaRepository;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsistenciaServiceTest extends BaseRepositoryTest {

    private final AsistenciaService service = new AsistenciaService();
    private final AsistenciaRepository asistenciaRepository = new AsistenciaRepository();
    private final AsistenciaMaterialRepository materialRepository = new AsistenciaMaterialRepository();
    private int docenteID;

    @BeforeEach
    void crearDocente() throws SQLException {
        DocenteRepository docenteRepository = new DocenteRepository();
        Docente docente = new Docente();
        docente.setNombres("María");
        docente.setApellidos("González Rivas");
        docente.setEstado(1);
        docenteID = docenteRepository.insert(docente);
    }

    @Test
    void abrirDiaCreaRegistroActivo() throws SQLException {
        Asistencia dia = service.abrirDia(docenteID, "2026-08-03", "08:00:00");

        assertNotNull(dia);
        assertEquals("ACTIVO", dia.getEstado());
    }

    @Test
    void abrirDiaReutilizaRegistroExistente() throws SQLException {
        Asistencia primero = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        Asistencia segundo = service.abrirDia(docenteID, "2026-08-03", "08:00:00");

        assertEquals(primero.getAsistenciaID(), segundo.getAsistenciaID());
    }

    @Test
    void acumulaCantidadesDelMismoMaterial() throws SQLException {
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 2.0));
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 3.0));

        List<AsistenciaMaterialRepository.MaterialConCantidad> materiales = service.materialesDelDia(
                asistenciaRepository.findActivoPorDocenteYFecha(docenteID, "2026-08-03").getAsistenciaID());
        assertEquals(1, materiales.size());
        assertEquals(5.0, materiales.get(0).getCantidad(), 0.001);
    }

    @Test
    void agregaNuevoMaterialSinSobrescribir() throws SQLException {
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 2.0));
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(2, 1.5));

        List<AsistenciaMaterialRepository.MaterialConCantidad> materiales = service.materialesDelDia(
                asistenciaRepository.findActivoPorDocenteYFecha(docenteID, "2026-08-03").getAsistenciaID());
        assertEquals(2, materiales.size());
    }

    @Test
    void unSoloRegistroPorDocenteYFecha() throws SQLException {
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 2.0));
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 2.0));

        List<com.odontologia.formatos.model.AsistenciaMaterial> crudos = service.materialesCrudos(
                asistenciaRepository.findActivoPorDocenteYFecha(docenteID, "2026-08-03").getAsistenciaID());
        assertEquals(1, crudos.size());
        assertEquals(4.0, crudos.get(0).getCantidad(), 0.001);
    }

    @Test
    void rechazaDocenteInexistente() {
        assertThrows(NegocioException.class, () -> service.abrirDia(9999, "2026-08-03", "08:00:00"));
    }

    @Test
    void rechazaFechaInvalida() {
        assertThrows(NegocioException.class, () -> service.abrirDia(docenteID, "03/08/2026", "08:00:00"));
    }

    @Test
    void rechazaCantidadCero() {
        assertThrows(NegocioException.class,
                () -> service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 0.0)));
    }

    @Test
    void rechazaSinMateriales() {
        assertThrows(NegocioException.class,
                () -> service.registrarMateriales(docenteID, "2026-08-03", Map.of()));
    }

    @Test
    void rechazaMaterialInexistente() {
        assertThrows(NegocioException.class,
                () -> service.registrarMateriales(docenteID, "2026-08-03", Map.of(9999, 1.0)));
    }

    @Test
    void anularCambiaEstado() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        service.registrarMateriales(docenteID, "2026-08-03", Map.of(1, 2.0));

        service.anular(a.getAsistenciaID(), "Motivo de prueba");

        Asistencia anulada = asistenciaRepository.findById(a.getAsistenciaID());
        assertEquals("ANULADO", anulada.getEstado());
    }

    @Test
    void anularYaAnuladoArrojaError() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        service.anular(a.getAsistenciaID(), "Motivo");

        assertThrows(NegocioException.class, () -> service.anular(a.getAsistenciaID(), "Otra vez"));
    }

    @Test
    void anularSinMotivoArrojaError() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");

        assertThrows(NegocioException.class, () -> service.anular(a.getAsistenciaID(), null));
        assertThrows(NegocioException.class, () -> service.anular(a.getAsistenciaID(), "  "));
    }

    @Test
    void abrirDiaGuardaHoraEntrada() throws SQLException {
        Asistencia dia = service.abrirDia(docenteID, "2026-08-03", "08:30:00");

        assertEquals("08:30:00", dia.getHoraEntrada());
        assertNull(dia.getHoraSalida());
    }

    @Test
    void abrirDiaReutilizadoConservaHoraEntradaOriginal() throws SQLException {
        Asistencia primero = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        Asistencia segundo = service.abrirDia(docenteID, "2026-08-03", "09:00:00");

        assertEquals(primero.getAsistenciaID(), segundo.getAsistenciaID());
        assertEquals("08:00:00", segundo.getHoraEntrada());
    }

    @Test
    void registrarSalidaGuardaHora() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        service.registrarSalida(a.getAsistenciaID(), "13:00:00");

        Asistencia actualizada = asistenciaRepository.findById(a.getAsistenciaID());
        assertEquals("13:00:00", actualizada.getHoraSalida());
    }

    @Test
    void registrarSalidaDosVecesArrojaError() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        service.registrarSalida(a.getAsistenciaID(), "13:00:00");

        assertThrows(NegocioException.class,
                () -> service.registrarSalida(a.getAsistenciaID(), "14:00:00"));
    }

    @Test
    void registrarSalidaConAusenciaAbiertaArrojaError() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        service.iniciarAusencia(a.getAsistenciaID(), "10:00:00", null);

        assertThrows(NegocioException.class,
                () -> service.registrarSalida(a.getAsistenciaID(), "13:00:00"));
    }

    @Test
    void iniciarAusenciaCreaPeriodo() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        PeriodoAusencia ausencia = service.iniciarAusencia(a.getAsistenciaID(), "10:00:00", "Reunion");

        assertNotNull(ausencia.getAusenciaID());
        assertEquals("10:00:00", ausencia.getHoraInicio());
        assertEquals("Reunion", ausencia.getMotivo());
        assertNull(ausencia.getHoraFin());
    }

    @Test
    void iniciarAusenciaSinMotivoPermiteNull() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        PeriodoAusencia ausencia = service.iniciarAusencia(a.getAsistenciaID(), "10:00:00", null);

        assertNull(ausencia.getMotivo());
    }

    @Test
    void iniciarAusenciaDosVecesArrojaError() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");
        service.iniciarAusencia(a.getAsistenciaID(), "10:00:00", null);

        assertThrows(NegocioException.class,
                () -> service.iniciarAusencia(a.getAsistenciaID(), "11:00:00", null));
    }

    @Test
    void registrarSalidaRechazaHoraSalidaInvalida() throws SQLException {
        Asistencia a = service.abrirDia(docenteID, "2026-08-03", "08:00:00");

        assertThrows(NegocioException.class,
                () -> service.registrarSalida(a.getAsistenciaID(), "13"));
    }
}
