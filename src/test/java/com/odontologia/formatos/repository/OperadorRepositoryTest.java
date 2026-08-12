package com.odontologia.formatos.repository;

import com.odontologia.formatos.model.Operador;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperadorRepositoryTest extends BaseRepositoryTest {

    private final OperadorRepository repository = new OperadorRepository();

    @Test
    void insertYFindByPeriodo() throws SQLException {
        int semilla = repository.findByPeriodo(2026).size();
        repository.insert(operador("Ana", "Perez", "PRE", "4", 2026));

        List<Operador> periodo2026 = repository.findByPeriodo(2026);
        assertEquals(semilla + 1, periodo2026.size());
    }

    @Test
    void mismoOperadorDistintosPeriodos() throws SQLException {
        int semilla = repository.findAll().size();
        repository.insert(operador("Ana", "Perez", "PRE", "4", 2025));
        repository.insert(operador("Ana", "Perez", "PRE", "4", 2026));

        assertEquals(semilla + 2, repository.findAll().size());
    }

    @Test
    void buscarPorTextoConApellido() throws SQLException {
        repository.insert(operador("Ana", "Perez", "PRE", "4", 2026));
        repository.insert(operador("Luis", "Gomez", "POS", "R1", 2026));

        List<Operador> resultado = repository.buscarPorTexto("gomez");
        assertEquals(1, resultado.size());
        assertEquals("Luis", resultado.get(0).getNombres());
    }

    private Operador operador(String nombres, String apellidos, String grado, String tipo, int periodo) {
        Operador o = new Operador();
        o.setNombres(nombres);
        o.setApellidos(apellidos);
        o.setGrado(grado);
        o.setTipo(tipo);
        o.setPeriodo(periodo);
        o.setEstado(1);
        return o;
    }
}
