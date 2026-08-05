package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.TratamientoMaterial;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.service.NegocioException;
import com.odontologia.formatos.service.TratamientoService;
import io.javalin.Javalin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TratamientoController {

    private final TratamientoService service = new TratamientoService();
    private final TratamientoMaterialRepository materialRepo = new TratamientoMaterialRepository();

    public void register(Javalin app) {
        app.get("/api/tratamientos", ctx -> {
            ctx.json(service.activos());
        });

        app.get("/api/tratamientos/unidad/{id}", ctx -> {
            int unidadId = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(service.porUnidad(unidadId));
        });

        app.get("/api/tratamientos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Tratamiento t = service.buscarPorId(id);
            if (t == null) {
                ctx.status(404).json(Map.of("error", "Tratamiento no encontrado"));
            } else {
                ctx.json(t);
            }
        });

        app.post("/api/tratamientos", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int operadorID = ((Number) body.get("operadorID")).intValue();
            int pacienteID = ((Number) body.get("pacienteID")).intValue();
            Integer unidadID = body.get("unidadID") != null
                    ? ((Number) body.get("unidadID")).intValue() : null;
            String fecha = (String) body.get("fecha");
            Integer tratPredID = body.get("tratPredID") != null
                    ? ((Number) body.get("tratPredID")).intValue() : null;
            Double monto = body.get("monto") != null
                    ? ((Number) body.get("monto")).doubleValue() : null;
            String tipo = (String) body.get("tipo");

            try {
                int id = service.crear(operadorID, pacienteID, unidadID, fecha, tratPredID, monto, tipo);
                ctx.status(201).json(Map.of("id", id));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/cerrar", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                service.cerrar(id);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/anular", ctx -> {
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

        app.post("/api/tratamientos/{id}/reabrir", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                service.reabrir(id);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/pago", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            double abono = ((Number) body.get("abono")).doubleValue();
            try {
                service.registrarPago(id, abono);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/tratamientos/{id}/editar", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);

            TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
            dto.monto = body.get("monto") != null
                    ? ((Number) body.get("monto")).doubleValue() : null;
            dto.montoPagado = body.get("montoPagado") != null
                    ? ((Number) body.get("montoPagado")).doubleValue() : null;
            dto.estadoPago = (String) body.get("estadoPago");
            dto.fecha = (String) body.get("fecha");
            dto.nombreTratamiento = (String) body.get("nombreTratamiento");
            dto.operadorID = body.get("operadorID") != null
                    ? ((Number) body.get("operadorID")).intValue() : null;
            dto.pacienteID = body.get("pacienteID") != null
                    ? ((Number) body.get("pacienteID")).intValue() : null;

            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) body.get("cantidadesMateriales");
            if (raw != null) {
                dto.cantidadesMateriales = new HashMap<>();
                for (Map.Entry<String, Object> e : raw.entrySet()) {
                    dto.cantidadesMateriales.put(
                            Integer.parseInt(e.getKey()),
                            ((Number) e.getValue()).doubleValue());
                }
            }

            try {
                service.editarRetroactivo(id, dto);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.get("/api/tratamientos/{id}/materiales", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(service.materialesConNombre(id));
        });

        app.post("/api/tratamientos/{id}/materiales", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var body = ctx.bodyAsClass(Map.class);
            int materialId = ((Number) body.get("materialId")).intValue();
            double cantidad = ((Number) body.get("cantidad")).doubleValue();
            try {
                service.agregarMaterial(id, materialId, cantidad);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/tratamientos/materiales/{mlid}", ctx -> {
            int mlid = Integer.parseInt(ctx.pathParam("mlid"));
            var body = ctx.bodyAsClass(Map.class);
            double cantidad = ((Number) body.get("cantidad")).doubleValue();
            try {
                service.actualizarCantidad(mlid, cantidad);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.delete("/api/tratamientos/materiales/{mlid}", ctx -> {
            int mlid = Integer.parseInt(ctx.pathParam("mlid"));
            service.quitarMaterial(mlid);
            ctx.status(204);
        });
    }
}
