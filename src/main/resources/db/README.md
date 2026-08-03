Definicion de la base de datos local.

Migraciones Flyway (classpath:db/migration):

- `V1__schema_inicial.sql`: crea las 13 tablas (Operadores, Materiales, Docentes,
  Pacientes, Unidad, Tratamiento_PRED, Materiales_List_PRED, Tratamiento,
  Materiales_List, Asistencia, Materiales_Asistencia, Unidad_Conversion,
  RegistroAnulacion) con FKs, CHECK y UNIQUE.

Diagrama ER: Docs/FormatosDB.mmd

La migracion se aplica automaticamente al arrancar (ConnectionManager).
