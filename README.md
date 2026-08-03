# Formatos-Odontologia

Estado actual: Fase 0 y Fase 1 (catálogos) implementadas. Compila y tests pasan.

Aplicación de escritorio en Java (JavaFX) con base de datos local SQLite y exportación a Excel.

## Estado de implementación

- **Fase 0 (completada)**: build Gradle 8.14.2 (wrapper), Java 21, JavaFX 21, migraciones Flyway, `ConnectionManager`, configuración por `application.properties`.
- **Modelos (completados)**: 13 clases POJO alineadas con el esquema V1.
- **Fase 1 (completada)**: repositorios y servicios de catálogos (Materiales, Docentes, Operadores, Pacientes, Unidad, Tratamiento_PRED + Materiales_List_PRED) con validaciones (unicidad RF-1.1.3, grado/tipo RD-3.1.1, numeración secuencial de unidades, bloqueo por ocupación).
- **Tests**: JUnit 5 contra SQLite en archivo temporal (mismo motor que producción). 15 tests pasan.
- **Pendiente**: UI, Asistencia Docente, Tratamientos, Reportes.

## Comandos

```text
.\gradlew.bat compileJava   # compilar
.\gradlew.bat test          # ejecutar tests
.\gradlew.bat run           # lanzar la app
```

## Referencia funcional actual

- `REGISTRO_DE_CONSUMOS_VBA.xlsm`: ejemplo del Excel que sirve como referencia para el formato final de salida.

## Estructura propuesta

```text
src/
  main/
    java/
      com/odontologia/formatos/
        config/
        db/
        model/
        repository/
        service/
        export/
        ui/
          controller/
    resources/
      db/
      templates/
      ui/
      styles/
  test/
    java/
      com/odontologia/formatos/
```

## Nota

Por ahora solo se dejaron carpetas y archivos `README.md` para documentar que debe ir en cada lugar. La implementacion se agregara despues, por modulos.
