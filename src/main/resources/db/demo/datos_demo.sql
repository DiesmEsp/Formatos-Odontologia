-- ============================================================
-- datos_demo.sql
-- Datos de demostracion cargados al iniciar la aplicacion.
-- No es migracion Flyway — solo se ejecuta si el sistema esta vacio.
-- ============================================================

INSERT OR IGNORE INTO Unidad (UnidadID, UnidadNro) VALUES (2, 2);
INSERT OR IGNORE INTO Unidad (UnidadID, UnidadNro) VALUES (3, 3);
INSERT OR IGNORE INTO Unidad (UnidadID, UnidadNro) VALUES (4, 4);
INSERT OR IGNORE INTO Unidad (UnidadID, UnidadNro) VALUES (5, 5);
INSERT OR IGNORE INTO Unidad (UnidadID, UnidadNro) VALUES (6, 6);

INSERT OR IGNORE INTO Docentes (DocenteID, Nombres, Apellidos, Telefono, Estado) VALUES
    (1,'Maria','Gonzalez Rivas','999111222',1),
    (2,'Carlos','Mendoza Paredes','999333444',1),
    (3,'Ana','Huaman Torres',NULL,1),
    (4,'Luis','Quispe Condori','999555666',1),
    (5,'Patricia','Vargas Leon','999777888',1),
    (6,'Roberto','Castro Diaz',NULL,1);

INSERT OR IGNORE INTO Operadores (OperadorID, Nombres, Apellidos, DNI, Grado, Tipo, Periodo, Estado) VALUES
    (1,'Pedro','Ramirez Lopez','72456123','PRE','4',2026,1),
    (2,'Sofia','Lopez Garcia','73542189','POS','R1',2026,1),
    (3,'Diego','Marquez Rojas','70123567','PRE','5',2025,1),
    (4,'Lucia','Fernandez Paz','74523190','POS','R3',2026,1),
    (5,'Jorge','Alvarez Mori','75412098','PRE','5',2026,1),
    (6,'Carmen','Torres Silva','76843901','POS','R2',2025,1),
    (7,'Miguel','Rios Ortega','71234987','PRE','4',2025,1),
    (8,'Valeria','Paredes Cueva','78901234','POS','R1',2026,1);

INSERT OR IGNORE INTO Pacientes (PacienteID, Nombres, Apellidos) VALUES
    (1,'Juan','Perez Mendez'),
    (2,'Ana','Torres Galvez'),
    (3,'Luis','Garcia Ramos'),
    (4,'Carla','Ruiz Castillo'),
    (5,'Maria','Lopez Quiroz'),
    (6,'Jose','Sanchez Davila'),
    (7,'Rosa','Flores Medina'),
    (8,'Manuel','Vargas Nunez'),
    (9,'Elena','Campos Rivas'),
    (10,'Alberto','Morales Ponce');

INSERT OR IGNORE INTO Tratamiento_PRED (TratPredID, NombreTratamiento, MontoSugerido) VALUES
    (1,'Exodoncia simple',30.00),
    (2,'Obturacion amalgama',25.00),
    (3,'Profilaxis dental',15.00),
    (4,'Endodoncia unirradicular',80.00),
    (5,'Corona metalica',120.00),
    (6,'Limpieza y revision',0.00);

INSERT OR IGNORE INTO Materiales_List_PRED (MaterialListPredID, TratPredID, MaterialID, Cantidad) VALUES
    (1,1,5,1),(2,1,4,2),(3,1,3,1),
    (4,2,5,1),(5,2,4,2),(6,2,1,1),(7,2,29,1),
    (8,3,5,1),(9,3,1,1),(10,3,8,1),
    (11,4,5,1),(12,4,4,3),(13,4,3,2),(14,4,23,2),(15,4,45,1),
    (16,5,5,1),(17,5,4,2),(18,5,3,1),(19,5,25,1),(20,5,27,1);

