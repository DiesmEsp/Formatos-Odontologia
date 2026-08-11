-- V9: Creación de índices para consultas críticas (Fase 2.5 Auditoría)

CREATE INDEX IF NOT EXISTS idx_tratamiento_estado ON Tratamiento(Estado);
CREATE INDEX IF NOT EXISTS idx_tratamiento_unidad_estado ON Tratamiento(UnidadID, Estado);
CREATE INDEX IF NOT EXISTS idx_periodoausencia_asistencia_horafin ON PeriodoAusencia(AsistenciaID, HoraFin);
CREATE INDEX IF NOT EXISTS idx_materiales_asistencia_lookup ON Materiales_Asistencia(MaterialesID, AsistenciaID);
CREATE INDEX IF NOT EXISTS idx_materiales_list_lookup ON Materiales_List(MaterialID, TratamientoID);
