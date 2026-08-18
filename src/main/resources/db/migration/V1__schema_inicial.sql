-- ============================================================
-- V1__schema_inicial.sql
-- Esquema consolidado: Formatos-Odontologia (clínica odontológica)
-- Motor: SQLite 3.x
--
-- Estado final de todas las tablas, columnas, CHECK y índices.
-- Historial de cambios previos al primer lanzamiento (V1..V15):
--   - Docentes.Telefono, Operadores.DNI
--   - Asistencia.HoraEntrada/HoraSalida, PeriodoAusencia
--   - Operadores PRE en tipos 3,4,5
--   - Estado en Pacientes y Tratamiento_PRED
--   - Multi-clínica (Clinica + ClinicaID en catálogos y transacciones)
--   - Pagos (Pago) y avances como sesión (Tratamiento_Avance,
--     Materiales_List_Avance)
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
    DNI         TEXT,
    ClinicaID   INTEGER NOT NULL DEFAULT 1,
    CHECK ((Grado = 'PRE' AND Tipo IN ('3','4','5'))
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
    Estado      INTEGER NOT NULL DEFAULT 1,
    Telefono    TEXT,
    ClinicaID   INTEGER NOT NULL DEFAULT 1
);

-- Pacientes
CREATE TABLE Pacientes (
    PacienteID  INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombres     TEXT    NOT NULL,
    Apellidos   TEXT    NOT NULL,
    Estado      INTEGER NOT NULL DEFAULT 1,
    ClinicaID   INTEGER NOT NULL DEFAULT 1
);

-- Unidad de tratamiento / módulo
CREATE TABLE Unidad (
    UnidadID    INTEGER PRIMARY KEY AUTOINCREMENT,
    UnidadNro   INTEGER NOT NULL,
    ClinicaID   INTEGER NOT NULL DEFAULT 1
);

-- Plantilla de tratamiento
CREATE TABLE Tratamiento_PRED (
    TratPredID        INTEGER PRIMARY KEY AUTOINCREMENT,
    NombreTratamiento TEXT    NOT NULL UNIQUE,
    MontoSugerido     REAL,
    Estado            INTEGER NOT NULL DEFAULT 1
);

-- Materiales sugeridos por plantilla
CREATE TABLE Materiales_List_PRED (
    MaterialListPredID  INTEGER PRIMARY KEY AUTOINCREMENT,
    TratPredID          INTEGER NOT NULL REFERENCES Tratamiento_PRED (TratPredID),
    MaterialID          INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad            REAL    NOT NULL DEFAULT 1,
    UNIQUE (TratPredID, MaterialID)
);

-- Clínica
CREATE TABLE Clinica (
    ClinicaID INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombre    TEXT NOT NULL UNIQUE,
    Grupo     TEXT,
    Estado    INTEGER NOT NULL DEFAULT 1
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
    CerradoEn        TEXT,
    MontoAnterior    REAL,
    ClinicaID        INTEGER NOT NULL DEFAULT 1
);

-- Materiales consumidos en un tratamiento (base)
CREATE TABLE Materiales_List (
    MaterialesListID  INTEGER PRIMARY KEY AUTOINCREMENT,
    TratamientoID     INTEGER NOT NULL REFERENCES Tratamiento (TratamientoID),
    MaterialID        INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad          REAL    NOT NULL CHECK (Cantidad > 0)
);

