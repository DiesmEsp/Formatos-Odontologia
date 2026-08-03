package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configura una base de datos SQLite en un archivo temporal antes de cada test
 * y aplica las migraciones de Flyway (mismo motor que producción).
 */
public abstract class BaseRepositoryTest {

    private Path dbFile;

    @BeforeEach
    void setUpDatabase() throws IOException {
        dbFile = Files.createTempFile("formatos_test_", ".db");
        Files.deleteIfExists(dbFile);
        System.setProperty("db.path", dbFile.toString());
        ConnectionManager.reset();
        ConnectionManager.getInstance();
    }

    @AfterEach
    void tearDownDatabase() throws IOException {
        ConnectionManager.reset();
        System.clearProperty("db.path");
        if (dbFile != null) {
            Files.deleteIfExists(dbFile);
        }
    }
}
