# Plan de Correcciones - Fases

> **Estado:** ✅ COMPLETADO (commit `5c0a79c`, 2026-08-11)
> Todas las 6 fases fueron implementadas exitosamente.

---

## Fase 1: Corrección de tipos PRE (bug funcional) ✅ COMPLETADA

**Problema:** Frontend expone `['3','4','5']` pero la DB acepta `('4','5','6')`. Seleccionar '3' causa error en BD.

**Archivos a modificar:**

| Archivo | Línea | Cambio |
|---------|-------|--------|
| `frontend/src/pages/Catalogos.tsx` | 31 | `['3','4','5']` → `['4','5','6']` |
| `frontend/src/pages/Tratamientos.tsx` | 14 | `['3','4','5']` → `['4','5','6']` |
| `frontend/src/api/types.ts` | 264 | `'3' \| '4' \| '5'` → `'4' \| '5' \| '6'` |

**Riesgo:** Bajo. Cambio puntual en 3 archivos, 3 líneas cada uno.

---

## Fase 2: Estados activo/inactivo en Pacientes y Tratamientos Predefinidos ✅ COMPLETADA (`ceec7d5`, `c0588d8`)

> **Nota:** La migración real es `V8__agregar_estado.sql` (no V7). V7 es `V7__corregir_tipos_pre.sql`.

**Problema:** Ninguna de las dos entidades tiene campo `Estado`. No se pueden desactivar desde el modal de edición.

### Migración DB (V7)

| Archivo | Acción |
|---------|--------|
| `V7__agregar_estado.sql` (nuevo) | `ALTER TABLE Pacientes ADD COLUMN Estado INTEGER NOT NULL DEFAULT 1` |
| | `ALTER TABLE Tratamiento_PRED ADD COLUMN Estado INTEGER NOT NULL DEFAULT 1` |

### Backend Java

| Archivo | Cambio |
|---------|--------|
| `model/Paciente.java` | Agregar `private int estado` + getter/setter |
| `model/TratamientoPredefinido.java` | Agregar `private int estado` + getter/setter |
| `PacienteRepository.java` | Mapear `Estado` en queries (select, insert, update) |
| `TratamientoPredefinidoRepository.java` | Mapear `Estado` en queries (select, insert, update) |
| `PacienteService.java` | Permitir `estado` en `actualizar` |
| `TratamientoPredefinidoService.java` | Permitir `estado` en `actualizar` |

### Frontend

| Archivo | Cambio |
|---------|--------|
| `frontend/src/api/types.ts` | `Paciente`: agregar `estado: number` |
| | `TratamientoPredefinido`: agregar `estado: number` |
| `Catalogos.tsx` (TabPacientes) | Agregar columna Estado (Badge) y campo estado en modal edición |
| `Catalogos.tsx` (TabTratamientosPred) | Agregar columna Estado (Badge) y campo estado en modal edición |

**Riesgo:** Medio. Afecta DB, backend y frontend. Requiere migración Flyway correcta.

---

## Fase 3: Comportamiento del monto total y entradas numéricas ✅ COMPLETADA (`ceec7d5`)

**Problema:** `onBlur` fuerza `'0'` automático. Inputs numéricos permiten teclear letras momentáneamente. El "0 inborrable" molesta al usuario.

### Monto total (Tratamientos.tsx)

| Línea | Cambio |
|-------|--------|
| 127-129 | Eliminar `handleMontoBlurOrEnter` (quita la función y el `onBlur`) |
| 131-137 | `handleMontoKeyDown`: mantener solo Enter → `'0'` + blur |
| 182-186 | Quitar `onBlur={handleMontoBlurOrEnter}`, mantener `onKeyDown` |
| 119-125 | `filterNumeric`: revisar que filtre correctamente (letras, símbolos) |

### MaterialTable cantidad

| Línea | Cambio |
|-------|--------|
| 67-80 | Cambiar input `value` y `onChange` para manejar vacío sin forzar `0` |
| | `value={row.cantidad === 0 ? '' : row.cantidad}` → estado local + blur handler |
| | `onChange`: si vacío → mantener vacío (no forzar `0`), solo en blur/enter setear default |

### Comportamiento deseado:
- **Blur (perder foco):** dejar vacío si está vacío
- **Enter:** si vacío → poner 0 y salir
- **Tipeando:** sin interferencia, sin 0 a la izquierda
- **No letras ni símbolos** (solo números y un punto decimal)

