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
| Tests Backend | JUnit 5 (125 tests) |
| Tests Frontend | Vitest (128 tests) + Playwright (E2E) |

## Arquitectura

El Electron main process spawn el JAR del backend Java (JRE minima via jlink), espera el health check en `localhost:7070`, y carga la SPA de React desde el sistema de archivos. El frontend consume la API REST via `fetch()`.

## Requisitos

- **JDK 21** (para compilar y ejecutar)
- **Node.js 22+** (para frontend y empaquetado)

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
│   │   ├── db/                     # ConnectionManager, DemoDataLoader
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
- **Catalogos**: Materiales, Docentes, Especialistas, Tratamientos Predefinidos, Conversiones de unidad
- **Unidades**: Gestion de modulos de atencion con bloqueo visual
- **Reportes**: Exportacion Excel — Materiales, Economico, Asistencia Docente, Anual
