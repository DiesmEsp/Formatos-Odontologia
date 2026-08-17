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