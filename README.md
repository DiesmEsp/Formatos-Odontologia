# Formatos-Odontologia

Estado actual: Fases 0 a 4 (backend) implementadas. Compila y tests pasan.

Aplicación de escritorio en Java (JavaFX) con base de datos local SQLite y exportación a Excel.

## Estado de implementación

- **Fase 0 (completada)**: build Gradle 8.14.2 (wrapper), Java 21, JavaFX 21, migraciones Flyway, `ConnectionManager`, configuración por `application.properties`, seed de materiales (V2) y utilidades de logging/errores.
- **Modelos (completados)**: 13 clases POJO alineadas con el esquema V1.
- **Fase 1 (completada)**: repositorios y servicios de catálogos (Materiales, Docentes, Operadores, Pacientes, Unidad, Tratamiento_PRED + Materiales_List_PRED) con validaciones (unicidad RF-1.1.3, grado/tipo RD-3.1.1, numeración secuencial de unidades, bloqueo por ocupación).
- **Fase 2 (completada)**: asistencia docente diaria (Asistencia + Materiales_Asistencia) con acumulación automática y unicidad por docente/fecha (RD-3.1.4).
- **Fase 3 (completada)**: flujo de tratamientos (Paciente, Tratamiento, Materiales_List) con carga desde plantilla, consumo dinámico acumulativo, cierre/reapertura, tipo CONTINUO y pagos parciales (RF-1.4.x, RD-3.1.12/13).
- **Fase 4 (completada, backend)**: reportes a Excel con Apache POI — Materiales, Ingresos, Docente (consolidado + detalle diario), Especialista y Anual (12 meses) con nomenclatura `{Tipo}_{Mes}_{Año}.xlsx` (RNF-2.3.2). Conversión a unidad base vía `Unidad_Conversion` (factor 1 si no hay). Incluye `UnidadConversion` (repositorio + servicio).
- **Tests**: JUnit 5 contra SQLite en archivo temporal (mismo motor que producción).
- **Pendiente**: UI de JavaFX (catálogos, asistencia, tratamientos, selector de reportes, spinner de Anual), Fase 5 (anulación/auditoría) y Fase 6 (préstamo de equipos).

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
