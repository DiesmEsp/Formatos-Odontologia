-- ============================================================
-- V12__seed_tratamientos_pred.sql
-- Siembra los tratamientos predefinidos y sus materiales
-- sugeridos a partir de Docs/tratamientos.csv.
--
-- Columnas CSV usadas: Tratamiento, MontoSugerido, Material,
-- Cantidad. La columna "Nota" se descarta (solo era anotacion
-- humana). Las variantes "(sin materiales)" quedan como plantilla
-- sin insumos asociados.
--
-- Los MaterialID se resuelven por nombre contra el catalogo
-- sembrado en V11 (281 materiales). Los TratPredID se resuelven
-- por NombreTratamiento (UNIQUE en Tratamiento_PRED).
-- ============================================================

-- ------------------------------------------------------------
-- 1. Plantillas (una fila por nombre unico; 94 nombres)
-- ------------------------------------------------------------
INSERT INTO Tratamiento_PRED (NombreTratamiento, MontoSugerido) VALUES
  ('Fase Higiénica Paciente con Gingivitis', 30),
  ('Fase Higiénica Paciente con Gingivitis (sin materiales)', 20),
  ('Fase Higiénica Índice de Higiene Oral', 10),
  ('Fase Higiénica Índice de Higiene Oral (sin materiales)', 5),
  ('Flúor Barniz por Arcada', 20),
  ('Flúor Barniz por Arcada (sin materiales)', 10),
  ('Tratamiento de Apexificación', 37),
  ('Tratamiento de Apexificación (sin materiales)', 20),
  ('Resina Fluida', 35),
  ('Resina Fluida (sin materiales)', 20),
  ('Tratamiento de Incrustación con Resina', 68),
  ('Tratamiento de Incrustación con Resina (sin materiales)', 40),
  ('Recubrimiento Pulpar Indirecto', 25),
  ('Recubrimiento Pulpar Indirecto (sin materiales)', 15),
  ('Recubrimiento Pulpar Directo', 40),
  ('Recubrimiento Pulpar Directo (sin materiales)', 20),
  ('Ionómero de Reconstrucción Compuesto', 45),
  ('Ionómero de Reconstrucción Compuesto (sin materiales)', 30),
  ('Exodoncia Simple', 6),
  ('Exodoncia Simple (sin materiales)', 6),
  ('Obturación Provisional de Emergencia (Eugenato)', 5),
  ('Obturación Provisional de Emergencia (Eugenato) (sin materiales)', 5),
  ('Restauración con Ionómero Vítreo más Base', 18),
  ('Restauración con Ionómero Vítreo más Base (sin materiales)', 18),
  ('Restauración con Resina Fotocurable Simple más Base', 20),
  ('Restauración con Resina Fotocurable Simple más Base (sin materiales)', 20),
  ('Restauración con Resina Fotocurable Compuesta más Base', 20),
  ('Restauración con Resina Fotocurable Compuesta más Base (sin materiales)', 20),
  ('Topicaciones con Flúor', 10),
  ('Pulpotomía', 15),
  ('Pulpectomía', 20),
  ('Sellante por Cuadrante', 10),
  ('Corona de Acero Inoxidable', 40),
  ('Corona de Acrílico', 40),
  ('Corona de Resina', 40),
  ('Tratamiento de Incisivos Fracturados', 50),
  ('Mantenedor de Espacio Banda-ANSA', 53),
  ('Mantenedor de Espacio Banda-ANSA (sin materiales)', 40),
  ('Mantenedor de Espacio Corona de Acero-ANSA', 60),
  ('Mantenedor de Espacio Corona de Acero-ANSA (sin materiales)', 40),
  ('Arco Lingual Fijo o Removible', 80),
  ('Arco Lingual Fijo o Removible (sin materiales)', 50),
  ('Rompe Hábitos Fijos o Removibles', 90),
  ('Rompe Hábitos Fijos o Removibles (sin materiales)', 60),
  ('Plano Inclinado Directo', 25),
  ('Plano Inclinado Directo (sin materiales)', 20),
  ('Plano Inclinado Indirecto', 35),
  ('Plano Inclinado Indirecto (sin materiales)', 20),
  ('Quad Helix Fijo o Removible (al contado)', 90),
  ('Quad Helix Fijo o Removible (Cuota inicial)', 50),
  ('Quad Helix Fijo o Removible (cancelación)', 40),
  ('Quad Helix Fijo o Removible (sin materiales)', 60),
  ('Bi Helix Fijo o Removible (al contado)', 90),
  ('Bi Helix Fijo o Removible (Cuota inicial)', 50),
  ('Bi Helix Fijo o Removible (cancelación)', 40),
  ('Bi Helix Fijo o Removible (sin materiales)', 60),
  ('Arco Extraoral (al contado)', 190),
  ('Arco Extraoral (Cuota inicial)', 100),
  ('Arco Extraoral (cancelación)', 90),
  ('Arco Extraoral (sin materiales)', 85),
  ('Disyuntor Hyrax (al contado)', 190),
  ('Disyuntor Hyrax (Cuota inicial)', 100),
  ('Disyuntor Hyrax (cancelación)', 90),
  ('Disyuntor Hyrax (sin materiales)', 85),
  ('Disyuntor Hass (al contado)', 150),
  ('Disyuntor Hass (Cuota inicial)', 100),
  ('Disyuntor Hass (cancelación)', 50),
  ('Disyuntor Hass (sin materiales)', 85),
  ('Aparato de Ortopedia Funcional (al contado)', 80),
  ('Aparato de Ortopedia Funcional (Cuota inicial)', 50),
  ('Aparato de Ortopedia Funcional (cancelación)', 30),
  ('Aparato de Ortopedia Funcional (sin materiales)', 50),
  ('Aparatología Fija 2x4 por Arcada (al contado)', 150),
  ('Aparatología Fija 2x4 por Arcada (Cuota inicial)', 100),
  ('Aparatología Fija 2x4 por Arcada (cancelación)', 50),
  ('Aparatología Fija 2x4 por Arcada (sin materiales)', 80),
  ('Mentonera (al contado)', 100),
  ('Mentonera (Cuota inicial)', 60),
  ('Mentonera (cancelación)', 40),
  ('Mentonera (sin materiales)', 60),
  ('Máscara Facial Petit (al contado)', 180),
  ('Máscara Facial Petit (Cuota inicial)', 100),
  ('Máscara Facial Petit (cancelación)', 80),
  ('Máscara Facial Petit (sin materiales)', 85),
  ('Botón de Nance', 80),
  ('Botón de Nance (sin materiales)', 50),
  ('Lip Bumper Semi Fijo', 80),
  ('Lip Bumper Semi Fijo (sin materiales)', 50),
  ('Lip Bumper Removible', 70),
  ('Lip Bumper Removible (sin materiales)', 40),
  ('Placa Schwartz Estándar', 70),
  ('Placa Schwartz Estándar (sin materiales)', 40),
  ('Placa Schwartz con Tornillo', 80),
  ('Placa Schwartz con Tornillo (sin materiales)', 40);

