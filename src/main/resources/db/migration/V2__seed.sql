-- ============================================================
-- V2__seed.sql
-- Datos de seed consolidados (carga inicial del primer lanzamiento):
--   - 281 materiales (catalogo completo)
--   - 94 tratamientos predefinidos con sus materiales sugeridos
--   - Unidad de tratamiento inicial (Unidad 1)
--
-- No se siembra clinica: la primera que el usuario cree ocupara
-- ClinicaID = 1.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Materiales (281)
-- ------------------------------------------------------------
INSERT INTO Materiales (Nombre, Unidad, Estado) VALUES
  ('Ácido fluorhídrico', 'frasco', 1),
  ('Ácido fosfórico', 'frasco', 1),
  ('Ácido grabador', 'frasco', 1),
  ('Acondicionador de tejido', 'frasco', 1),
  ('Acrílico 59', 'kit', 1),
  ('Acrílico 62', 'kit', 1),
  ('Acrílico 65', 'kit', 1),
  ('Acrílico líquido monómero', 'frasco', 1),
  ('Acrílico patrón (Pattern Resin)', 'kit', 1),
  ('Acrílico rosado', 'kit', 1),
  ('Acrílico transparente', 'kit', 1),
  ('Adhesivo', 'frasco', 1),
  ('Adhesivo Mucopren', 'frasco', 1),
  ('Agua oxigenada', 'frasco', 1),
  ('Aguja corta', 'unidad', 1),
  ('Aguja extra corta', 'unidad', 1),
  ('Aguja larga', 'unidad', 1),
  ('Aguja Navitip', 'unidad', 1),
  ('Alambre', 'unidad', 1),
  ('Alambre 0.8', 'unidad', 1),
  ('Alambre 0.9', 'unidad', 1),
  ('Alambre de ligadura', 'unidad', 1),
  ('Alcohol puro 70°', 'frasco', 1),
  ('Alcohol yodado', 'frasco', 1),
  ('Alginato', 'bolsa', 1),
  ('Algodón 500 g', 'bolsa', 1),
  ('Alveogil', 'frasco', 1),
  ('Amonio cuaternario', 'frasco', 1),
  ('Anestesia con epinefrina', 'cartucho', 1),
  ('Anestesia sin epinefrina', 'cartucho', 1),
  ('Anestesia tópica', 'frasco', 1),
  ('Arco de dique de goma', 'unidad', 1),
  ('Arco extraoral', 'unidad', 1),
  ('Arco Niti', 'unidad', 1),
  ('Azul de metileno', 'frasco', 1),
  ('Bajalengua', 'paquete', 1),
  ('Bandas', 'unidad', 1),
  ('Bata de cirujano', 'unidad', 1),
  ('Benzocaína', 'frasco', 1),
  ('Bio-C Sealer', 'kit', 1),
  ('Bisacril', 'kit', 1),
  ('Bolsa de chupete', 'unidad', 1),
  ('Botas quirúrgicas', 'par', 1),
  ('Brackets', 'unidad', 1),
  ('Brackets S-I', 'unidad', 1),
  ('Brackets superiores', 'unidad', 1),
  ('Caja roja punzocortante', 'unidad', 1),
  ('Campo descartable', 'paquete', 1),
  ('Campo fenestrado', 'paquete', 1),
  ('Campo simple', 'paquete', 1),
  ('Cánula de succión', 'unidad', 1),
  ('Caucho de goma', 'unidad', 1),
  ('Cemento para carillas Allcem A1', 'kit', 1),
  ('Cemento para carillas Allcem Trans', 'kit', 1),
  ('Cemento para cementación', 'kit', 1),
  ('Cemento policarboxilato', 'kit', 1),
  ('Cemento quirúrgico', 'kit', 1),
  ('Cemento RelyX U200', 'kit', 1),
  ('Cemento RelyX Ultimate', 'kit', 1),
  ('Cemento Variolink', 'kit', 1),
  ('Cera amarilla', 'caja', 1),
  ('Cera base', 'caja', 1),
  ('Cera calibrada', 'caja', 1),
  ('Cera Cavex', 'caja', 1),
  ('Cinta celuloide', 'caja', 1),
  ('Cinta matriz metálica', 'caja', 1),
  ('Clorhexidina', 'frasco', 1),
  ('Coltosol', 'frasco', 1),
  ('Conos de gutapercha', 'caja', 1),
  ('Conos de papel N° 40', 'caja', 1),
  ('Coronas de acero', 'unidad', 1),
  ('Cubeta para flúor', 'unidad', 1),
  ('Cuñas de madera', 'caja', 1),
  ('Desobturador', 'unidad', 1),
  ('Detergente enzimático', 'sobre', 1),
  ('Dique de goma', 'hoja', 1),
  ('Disyuntor Hyrax', 'unidad', 1),
  ('Disyuntor N° 11', 'unidad', 1),
  ('Disyuntor N° 13', 'unidad', 1),
  ('Disyuntor N° 6', 'unidad', 1),
  ('Duralay', 'kit', 1),
  ('Duralay 62', 'kit', 1),
  ('Duralay 65', 'kit', 1),
  ('Duralay rojo', 'kit', 1),
  ('Dycal', 'frasco', 1),
  ('EDTA', 'frasco', 1),
  ('Elásticos intermaxilares', 'paquete', 1),
  ('Endo Ice', 'unidad', 1),
  ('Escobilla interproximal', 'unidad', 1),
  ('Escobilla profiláctica', 'unidad', 1),
  ('Eugenato', 'frasco', 1),
  ('Eugenol', 'frasco', 1),
  ('Fibra de vidrio 0.5', 'unidad', 1),
  ('Fibra de vidrio 1', 'unidad', 1),
  ('Fibra de vidrio 2', 'unidad', 1),
  ('Flúor', 'frasco', 1),
  ('Flúor barniz', 'frasco', 1),
  ('Flúor barniz Climpro', 'frasco', 1),
  ('Flúor neutro', 'frasco', 1),
  ('Formocresol', 'frasco', 1),
  ('Fresa de alta velocidad', 'unidad', 1),
  ('Fresa de baja velocidad', 'unidad', 1),
  ('Fresa de desgaste', 'unidad', 1),
  ('Fundente', 'frasco', 1),
  ('Gasa', 'paquete', 1),
  ('Gasa bucal', 'paquete', 1),
  ('Gasa por paquete', 'paquete', 1),
  ('Gasa x 10 unidades', 'paquete', 1),
  ('Gasa x 5 unidades', 'paquete', 1),
  ('GC Fuji II', 'frasco', 1),
  ('GC Fuji IX', 'frasco', 1),
  ('Gingifast', 'kit', 1),
  ('Glicerina', 'frasco', 1),
  ('Godiva en barra', 'unidad', 1),
  ('Gorro cirujano', 'caja', 1),
  ('Gorro de enfermera', 'caja', 1),
  ('Grapas de dique', 'caja', 1),
  ('Guantes descartables L', 'caja', 1),
  ('Guantes descartables M', 'caja', 1),
  ('Guantes descartables S', 'caja', 1),
  ('Guantes descartables XS', 'caja', 1),
  ('Guantes quirúrgicos 6 1/2', 'caja', 1),
  ('Guantes quirúrgicos 7', 'caja', 1),
  ('Guantes quirúrgicos 7 1/2', 'caja', 1),
  ('Gutapercha en barra', 'caja', 1),
  ('Hemocoldágeno', 'frasco', 1),
  ('Hemotam', 'frasco', 1),
  ('Hibiclens', 'frasco', 1),
  ('Hidróxido de calcio', 'frasco', 1),
  ('Hilo de sutura nylon 4/0', 'sobre', 1),
  ('Hilo de sutura nylon 5/0', 'sobre', 1),
  ('Hilo de sutura poliglicólico 4/0', 'sobre', 1),
  ('Hilo de sutura poliglicólico 5/0', 'sobre', 1),
  ('Hilo de sutura seda trenzada 3/0', 'sobre', 1),
  ('Hilo dental', 'unidad', 1),
  ('Hilo retractor 0', 'unidad', 1),
  ('Hilo retractor 00', 'unidad', 1),
  ('Hilo retractor 000', 'unidad', 1),
  ('Hipoclorito de sodio', 'frasco', 1),
  ('Hisol', 'frasco', 1),
  ('Hisopos', 'paquete', 1),
  ('Hoja de bisturí N° 11', 'unidad', 1),
  ('Hoja de bisturí N° 12', 'unidad', 1),
  ('Hoja de bisturí N° 15', 'unidad', 1),
  ('Hoja de bisturí N° 15C', 'unidad', 1),
  ('Ionómero de base', 'frasco', 1),
  ('Ionómero de cementación Ketac', 'frasco', 1),
  ('Ionómero de cementación Meron', 'frasco', 1),
  ('Ionómero de reconstrucción Ketac', 'frasco', 1),
  ('Ionómero Glass Liners', 'frasco', 1),
  ('Ionómero Vitrebond', 'frasco', 1),
  ('Ionómero Vitremer', 'frasco', 1),
  ('Jeringa carpule', 'unidad', 1),
  ('Jeringa descartable 10 ml', 'unidad', 1),
  ('Jeringa descartable 20 ml', 'unidad', 1),
  ('Jeringa descartable 5 ml', 'unidad', 1),
  ('Jeringa tuberculina', 'unidad', 1),
  ('Ketac Molar', 'frasco', 1),
  ('Ketac Universal', 'frasco', 1),
  ('Liga metálica', 'unidad', 1),
  ('Ligas intraorales', 'paquete', 1),
  ('Ligas separadoras', 'paquete', 1),
  ('Lip Bumper', 'unidad', 1),
  ('Líquido fijador', 'frasco', 1),
  ('Líquido revelador', 'frasco', 1),
  ('Mandil descartable', 'unidad', 1),
  ('Mandil quirúrgico', 'unidad', 1),
  ('Mangas quirúrgicas', 'par', 1),
  ('Máscara facial', 'unidad', 1),
  ('Mascarilla descartable', 'caja', 1),
  ('Mascarilla KN95', 'caja', 1),
  ('Mentonera', 'unidad', 1),
  ('Microbrush', 'unidad', 1),
  ('Monómero', 'frasco', 1),
  ('MTA', 'frasco', 1),
  ('Mucopren', 'kit', 1),
  ('Neociler Flo', 'frasco', 1),
  ('Neoputy', 'kit', 1),
  ('Oclufast', 'kit', 1),
  ('Orange Wash + activador', 'kit', 1),
  ('Orto-Bite', 'unidad', 1),
  ('Óxido de aluminio', 'frasco', 1),
  ('Óxido de zinc + eugenol', 'frasco', 1),
  ('Papel absorbente', 'caja', 1),
  ('Papel articular', 'caja', 1),
  ('Papel articular arcada completa', 'caja', 1),
  ('Papel crepado x 2 pliegos', 'paquete', 1),
  ('Papel crepado x 4 pliegos', 'paquete', 1),
  ('Papel higiénico', 'rollo', 1),
  ('Papel toalla', 'rollo', 1),
  ('Paramonoclorofenol', 'frasco', 1),
  ('Pasta profiláctica', 'frasco', 1),
  ('Pastilla reveladora', 'unidad', 1),
  ('Pelex', 'unidad', 1),
  ('Piedra pómez', 'frasco', 1),
  ('Polvo transparente', 'frasco', 1),
  ('Provinol', 'kit', 1),
  ('Punta diamantada', 'unidad', 1),
  ('Radiografía', 'unidad', 1),
  ('Resina Body', 'jeringa', 1),
  ('Resina Body Esmalte A1', 'jeringa', 1),
  ('Resina Body Esmalte A2', 'jeringa', 1),
  ('Resina Body Shade', 'jeringa', 1),
  ('Resina compuesta', 'jeringa', 1),
  ('Resina Enamel A3', 'jeringa', 1),
  ('Resina fluida A1', 'jeringa', 1),
  ('Resina fluida A2', 'jeringa', 1),
  ('Resina fluida B2', 'jeringa', 1),
  ('Resina fotocurable', 'jeringa', 1),
  ('Resina fotocurable A1', 'jeringa', 1),
  ('Resina fotocurado A1 Body', 'jeringa', 1),
  ('Resina fotocurado A2 Body', 'jeringa', 1),
  ('Resina fotocurado A3 Body', 'jeringa', 1),
  ('Resina fotocurado B2 Body', 'jeringa', 1),
  ('Resina fotocurado Dentin A3', 'jeringa', 1),
  ('Resina fotocurado Enamel A1', 'jeringa', 1),
  ('Resina One Bulk Fill Restorative A3', 'jeringa', 1),
  ('Resina Opalis', 'jeringa', 1),
  ('Resina Orthocem', 'jeringa', 1),
  ('Resina Tetric A1', 'jeringa', 1),
  ('Resina Tetric A2', 'jeringa', 1),
  ('Resina Tetric A3', 'jeringa', 1),
  ('Resina Tetric A3.5', 'jeringa', 1),
  ('Resina Tetric Ceram A1', 'jeringa', 1),
  ('Resina Tetric Ceram A2', 'jeringa', 1),
  ('Resina Tetric Ceram B2', 'jeringa', 1),
  ('Resina translúcida', 'jeringa', 1),
  ('Resina Universal A2', 'jeringa', 1),
  ('Revelador de placa', 'frasco', 1),
  ('RX oclusal', 'unidad', 1),
  ('RX periapical adulto', 'unidad', 1),
  ('RX periapical niño', 'unidad', 1),
  ('Sellante', 'jeringa', 1),
  ('Sellante 3M', 'jeringa', 1),
  ('Sellante Climpro', 'jeringa', 1),
  ('Silano', 'frasco', 1),
  ('Silicona Coltene', 'kit', 1),
  ('Silicona de adición', 'kit', 1),
  ('Silicona de adición + fluida', 'kit', 1),
  ('Silicona de adición Panasil', 'kit', 1),
  ('Silicona de adición pesada', 'kit', 1),
  ('Silicona de condensación pesada', 'kit', 1),
  ('Silicona de condensación Z-Plus', 'kit', 1),
  ('Silicona Elite Glass', 'kit', 1),
  ('Silicona Elite HD Busetcarn', 'kit', 1),
  ('Silicona Elite HD Light', 'kit', 1),
  ('Silicona Elite HD Super Light', 'kit', 1),
  ('Silicona fluida Medium Body', 'kit', 1),
  ('Silicona fluida Regular Body', 'kit', 1),
  ('Silicona Futar', 'kit', 1),
  ('Silicona HD Azul', 'kit', 1),
  ('Silicona HD Morado Regular Body', 'kit', 1),
  ('Silicona HD Regular Light Body', 'kit', 1),
  ('Silicona Light Adición', 'kit', 1),
  ('Silicona pesada', 'kit', 1),
  ('Silicona pesada Z-Plus', 'kit', 1),
  ('Sobres de esterilización', 'paquete', 1),
  ('Soldadura de plata', 'rollo', 1),
  ('Spidlex Silicona', 'kit', 1),
  ('Suero fisiológico', 'frasco', 1),
  ('Teflón', 'rollo', 1),
  ('TheraCal', 'kit', 1),
  ('Tiras de lija para resina', 'tira', 1),
  ('Top Dam', 'frasco', 1),
  ('Tubos', 'unidad', 1),
  ('Tubos linguales', 'unidad', 1),
  ('Tubos mixtos', 'unidad', 1),
  ('Tubos triples', 'unidad', 1),
  ('Vaselina', 'frasco', 1),
  ('Vaso descartable', 'paquete', 1),
  ('Visacril', 'kit', 1),
  ('Yeso azul', 'bolsa', 1),
  ('Yeso extraduro', 'bolsa', 1),
  ('Yeso ortodoncia', 'bolsa', 1),
  ('Yeso París', 'bolsa', 1),
  ('Yeso piedra', 'bolsa', 1),
  ('Yeso tipo III', 'bolsa', 1),
  ('Yeso tipo IV', 'bolsa', 1),
  ('Yodo povidona', 'frasco', 1),
  ('Zirclean', 'frasco', 1),
  ('Z-Prime', 'frasco', 1);

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


-- ------------------------------------------------------------
-- 2. Unidad de tratamiento inicial
-- ------------------------------------------------------------
INSERT INTO Unidad (UnidadNro) VALUES (1);