-- ============================================================
-- V13__tratamiento_avances_pagos.sql
-- Agrega el tipo AVANCE, la relacion padre/hijo, el monto anterior
-- y la tabla de pagos con historial.
--
-- Nota: SQLite no permite alterar un CHECK, por lo que se
-- reconstruye la tabla Tratamiento. Las migraciones corren con
-- foreign_keys=OFF (ver ConnectionManager), asi que el DROP no
-- falla por las FKs que apuntan a Tratamiento.
-- ============================================================

-- 1. Reconstruccion de Tratamiento
CREATE TABLE Tratamiento_nueva (
    TratamientoID    INTEGER PRIMARY KEY AUTOINCREMENT,
    OperadorID       INTEGER NOT NULL REFERENCES Operadores (OperadorID),
    PacienteID       INTEGER NOT NULL REFERENCES Pacientes (PacienteID),
    UnidadID         INTEGER REFERENCES Unidad (UnidadID) ON DELETE SET NULL,
    Fecha            TEXT    NOT NULL,
    NombreTratamiento TEXT   NOT NULL,
    Monto            REAL    NOT NULL,
    Tipo             TEXT    NOT NULL DEFAULT 'NORMAL'
                     CHECK (Tipo IN ('NORMAL','CONTINUO','AVANCE')),
    EstadoPago       TEXT    NOT NULL DEFAULT 'PENDIENTE'
                     CHECK (EstadoPago IN ('PENDIENTE','PARCIAL','PAGADO')),
    MontoPagado      REAL    NOT NULL DEFAULT 0,
    Estado           TEXT    NOT NULL DEFAULT 'ABIERTO'
                     CHECK (Estado IN ('ABIERTO','CERRADO','ANULADO')),
    CerradoEn        TEXT,
    TratamientoPadreID INTEGER NULL REFERENCES Tratamiento (TratamientoID),
    MontoAnterior    REAL    NULL
);

INSERT INTO Tratamiento_nueva
    (TratamientoID, OperadorID, PacienteID, UnidadID, Fecha, NombreTratamiento,
     Monto, Tipo, EstadoPago, MontoPagado, Estado, CerradoEn)
SELECT TratamientoID, OperadorID, PacienteID, UnidadID, Fecha, NombreTratamiento,
       Monto, Tipo, EstadoPago, MontoPagado, Estado, CerradoEn
FROM Tratamiento;

DROP TABLE Tratamiento;

ALTER TABLE Tratamiento_nueva RENAME TO Tratamiento;

CREATE INDEX idx_tratamiento_fecha    ON Tratamiento (Fecha);
CREATE INDEX idx_tratamiento_operador ON Tratamiento (OperadorID);

-- 2. Tabla de pagos con historial
CREATE TABLE Pago (
    PagoID        INTEGER PRIMARY KEY AUTOINCREMENT,
    TratamientoID INTEGER NOT NULL REFERENCES Tratamiento (TratamientoID),
    Fecha         TEXT    NOT NULL,
    Monto         REAL    NOT NULL CHECK (Monto > 0),
    Timestamp     TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX idx_pago_tratamiento ON Pago (TratamientoID);

-- 3. Backfill: un pago por cada tratamiento que ya tenia MontoPagado > 0
INSERT INTO Pago (TratamientoID, Fecha, Monto)
SELECT TratamientoID, Fecha, MontoPagado
FROM Tratamiento
WHERE MontoPagado > 0;
