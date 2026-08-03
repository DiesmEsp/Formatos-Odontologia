package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.AsistenciaMaterial;
import com.odontologia.formatos.model.Docente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AsistenciaRepositoryTest extends BaseRepositoryTest {

    private final AsistenciaRepository repository = new AsistenciaRepository();
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
    void insertYFindById() throws SQLException {
        int id = repository.insert(asistencia(docenteID, "2026-08-03"));

        Asistencia encontrada = repository.findById(id);
        assertNotNull(encontrada);
        assertEquals(docenteID, encontrada.getDocenteID());
        assertEquals("2026-08-03", encontrada.getFecha());
        assertEquals("ACTIVO", encontrada.getEstado());
    }

    @Test
    void encuentraActivoPorDocenteYFecha() throws SQLException {
        repository.insert(asistencia(docenteID, "2026-08-03"));

        Asistencia activa = repository.findActivoPorDocenteYFecha(docenteID, "2026-08-03");
        assertNotNull(activa);
        assertEquals("2026-08-03", activa.getFecha());
    }

    @Test
    void noEncuentraActivoParaOtraFecha() throws SQLException {
        repository.insert(asistencia(docenteID, "2026-08-03"));

        assertNull(repository.findActivoPorDocenteYFecha(docenteID, "2026-08-04"));
    }

    @Test
    void insertMaterialYFindByMaterial() throws SQLException {
        int asistenciaID = repository.insert(asistencia(docenteID, "2026-08-03"));

        AsistenciaMaterial item = new AsistenciaMaterial();
        item.setAsistenciaID(asistenciaID);
        item.setMaterialID(1);
        item.setCantidad(2.5);
        int id = materialRepository.insert(item);

        AsistenciaMaterial encontrado = materialRepository.findByMaterial(asistenciaID, 1);
        assertNotNull(encontrado);
        assertEquals(id, encontrado.getMatAsistenciaID());
        assertEquals(2.5, encontrado.getCantidad(), 0.001);
    }

    @Test
    void updateAcumulaCantidad() throws SQLException {
        int asistenciaID = repository.insert(asistencia(docenteID, "2026-08-03"));
        AsistenciaMaterial item = new AsistenciaMaterial();
        item.setAsistenciaID(asistenciaID);
        item.setMaterialID(1);
        item.setCantidad(1.0);
        int id = materialRepository.insert(item);

        item = materialRepository.findByMaterial(asistenciaID, 1);
        item.setCantidad(item.getCantidad() + 3.0);
        materialRepository.update(item);

        AsistenciaMaterial actualizado = materialRepository.findByMaterial(asistenciaID, 1);
        assertEquals(4.0, actualizado.getCantidad(), 0.001);
    }

    private Asistencia asistencia(int docenteID, String fecha) {
        Asistencia a = new Asistencia();
        a.setDocenteID(docenteID);
        a.setFecha(fecha);
        a.setEstado("ACTIVO");
        return a;
    }
}
