# Bitácora de Cambios

Registro cronológico de los cambios significativos del proyecto.

---

## 2026-08-17 — Multi-clínica (BD única + ClinicaID)

**Alcance:** G1–G9 del plan multi-clínica.

### Arquitectura

- Se reemplaza el modelo de "instancia dedicada" (RD-3.1.7) por **base de datos única compartida** con aislamiento por `ClinicaID`.
- Nueva tabla `Clinica` (ClinicaID, Nombre UNIQUE, Grupo, Estado).
- Columna `ClinicaID` añadida a: Operadores, Docentes, Pacientes, Unidad, Tratamiento, Asistencia y RegistroAnulacion (migración `V14__clinicas.sql`).
- Identificación de clínica por header HTTP `X-Clinica-Nombre` (URL-encoded). Header faltante o clínica inexistente → 400.
- Default de compatibilidad `ClinicaID = 1` en constructores y sobrecargas de servicio; producción siempre filtra por clínica.

### Backend

- CRUD de clínicas (`ClinicaController` / `ClinicaService`) con bloqueo de eliminación si la clínica tiene registros.
- Catálogos (operador, docente, paciente, unidad) filtrados por `ClinicaID`.
- Transacciones (tratamiento, asistencia, anulaciones) filtradas por `ClinicaID`.
- Reportes y dashboard filtrados por `ClinicaID`.

### Frontend

- Selector de clínica al iniciar la aplicación (`SelectClinicScreen`) y switcher de clínica en el topbar (`ClinicSwitcher`).
- `request()` envía `X-Clinica-Nombre` desde el store de sesión (`clinicaStore`).
- Remount de toda la vista al cambiar de clínica (key por `clinica.clinicaID`).
- Página de gestión de clínicas (CRUD) en `/clinicas`.

### Tests

- Test de aislamiento multi-clínica: catálogos, transacciones y anulaciones no cruzan entre clínicas.
- Test de reportes filtrados por clínica.

### Documentación

- RD-3.1.7 actualizado a "Multi-clínica (BD única)" en especificación de requerimientos y definición técnica.
- Checklist de definición: nuevos ítems 2.20–2.22 (tabla Clinica, ClinicaID, header) y 3.12–3.13 (aislamiento y bloqueo de eliminación).

---

## 2026-08-18 — Avance de tratamiento como sesión

**Alcance:** Redefinir el avance de tratamiento (antes `Tipo='AVANCE'` con `TratamientoPadreID`) como una sesión del mismo tratamiento.

### Base de datos

- Migración `V15__avance_como_sesion.sql`: nueva tabla `Tratamiento_Avance` (sesiones) y `Materiales_List_Avance` (materiales por sesión).
- `Tratamiento` reconstruida sin `TratamientoPadreID`; CHECK de `Tipo` reducido a `NORMAL`/`CONTINUO`. `MontoAnterior` se conserva (lo usa `cambiarTipo`).
- `Pago` gana columna `AvanceID` opcional para trazabilidad.

### Backend

- Nuevos `TratamientoAvance` y `MaterialAvance` (modelo + repositorio).
- `TratamientoService`: `agregarAvance`, `listarAvances`, `anularAvance`, `obtenerConsolidado`. Se eliminan `crearAvance`, `validarPadre`, `avancesDe` y `candidatosPadre`.
- Anular avance elimina el pago vinculado y recalcula el tratamiento padre.
- Reportes (`materiales`, `especialista`, `consumoPorTratamiento`) suman `Materiales_List_Avance` de sesiones ACTIVAS.

### API

- Se eliminan `POST /api/tratamientos/avance`, `GET /api/tratamientos/{id}/avances` (semántica antigua) y `GET /api/tratamientos/candidatos-padre`.
- Nuevos: `POST /api/tratamientos/{id}/avances`, `GET /api/tratamientos/{id}/avances`, `POST /api/tratamientos/avances/{avanceID}/anular`, `GET /api/tratamientos/{id}/consolidado`.

### Frontend

- `RegistrarAvanceModal`: registrar avance con fecha, materiales y pago opcional.
- `DetalleTratamientoSubventana`: lista de avances (con anulación) y vista de materiales consolidados.
- `CrearTratamientoModal`: se elimina el tipo AVANCE y el selector de tratamiento padre.
- `Reportes.tsx`: se elimina el filtro AVANCE.

### Documentación

- HU-19 "Registrar avances de un tratamiento (sesiones)" y RF-1.4.10–1.4.12.
- RD-3.1.15 "Avances como sesiones". Checklist 2.20–2.21. API REST actualizada (49 endpoints).