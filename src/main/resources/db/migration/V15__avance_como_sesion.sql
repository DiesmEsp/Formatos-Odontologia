-- ============================================================
-- V15__avance_como_sesion.sql
-- Redefine el avance de tratamiento: deja de ser un Tratamiento
-- hijo (Tipo='AVANCE' con TratamientoPadreID) y pasa a ser una
-- sesion del mismo Tratamiento (tabla Tratamiento_Avance).
--
-- Cambios:
--   1. Se eliminan los datos AVANCE existentes (datos de prueba).
--   2. Se reconstruye Tratamiento sin TratamientoPadreID y con
--      CHECK de Tipo reducido a NORMAL/CONTINUO. MontoAnterior se
--      conserva (lo usa cambiarTipo NORMAL<->CONTINUO, RF-1.4.7).
--   3. Nueva tabla Tratamiento_Avance (sesiones).
--   4. Nueva tabla Materiales_List_Avance (materiales por sesion).
--   5. Pago gana AvanceID opcional para trazabilidad.
--
-- Nota: SQLite no permite alterar un CHECK, por lo que se
-- reconstruye la tabla Tratamiento (mismo patron que V13). Las
-- migraciones corren con foreign_keys=OFF (ver ConnectionManager).
-- ============================================================

-- 1. Borrado defensivo de avances existentes (solo datos de prueba)
DELETE FROM Pago
WHERE TratamientoID IN (SELECT TratamientoID FROM Tratamiento WHERE Tipo = 'AVANCE');

DELETE FROM Materiales_List
WHERE TratamientoID IN (SELECT TratamientoID FROM Tratamiento WHERE Tipo = 'AVANCE');

DELETE FROM Tratamiento WHERE Tipo = 'AVANCE';

-- 2. Reconstruccion de Tratamiento (sin TratamientoPadreID)
CREATE TABLE Tratamiento_nueva (
    TratamientoID    INTEGER PRIMARY KEY AUTOINCREMENT,
    OperadorID       INTEGER NOT NULL REFERENCES Operadores (OperadorID),
    PacienteID       INTEGER NOT NULL REFERENCES Pacientes (PacienteID),
    UnidadID         INTEGER REFERENCES Unidad (UnidadID) ON DELETE SET NULL,
    Fecha            TEXT    NOT NULL,
    NombreTratamiento TEXT   NOT NULL,
    Monto            REAL    NOT NULL,
    Tipo             TEXT    NOT NULL DEFAULT 'NORMAL'
                     CHECK (Tipo IN ('NORMAL','CONTINUO')),
    EstadoPago       TEXT    NOT NULL DEFAULT 'PENDIENTE'
                     CHECK (EstadoPago IN ('PENDIENTE','PARCIAL','PAGADO')),
    MontoPagado      REAL    NOT NULL DEFAULT 0,
    Estado           TEXT    NOT NULL DEFAULT 'ABIERTO'
                     CHECK (Estado IN ('ABIERTO','CERRADO','ANULADO')),
    CerradoEn        TEXT,
    MontoAnterior    REAL    NULL,
    ClinicaID        INTEGER NOT NULL DEFAULT 1
);

INSERT INTO Tratamiento_nueva
    (TratamientoID, OperadorID, PacienteID, UnidadID, Fecha, NombreTratamiento,
     Monto, Tipo, EstadoPago, MontoPagado, Estado, CerradoEn, MontoAnterior, ClinicaID)
SELECT TratamientoID, OperadorID, PacienteID, UnidadID, Fecha, NombreTratamiento,
       Monto, Tipo, EstadoPago, MontoPagado, Estado, CerradoEn, MontoAnterior, ClinicaID
FROM Tratamiento;

DROP TABLE Tratamiento;

ALTER TABLE Tratamiento_nueva RENAME TO Tratamiento;

CREATE INDEX idx_tratamiento_fecha    ON Tratamiento (Fecha);
CREATE INDEX idx_tratamiento_operador ON Tratamiento (OperadorID);
CREATE INDEX idx_tratamiento_clinica  ON Tratamiento (ClinicaID, Estado);

-- 3. Sesiones de tratamiento (avances)
CREATE TABLE Tratamiento_Avance (
    AvanceID      INTEGER PRIMARY KEY AUTOINCREMENT,
    TratamientoID INTEGER NOT NULL REFERENCES Tratamiento (TratamientoID),
    Fecha         TEXT    NOT NULL,
    UnidadID      INTEGER REFERENCES Unidad (UnidadID) ON DELETE SET NULL,
    Estado        TEXT    NOT NULL DEFAULT 'ACTIVO'
                  CHECK (Estado IN ('ACTIVO','ANULADO')),
    Timestamp     TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX idx_avance_tratamiento ON Tratamiento_Avance (TratamientoID);

-- 4. Materiales consumidos por sesion
CREATE TABLE Materiales_List_Avance (
    MaterialesListAvanceID INTEGER PRIMARY KEY AUTOINCREMENT,
    AvanceID               INTEGER NOT NULL REFERENCES Tratamiento_Avance (AvanceID) ON DELETE CASCADE,
    MaterialID             INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad               REAL    NOT NULL CHECK (Cantidad > 0)
);

CREATE INDEX idx_mat_avance_avance ON Materiales_List_Avance (AvanceID);

-- 5. Pago vinculable a una sesion
ALTER TABLE Pago ADD COLUMN AvanceID INTEGER NULL REFERENCES Tratamiento_Avance (AvanceID);

CREATE INDEX idx_pago_avance ON Pago (AvanceID);
