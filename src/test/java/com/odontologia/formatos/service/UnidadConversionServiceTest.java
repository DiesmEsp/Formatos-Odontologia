package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.model.UnidadConversion;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import com.odontologia.formatos.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnidadConversionServiceTest extends BaseRepositoryTest {

    private final UnidadConversionService service = new UnidadConversionService();
    private final MaterialRepository materialRepository = new MaterialRepository();
    private int materialID;

    @BeforeEach
    void crearMaterial() throws SQLException {
        Materiales m = new Materiales();
        m.setNombre("Guantes nitrilo talla M");
        m.setUnidad("guante");
        m.setEstado(1);
        materialID = materialRepository.insert(m);
    }

    @Test
    void crearValidaYPersiste() throws SQLException {
        int id = service.crear(materialID, "guante", "caja", 50.0);

        UnidadConversion c = service.buscarPorId(id);
        assertEquals("guante", c.getUnidadBase());
        assertEquals("caja", c.getUnidadEmpaque());
        assertEquals(50.0, c.getFactor(), 0.001);
    }

    @Test
    void rechazaEmpaqueDuplicado() throws SQLException {
        service.crear(materialID, "guante", "caja", 50.0);

        assertThrows(EntidadDuplicadaException.class,
                () -> service.crear(materialID, "guante", "caja", 48.0));
    }

    @Test
    void rechazaMaterialInexistente() {
        assertThrows(NegocioException.class, () -> service.crear(9999, "guante", "caja", 50.0));
    }

    @Test
    void rechazaUnidadesVacias() {
        assertThrows(NegocioException.class, () -> service.crear(materialID, "", "caja", 50.0));
        assertThrows(NegocioException.class, () -> service.crear(materialID, "guante", " ", 50.0));
    }

    @Test
    void rechazaFactorNoPositivo() {
        assertThrows(NegocioException.class, () -> service.crear(materialID, "guante", "caja", 0.0));
        assertThrows(NegocioException.class, () -> service.crear(materialID, "guante", "caja", -1.0));
    }

    @Test
    void actualizarCambiaFactor() throws SQLException {
        int id = service.crear(materialID, "guante", "caja", 50.0);

        UnidadConversion c = service.buscarPorId(id);
        c.setFactor(48.0);
        service.actualizar(c);

        assertEquals(48.0, service.buscarPorId(id).getFactor(), 0.001);
    }
}
