package com.odontologia.formatos.controller;

import com.odontologia.formatos.config.AppConfig;
import com.odontologia.formatos.export.ReporteEconomicoGenerator;
import com.odontologia.formatos.export.ReporteMaterialesGenerator;
import com.odontologia.formatos.export.ReporteAsistenciaGenerator;
import com.odontologia.formatos.export.ReporteConsolidadoGenerator;
import com.odontologia.formatos.export.ReporteDocenteGenerator;
import com.odontologia.formatos.export.ReporteEspecialistaGenerator;
import com.odontologia.formatos.export.ReporteTratamientoGenerator;
import com.odontologia.formatos.service.NegocioException;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteController {

    private final ReporteMaterialesGenerator materialesGen = new ReporteMaterialesGenerator();
    private final ReporteEconomicoGenerator economicoGen = new ReporteEconomicoGenerator();
    private final ReporteAsistenciaGenerator asistenciaGen = new ReporteAsistenciaGenerator();
    private final ReporteDocenteGenerator docenteGen = new ReporteDocenteGenerator();
    private final ReporteEspecialistaGenerator especialistaGen = new ReporteEspecialistaGenerator();
    private final ReporteTratamientoGenerator tratamientoGen = new ReporteTratamientoGenerator();
    private final ReporteConsolidadoGenerator consolidadoGen = new ReporteConsolidadoGenerator();

    public void register(Javalin app) {
        app.post("/api/reportes/materiales/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mes = ((Number) body.get("mes")).intValue();
            try {
                materialesGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = materialesGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                materialesGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/economico/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mes = ((Number) body.get("mes")).intValue();
            try {
                economicoGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = economicoGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                economicoGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/docente/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mes = ((Number) body.get("mes")).intValue();
            try {
                docenteGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = docenteGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                docenteGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/especialista/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mes = ((Number) body.get("mes")).intValue();
            try {
                especialistaGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = especialistaGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                especialistaGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/tratamiento/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mes = ((Number) body.get("mes")).intValue();
            Integer operadorID = body.get("operadorID") != null
                    ? ((Number) body.get("operadorID")).intValue() : null;
            String tipo = (String) body.get("tipo");
            tratamientoGen.setOperadorID(operadorID);
            tratamientoGen.setTipo(tipo);
            try {
                tratamientoGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = tratamientoGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                tratamientoGen.setOperadorID(null);
                tratamientoGen.setTipo(null);
                tratamientoGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/anual/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            try {
                consolidadoGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = consolidadoGen.generar(anio, 1, 12, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                consolidadoGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/asistencia/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mes = ((Number) body.get("mes")).intValue();
            try {
                asistenciaGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = asistenciaGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte: " + e.getMessage()));
            } finally {
                asistenciaGen.setClinicaID(1);
            }
        });

        app.post("/api/reportes/consolidado/generar", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int anio = ((Number) body.get("anio")).intValue();
            int mesInicio = ((Number) body.get("mesInicio")).intValue();
            int mesFin = ((Number) body.get("mesFin")).intValue();
            try {
                consolidadoGen.setClinicaID(ControllerUtil.clinicaID(ctx));
                Path path = consolidadoGen.generar(anio, mesInicio, mesFin, carpetaReportes());
                ctx.json(Map.of("path", path.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reporte consolidado: " + e.getMessage()));
            } finally {
                consolidadoGen.setClinicaID(1);
            }
        });

        app.get("/api/reportes/recientes", ctx -> {
            ctx.json(archivosRecientes());
        });

        app.post("/api/reportes/semilla/generar", ctx -> {
            try {
                int mes = mesActual();
                int anio = anioActual();
                int clinicaID = ControllerUtil.clinicaID(ctx);
                materialesGen.setClinicaID(clinicaID);
                economicoGen.setClinicaID(clinicaID);
                docenteGen.setClinicaID(clinicaID);
                especialistaGen.setClinicaID(clinicaID);
                asistenciaGen.setClinicaID(clinicaID);
                Path matPath = materialesGen.generar(anio, mes, carpetaReportes());
                Path ecoPath = economicoGen.generar(anio, mes, carpetaReportes());
                Path docPath = docenteGen.generar(anio, mes, carpetaReportes());
                Path espPath = especialistaGen.generar(anio, mes, carpetaReportes());
                Path asisPath = asistenciaGen.generar(anio, mes, carpetaReportes());
                ctx.json(Map.of(
                        "materiales", matPath.toAbsolutePath().toString(),
                        "economico", ecoPath.toAbsolutePath().toString(),
                        "docente", docPath.toAbsolutePath().toString(),
                        "especialista", espPath.toAbsolutePath().toString(),
                        "asistencia", asisPath.toAbsolutePath().toString()));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Error al generar reportes: " + e.getMessage()));
            } finally {
                materialesGen.setClinicaID(1);
                economicoGen.setClinicaID(1);
                docenteGen.setClinicaID(1);
                especialistaGen.setClinicaID(1);
                asistenciaGen.setClinicaID(1);
            }
        });
    }

    private Path carpetaReportes() {
        Path dir = Paths.get(AppConfig.carpetaInicialReportes());
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            dir = Paths.get(System.getProperty("java.io.tmpdir"), "FormatosOdontologia", "Reportes");
            try {
                Files.createDirectories(dir);
            } catch (Exception ex) {
                throw new NegocioException("No se pudo crear la carpeta de reportes: " + ex.getMessage());
            }
        }
        return dir;
    }

    private List<Map<String, String>> archivosRecientes() {
        List<Map<String, String>> lista = new ArrayList<>();
        Path dir = carpetaReportes();
        File[] archivos = dir.toFile().listFiles((d, name) -> name.endsWith(".xlsx"));
        if (archivos == null) return lista;

        for (File f : archivos) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("nombre", f.getName());
            entry.put("path", f.getAbsolutePath());
            entry.put("tamano", String.valueOf(f.length()));
            lista.add(entry);
        }

        lista.sort((a, b) -> b.get("nombre").compareTo(a.get("nombre")));
        return lista;
    }

    private int mesActual() {
        return LocalDate.now().getMonthValue();
    }

    private int anioActual() {
        return LocalDate.now().getYear();
    }
}
