-- ============================================================
-- V4__consumo_clinica.sql
-- Registro de materiales consumidos directamente por la clinica
-- (fuera de tratamientos, sesiones o entrega a docentes).
-- ============================================================

CREATE TABLE Consumo_Clinica (
    ConsumoID  INTEGER PRIMARY KEY AUTOINCREMENT,
    Fecha      TEXT    NOT NULL,
    MaterialID INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad   REAL    NOT NULL CHECK (Cantidad > 0),
    Timestamp  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    ClinicaID  INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_consumo_clinica_fecha    ON Consumo_Clinica (ClinicaID, Fecha);
CREATE INDEX idx_consumo_clinica_material ON Consumo_Clinica (MaterialID);
