-- ============================================================
-- V10__limpiar_demo_data.sql
-- Elimina la demo data sembrada en V6 (docentes, operadores,
-- pacientes, tratamientos predefinidos y unidades 2-6).
-- Deja la base lista para que el usuario registre sus propios
-- datos, conservando unicamente la unidad 1.
-- El orden respeta las claves foraneas (hijos antes que padres).
-- ============================================================

DELETE FROM Materiales_List_PRED;
DELETE FROM Materiales_List;
DELETE FROM Materiales_Asistencia;
DELETE FROM PeriodoAusencia;
DELETE FROM Unidad_Conversion;
DELETE FROM Tratamiento;
DELETE FROM RegistroAnulacion;
DELETE FROM Tratamiento_PRED;
DELETE FROM Asistencia;
DELETE FROM Operadores;
DELETE FROM Pacientes;
DELETE FROM Docentes;
DELETE FROM Unidad;

-- Reinicia la numeracion automatica para dejar un estado limpio
DELETE FROM sqlite_sequence WHERE name IN (
  'Materiales_List_PRED',
  'Materiales_List',
  'Materiales_Asistencia',
  'PeriodoAusencia',
  'Unidad_Conversion',
  'Tratamiento',
  'RegistroAnulacion',
  'Tratamiento_PRED',
  'Asistencia',
  'Operadores',
  'Pacientes',
  'Docentes',
  'Unidad'
);

-- Unidad de tratamiento inicial (numeracion automatica)
INSERT INTO Unidad (UnidadNro) VALUES (1);