-- Sesiones de tratamiento (avances)
CREATE TABLE Tratamiento_Avance (
    AvanceID      INTEGER PRIMARY KEY AUTOINCREMENT,
    TratamientoID INTEGER NOT NULL REFERENCES Tratamiento (TratamientoID),
    Fecha         TEXT    NOT NULL,
    UnidadID      INTEGER REFERENCES Unidad (UnidadID) ON DELETE SET NULL,
    Estado        TEXT    NOT NULL DEFAULT 'ACTIVO'
                  CHECK (Estado IN ('ACTIVO','ANULADO')),
    Timestamp     TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- Materiales consumidos por sesion
CREATE TABLE Materiales_List_Avance (
    MaterialesListAvanceID INTEGER PRIMARY KEY AUTOINCREMENT,
    AvanceID               INTEGER NOT NULL REFERENCES Tratamiento_Avance (AvanceID) ON DELETE CASCADE,
    MaterialID             INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad               REAL    NOT NULL CHECK (Cantidad > 0)
);

-- Pagos con historial (vinculables a una sesion)
CREATE TABLE Pago (
    PagoID        INTEGER PRIMARY KEY AUTOINCREMENT,
    TratamientoID INTEGER NOT NULL REFERENCES Tratamiento (TratamientoID),
    Fecha         TEXT    NOT NULL,
    Monto         REAL    NOT NULL CHECK (Monto > 0),
    Timestamp     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    AvanceID      INTEGER REFERENCES Tratamiento_Avance (AvanceID)
);

-- Asistencia diaria de docente
CREATE TABLE Asistencia (
    AsistenciaID  INTEGER PRIMARY KEY AUTOINCREMENT,
    DocenteID     INTEGER NOT NULL REFERENCES Docentes (DocenteID),
    Fecha         TEXT    NOT NULL,
    Estado        TEXT    NOT NULL DEFAULT 'ACTIVO'
                  CHECK (Estado IN ('ACTIVO','ANULADO')),
    HoraEntrada   TEXT,
    HoraSalida    TEXT,
    ClinicaID     INTEGER NOT NULL DEFAULT 1,
    UNIQUE (DocenteID, Fecha, Estado)
);

-- Materiales recibidos por un docente en un día
CREATE TABLE Materiales_Asistencia (
    MatAsistenciaID  INTEGER PRIMARY KEY AUTOINCREMENT,
    AsistenciaID     INTEGER NOT NULL REFERENCES Asistencia (AsistenciaID),
    MaterialesID     INTEGER NOT NULL REFERENCES Materiales (MaterialID),
    Cantidad         REAL    NOT NULL
);

-- Periodos de ausencia del docente
CREATE TABLE PeriodoAusencia (
    AusenciaID   INTEGER PRIMARY KEY AUTOINCREMENT,
    AsistenciaID INTEGER NOT NULL REFERENCES Asistencia (AsistenciaID),
    HoraInicio   TEXT    NOT NULL,
    HoraFin      TEXT,
    Motivo       TEXT
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
    Timestamp         TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    ClinicaID         INTEGER NOT NULL DEFAULT 1
);

-- ------------------------------------------------------------
-- Índices para consultas frecuentes por mes/reporte y por clínica
-- ------------------------------------------------------------
CREATE INDEX idx_tratamiento_fecha         ON Tratamiento (Fecha);
CREATE INDEX idx_tratamiento_operador      ON Tratamiento (OperadorID);
CREATE INDEX idx_tratamiento_clinica       ON Tratamiento (ClinicaID, Estado);
CREATE INDEX idx_tratamiento_estado        ON Tratamiento (Estado);
CREATE INDEX idx_tratamiento_unidad_estado ON Tratamiento (UnidadID, Estado);

CREATE INDEX idx_asistencia_fecha          ON Asistencia (Fecha);
CREATE INDEX idx_asistencia_clinica        ON Asistencia (ClinicaID);

CREATE INDEX idx_materiales_list_trat      ON Materiales_List (TratamientoID);
CREATE INDEX idx_materiales_list_lookup    ON Materiales_List (MaterialID, TratamientoID);

CREATE INDEX idx_avance_tratamiento        ON Tratamiento_Avance (TratamientoID);
CREATE INDEX idx_mat_avance_avance         ON Materiales_List_Avance (AvanceID);

CREATE INDEX idx_pago_tratamiento          ON Pago (TratamientoID);
CREATE INDEX idx_pago_avance               ON Pago (AvanceID);

CREATE INDEX idx_mat_asistencia_asis       ON Materiales_Asistencia (AsistenciaID);
CREATE INDEX idx_materiales_asistencia_lookup ON Materiales_Asistencia (MaterialesID, AsistenciaID);

CREATE INDEX idx_ausencia_asistencia       ON PeriodoAusencia (AsistenciaID);
CREATE INDEX idx_periodoausencia_asistencia_horafin ON PeriodoAusencia (AsistenciaID, HoraFin);

CREATE INDEX idx_operadores_clinica        ON Operadores (ClinicaID);
CREATE INDEX idx_docentes_clinica          ON Docentes (ClinicaID);
CREATE INDEX idx_unidad_clinica            ON Unidad (ClinicaID);
CREATE INDEX idx_pacientes_clinica         ON Pacientes (ClinicaID);
