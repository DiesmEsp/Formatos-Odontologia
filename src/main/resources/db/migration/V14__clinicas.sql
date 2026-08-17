-- ============================================================
-- V14__clinicas.sql
-- Agrega la entidad Clinica y la columna ClinicaID a las tablas
-- que son por clínica (catálogos y transacciones).
--
-- Modelo: BD única + ClinicaID (reemplaza RD-3.1.7 "instancia
-- dedicada"). No se siembra ninguna clínica: la primera que el
-- usuario cree en la pantalla de inicio ocupará ClinicaID = 1 y
-- heredará los datos existentes (backfill con DEFAULT 1).
-- ============================================================

-- 1. Tabla de clínicas
CREATE TABLE Clinica (
    ClinicaID INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombre    TEXT NOT NULL UNIQUE,
    Grupo     TEXT,
    Estado    INTEGER NOT NULL DEFAULT 1
);

-- 2. Columna ClinicaID en catálogos por clínica
ALTER TABLE Operadores ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;
ALTER TABLE Docentes   ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;
ALTER TABLE Unidad     ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;
ALTER TABLE Pacientes  ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;

-- 3. Columna ClinicaID en transacciones
ALTER TABLE Tratamiento       ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;
ALTER TABLE Asistencia        ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;
ALTER TABLE RegistroAnulacion ADD COLUMN ClinicaID INTEGER NOT NULL DEFAULT 1;

-- 4. Índices de rendimiento por clínica
CREATE INDEX idx_operadores_clinica  ON Operadores (ClinicaID);
CREATE INDEX idx_docentes_clinica    ON Docentes (ClinicaID);
CREATE INDEX idx_unidad_clinica      ON Unidad (ClinicaID);
CREATE INDEX idx_pacientes_clinica   ON Pacientes (ClinicaID);
CREATE INDEX idx_tratamiento_clinica ON Tratamiento (ClinicaID, Estado);
CREATE INDEX idx_asistencia_clinica  ON Asistencia (ClinicaID);