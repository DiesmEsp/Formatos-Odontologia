package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.model.UnidadConversion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnidadConversionRepositoryTest extends BaseRepositoryTest {

    private final UnidadConversionRepository repository = new UnidadConversionRepository();
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
    void insertYFindById() throws SQLException {
        int id = repository.insert(conversion(50.0));

        UnidadConversion encontrada = repository.findById(id);
        assertNotNull(encontrada);
        assertEquals("guante", encontrada.getUnidadBase());
        assertEquals("caja", encontrada.getUnidadEmpaque());
        assertEquals(50.0, encontrada.getFactor(), 0.001);
    }

    @Test
    void updateModificaFactor() throws SQLException {
        int id = repository.insert(conversion(50.0));

        UnidadConversion cargada = repository.findById(id);
        cargada.setFactor(48.0);
        repository.update(cargada);

        assertEquals(48.0, repository.findById(id).getFactor(), 0.001);
    }

    @Test
    void findByMaterialEmpaqueYFindByMaterial() throws SQLException {
        repository.insert(conversion(50.0));

        assertNotNull(repository.findByMaterialEmpaque(materialID, "caja"));
        List<UnidadConversion> porMaterial = repository.findByMaterial(materialID);
        assertEquals(1, porMaterial.size());
    }

    @Test
    void deleteEliminaRegistro() throws SQLException {
        int id = repository.insert(conversion(50.0));

        repository.delete(id);

        assertNull(repository.findById(id));
    }

    @Test
    void noPermiteEmpaqueDuplicado() throws SQLException {
        repository.insert(conversion(50.0));

        assertThrows(SQLException.class, () -> repository.insert(conversion(48.0)));
    }

    private UnidadConversion conversion(double factor) {
        UnidadConversion c = new UnidadConversion();
        c.setMaterialID(materialID);
        c.setUnidadBase("guante");
        c.setUnidadEmpaque("caja");
        c.setFactor(factor);
        return c;
    }
}
