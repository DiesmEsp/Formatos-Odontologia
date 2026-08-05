package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.Unidad;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.service.NegocioException;
import com.odontologia.formatos.service.UnidadService;
import io.javalin.Javalin;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UnidadController {

    private final UnidadService service = new UnidadService();
    private final UnidadRepository repository = new UnidadRepository();

    public void register(Javalin app) {
        app.get("/api/unidades", ctx -> {
            List<Unidad> unidades = repository.findAll();
            ctx.json(unidades);
        });

        app.post("/api/unidades", ctx -> {
            int id = service.crear();
            ctx.status(201).json(Map.of("id", id));
        });

        app.delete("/api/unidades/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                service.eliminar(id);
                ctx.status(204);
            } catch (NegocioException e) {
                ctx.status(409).json(Map.of("error", e.getMessage()));
            }
        });
    }
}
