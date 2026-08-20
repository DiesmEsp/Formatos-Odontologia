-- Limpieza defensiva: la columna NombreTratamiento ya es NOT NULL desde V1,
-- pero se normalizan posibles nombres vacíos de bases previas.
UPDATE Tratamiento
SET NombreTratamiento = 'Sin nombre'
WHERE NombreTratamiento IS NULL OR TRIM(NombreTratamiento) = '';