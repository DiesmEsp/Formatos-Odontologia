-- ============================================================
-- V6__seed_tratamientos_pred.sql
-- Datos semilla: tratamientos predefinidos, sus materiales y
-- datos demo (docentes, pacientes, operadores, unidades)
-- ============================================================

-- ------------------------------------------------------------
-- 1. Tratamientos predefinidos
-- ------------------------------------------------------------
INSERT INTO Tratamiento_PRED (NombreTratamiento, MontoSugerido) VALUES
('Exodoncia simple', 30.00),
('Obturacion con amalgama', 25.00),
('Profilaxis dental', 15.00),
('Endodoncia unirradicular', 80.00),
('Corona metalica', 120.00),
('Limpieza y revision', 0.00),
('Curacion dental', 20.00),
('Aplicacion de fluor', 10.00);

-- ------------------------------------------------------------
-- 2. Materiales asociados a cada tratamiento predefinido
-- ------------------------------------------------------------

-- (1) Exodoncia simple: anestesia infiltrativa, gasa, guantes M, barbijo, jeringa carpule
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(1, 3, 2.0),
(1, 4, 4.0),
(1, 5, 2.0),
(1, 8, 1.0),
(1, 13, 1.0);

-- (2) Obturacion con amalgama: anestesia infiltrativa, gasa, guantes M, barbijo, algodon
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(2, 3, 2.0),
(2, 4, 4.0),
(2, 5, 2.0),
(2, 8, 1.0),
(2, 1, 3.0);

-- (3) Profilaxis dental: piedra pomez, gasa, guantes M, barbijo, vasos
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(3, 23, 1.0),
(3, 4, 2.0),
(3, 5, 2.0),
(3, 8, 1.0),
(3, 21, 2.0);

-- (4) Endodoncia unirradicular: anestesia, gasa, guantes M, barbijo, hipoclorito, agua oxigenada, algodon, jeringa carpule
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(4, 3, 2.0),
(4, 4, 6.0),
(4, 5, 2.0),
(4, 8, 1.0),
(4, 33, 1.0),
(4, 34, 1.0),
(4, 1, 4.0),
(4, 13, 1.0);

-- (5) Corona metalica: anestesia, gasa, guantes M, barbijo, ionomero de vidrio, fresa alta velocidad, algodon
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(5, 3, 2.0),
(5, 4, 4.0),
(5, 5, 2.0),
(5, 8, 1.0),
(5, 28, 1.0),
(5, 24, 1.0),
(5, 1, 2.0);

-- (6) Limpieza y revision: guantes M, barbijo, gasa bucal, vasos, servilletas de campo
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(6, 5, 2.0),
(6, 8, 1.0),
(6, 35, 2.0),
(6, 21, 2.0),
(6, 22, 2.0);

-- (7) Curacion dental: anestesia topica, gasa, guantes M, barbijo, ionomero de vidrio, algodon
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(7, 3, 1.0),
(7, 4, 3.0),
(7, 5, 2.0),
(7, 8, 1.0),
(7, 28, 1.0),
(7, 1, 2.0);

-- (8) Aplicacion de fluor: guantes M, barbijo, vasos, gasa bucal
INSERT INTO Materiales_List_PRED (TratPredID, MaterialID, Cantidad) VALUES
(8, 5, 2.0),
(8, 8, 1.0),
(8, 21, 1.0),
(8, 35, 1.0);

-- ------------------------------------------------------------
-- 3. Datos demo
-- ------------------------------------------------------------

-- Unidades adicionales (la unidad 1 ya existe por V2)
INSERT INTO Unidad (UnidadNro) VALUES
(2), (3), (4), (5), (6);

-- Docentes
INSERT INTO Docentes (Nombres, Apellidos, Estado, Telefono) VALUES
('Maria', 'Gonzalez Rivas', 1, ''),
('Carlos', 'Mendoza Paredes', 1, ''),
('Ana', 'Huaman Torres', 1, ''),
('Luis', 'Quispe Condori', 1, ''),
('Patricia', 'Vargas Leon', 1, ''),
('Roberto', 'Castro Diaz', 1, '');

-- Operadores (especialistas)
INSERT INTO Operadores (Nombres, Apellidos, Grado, Tipo, Periodo, Estado) VALUES
('Pedro', 'Ramirez Lopez', 'PRE', '4', 2026, 1),
('Sofia', 'Lopez Garcia', 'POS', 'R1', 2026, 1),
('Diego', 'Marquez Rojas', 'PRE', '5', 2026, 1),
('Lucia', 'Fernandez Paz', 'POS', 'R3', 2026, 1),
('Jorge', 'Alvarez Mori', 'PRE', '4', 2026, 1),
('Carmen', 'Torres Silva', 'POS', 'R2', 2026, 1),
('Miguel', 'Rios Ortega', 'PRE', '4', 2026, 1),
('Valeria', 'Paredes Cueva', 'POS', 'R1', 2026, 1);

-- Pacientes
INSERT INTO Pacientes (Nombres, Apellidos) VALUES
('Juan', 'Perez Mendez'),
('Ana', 'Torres Galvez'),
('Luis', 'Garcia Ramos'),
('Carla', 'Ruiz Castillo'),
('Maria', 'Lopez Quiroz'),
('Jose', 'Sanchez Davila'),
('Rosa', 'Flores Medina'),
('Manuel', 'Vargas Nunez'),
('Elena', 'Campos Rivas'),
('Alberto', 'Morales Ponce');
