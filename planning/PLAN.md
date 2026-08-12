# PLAN — Formatos-Odontologia

Plan maestro para terminar el proyecto. Se actualiza al cierre de cada hito
(no está versionado: es documento de trabajo local, fuera de git).

Convención: `[x]` = hecho, `[ ]` = pendiente, `[~]` = en curso.

---

## Fase 0 — Proyecto Base (completada)

- [x] 0.1 Gradle wrapper + build.gradle (Gradle 8.14.2, Java 21, JavaFX 21.0.12)
- [x] 0.2 MainApp + config (classpath tradicional, sin module-info)
- [x] 0.3 SQLite + Flyway V1 (13 tablas)
- [x] 0.4 ConnectionManager singleton
- [ ] 0.5 Ventana principal con menú/Dashboard (→ Fase 6 UI)

## Fase 1 — Catálogos (backend completado, UI pendiente)

- [x] 1.1 Material (modelo + repo + servicio)
- [x] 1.2 Docente (modelo + repo + servicio)
- [x] 1.3 Operador (modelo + repo + servicio)
- [x] 1.4 TratamientoPredefinido (modelo + repo + servicio)
- [x] 1.5 TratamientoPredefinidoMaterial (modelo + repo)
- [ ] 1.6 UI: vista de catálogo con pestañas y tabla
- [ ] 1.7 UI: componente de búsqueda predictiva (RNF-2.1.2)
- [ ] 1.8 UI: modal de creación on-the-fly (RF-1.1.2)
- [x] 1.9 Validación de unicidad (RF-1.1.3)
- [ ] 1.10 Validación de grado/tipo en Operador (frontend, RD-3.1.1)
- [x] 1.11 Unidad (modelo + repo + servicio)

## Fase 0.5 — Infraestructura restante

- [x] 0.5.1 planning/PLAN.md + .gitignore (carpeta de planificación)
- [x] 0.5.2 Seed inicial V2__seed.sql (materiales + unidad base, checklist 2.14)
- [x] 0.5.3 Traducción de errores SQL (8.2) — util/SqlErrorUtil
- [x] 0.5.4 Errores de exportación (8.3) — util/ExportErrorUtil
- [x] 0.5.5 Logging JUL a archivo (8.4) — util/LogConfig + MainApp
- [x] 0.5.6 Actualizar checklist docs (7.7, 8.2, 8.3, 8.4, 11.3, 11.4)
- [x] 0.5.7 Tests + build + commit (22/22 OK)

## Fase 2 — Asistencia Docente Diaria

- [x] 2.1 Asistencia (modelo + repo)
- [x] 2.2 AsistenciaMaterial (modelo + repo)
- [x] 2.3 AsistenciaService: creación + acumulación diaria (RF-1.2.1/1.2.2, RD-3.1.4)
- [ ] 2.4 UI: selección de docente + materiales + cantidades (→ Fase 6)
- [ ] 2.5 UI: flujo rápido (< 3 clics, RNF-2.1.1) (→ Fase 6)
- [x] Tests de acumulación, unicidad y anulación (15/15 OK)

## Fase 3 — Flujo de Tratamientos

- [x] 3.1 Paciente (modelo + repo + servicio) — hecho backend 1.x; validado
- [x] 3.2 Tratamiento (modelo + repo)
- [x] 3.3 TratamientoMaterial (modelo + repo)
- [x] 3.4 TratamientoService: creación
- [x] 3.5 Carga automática de materiales (RF-1.4.2, RNF-2.2.1)
- [x] 3.6 Consumo dinámico (RF-1.4.4)
- [x] 3.7 Cierre + consolidación (RF-1.4.5)
- [x] 3.8 Reapertura (RF-1.4.6)
- [ ] 3.9 UI: formulario completo de tratamiento (→ Fase 6)
- [ ] 3.10 UI: resumen pre-cierre (→ Fase 6)
- [x] Tests: carga plantilla, acumulación, cierre, CONTINUO, pagos (23/23 OK)

## Fase 4 — Reportes (backend completado, UI pendiente)

- [x] 4.1 ExcelExporter con Apache POI
- [ ] 4.2 UI: selector Mes/Año (RNF-2.1.3) (→ Fase 6)
- [x] 4.3 Reporte Materiales Generales
- [x] 4.4 Reporte Ingresos
- [x] 4.5 Reporte Docente (consolidado + detalle diario)
- [x] 4.6 Reporte Especialista
- [x] 4.7 Reporte Anual (4 hojas)
- [x] 4.8 Nomenclatura dinámica de archivos (RNF-2.3.2)
- [ ] 4.9 Spinner/indicador de progreso en Anual (RNF-2.2.2) (→ Fase 6)
- [x] 4.10 UnidadConversion (modelo + repo + servicio, suma a unidad base)
- [x] 4.11 **Rediseño 4.5** — Reportes unificados con tablas por persona (pivot docente, secciones operador)
  - [x] ExcelExporter: helpers visuales (título, sección, total, estilos)
  - [x] ReporteGeneradorBase: overload generar(anio, mesInicio, mesFin) + helpers rango
  - [x] ReporteNomenclatura: TIPO_MATERIALES/TIPO_ECONOMICO con soporte mes/rango/año
  - [x] ReporteRepository: conversión unidad base en especialista/docente + ingresosPorTratamiento + ingresosPorOperador
  - [x] ReporteService: delegar ingresosPorTratamiento e ingresosPorOperador
  - [x] ReporteMaterialesGenerator: 3 hojas (General, Detalle Docente pivot, Por Operador con tablas individuales)
  - [x] ReporteEconomicoGenerator: 2 hojas (General, Por Operador con tablas individuales)
  - [x] Eliminados: ReporteAnualGenerator, ReporteEspecialistaGenerator, ReporteDocenteGenerator, ReporteIngresosGenerator
  - [x] Exclusión de tratamientos CONTINUO en reportes económicos
  - [x] Tests actualizados: conversión especialista (1000g), docente (500g), estructura de secciones
  - [x] Suite completa: 83/83 tests pasando