INSERT OR IGNORE INTO Tratamiento (TratamientoID, OperadorID, PacienteID, UnidadID, Fecha, NombreTratamiento, Monto, Tipo, EstadoPago, MontoPagado, Estado, CerradoEn) VALUES
    (1,1,1,2,'2026-07-30','Exodoncia simple',30.00,'NORMAL','PAGADO',30.00,'CERRADO','2026-07-30T10:00:00'),
    (2,2,2,1,'2026-07-30','Obturacion amalgama',25.00,'NORMAL','PENDIENTE',0,'ABIERTO',NULL),
    (3,1,3,5,'2026-07-29','Profilaxis dental',15.00,'NORMAL','PAGADO',15.00,'CERRADO','2026-07-29T14:30:00'),
    (4,4,4,4,'2026-07-28','Endodoncia unirradicular',80.00,'NORMAL','PENDIENTE',0,'ANULADO',NULL),
    (5,3,5,3,'2026-07-28','Corona metalica',120.00,'NORMAL','PARCIAL',80.00,'CERRADO','2026-07-28T12:00:00'),
    (6,2,1,6,'2026-07-27','Limpieza y revision',0,'CONTINUO','PAGADO',0,'ANULADO',NULL),
    (7,5,6,2,'2026-07-31','Exodoncia simple',30.00,'NORMAL','PENDIENTE',0,'ABIERTO',NULL),
    (8,8,5,4,'2026-07-31','Endodoncia unirradicular',80.00,'NORMAL','PARCIAL',40.00,'ABIERTO',NULL),
    (9,6,7,2,'2026-06-15','Obturacion amalgama',25.00,'NORMAL','PAGADO',25.00,'CERRADO','2026-06-15T11:00:00'),
    (10,7,8,1,'2026-06-20','Profilaxis dental',15.00,'NORMAL','PAGADO',15.00,'CERRADO','2026-06-20T09:30:00'),
    (11,3,9,3,'2026-06-25','Corona metalica',120.00,'NORMAL','PARCIAL',50.00,'CERRADO','2026-06-25T16:00:00'),
    (12,4,10,5,'2026-06-10','Endodoncia unirradicular',80.00,'NORMAL','PAGADO',80.00,'CERRADO','2026-06-10T13:00:00');

INSERT OR IGNORE INTO Materiales_List (MaterialesListID, TratamientoID, MaterialID, Cantidad) VALUES
    (1,1,5,1),(2,1,4,2),(3,1,3,1),
    (4,2,5,1),(5,2,4,1),(6,2,1,1),(7,2,29,1),
    (8,3,5,1),(9,3,1,1),(10,3,8,1),
    (11,4,5,1),(12,4,4,2),(13,4,3,2),
    (14,5,5,1),(15,5,4,2),(16,5,3,1),(17,5,25,1),(18,5,27,1),
    (19,7,5,1),(20,7,4,2),
    (21,8,5,1),(22,8,4,3),(23,8,3,1),
    (24,9,5,1),(25,9,4,2),(26,9,1,1),
    (27,10,5,1),(28,10,1,1),
    (29,11,5,1),(30,11,3,1),
    (31,12,5,1),(32,12,4,2),(33,12,3,2),(34,12,23,1);

INSERT OR IGNORE INTO Asistencia (AsistenciaID, DocenteID, Fecha, Estado) VALUES
    (1,1,'2026-07-31','ACTIVO'),
    (2,2,'2026-07-31','ACTIVO'),
    (3,3,'2026-07-31','ACTIVO'),
    (4,4,'2026-07-31','ACTIVO'),
    (5,5,'2026-07-31','ACTIVO');

INSERT OR IGNORE INTO Materiales_Asistencia (MatAsistenciaID, AsistenciaID, MaterialesID, Cantidad) VALUES
    (1,1,5,2),(2,1,4,3),(3,1,8,1),
    (4,2,5,3),(5,2,4,2),
    (6,3,5,2),(7,3,1,1),
    (8,4,5,1),(9,4,4,4),(10,4,3,2),
    (11,5,5,2),(12,5,8,2);
