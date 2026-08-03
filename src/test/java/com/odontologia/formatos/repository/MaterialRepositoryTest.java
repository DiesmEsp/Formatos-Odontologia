package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Materiales;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaterialRepositoryTest extends BaseRepositoryTest {

    private final MaterialRepository repository = new MaterialRepository();

    @Test
    void insertYFindById() throws SQLException {
        Materiales m = new Materiales();
        m.setNombre("Guante");
        m.setUnidad("guante");
        m.setEstado(1);

        int id = repository.insert(m);

        Materiales encontrado = repository.findById(id);
        assertNotNull(encontrado);
        assertEquals("Guante", encontrado.getNombre());
        assertEquals("guante", encontrado.getUnidad());
    }

    @Test
    void findByNombreDuplicado() throws SQLException {
        Materiales m = new Materiales();
        m.setNombre("Gasa");
        m.setUnidad("paquete");
        m.setEstado(1);
        repository.insert(m);

        assertNotNull(repository.findByNombre("Gasa"));
    }

    @Test
    void updateModificaDatos() throws SQLException {
        Materiales m = new Materiales();
        m.setNombre("Anestesia");
        m.setUnidad("carpule");
        m.setEstado(1);
        int id = repository.insert(m);

        Materiales cargado = repository.findById(id);
        cargado.setUnidad("caja");
        repository.update(cargado);

        assertEquals("caja", repository.findById(id).getUnidad());
    }

    @Test
    void findAllYBuscarPorTexto() throws SQLException {
        insertar("Guante", "guante");
        insertar("Gasa", "paquete");
        insertar("Alginato", "cucharada");

        List<Materiales> todos = repository.findAll();
        assertEquals(3, todos.size());

        List<Materiales> gaseosa = repository.buscarPorTexto("gas");
        assertEquals(1, gaseosa.size());
    }

    @Test
    void deleteEliminaRegistro() throws SQLException {
        int id = insertar("Algodón", "paquete");

        repository.delete(id);

        assertNull(repository.findById(id));
    }

    private int insertar(String nombre, String unidad) throws SQLException {
        Materiales m = new Materiales();
        m.setNombre(nombre);
        m.setUnidad(unidad);
        m.setEstado(1);
        return repository.insert(m);
    }
}
