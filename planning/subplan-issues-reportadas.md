# Subplan — Corrección de Issues Reportados por el Usuario

**Fecha:** 11 de agosto de 2026
**Relacionado con:** Auditoría integral `Docs/6. Informe de auditoría.md`

---

## Issues del Usuario — TODOS COMPLETADOS ✅

| # | Issue | Estado |
|---|---|---|
| 1 | Filtrado SearchableCombo | ✅ |
| 2 | Guardado materiales tratamiento | ✅ |
| 3 | Scroll lista docentes | ✅ |
| 4 | Revertir salida | ✅ |
| 5 | Guardado materiales asistencia | ✅ |
| 6 | Cambios de fecha asistencia | ✅ |
| 7 | Error estado material | ✅ (resuelto con #8) |
| 8 | Datos residuales catálogos | ✅ |
| 9 | Tildes en UI | ✅ |
| 10 | Tratamiento predefinido | ✅ (resuelto con #1) |
| 11 | Botón misterioso reportes | ✅ |
| 12 | Eliminar semilla | ✅ |
| 13 | Reportes consolidados | ✅ |

---

## Auditoría — Progreso por Fase

### 🔴 Fase 1 — Críticos: 10/10 ✅

| # | Tarea | Commit |
|---|---|---|
| 1.1 | TransaccionBD rollback RuntimeException | `36057fa` |
| 1.2 | 4 fugas JDBC en AsistenciaRepository | `36057fa` |
| 1.3 | ErrorBoundary en frontend | `36057fa` |
| 1.4 | /shutdown solo en dev | `36057fa` |
| 1.5 | CORS reflectClientOrigin | `36057fa` |
| 1.6 | Electron webSecurity:true + sandbox:true | `36057fa` |
| 1.7 | preload.ts reducido a 8 líneas | `36057fa` |
| 1.8 | useApi fetcher vía useRef | `36057fa` |
| 1.9 | Docs/4 stack actualizado | `f0de8d9` |
| 1.10 | Tipos PRE unificados 3,4,5 | `f0de8d9` |

### 🟠 Fase 2 — Backend: 8/11 ✅

| # | Tarea | Commit |
|---|---|---|
| 2.1 | findById() dentro de transacciones | `30bb97d` |
| 2.2 | Race conditions (reabrir, unidad) | `30bb97d` |
| 2.3 | Validación inputs (ControllerUtil) | `5c82f14` |
| 2.4 | Bug estado=0 en PUT catálogos | `30bb97d` |
| 2.5 | V9 índices críticos | `30bb97d` |
| 2.9 | DashboardController N+1 fix | `5c82f14` |
| 2.10 | DemoDataLoader split SQL | `30bb97d` |
| 2.11 | M4/M10/M14 varios | `30bb97d`, `c0588d8` |

**Pendientes Fase 2 (diferido — baja prioridad, app single-user):**
- 2.6: Jakarta Bean Validation
- 2.7: Pool de conexiones HikariCP
- 2.8: Rate limiting en Javalin

### 🟡 Fase 3 — Frontend: 5/8 ✅

| # | Tarea | Commit |
|---|---|---|
| 3.4 | Loading Unidades + errores Dashboard | `cd3e6ad` |
| 3.7 | API client timeout 15s + AbortController | `cd3e6ad` |
| 3.8 | useToast timeouts, vite tipos, build script, Chart CSS vars, Sidebar aria | `5c0a79c` |

**Pendientes Fase 3 (refactor — mejora de calidad, no bugs):**
- 3.1: Refactorizar Tratamientos.tsx en archivos separados (~568 líneas)
- 3.2: Eliminar código duplicado Tratamientos/Catálogos
- 3.5: Migrar estilos inline a clases CSS

### 🟢 Fase 4 — Testing: 0/3

**Pendientes:**
- 4.1: Tests de integración para 6 páginas
- 4.2: Tests E2E para flujos críticos
- 4.3: JaCoCo para cobertura backend

### 🔵 Fase 5 — CI/CD + Docs: 0/4

**Pendientes:**
- 5.1: CI: build frontend + empaquetado Electron
- 5.2: CI: lint real (backend + frontend)
- 5.3: Documentar API REST (~45 endpoints)
- 5.4: Actualizar docs restantes (Checklist, FormatosDB, README, HU)

---

## Issue Nuevo — Tratamientos manuales sin unidad

**Descubierto en verificación:** El backend soporta crear tratamientos con `unidadID = null` (campo nullable), pero el frontend solo permite crear tratamientos vinculados a una unidad física (StationCard).

**Cambios necesarios:**
1. Botón "+ Nuevo tratamiento (manual)" en `Tratamientos.tsx` header
2. `CrearTratamientoModal` acepte `unidad` como opcional
3. Campo "Unidad" como combo desplegable en vez de readonly
4. Permitir seleccionar cualquier fecha (ya lo soporta)

---

## 🧾 Lista Consolidada de Pendientes

### Backlog Técnico (prioridad alta)
| # | Tarea | Fase | Esfuerzo |
|---|---|---|---|
| P1 | Tratamientos manuales sin unidad (nuevo botón + modal opcional) | Feature | 1h |
| P2 | Refactorizar Tratamientos.tsx (568 líneas → 4 archivos) | 3.1 | 2h |
| P3 | Eliminar código duplicado Tratamientos/Catálogos | 3.2 | 1.5h |

### Backlog Técnico (prioridad media)
| # | Tarea | Fase | Esfuerzo |
|---|---|---|---|
| P4 | Jakarta Bean Validation en 14 modelos | 2.6 | 2h |
| P5 | Pool de conexiones HikariCP | 2.7 | 1.5h |
| P6 | Rate limiting en Javalin para reportes | 2.8 | 30m |
| P7 | Migrar ~200 estilos inline a clases CSS | 3.5 | ✅ |
| P8 | Accesibilidad: roles ARIA en StationCard, ConfirmDialog, CatalogoModal, SearchableCombo, KpiCard | 3.6 | 2h |

### Backlog de Testing
| # | Tarea | Fase | Esfuerzo |
|---|---|---|---|
| P9 | Tests de integración para 6 páginas (Dashboard, Tratamientos, Asistencia, Catálogos, Reportes, Unidades) | 4.1 | ✅ 3/6 |
| P10 | Tests E2E para 4 flujos críticos (tratamiento, asistencia, reportes, catálogos) | 4.2 | 3h |
| P11 | JaCoCo coverage report backend | 4.3 | ✅ |

### Backlog de CI/CD
| # | Tarea | Fase | Esfuerzo |
|---|---|---|---|
| P12 | CI: job de build frontend + empaquetado Electron | 5.1 | ✅ |
| P13 | CI: lint real backend (Checkstyle) + frontend sin `\|\| true` | 5.2 | ✅ |

### Backlog de Documentación
| # | Tarea | Fase | Esfuerzo |
|---|---|---|---|
| P14 | Documentar API REST (~45 endpoints en 7 controladores) | 5.3 | ✅ |
| P15 | Actualizar `Docs/5. Checklist.md` (stack real, items completados) | 5.4 | — |
| P16 | Actualizar `Docs/FormatosDB.mmd` (PeriodoAusencia, DNI, teléfono, relación) | 5.4 | ✅ |
| P17 | README.md: sección troubleshooting + requisitos sistema + badge CI | 5.4 | ✅ |
| P18 | Actualizar `Docs/2. HU.md` (marcar decisiones como resueltas) | 5.4 | ✅ |
| P19 | Actualizar `Docs/3. Especificación.md` (marcar RFs implementados) | 5.4 | — |
| P20 | `planning/PLAN-CORRECCIONES.md` — marcar fases completadas | 5.4 | ✅ |

---

## Resumen de Commits de la Sesión

| Commit | Descripción |
|---|---|
| `b280ac1` | feat: filtrado client-side en SearchableCombo |
| `f7839d3` | fix: guardado materiales tratamiento (local + botón + autosave) |
| `f515e0c` | fix: scroll con max-height tabla docentes |
| `9d37dc4` | feat: issues 4-12 — revertir salida, por-fecha, guardado asistencia, catálogos, tildes, reportes |
| `543989b` | feat: issue 13 — reportes consolidados por plazos |
| `36057fa` | fix: fase 1 auditoría — TransaccionBD, fugas JDBC, ErrorBoundary, seguridad |
| `f0de8d9` | docs: fase 1 — Docs/4 stack, tipos PRE |
| `30bb97d` | fix: fase 2 — TOCTOU, race conditions, bug estado=0, V9 índices, M10/M14 |
| `c0588d8` | fix: fase 2 — TratamientoPredefinido transacción, sobrecargas Connection |
| `5c82f14` | fix: fase 2 — ControllerUtil validación, DashboardController N+1 |
| `cd3e6ad` | fix: fase 3 — API timeout, loading Unidades, errores Dashboard |
| `5c0a79c` | fix: fase 3 — useToast, vite.config, Chart vars CSS, Sidebar aria |

### Commits Sesión 2026-08-11 (noche)

| Commit | Descripción |
|---|---|
| — | feat(fase1): catch vacíos Asistencia + migrar estilos inline a clases CSS (-74%) |
| — | fix(fase2): LOG.warning Main, validación duplicados update(), extraer validarAsistenciaActiva |
| — | feat(fase3): JaCoCo backend + tests páginas Dashboard/Catalogos/Unidades (10 tests) |
| — | feat(fase4+5): CI pipeline, Checkstyle, API docs, FormatosDB, HU nuevas, README troubleshooting |

---

## Verificación de Planes — 2026-08-11

**Resultado:** Todos los archivos de planificación revisados contra código real.
- `PLAN-CORRECCIONES.md`: ✅ Fases 1-6 completadas y marcadas.
- `PLAN.md`: ✅ Fases 0-6 completas (Fase 7 pendiente: empaquetado).
- `subplan-asistencia-horarios.md`: ✅ Fases A-E completas.
- Discrepancias encontradas: naming Material→Materiales, migraciones V7/V8 intercambiadas, tests 119 vs 99 planeados, archivos extra no documentados (detalle en verificación).
