-- ============================================================
-- V2__seed.sql
-- Datos de seed / carga inicial (checklist 2.14)
-- Primera ejecución: catálogo básico de materiales comunes y
-- la unidad de tratamiento inicial. El resto se crea en el uso.
-- ============================================================

-- ------------------------------------------------------------
-- Materiales comunes (Nombre UNIQUE)
-- ------------------------------------------------------------
INSERT INTO Materiales (Nombre, Unidad, Estado) VALUES
  ('Algodón',                      'bolsa', 1),
  ('Anestesia tópica',             'frasco', 1),
  ('Anestesia infiltrativa',       'cartucho', 1),
  ('Gasa estéril',                 'paquete', 1),
  ('Guantes descartables talla M', 'caja', 1),
  ('Guantes descartables talla S', 'caja', 1),
  ('Guantes descartables talla L', 'caja', 1),
  ('Barbijo descartable',          'caja', 1),
  ('Gorro descartable',            'caja', 1),
  ('Mascarilla facial',            'unidad', 1),
  ('Hilo de sutura',               'unidad', 1),
  ('Aguja de sutura',              'unidad', 1),
  ('Jeringa carpule',              'unidad', 1),
  ('Jeringa descartable 5 ml',     'unidad', 1),
  ('Jeringa descartable 10 ml',    'unidad', 1),
  ('Aguja corta',                  'unidad', 1),
  ('Aguja larga',                  'unidad', 1),
  ('Algodonero',                   'unidad', 1),
  ('Dique de goma',                'hoja', 1),
  ('Arco de dique de goma',        'unidad', 1),
  ('Grapas de dique',              'caja', 1),
  ('Vasos descartables',           'paquete', 1),
  ('Servilletas de campo',         'paquete', 1),
  ('Piedra pómez',                 'frasco', 1),
  ('Fresa de alta velocidad',      'unidad', 1),
  ('Fresa de baja velocidad',      'unidad', 1),
  ('Punta diamantada',             'unidad', 1),
  ('Óxido de zinc eugenol',        'frasco', 1),
  ('Ionómero de vidrio',           'frasco', 1),
  ('Resina compuesta',             'jeringa', 1),
  ('Sistema adhesivo',             'frasco', 1),
  ('Grabado ácido',                'frasco', 1),
  ('Amonio cuaternario',           'frasco', 1),
  ('Hipoclorito de sodio',         'frasco', 1),
  ('Agua oxigenada',               'frasco', 1),
  ('Gasa bucal',                   'paquete', 1);

-- ------------------------------------------------------------
-- Unidad de tratamiento inicial (numeración automática)
-- ------------------------------------------------------------
INSERT INTO Unidad (UnidadNro) VALUES (1);
