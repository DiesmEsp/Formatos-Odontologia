-- ============================================================
-- V7__corregir_tipos_pre.sql
-- Corrige la restriccion CHECK de Operadores para que el grado
-- PRE acepte los tipos 3, 4 y 5 (antes 4, 5, 6).
-- ============================================================

-- SQLite no permite ALTER CONSTRAINT, se recrea la tabla
CREATE TABLE Operadores_nueva (
    OperadorID  INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombres     TEXT    NOT NULL,
    Apellidos   TEXT    NOT NULL,
    Grado       TEXT    NOT NULL CHECK (Grado IN ('PRE','POS')),
    Tipo        TEXT    NOT NULL,
    Periodo     INTEGER NOT NULL,
    Estado      INTEGER NOT NULL DEFAULT 1,
    DNI         TEXT,
    CHECK ((Grado = 'PRE' AND Tipo IN ('3','4','5'))
        OR (Grado = 'POS' AND Tipo IN ('R1','R2','R3')))
);

INSERT INTO Operadores_nueva (OperadorID, Nombres, Apellidos, Grado, Tipo, Periodo, Estado, DNI)
SELECT OperadorID, Nombres, Apellidos, Grado,
       CASE WHEN Grado = 'PRE' AND Tipo = '6' THEN '3' ELSE Tipo END,
       Periodo, Estado, DNI
FROM Operadores;

DROP TABLE Operadores;

ALTER TABLE Operadores_nueva RENAME TO Operadores;
