Definicion de la base de datos local.

Migraciones Flyway (classpath:db/migration):

- `V1__schema_inicial.sql`: esquema consolidado del primer lanzamiento.
  Crea las 18 tablas (Operadores, Materiales, Docentes, Pacientes, Unidad,
  Tratamiento_PRED, Materiales_List_PRED, Clinica, Tratamiento,
  Materiales_List, Tratamiento_Avance, Materiales_List_Avance, Pago,
  Asistencia, Materiales_Asistencia, PeriodoAusencia, Unidad_Conversion,
  RegistroAnulacion) con FKs, CHECK y UNIQUE, mas sus indices.
- `V2__seed.sql`: carga inicial (281 materiales, 94 tratamientos predefinidos
  con sus materiales sugeridos y la unidad de tratamiento 1). No siembra
  clinica: la primera creada por el usuario ocupa ClinicaID = 1.

Diagrama ER: Docs/FormatosDB.mmd

La migracion se aplica automaticamente al arrancar (ConnectionManager).
