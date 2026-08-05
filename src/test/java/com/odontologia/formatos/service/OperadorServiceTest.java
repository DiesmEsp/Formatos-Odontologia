package com.odontologia.formatos.service;

import com.odontologia.formatos.repository.BaseRepositoryTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperadorServiceTest extends BaseRepositoryTest {

    private final OperadorService service = new OperadorService();

    @Test
    void crearOperadorValido() throws SQLException {
        int id = service.crear("Ana", "Perez", "12345678", "PRE", "4", 2026);
        assertEquals(1, id);
    }

    @Test
    void rechazaCombinacionInvalida() {
        assertThrows(NegocioException.class, () -> service.crear("Ana", "Perez", "12345678", "PRE", "R1", 2026));
    }

    @Test
    void rechazaGradoInvalido() {
        assertThrows(NegocioException.class, () -> service.crear("Ana", "Perez", "12345678", "DR", "4", 2026));
    }

    @Test
    void aceptaPosR1() throws SQLException {
        int id = service.crear("Luis", "Gomez", "87654321", "POS", "R1", 2026);
        assertEquals(1, id);
    }
}
