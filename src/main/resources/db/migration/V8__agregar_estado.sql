-- ============================================================
-- V8__agregar_estado.sql
-- Agrega columna Estado a Pacientes y Tratamiento_PRED
-- para soportar activacion / desactivacion desde catalogos.
-- ============================================================

ALTER TABLE Pacientes ADD COLUMN Estado INTEGER NOT NULL DEFAULT 1;

ALTER TABLE Tratamiento_PRED ADD COLUMN Estado INTEGER NOT NULL DEFAULT 1;