-- ------------------------------------------------------------
-- 2. Materiales por plantilla (una fila por cada linea del CSV
--    con Material no vacio; se omiten las variantes "(sin materiales)")
-- ------------------------------------------------------------
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad)
SELECT p.TratPredID, m.MaterialID, 1
FROM (
    SELECT 'Fase Higiénica Paciente con Gingivitis' AS trat, 'Escobilla profiláctica' AS mat
    UNION ALL SELECT 'Fase Higiénica Paciente con Gingivitis', 'Pasta profiláctica'
    UNION ALL SELECT 'Fase Higiénica Paciente con Gingivitis', 'Pastilla reveladora'
    UNION ALL SELECT 'Fase Higiénica Paciente con Gingivitis', 'Cánula de succión'
    UNION ALL SELECT 'Fase Higiénica Paciente con Gingivitis', 'Gasa'
    UNION ALL SELECT 'Fase Higiénica Paciente con Gingivitis', 'Clorhexidina'
    UNION ALL SELECT 'Fase Higiénica Índice de Higiene Oral', 'Pastilla reveladora'
    UNION ALL SELECT 'Fase Higiénica Índice de Higiene Oral', 'Cánula de succión'
    UNION ALL SELECT 'Fase Higiénica Índice de Higiene Oral', 'Gasa'
    UNION ALL SELECT 'Fase Higiénica Índice de Higiene Oral', 'Clorhexidina'
    UNION ALL SELECT 'Flúor Barniz por Arcada', 'Gasa'
    UNION ALL SELECT 'Flúor Barniz por Arcada', 'Flúor barniz'
    UNION ALL SELECT 'Flúor Barniz por Arcada', 'Cánula de succión'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Dique de goma'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Suero fisiológico'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Hipoclorito de sodio'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'EDTA'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'MTA'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Papel absorbente'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Ionómero Vitremer'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Ácido grabador'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Adhesivo'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Resina compuesta'
    UNION ALL SELECT 'Tratamiento de Apexificación', 'Ketac Molar'
    UNION ALL SELECT 'Resina Fluida', 'Aguja corta'
    UNION ALL SELECT 'Resina Fluida', 'Anestesia tópica'
    UNION ALL SELECT 'Resina Fluida', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Resina Fluida', 'Dique de goma'
    UNION ALL SELECT 'Resina Fluida', 'Ácido grabador'
    UNION ALL SELECT 'Resina Fluida', 'Adhesivo'
    UNION ALL SELECT 'Resina Fluida', 'Microbrush'
    UNION ALL SELECT 'Resina Fluida', 'Resina fluida A2'
    UNION ALL SELECT 'Resina Fluida', 'Papel articular'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Anestesia tópica'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Aguja corta'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Aguja larga'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Ácido grabador'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Adhesivo'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Resina compuesta'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Ionómero de cementación Ketac'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Ketac Universal'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Microbrush'
    UNION ALL SELECT 'Tratamiento de Incrustación con Resina', 'Papel articular'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'Aguja corta'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'Aguja larga'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'Anestesia tópica'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'Dique de goma'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'TheraCal'
    UNION ALL SELECT 'Recubrimiento Pulpar Indirecto', 'Ionómero Vitremer'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'Aguja corta'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'Aguja larga'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'Anestesia tópica'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'Dique de goma'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'TheraCal'
    UNION ALL SELECT 'Recubrimiento Pulpar Directo', 'Ionómero Vitremer'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Aguja corta'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Aguja larga'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Anestesia tópica'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Ácido grabador'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Adhesivo'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Microbrush'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Ionómero Vitremer'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Ketac Molar'
    UNION ALL SELECT 'Ionómero de Reconstrucción Compuesto', 'Ketac Universal'
    UNION ALL SELECT 'Exodoncia Simple', 'Aguja corta'
    UNION ALL SELECT 'Exodoncia Simple', 'Aguja larga'
    UNION ALL SELECT 'Exodoncia Simple', 'Anestesia tópica'
    UNION ALL SELECT 'Exodoncia Simple', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Exodoncia Simple', 'Gasa'
    UNION ALL SELECT 'Exodoncia Simple', 'Cánula de succión'
    UNION ALL SELECT 'Exodoncia Simple', 'Agua oxigenada'
    UNION ALL SELECT 'Exodoncia Simple', 'Jeringa descartable 10 ml'
    UNION ALL SELECT 'Obturación Provisional de Emergencia (Eugenato)', 'Óxido de zinc + eugenol'
    UNION ALL SELECT 'Obturación Provisional de Emergencia (Eugenato)', 'Eugenato'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Dique de goma'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Anestesia tópica'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Aguja corta'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Cánula de succión'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Ácido grabador'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Resina compuesta'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Adhesivo'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Microbrush'
    UNION ALL SELECT 'Restauración con Ionómero Vítreo más Base', 'Ionómero Vitremer'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Ácido grabador'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Adhesivo'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Microbrush'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Cánula de succión'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Resina compuesta'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Ionómero Vitremer'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Simple más Base', 'Papel articular'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Dique de goma'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Cánula de succión'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Hilo dental'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Hidróxido de calcio'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Ionómero Vitrebond'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Dycal'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Ácido grabador'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Adhesivo'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Microbrush'
    UNION ALL SELECT 'Restauración con Resina Fotocurable Compuesta más Base', 'Resina compuesta'
    UNION ALL SELECT 'Pulpotomía', 'Anestesia tópica'
    UNION ALL SELECT 'Pulpotomía', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Pulpotomía', 'Aguja corta'
    UNION ALL SELECT 'Pulpotomía', 'Aguja larga'
    UNION ALL SELECT 'Pulpotomía', 'Dique de goma'
    UNION ALL SELECT 'Pulpotomía', 'Ionómero de cementación Ketac'
    UNION ALL SELECT 'Pulpectomía', 'Anestesia tópica'
    UNION ALL SELECT 'Pulpectomía', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Pulpectomía', 'Aguja corta'
    UNION ALL SELECT 'Pulpectomía', 'Aguja larga'
    UNION ALL SELECT 'Pulpectomía', 'Dique de goma'
    UNION ALL SELECT 'Pulpectomía', 'Ionómero de cementación Ketac'
    UNION ALL SELECT 'Sellante por Cuadrante', 'Ácido grabador'
    UNION ALL SELECT 'Sellante por Cuadrante', 'Adhesivo'
    UNION ALL SELECT 'Sellante por Cuadrante', 'Sellante'
    UNION ALL SELECT 'Sellante por Cuadrante', 'Cánula de succión'
    UNION ALL SELECT 'Sellante por Cuadrante', 'Microbrush'
    UNION ALL SELECT 'Sellante por Cuadrante', 'Papel articular'
    UNION ALL SELECT 'Corona de Acero Inoxidable', 'Anestesia tópica'
    UNION ALL SELECT 'Corona de Acero Inoxidable', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Corona de Acero Inoxidable', 'Aguja corta'
    UNION ALL SELECT 'Corona de Acero Inoxidable', 'Aguja larga'
    UNION ALL SELECT 'Corona de Acero Inoxidable', 'Coronas de acero'
    UNION ALL SELECT 'Corona de Acero Inoxidable', 'Ionómero de cementación Ketac'
    UNION ALL SELECT 'Corona de Acrílico', 'Anestesia tópica'
    UNION ALL SELECT 'Corona de Acrílico', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Corona de Acrílico', 'Aguja corta'
    UNION ALL SELECT 'Corona de Acrílico', 'Aguja larga'
    UNION ALL SELECT 'Corona de Acrílico', 'Hilo retractor 0'
    UNION ALL SELECT 'Corona de Acrílico', 'Cánula de succión'
    UNION ALL SELECT 'Corona de Acrílico', 'Polvo transparente'
    UNION ALL SELECT 'Corona de Acrílico', 'Monómero'
    UNION ALL SELECT 'Corona de Resina', 'Anestesia tópica'
    UNION ALL SELECT 'Corona de Resina', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Corona de Resina', 'Aguja corta'
    UNION ALL SELECT 'Corona de Resina', 'Aguja larga'
    UNION ALL SELECT 'Corona de Resina', 'Hilo retractor 0'
    UNION ALL SELECT 'Corona de Resina', 'Cánula de succión'
    UNION ALL SELECT 'Corona de Resina', 'Resina compuesta'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Anestesia tópica'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Anestesia con epinefrina'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Aguja corta'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Aguja larga'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Cánula de succión'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Dique de goma'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Ácido grabador'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Adhesivo'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Microbrush'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Resina compuesta'
    UNION ALL SELECT 'Tratamiento de Incisivos Fracturados', 'Papel articular'
    UNION ALL SELECT 'Mantenedor de Espacio Banda-ANSA', 'Alambre'
    UNION ALL SELECT 'Mantenedor de Espacio Banda-ANSA', 'Fundente'
    UNION ALL SELECT 'Mantenedor de Espacio Banda-ANSA', 'Soldadura de plata'
    UNION ALL SELECT 'Mantenedor de Espacio Banda-ANSA', 'Ionómero de cementación Ketac'
    UNION ALL SELECT 'Mantenedor de Espacio Corona de Acero-ANSA', 'Alambre'
    UNION ALL SELECT 'Mantenedor de Espacio Corona de Acero-ANSA', 'Fundente'
    UNION ALL SELECT 'Mantenedor de Espacio Corona de Acero-ANSA', 'Soldadura de plata'
    UNION ALL SELECT 'Arco Lingual Fijo o Removible', 'Bandas'
    UNION ALL SELECT 'Arco Lingual Fijo o Removible', 'Alambre'
    UNION ALL SELECT 'Arco Lingual Fijo o Removible', 'Soldadura de plata'
    UNION ALL SELECT 'Arco Lingual Fijo o Removible', 'Tubos linguales'
    UNION ALL SELECT 'Arco Lingual Fijo o Removible', 'Fundente'
    UNION ALL SELECT 'Rompe Hábitos Fijos o Removibles', 'Bandas'
    UNION ALL SELECT 'Rompe Hábitos Fijos o Removibles', 'Arco Niti'
    UNION ALL SELECT 'Rompe Hábitos Fijos o Removibles', 'Soldadura de plata'
    UNION ALL SELECT 'Rompe Hábitos Fijos o Removibles', 'Fundente'
    UNION ALL SELECT 'Plano Inclinado Directo', 'Acrílico rosado'
    UNION ALL SELECT 'Plano Inclinado Directo', 'Alambre'
    UNION ALL SELECT 'Plano Inclinado Directo', 'Bandas'
    UNION ALL SELECT 'Plano Inclinado Directo', 'Soldadura de plata'
    UNION ALL SELECT 'Plano Inclinado Directo', 'Ionómero de cementación Ketac'
    UNION ALL SELECT 'Plano Inclinado Directo', 'Fundente'
    UNION ALL SELECT 'Plano Inclinado Indirecto', 'Acrílico rosado'
    UNION ALL SELECT 'Plano Inclinado Indirecto', 'Alambre'
    UNION ALL SELECT 'Plano Inclinado Indirecto', 'Bandas'
    UNION ALL SELECT 'Plano Inclinado Indirecto', 'Soldadura de plata'
    UNION ALL SELECT 'Plano Inclinado Indirecto', 'Ketac Molar'
    UNION ALL SELECT 'Plano Inclinado Indirecto', 'Fundente'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (al contado)', 'Alambre'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (al contado)', 'Bandas'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (al contado)', 'Fundente'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (al contado)', 'Soldadura de plata'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (al contado)', 'Tubos linguales'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (Cuota inicial)', 'Alambre'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (Cuota inicial)', 'Fundente'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (Cuota inicial)', 'Soldadura de plata'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (Cuota inicial)', 'Tubos linguales'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (cancelación)', 'Alambre'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (cancelación)', 'Bandas'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (cancelación)', 'Fundente'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (cancelación)', 'Soldadura de plata'
    UNION ALL SELECT 'Quad Helix Fijo o Removible (cancelación)', 'Tubos linguales'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (al contado)', 'Alambre'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (al contado)', 'Bandas'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (al contado)', 'Fundente'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (al contado)', 'Soldadura de plata'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (al contado)', 'Tubos linguales'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (Cuota inicial)', 'Alambre'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (Cuota inicial)', 'Fundente'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (Cuota inicial)', 'Soldadura de plata'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (Cuota inicial)', 'Tubos linguales'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (cancelación)', 'Alambre'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (cancelación)', 'Bandas'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (cancelación)', 'Fundente'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (cancelación)', 'Soldadura de plata'
    UNION ALL SELECT 'Bi Helix Fijo o Removible (cancelación)', 'Tubos linguales'
    UNION ALL SELECT 'Arco Extraoral (al contado)', 'Bandas'
    UNION ALL SELECT 'Arco Extraoral (al contado)', 'Tubos triples'
    UNION ALL SELECT 'Arco Extraoral (al contado)', 'Arco extraoral'
    UNION ALL SELECT 'Arco Extraoral (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Arco Extraoral (Cuota inicial)', 'Tubos triples'
    UNION ALL SELECT 'Arco Extraoral (Cuota inicial)', 'Arco extraoral'
    UNION ALL SELECT 'Arco Extraoral (cancelación)', 'Bandas'
    UNION ALL SELECT 'Arco Extraoral (cancelación)', 'Tubos triples'
    UNION ALL SELECT 'Arco Extraoral (cancelación)', 'Arco extraoral'
    UNION ALL SELECT 'Disyuntor Hyrax (al contado)', 'Bandas'
    UNION ALL SELECT 'Disyuntor Hyrax (al contado)', 'Soldadura de plata'
    UNION ALL SELECT 'Disyuntor Hyrax (al contado)', 'Fundente'
    UNION ALL SELECT 'Disyuntor Hyrax (al contado)', 'Disyuntor Hyrax'
    UNION ALL SELECT 'Disyuntor Hyrax (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Disyuntor Hyrax (Cuota inicial)', 'Soldadura de plata'
    UNION ALL SELECT 'Disyuntor Hyrax (Cuota inicial)', 'Fundente'
    UNION ALL SELECT 'Disyuntor Hyrax (Cuota inicial)', 'Disyuntor Hyrax'
    UNION ALL SELECT 'Disyuntor Hyrax (cancelación)', 'Bandas'
    UNION ALL SELECT 'Disyuntor Hyrax (cancelación)', 'Soldadura de plata'
    UNION ALL SELECT 'Disyuntor Hyrax (cancelación)', 'Fundente'
    UNION ALL SELECT 'Disyuntor Hyrax (cancelación)', 'Disyuntor Hyrax'
    UNION ALL SELECT 'Disyuntor Hass (al contado)', 'Bandas'
    UNION ALL SELECT 'Disyuntor Hass (al contado)', 'Soldadura de plata'
    UNION ALL SELECT 'Disyuntor Hass (al contado)', 'Fundente'
    UNION ALL SELECT 'Disyuntor Hass (al contado)', 'Disyuntor Hyrax'
    UNION ALL SELECT 'Disyuntor Hass (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Disyuntor Hass (Cuota inicial)', 'Soldadura de plata'
    UNION ALL SELECT 'Disyuntor Hass (Cuota inicial)', 'Fundente'
    UNION ALL SELECT 'Disyuntor Hass (Cuota inicial)', 'Disyuntor Hyrax'
    UNION ALL SELECT 'Disyuntor Hass (cancelación)', 'Bandas'
    UNION ALL SELECT 'Disyuntor Hass (cancelación)', 'Soldadura de plata'
    UNION ALL SELECT 'Disyuntor Hass (cancelación)', 'Fundente'
    UNION ALL SELECT 'Disyuntor Hass (cancelación)', 'Disyuntor Hyrax'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (al contado)', 'Acrílico rosado'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (al contado)', 'Polvo transparente'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (al contado)', 'Monómero'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (al contado)', 'Bandas'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (Cuota inicial)', 'Acrílico rosado'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (Cuota inicial)', 'Polvo transparente'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (Cuota inicial)', 'Monómero'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (cancelación)', 'Acrílico rosado'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (cancelación)', 'Polvo transparente'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (cancelación)', 'Monómero'
    UNION ALL SELECT 'Aparato de Ortopedia Funcional (cancelación)', 'Bandas'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (al contado)', 'Brackets'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (al contado)', 'Tubos'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (al contado)', 'Alambre'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (al contado)', 'Bandas'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (Cuota inicial)', 'Brackets'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (Cuota inicial)', 'Tubos'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (Cuota inicial)', 'Alambre'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (cancelación)', 'Brackets'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (cancelación)', 'Tubos'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (cancelación)', 'Alambre'
    UNION ALL SELECT 'Aparatología Fija 2x4 por Arcada (cancelación)', 'Bandas'
    UNION ALL SELECT 'Mentonera (al contado)', 'Mentonera'
    UNION ALL SELECT 'Mentonera (Cuota inicial)', 'Mentonera'
    UNION ALL SELECT 'Mentonera (cancelación)', 'Mentonera'
    UNION ALL SELECT 'Máscara Facial Petit (al contado)', 'Bandas'
    UNION ALL SELECT 'Máscara Facial Petit (al contado)', 'Tubos triples'
    UNION ALL SELECT 'Máscara Facial Petit (al contado)', 'Máscara facial'
    UNION ALL SELECT 'Máscara Facial Petit (al contado)', 'Elásticos intermaxilares'
    UNION ALL SELECT 'Máscara Facial Petit (Cuota inicial)', 'Bandas'
    UNION ALL SELECT 'Máscara Facial Petit (Cuota inicial)', 'Tubos triples'
    UNION ALL SELECT 'Máscara Facial Petit (Cuota inicial)', 'Máscara facial'
    UNION ALL SELECT 'Máscara Facial Petit (Cuota inicial)', 'Elásticos intermaxilares'
    UNION ALL SELECT 'Máscara Facial Petit (cancelación)', 'Bandas'
    UNION ALL SELECT 'Máscara Facial Petit (cancelación)', 'Tubos triples'
    UNION ALL SELECT 'Máscara Facial Petit (cancelación)', 'Máscara facial'
    UNION ALL SELECT 'Máscara Facial Petit (cancelación)', 'Elásticos intermaxilares'
    UNION ALL SELECT 'Botón de Nance', 'Acrílico rosado'
    UNION ALL SELECT 'Botón de Nance', 'Alambre'
    UNION ALL SELECT 'Botón de Nance', 'Bandas'
    UNION ALL SELECT 'Lip Bumper Semi Fijo', 'Acrílico rosado'
    UNION ALL SELECT 'Lip Bumper Semi Fijo', 'Alambre'
    UNION ALL SELECT 'Lip Bumper Semi Fijo', 'Bandas'
    UNION ALL SELECT 'Lip Bumper Semi Fijo', 'Soldadura de plata'
    UNION ALL SELECT 'Lip Bumper Semi Fijo', 'Fundente'
    UNION ALL SELECT 'Lip Bumper Removible', 'Acrílico rosado'
    UNION ALL SELECT 'Lip Bumper Removible', 'Alambre'
    UNION ALL SELECT 'Lip Bumper Removible', 'Bandas'
    UNION ALL SELECT 'Lip Bumper Removible', 'Tubos triples'
    UNION ALL SELECT 'Placa Schwartz Estándar', 'Acrílico rosado'
    UNION ALL SELECT 'Placa Schwartz Estándar', 'Alambre'
    UNION ALL SELECT 'Placa Schwartz Estándar', 'Bandas'
    UNION ALL SELECT 'Placa Schwartz con Tornillo', 'Acrílico rosado'
    UNION ALL SELECT 'Placa Schwartz con Tornillo', 'Alambre'
    UNION ALL SELECT 'Placa Schwartz con Tornillo', 'Bandas'
) AS t
JOIN Tratamiento_PRED p ON p.NombreTratamiento = t.trat
JOIN Materiales m ON m.Nombre = t.mat;
