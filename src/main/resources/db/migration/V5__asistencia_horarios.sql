-- ============================================================
-- V5__asistencia_horarios.sql
-- Agrega registro de horarios (entrada/salida) y periodos de
-- ausencia a la asistencia docente.
-- ============================================================

ALTER TABLE Asistencia ADD COLUMN HoraEntrada TEXT;
ALTER TABLE Asistencia ADD COLUMN HoraSalida TEXT;

CREATE TABLE PeriodoAusencia (
    AusenciaID   INTEGER PRIMARY KEY AUTOINCREMENT,
    AsistenciaID INTEGER NOT NULL REFERENCES Asistencia (AsistenciaID),
    HoraInicio   TEXT    NOT NULL,
    HoraFin      TEXT,
    Motivo       TEXT
);

CREATE INDEX idx_ausencia_asistencia ON PeriodoAusencia (AsistenciaID);
