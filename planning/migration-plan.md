# Plan de Migración Progresiva
## Java Backend (Javalin REST) + React Frontend + Electron Desktop

---

## Arquitectura Final

```
┌──────────────────────────────────────────────────────────────────┐
│                     Electron App (.exe)                           │
│  ┌─────────────────────────┐    ┌──────────────────────────────┐ │
│  │  React 19 + Vite        │    │  Main Process (Node.js)      │ │
│  │  (Renderer)             │    │                              │ │
│  │                         │    │  1. Spawn JRE + JAR          │ │
│  │  App.tsx                │    │  2. Health check polling     │ │
│  │   ├── Layout.tsx        │    │  3. Cargar BrowserWindow     │ │
│  │   ├── Sidebar.tsx       │    │  4. tree-kill en close       │ │
│  │   ├── Router            │    │                              │ │
│  │   └── Pages             │    │  ┌─────────────────────────┐ │ │
│  │       ├── Dashboard/    │    │  │  JRE minima (jlink)     │ │ │
│  │       ├── Tratamientos/ │    │  │     ~40 MB              │ │ │
│  │       ├── Asistencia/   │    │  └─────────────────────────┘ │ │
│  │       ├── Catalogos/    │    │                              │ │
│  │       ├── Reportes/     │    │  ┌─────────────────────────┐ │ │
│  │       └── Unidades/     │    │  │  Fat JAR (Javalin)      │ │ │
│  │                         │    │  │  Servicios Java          │ │ │
│  │  ┌──────────────────┐   │    │  │  Repositorios Java       │ │ │
│  │  │ window.api.fetch │───┼────┼──│▶ http://localhost:7070   │ │ │
│  │  └──────────────────┘   │    │  │  SQLite JDBC             │ │ │
│  │                         │    │  └─────────────────────────┘ │ │
│  │  preload.ts             │    │                              │ │
│  └─────────────────────────┘    │  extraResources:             │ │
│                                  │    jre/ + backend.jar        │ │
│                                  └──────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘

Peso final .exe: ~195MB (Chromium ~100 + JRE ~40 + JAR ~5 + Assets ~50)
```

---

## Reutilizacion de Codigo

| Capa | Archivos | Se conserva? | Cambios necesarios |
|------|----------|:---:|-------------------|
| **Modelos** (13 POJOs) | `model/*.java` | Si 100% | Ninguno |
| **Repositorios** (14 DAOs) | `repository/*.java` | Si 100% | Ninguno |
| **Servicios** (10 clases) | `service/*.java` | Si 100% | Ninguno |
| **Export** (6 clases) | `export/*.java` | Si 100% | Ninguno |
| **Utilidades** (4 clases) | `util/*.java` | Si 100% | Ninguno |
| **Config** (2 clases) | `config/*.java` | Si 100% | Ninguno |
| **DB** (2 clases) | `db/*.java` | Si 100% | Ninguno |
| **UI JavaFX** (16 clases) | `ui/**/*.java` | No se descarta | Reemplazado por React |
| **Nuevo** (7 clases) | `controller/*.java` | Nuevo | Javalin REST endpoints |

**Resultado**: Se conservan **36 de 54** archivos Java. Solo se reescribe la UI (16 archivos) y se anaden 7 controladores REST.

---

## Estructura del Proyecto

```
Formatos-Odontologia/
├── build.gradle                         # Backend Java + Javalin + Shadow
├── settings.gradle
├── gradlew / gradlew.bat
│
├── src/main/java/com/odontologia/formatos/
│   ├── config/
│   │   ├── AppConfig.java               # Conservado
│   │   └── DatabaseConfig.java          # Conservado
│   ├── db/
│   │   ├── ConnectionManager.java       # Conservado
│   │   └── DemoDataLoader.java          # Conservado
│   ├── model/                           # 13 POJOs conservados
│   ├── repository/                      # 14 DAOs conservados
│   ├── service/                         # 10 servicios conservados
│   ├── export/                          # 6 clases conservadas
│   ├── util/                            # 4 clases conservadas
│   ├── ui/                              # 16 archivos eliminados (JavaFX)
│   └── controller/                      # Nuevo: controladores REST
│       ├── Main.java                    # Entry point Javalin
│       ├── TratamientoController.java
│       ├── AsistenciaController.java
│       ├── CatalogoController.java
│       ├── ReporteController.java
│       ├── DashboardController.java
│       └── UnidadController.java
│
├── src/main/resources/
│   ├── application.properties           # Conservado
│   ├── db/migration/                    # 4 V*.sql conservados
│   ├── db/demo/datos_demo.sql           # Conservado
│   ├── images/logo.png                  # Conservado
│   └── fonts/                           # Conservado
│
├── src/test/java/...                    # 14 tests conservados
│
├── frontend/                            # Nuevo: Electron + React
│   ├── package.json
│   ├── electron-builder.yml
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── tsconfig.node.json
│   ├── electron/
│   │   ├── main.ts                      # Electron entry, spawns Java
│   │   └── preload.ts                   # contextBridge window.api
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── api/index.ts                 # fetch wrapper + types
│   │   ├── components/
│   │   │   ├── Layout.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── MaterialTable.tsx
│   │   │   ├── SearchableCombo.tsx
│   │   │   ├── ConfirmDialog.tsx
│   │   │   ├── Toast.tsx
│   │   │   ├── MonthYearPicker.tsx
│   │   │   ├── KpiCard.tsx
│   │   │   ├── StationCard.tsx
│   │   │   ├── Badge.tsx
│   │   │   └── Chart/
│   │   │       ├── LineChart.tsx
│   │   │       ├── DonutChart.tsx
│   │   │       └── BarChart.tsx
│   │   ├── pages/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Tratamientos.tsx
│   │   │   ├── Asistencia.tsx
│   │   │   ├── Catalogos.tsx
│   │   │   ├── Reportes.tsx
│   │   │   └── Unidades.tsx
│   │   ├── hooks/
│   │   ├── lib/
│   │   └── styles/
│   └── public/
│       └── fonts/
│
└── mockups/                             # Conservados como referencia
```

