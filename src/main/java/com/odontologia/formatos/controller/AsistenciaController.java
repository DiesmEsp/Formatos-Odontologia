package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.AsistenciaMaterial;
import com.odontologia.formatos.repository.AsistenciaMaterialRepository;
import com.odontologia.formatos.repository.AsistenciaRepository;
import com.odontologia.formatos.service.AsistenciaService;
import com.odontologia.formatos.service.NegocioException;
import io.javalin.Javalin;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AsistenciaController {

    private final AsistenciaService service = new AsistenciaService();
    private final AsistenciaMaterialRepository materialRepository = new AsistenciaMaterialRepository();

    public void register(Javalin app) {

        app.post("/api/asistencia/abrir", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int docenteId = ((Number) body.get("docenteId")).intValue();
            String fecha = (String) body.get("fecha");
            Asistencia a = service.abrirDia(docenteId, fecha);
            ctx.status(201).json(a);
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
    }
}