## Fase 5 — Edición, Auditoría y Anulación

- [x] 5.1 RegistroAnulacionRepository (insert con soporte transaccional)
- [x] 5.2 Anular tratamiento (RF-1.6.3) — TratamientoService.anular()
- [x] 5.3 Anular asistencia docente (RF-1.6.3) — AsistenciaService.anular()
- [ ] 5.4 UI: diálogo de confirmación + motivo (→ Fase 6)
- [x] 5.5 Exclusión de anulados en reportes (RD-3.1.6) — implementado en ReporteRepository
- [x] 5.6 Edición retroactiva (RF-1.6.1) — TratamientoService.editarRetroactivo(Dto)
- [x] 5.7 Recálculo automático (RF-1.6.2) — cambios persisten en BD, reportes reflejan datos actualizados
- [ ] 5.8 Vista de auditoría (RF-1.6.4) (→ Fase 6)
- [ ] 5.9 Indicador de reporte obsoleto (RNF-2.4.2) (→ Fase 6)
- [x] Tests: anulaciones, reopen con unidad, edición retroactiva (99 tests)

## Fase 6 — UI completa (Electron + React + Vite)

> **Migración completada** (ver `migration-plan.md`). La UI pasó de JavaFX a Electron+React+Javalin.

- [x] 6.0 Componentes: SearchableCombo, MonthYearPicker, MaterialTable, useToast, ConfirmDialog, ErrorBoundary, MonthYearRangePicker
- [x] 6.0 Sistema visual: CSS Instrumental clinica, IBM Plex Sans/Mono, tokens mockup
- [x] 6.1 Layout + Sidebar (marca, secciones Atencion/Gestion, footer, aria)
- [x] 6.2 Dashboard: KPIs (Recharts), chart grid, accesos rapidos navegables, alert banner
- [x] 6.3 Vista Catalogos (5 tabs: Materiales, Docentes, Especialistas, Trat. Predef., Trat. Realizados — filtros, badges, edicion)
- [x] 6.4 Vista Unidades: estados LED, bloqueo visual de ocupadas, modal creacion, loading state
- [x] 6.5 Vista Asistencia Docente: horarios entrada/salida, ausencias, timeline, materiales, guardar/anular
- [x] 6.6 Vista Tratamientos: grid de estaciones (StationCard), detalle overlay, creacion on-the-fly
- [x] 6.7 Vista Reportes: 4 tarjetas + consolidado por plazos + recientes + abrir archivo (showItemInFolder)
- [x] 6.8 Ventana principal Electron + spawn JAR backend
- [ ] TestFX → No aplica (reemplazado por vitest + Playwright, parcial)

## Fase 7 — Cierre

- [ ] 7.1 Empaquetado electron-builder (.exe para Windows) + build.gradle de distribución
- [ ] 7.2 Prueba E2E final (Playwright)
- [ ] 7.3 Actualizar README + checklist final

---

## Alcance decidido

- Backend completo primero (Fases 2-5), luego UI (Fase 6 incl. Dashboard).
- Préstamo de equipos (Fase 6 del roadmap, HU-14/15): EXCLUIDO de esta entrega.
- Trazabilidad fuera de git: `planning/` en `.gitignore`.

## Estado general

| Fase | Estado |
|---|--|
| Fase 0 | ✅ (falta 0.5 → Fase 6) |
| Fase 0.5 | ✅ Completada |
| Fase 1 | ✅ Backend + UI (Catálogos con tabs) |
| Fase 2 | ✅ Backend + UI (Asistencia con horarios y ausencias) |
| Fase 3 | ✅ Backend + UI (Tratamientos con grid de estaciones) |
| Fase 4 | ✅ Rediseño 4.5 + reporte consolidado (UI tarjetas) |
| Fase 5 | ✅ Backend + UI (anulación, edición retroactiva, ConfirmDialog) |
| Fase 6 | ✅ UI Electron+React completada |
| **Correcciones/Auditoría** | ✅ 3 fases completadas (issues usuario + seguridad + rendimiento) |
| **Asistencia Horarios** | ✅ Fases A-E completadas |
| Fase 7 | Pendiente (empaquetado + E2E + docs) |

> **Última revisión:** 2026-08-11 — verificado contra código real (67 archivos backend, ~50 archivos frontend, 9 migraciones Flyway, 119 tests).
