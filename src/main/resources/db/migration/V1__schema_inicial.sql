-- ============================================================
-- V1__schema_inicial.sql
-- Esquema inicial: Formatos-Odontologia (clínica odontológica)
-- Motor: SQLite 3.x
--
-- Nota: PRAGMA foreign_keys = ON se aplica por conexion en
-- ConnectionManager (no en migraciones, para evitar mezclar
-- sentencias transaccionales y no transaccionales).
-- ============================================================

-- ------------------------------------------------------------
-- 1. Catálogos
-- ------------------------------------------------------------

-- Operadores (especialistas / estudiantes)
CREATE TABLE Operadores (
    OperadorID  INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombres     TEXT    NOT NULL,
    Apellidos   TEXT    NOT NULL,
    Grado       TEXT    NOT NULL CHECK (Grado IN ('PRE','POS')),
    Tipo        TEXT    NOT NULL,
    Periodo     INTEGER NOT NULL,
    Estado      INTEGER NOT NULL DEFAULT 1,
    CHECK ((Grado = 'PRE' AND Tipo IN ('4','5','6'))
        OR (Grado = 'POS' AND Tipo IN ('R1','R2','R3')))
);

-- Materiales
CREATE TABLE Materiales (
    MaterialID  INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombre      TEXT    NOT NULL UNIQUE,
    Unidad      TEXT    NOT NULL,
    Estado      INTEGER NOT NULL DEFAULT 1
);

-- Docentes
CREATE TABLE Docentes (
    DocenteID   INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombres     TEXT    NOT NULL,
    Apellidos   TEXT    NOT NULL,
    Estado      INTEGER NOT NULL DEFAULT 1
);

-- Pacientes
CREATE TABLE Pacientes (
    PacienteID  INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombres     TEXT    NOT NULL,
    Apellidos   TEXT    NOT NULL
);

-- Unidad de tratamiento / módulo
CREATE TABLE Unidad (
    UnidadID    INTEGER PRIMARY KEY AUTOINCREMENT,
    UnidadNro   INTEGER NOT NULL
);

-- Plantilla de tratamiento
CREATE TABLE Tratamiento_PRED (
    TratPredID       INTEGER PRIMARY KEY AUTOINCREMENT,
    NombreTratamiento TEXT   NOT NULL UNIQUE,
    MontoSugerido    REAL
);

-- Materiales sugeridos por plantilla
CREATE TABLE Materiales_List_PRED (
    MaterialListPredID  INTEGER PRIMARY KEY AUTOINCREMENT,
    TratPredID          INTEGER NOT NULL REFERENCES Tratamiento_PRED (TratPredID),
    MaterialID          INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad            REAL    NOT NULL DEFAULT 1,
    UNIQUE (TratPredID, MaterialID)
);

-- ------------------------------------------------------------
-- 2. Registros transaccionales
-- ------------------------------------------------------------

-- Tratamiento (instancia)
CREATE TABLE Tratamiento (
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
    CerradoEn        TEXT
);

-- Materiales consumidos en un tratamiento (precargados de plantilla + adicionales)
CREATE TABLE Materiales_List (
    MaterialesListID  INTEGER PRIMARY KEY AUTOINCREMENT,
    TratamientoID     INTEGER NOT NULL REFERENCES Tratamiento (TratamientoID),
    MaterialID        INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad          REAL    NOT NULL CHECK (Cantidad > 0)
);

-- Asistencia diaria de docente
CREATE TABLE Asistencia (
    AsistenciaID  INTEGER PRIMARY KEY AUTOINCREMENT,
    DocenteID     INTEGER NOT NULL REFERENCES Docentes (DocenteID),
    Fecha         TEXT    NOT NULL,
    Estado        TEXT    NOT NULL DEFAULT 'ACTIVO'
                  CHECK (Estado IN ('ACTIVO','ANULADO')),
    UNIQUE (DocenteID, Fecha, Estado)
);

-- Materiales recibidos por un docente en un día
CREATE TABLE Materiales_Asistencia (
    MatAsistenciaID  INTEGER PRIMARY KEY AUTOINCREMENT,
    AsistenciaID     INTEGER NOT NULL REFERENCES Asistencia (AsistenciaID),
    MaterialesID     INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad         REAL    NOT NULL
);

-- ------------------------------------------------------------
-- 3. Conversiones y auditoría
-- ------------------------------------------------------------

-- Conversión de unidades de empaque a unidad base
CREATE TABLE Unidad_Conversion (
    ConversionID  INTEGER PRIMARY KEY AUTOINCREMENT,
    MaterialID    INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    UnidadBase    TEXT    NOT NULL,
    UnidadEmpaque TEXT    NOT NULL,
    Factor        REAL    NOT NULL,
    UNIQUE (MaterialID, UnidadEmpaque)
);

-- Auditoría de anulaciones
CREATE TABLE RegistroAnulacion (
    AnulacionID       INTEGER PRIMARY KEY AUTOINCREMENT,
    TablaAfectada     TEXT    NOT NULL,
    IdRegistroAnulado INTEGER NOT NULL,
    Motivo            TEXT,
    Usuario           TEXT    NOT NULL,
    Timestamp         TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- Índices para consultas frecuentes por mes/reporte
-- ------------------------------------------------------------
CREATE INDEX idx_tratamiento_fecha     ON Tratamiento (Fecha);
CREATE INDEX idx_tratamiento_operador  ON Tratamiento (OperadorID);
CREATE INDEX idx_asistencia_fecha      ON Asistencia (Fecha);
CREATE INDEX idx_materiales_list_trat  ON Materiales_List (TratamientoID);
CREATE INDEX idx_mat_asistencia_asis   ON Materiales_Asistencia (AsistenciaID);
