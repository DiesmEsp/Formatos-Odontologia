package com.odontologia.formatos.service;

import com.odontologia.formatos.repository.BaseRepositoryTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MaterialServiceTest extends BaseRepositoryTest {

    private final MaterialService service = new MaterialService();

    @Test
    void crearMaterialValido() throws SQLException {
        service.crear("Guante", "guante");
    }

    @Test
    void rechazaNombreObligatorio() {
        assertThrows(NegocioException.class, () -> service.crear("", "guante"));
    }

    @Test
    void rechazaDuplicado() throws SQLException {
        service.crear("Gasa", "paquete");
        assertThrows(EntidadDuplicadaException.class, () -> service.crear("Gasa", "paquete"));
    }
}