**Riesgo:** Bajo. Cambios solo en frontend. Afecta UX pero no DB.

---

## Fase 4: Bug modal tratamiento predefinido + dropdowns ✅ COMPLETADA (`64ebdf0`)

**Problema:** Al editar un tratamiento predefinido, la lista de materiales se abre "por sobre la pantalla" y no deja cerrar. Los dropdowns de SearchableCombo quedan clipeados por el `overflow: auto` del modal.

### Fix modal

| Archivo | Línea | Cambio |
|---------|-------|--------|
| `CatalogoModal.tsx` | 65 | Cambiar `overflow-y: auto` por `overflow-y: visible` o manejar con contenedor interno |
| `main.css` | 526-527 | Aumentar `min-height` del `.combo-dropdown` para asegurar 3 items visibles |

**Alternativa:** Agregar prop `overflowVisible` al CatalogoModal para cuando tenga children con dropdowns.

**Riesgo:** Medio. Cambios CSS pueden tener efectos secundarios en otros modales.

---

## Fase 5: Reportes — generadores separados + abrir ubicación ✅ COMPLETADA (`64ebdf0`)

**Problema 1:** Docente y Especialista usan el mismo generador de Materiales.

**Problema 2:** Botón "Abrir ubicación" no funciona en Electron.

### Generadores (Backend)

| Archivo | Acción |
|---------|--------|
| `export/ReporteDocenteGenerator.java` (nuevo) | Generador dedicado usando `docenteConsolidado()` y `docenteDetalleDia()` |
| `export/ReporteEspecialistaGenerator.java` (nuevo) | Generador dedicado usando `especialista()` |
| `ReporteController.java:62-63` | Cambiar `materialesGen.generar()` → `docenteGen.generar()` |
| `ReporteController.java:76-77` | Cambiar `materialesGen.generar()` → `especialistaGen.generar()` |

### Abrir ubicación (Electron + Frontend)

| Archivo | Cambio |
|---------|--------|
| `electron/main.ts:129-137` | Cambiar `shell.openPath(path.dirname(...))` → `shell.showItemInFolder(filePath)` |
| `electron/preload.ts:169-171` | Sin cambios (la interfaz se mantiene) |
| `Reportes.tsx:31-46` | Mejorar manejo de error, cambiar mensaje fallback |

### Reportes semilla

| Archivo | Acción |
|---------|--------|
| `ReporteController.java` | Agregar `POST /api/reportes/semilla/generar` que genera todos los tipos |

**Riesgo:** Medio. Generadores nuevos requieren queries correctas. `showItemInFolder` debe probarse en Windows.

---

## Fase 6: Mejoras en Asistencia Docente ✅ COMPLETADA (`5cb78fa`)

**Problema:** La lista de materiales predeterminados es un JSON estático hardcodeado. No hay UI para modificarlo.

### Acciones

| Archivo | Cambio |
|---------|--------|
| `Asistencia.tsx` | Agregar botón "Editar lista predeterminada" con mini-modal que use MaterialTable |
| `materiales-docente-default.json` | Eliminar y usar un endpoint REST en su lugar |
| Backend: nuevo endpoint | `GET/PUT /api/asistencia/materiales-default` que persista en DB tabla nueva o en archivo config |

**Riesgo:** Medio. Requiere nuevo endpoint backend.

---

## Resumen de fases

| Fase | Descripción | Riesgo | Estado | Commit |
|------|-------------|--------|--------|--------|
| 1 | Corregir tipos PRE (3→4,5,6) | Bajo | ✅ | `f0de8d9` |
| 2 | Agregar Estado a Pacientes y Trat.Predef. | Medio | ✅ | `ceec7d5`, `c0588d8` |
| 3 | Comportamiento monto total y numéricos | Bajo | ✅ | `ceec7d5` |
| 4 | Bug modal trat.predef + dropdowns | Medio | ✅ | `64ebdf0` |
| 5 | Reportes: generadores + abrir ubicación + semilla | Medio | ✅ | `64ebdf0` |
| 6 | Mejoras en Asistencia Docente | Medio | ✅ | `5cb78fa` |

> **Nota:** Las migraciones reales difieren del plan original — ver documento para detalle (V7→V8 intercambiadas).
