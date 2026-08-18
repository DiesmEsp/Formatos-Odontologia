-- ============================================================
-- V3__tratamiento_padre.sql
-- Vinculo de tratamientos CONTINUO con su tratamiento padre
-- (ABIERTO o CERRADO). Un CONTINUO referencia al tratamiento
-- que continua; es null para tratamientos NORMAL.
-- ============================================================

ALTER TABLE Tratamiento ADD COLUMN TratamientoPadreID INTEGER
    REFERENCES Tratamiento (TratamientoID);

CREATE INDEX idx_tratamiento_padre ON Tratamiento (TratamientoPadreID);