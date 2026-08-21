package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.ConsumoClinica;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumoRepositoryTest extends BaseRepositoryTest {

    private final ConsumoRepository repository = new ConsumoRepository();
    private final MaterialRepository materialRepo = new MaterialRepository();

    private int crearMaterial(String nombre) throws Exception {
        com.odontologia.formatos.model.Materiales m = new com.odontologia.formatos.model.Materiales();
        m.setNombre(nombre);
        m.setUnidad("paquete");
        m.setEstado(1);
        return materialRepo.insert(m);
    }

    private ConsumoClinica consumo(String fecha, int materialId, double cantidad, int clinicaID) {
        ConsumoClinica c = new ConsumoClinica();
        c.setFecha(fecha);
        c.setMaterialID(materialId);
        c.setCantidad(cantidad);
        c.setClinicaID(clinicaID);
        return c;
    }

    @Test
    void insertYFindByIdRoundtripConJoin() throws Exception {
        int materialId = crearMaterial("Guante latex repo");
        int id;
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            id = repository.insert(consumo("2026-08-05", materialId, 2.5, 1), con);
        }
        ConsumoClinica registro = repository.findById(id, 1);
        assertNotNull(registro);
        assertEquals("2026-08-05", registro.getFecha());
        assertEquals(materialId, registro.getMaterialID());
        assertEquals(2.5, registro.getCantidad(), 0.001);
        assertEquals("Guante latex repo", registro.getNombreMaterial());
        assertEquals("paquete", registro.getUnidad());
        assertEquals(1, registro.getClinicaID());
    }

    @Test
    void findByIdRespetaClinica() throws Exception {
        int materialId = crearMaterial("Gasa repo");
        int id;
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            id = repository.insert(consumo("2026-08-05", materialId, 1, 1), con);
        }
        assertNull(repository.findById(id, 2));
    }

    @Test
    void findByMesIncluyeUltimoDiaYExcluyeMesSiguiente() throws Exception {
        int materialId = crearMaterial("Algodon repo");
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            repository.insert(consumo("2026-07-31", materialId, 1, 1), con);
            repository.insert(consumo("2026-08-31", materialId, 2, 1), con);
            repository.insert(consumo("2026-09-01", materialId, 3, 1), con);
        }
        List<ConsumoClinica> agosto = repository.findByMes(2026, 8, 1);
        assertEquals(1, agosto.size());
        assertEquals("2026-08-31", agosto.get(0).getFecha());
    }

    @Test
    void updateModificaSoloRegistroDeLaClinica() throws Exception {
        int materialId = crearMaterial("Pinzas repo");
        int id;
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            id = repository.insert(consumo("2026-08-05", materialId, 1, 1), con);
        }
        ConsumoClinica cambios = consumo("2026-08-09", materialId, 9, 1);
        cambios.setConsumoID(id);
        assertTrue(repository.update(cambios, null));

        ConsumoClinica ajena = consumo("2026-08-09", materialId, 9, 2);
        ajena.setConsumoID(id);
        assertFalse(repository.update(ajena, null));
    }

    @Test
    void deleteEliminaSoloRegistroDeLaClinica() throws Exception {
        int materialId = crearMaterial("Babero repo");
        int id;
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            id = repository.insert(consumo("2026-08-05", materialId, 1, 1), con);
        }
        assertFalse(repository.delete(id, 2, null));
        assertTrue(repository.delete(id, 1, null));
        assertNull(repository.findById(id, 1));
    }
}
