package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.Docente;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Asistencia dia = service.abrirDia(docenteID, "2026-08-03");

        assertNotNull(dia);
        assertEquals("ACTIVO", dia.getEstado());
    }

    @Test
    void abrirDiaReutilizaRegistroExistente() throws SQLException {
        Asistencia primero = service.abrirDia(docenteID, "2026-08-03");
        Asistencia segundo = service.abrirDia(docenteID, "2026-08-03");

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
        assertThrows(NegocioException.class, () -> service.abrirDia(9999, "2026-08-03"));
    }

    @Test
    void rechazaFechaInvalida() {
        assertThrows(NegocioException.class, () -> service.abrirDia(docenteID, "03/08/2026"));
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
}
