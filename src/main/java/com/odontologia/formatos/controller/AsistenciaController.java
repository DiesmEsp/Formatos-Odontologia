package com.odontologia.formatos.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.PeriodoAusencia;
import com.odontologia.formatos.repository.AsistenciaMaterialRepository;
import com.odontologia.formatos.service.AsistenciaService;
import com.odontologia.formatos.service.NegocioException;
import io.javalin.Javalin;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AsistenciaController {

    private final AsistenciaService service = new AsistenciaService();
    private final ObjectMapper mapper = new ObjectMapper();

    public void register(Javalin app) {

        app.post("/api/asistencia/abrir", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int docenteId = ((Number) body.get("docenteId")).intValue();
            String fecha = (String) body.get("fecha");
            String horaEntrada = (String) body.get("horaEntrada");
            try {
                Asistencia a = service.abrirDia(docenteId, fecha, horaEntrada);
                ctx.status(201).json(a);
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/asistencia/{id}/entrada", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            String horaEntrada = (String) body.get("horaEntrada");
            try {
                service.registrarEntrada(id, horaEntrada);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/asistencia/{id}/salida", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            String horaSalida = (String) body.get("horaSalida");
            try {
                service.registrarSalida(id, horaSalida);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/asistencia/{id}/ausencias", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            String horaInicio = (String) body.get("horaInicio");
            String motivo = (String) body.get("motivo");
            try {
                PeriodoAusencia ausencia = service.iniciarAusencia(id, horaInicio, motivo);
                ctx.status(201).json(ausencia);
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/asistencia/{id}/ausencias/{ausId}/regresar", ctx -> {
            int ausId = Integer.parseInt(ctx.pathParam("ausId"));
            var body = ctx.bodyAsClass(Map.class);
            String horaFin = (String) body.get("horaFin");
            try {
                service.finalizarAusencia(ausId, horaFin);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.delete("/api/asistencia/{id}/ausencias/{ausId}", ctx -> {
            int ausId = Integer.parseInt(ctx.pathParam("ausId"));
            try {
                service.eliminarAusencia(ausId);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.get("/api/asistencia/{id}/detalle", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                ctx.json(service.obtenerDetalle(id));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/asistencia/{id}/materiales", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            int materialId = ((Number) body.get("materialId")).intValue();
            double cantidad = ((Number) body.get("cantidad")).doubleValue();
            service.acumularMaterial(id, materialId, cantidad);
            ctx.json(Map.of("ok", true));
        });

        app.post("/api/asistencia/{id}/registrar", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            int docenteId = ((Number) body.get("docenteId")).intValue();
            String fecha = (String) body.get("fecha");
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) body.get("materiales");
            Map<Integer, Double> materiales = raw.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> Integer.parseInt(e.getKey()),
                            e -> ((Number) e.getValue()).doubleValue(),
                            (a, b) -> b,
                            LinkedHashMap::new));
            service.registrarMateriales(docenteId, fecha, materiales);
            ctx.json(Map.of("ok", true));
        });

        app.post("/api/asistencia/{id}/anular", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            String motivo = (String) body.get("motivo");
            try {
                service.anular(id, motivo);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.get("/api/asistencia/{id}/materiales", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(service.materialesDelDia(id));
        });

        app.get("/api/asistencia/materiales-default", ctx -> {
            ctx.json(leerDefaults());
        });

        app.put("/api/asistencia/materiales-default", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> materiales = (List<Map<String, Object>>) body.get("materiales");
            guardarDefaults(materiales);
            ctx.json(Map.of("ok", true));
        });
    }

    private Path archivoDefaults() {
        String userHome = System.getProperty("user.home");
        Path dir = Paths.get(userHome, "Documents", "FormatosOdontologia", "Config");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        return dir.resolve("materiales-default.json");
    }

    private List<Map<String, Object>> leerDefaults() {
        Path archivo = archivoDefaults();
        if (!Files.exists(archivo)) {
            List<Map<String, Object>> defaults = new ArrayList<>();
            defaults.add(crearEntry(1, 2.0));
            defaults.add(crearEntry(2, 3.0));
            defaults.add(crearEntry(3, 1.0));
            guardarDefaults(defaults);
            return defaults;
        }
        try {
            String json = Files.readString(archivo);
            List<Map<String, Object>> lista = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void guardarDefaults(List<Map<String, Object>> materiales) {
        Path archivo = archivoDefaults();
        try {
            Files.writeString(archivo, mapper.writeValueAsString(materiales));
        } catch (IOException e) {
            throw new NegocioException("No se pudo guardar la configuracion de materiales predeterminados.");
        }
    }

    private Map<String, Object> crearEntry(int materialId, double cantidad) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("materialId", materialId);
        entry.put("cantidad", cantidad);
        return entry;
    }
}