---

## Fases de Migracion

### Phase 0.5: Fundacion del Proyecto (HECHO)

- Crear estructura `frontend/`
- `package.json` con dependencias
- Configuracion Vite, TypeScript, electron-builder
- Esqueletos Electron (main.ts, preload.ts)
- Esqueletos React (main.tsx, App.tsx)
- Esqueleto controlador Javalin (Main.java)
- Actualizar `build.gradle` (Javalin, shadow plugin, eliminar JavaFX)
- GitHub Actions CI pipeline
- `.gitignore` actualizado

### Phase 1: Capa REST - Semana 2-3

**7 controladores Javalin que exponen los servicios existentes:**

| Controller | Metodos REST | Servicio usado |
|---|---|---|
| `Main.java` | GET `/health` | - |
| `TratamientoController.java` | GET, POST, PUT, DELETE | `TratamientoService` |
| `AsistenciaController.java` | GET, POST, PUT | `AsistenciaService` |
| `CatalogoController.java` | CRUD 5 entidades | 5 servicios |
| `ReporteController.java` | POST, GET | `ReporteService` |
| `DashboardController.java` | GET | Consultas directas |
| `UnidadController.java` | GET, POST, DELETE | `UnidadService` |

### Phase 2: Frontend Core - Semana 3-5

- API Client (`window.api.fetch`)
- Layout + Sidebar + Componentes reutilizables
- Port de `estilo.css` (1249 lineas) a variables.css + main.css

### Phase 3: Paginas/Vistas - Semana 5-9

- **Semana 5-6**: Dashboard (KPIs, graficos Recharts, quick grid)
- **Semana 6-7**: Tratamientos (grid unidades, panel detalle, modal crear)
- **Semana 7-8**: Asistencia (2-step wizard, acumulacion materiales)
- **Semana 8**: Catalogos (5 tabs CRUD + filtros)
- **Semana 8-9**: Reportes (selector mes/ano, generacion Excel, tabla recientes)
- **Semana 9**: Unidades (CRUD simple)

### Phase 4: Electron Integration + Empaquetado - Semana 9-10

- `electron/main.ts`: spawn Java, health check, manejar cierre
- `electron-builder.yml`: extraResources JRE + JAR
- Scripts Gradle para build automatizado (jlink, shadowJar, electron-builder)

### Phase 5: Testing - Semana 10-12

| Nivel | Framework | Archivos |
|---|---|---|
| Repositorios Java | JUnit 5 (existente) | 5 tests conservados |
| Servicios Java | JUnit 5 (existente) | 6 tests conservados |
| Export Java | JUnit 5 (existente) | 2 tests conservados |
| Controladores REST | JUnit 5 + Javalin test | 6 tests nuevos |
| Componentes React | vitest + Testing Library | ~15 tests nuevos |
| Paginas React | vitest + MSW mock | ~6 tests nuevos |
| E2E | Playwright | ~5 tests nuevos |

---

## Timeline

| Fase | Semana | Entregable |
|------|--------|-----------|
| 0.5 | HECHO | Estructura base, configs, CI |
| 1 | 2-3 | 7 controladores Javalin REST |
| 2 | 3-5 | Layout, componentes, CSS |
| 3 | 5-9 | 6 paginas React completas |
| 4 | 9-10 | Electron main + empaquetado |
| 5 | 10-12 | Testing + pulido |
| **Beta** | 12 | `Setup 2.0.0.exe` funcional |

---

## Comparacion con otras opciones

| | Migracion Completa (better-sqlite3) | Migracion Progresiva (este plan) |
|---|---|---|
| Codigo Java a reescribir | 54 archivos | **0 archivos** |
| Archivos JavaFX descartados | 16 | 16 |
| Archivos nuevos | ~80 | **~50** |
| Riesgo de bugs | Alto (reescritura total) | **Bajo** (backend intacto) |
| Peso final .exe | ~150 MB | ~195 MB |
| Arranque en frio | Instantaneo | ~2s (JVM startup) |

---

## Stack Tecnologico

| Componente | Tecnologia | Version |
|---|---|---|
| Backend REST | Javalin (Jetty 12) | 7.2.2 |
| Backend ORM | JDBC raw + SQLite | 3.53.2.1 |
| Backend Migraciones | Flyway | 13.1.0 |
| Backend Excel | Apache POI | 5.5.1 |
| Frontend | React + Vite | 19 + 6 |
| Desktop | Electron | 34 |
| Empaquetado | electron-builder | 25 |
| Graficos | Recharts | 2.15 |
| Iconos | Lucide React | 0.460 |
| CI/CD | GitHub Actions | - |
| Tests Backend | JUnit 5 | 5.14 |
| Tests Frontend | vitest | 3 |
| Tests E2E | Playwright | 1.50 |
