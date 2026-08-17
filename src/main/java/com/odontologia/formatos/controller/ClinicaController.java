package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.Clinica;
import com.odontologia.formatos.service.ClinicaService;
import com.odontologia.formatos.service.NegocioException;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;

import java.util.List;
import java.util.Map;

public class ClinicaController {

    private final ClinicaService service = new ClinicaService();

    public void register(Javalin app) {
        app.get("/api/clinicas", ctx -> {
            List<Clinica> clinicas = service.listar();
            ctx.json(clinicas);
        });

        app.post("/api/clinicas", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            String nombre = ControllerUtil.requireStringBodyField(ctx, "nombre");
            String grupo = (String) body.get("grupo");
            int id = service.crear(nombre, grupo);
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/clinicas/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            Clinica c = new Clinica();
            c.setClinicaID(id);
            c.setNombre(ControllerUtil.requireStringBodyField(ctx, "nombre"));
            c.setGrupo((String) body.get("grupo"));
            c.setEstado(body.containsKey("estado") ? ((Number) body.get("estado")).intValue() : service.buscarPorId(id).getEstado());
            service.actualizar(c);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/clinicas/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            try {
                service.eliminar(id);
                ctx.status(204);
            } catch (NegocioException e) {
                ctx.status(409).json(Map.of("error", e.getMessage()));
            }
        });
    }
}