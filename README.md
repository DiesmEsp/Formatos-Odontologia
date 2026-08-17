# Formatos Odontologicos

Aplicacion de escritorio para gestion clinica odontologica (UNMSM).

Arquitectura: **Electron + React 19** (frontend) + **Java 21 + Javalin** (backend REST) + **SQLite** (local).

## Stack

| Capa | Tecnologia |
|---|---|
| Desktop Shell | Electron 35 |
| Frontend | React 19, TypeScript, Vite 6, Recharts |
| Backend API | Javalin 6.5 (REST en puerto 7070) |
| Base de datos | SQLite (archivo local) |
| Migraciones | Flyway |
| Excel | Apache POI 5.5 |
| Build Backend | Gradle 8.14 (Shadow JAR, jlink) |
| Build Frontend | Vite + electron-builder (NSIS .exe) |
| Tests Backend | JUnit 5 (119 tests) + JaCoCo |
| Tests Frontend | Vitest (138 tests) + Playwright (E2E) |

## Arquitectura

El Electron main process spawn el JAR del backend Java (JRE minima via jlink), espera el health check en `localhost:7070`, y carga la SPA de React desde el sistema de archivos. El frontend consume la API REST via `fetch()`.

## Requisitos

- **JDK 21** (para compilar y ejecutar)
- **Node.js 22+** (para frontend y empaquetado)
- **Windows 10/11** (instalador .exe generado con electron-builder NSIS)
- **~200 MB** de espacio libre para la aplicacion instalada
- **4 GB RAM** recomendado (Electron + JRE + SQLite)

## Comandos

### Desarrollo

```bash
# Backend
.\gradlew.bat compileJava

# Frontend (modo dev con Vite, backend debe estar corriendo aparte)
cd frontend
npm install
npm run dev

# Electron en modo dev (spawnea backend + frontend)
npm run electron:dev
```

### Tests

```bash
# Backend
.\gradlew.bat test

# Frontend unitarios
cd frontend
npm test

# Frontend TypeScript check
npm run typecheck

# E2E (Playwright, requiere backend corriendo)
npm run test:e2e
```

### Build de produccion

```bash
# 1. Compilar backend (fat JAR + JRE minima)
.\gradlew.bat shadowJar buildJre prepareElectronResources

# 2. Empaquetar Electron (genera instalador .exe)
cd frontend
npm run electron:build
```

El instalador se genera en `frontend/release/Formatos Odontologicos Setup *.exe`.

## Estructura del proyecto

```
Formatos-Odontologia/
├── build.gradle                    # Java build (shadowJar, jlink, copy resources)
├── src/
│   ├── main/java/com/odontologia/formatos/
│   │   ├── config/                 # AppConfig, DatabaseConfig
│   │   ├── controller/             # Javalin REST controllers + Main.java
│   │   ├── db/                     # ConnectionManager
│   │   ├── export/                 # Excel generators (POI)
│   │   ├── model/                  # 14 POJOs
│   │   ├── repository/             # JDBC data access
│   │   ├── service/                # Business logic
│   │   └── util/                   # Logging, errors, transactions
│   ├── main/resources/
│   │   └── db/migration/           # Flyway migrations (V1-V5)
│   └── test/                       # JUnit tests
├── frontend/
│   ├── electron/                   # main.ts, preload.ts
│   ├── src/
│   │   ├── api/                    # HTTP client + TypeScript types
│   │   ├── components/             # UI components
│   │   ├── hooks/                  # useApi, useToast, usePagination
│   │   ├── lib/                    # format.ts, constants.ts
│   │   └── pages/                  # Dashboard, Asistencia, Catalogos, etc.
│   ├── electron-builder.yml        # NSIS installer config
│   └── package.json
├── .github/workflows/ci.yml        # CI pipeline
└── planning/                       # Documentos de planificacion
```

## Funcionalidades

- **Dashboard**: KPIs, graficos de ingresos, top materiales, asistencia del dia
- **Asistencia Docente**: Registro con hora de entrada/salida, periodos de ausencia, acumulacion de materiales
- **Tratamientos**: Grid de unidades, creacion con plantilla, cierre, pagos, reapertura, edicion retroactiva
- **Catalogos**: Materiales, Docentes, Pacientes, Operadores, Tratamientos Predefinidos, Trat. Realizados, Conversiones de unidad
- **Unidades**: Gestion de modulos de atencion con bloqueo visual
- **Reportes**: Exportacion Excel — Materiales, Economico, Asistencia Docente, Anual

## Documentacion

- [API REST](Docs/7.%20API%20REST.md) — 45 endpoints en 7 controladores
- [Definicion tecnica](Docs/4.%20Definici%C3%B3n%20t%C3%A9cnica.md) — Stack, arquitectura y decisiones
- [Historias de usuario](Docs/Requerimientos/2.%20Historias%20de%20usuario.md) — Requerimientos funcionales
- [Especificacion de requerimientos](Docs/Requerimientos/3.%20Especificaci%C3%B3n%20de%20requerimientos.md) — RFs y RNFs
- [Checklist de definicion](Docs/5.%20Checklist%20de%20definici%C3%B3n.md)
- [Informe de auditoria](Docs/6.%20Informe%20de%20auditor%C3%ADa.md) — Hallazgos y plan de accion

