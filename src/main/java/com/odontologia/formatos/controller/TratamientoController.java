package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.service.NegocioException;
import com.odontologia.formatos.service.TratamientoService;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;

import java.util.HashMap;
import java.util.Map;

public class TratamientoController {

    private final TratamientoService service = new TratamientoService();
    private final TratamientoMaterialRepository materialRepo = new TratamientoMaterialRepository();

    public void register(Javalin app) {
        app.get("/api/tratamientos", ctx -> {
            ctx.json(service.activos(ControllerUtil.clinicaID(ctx)));
        });

        app.get("/api/tratamientos/cerrados", ctx -> {
            ctx.json(service.cerrados(ControllerUtil.clinicaID(ctx)));
        });

        app.get("/api/tratamientos/cerrados-por-pagar", ctx -> {
            ctx.json(service.cerradosConSaldo(ControllerUtil.clinicaID(ctx)));
        });

        app.get("/api/tratamientos/unidad/{id}", ctx -> {
            int unidadId = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(service.porUnidad(unidadId));
        });

        app.get("/api/tratamientos/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
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
            Integer tratamientoPadreID = body.get("tratamientoPadreID") != null
                    ? ((Number) body.get("tratamientoPadreID")).intValue() : null;

            Map<Integer, Double> materiales = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> rawMateriales = (Map<String, Object>) body.get("materiales");
            if (rawMateriales != null) {
                for (Map.Entry<String, Object> e : rawMateriales.entrySet()) {
                    materiales.put(Integer.parseInt(e.getKey()), ((Number) e.getValue()).doubleValue());
                }
            }

            try {
                int id = service.crear(operadorID, pacienteID, unidadID, fecha, tratPredID, monto, tipo,
                        rawMateriales != null ? materiales : null,
                        ControllerUtil.clinicaID(ctx));
                ctx.status(201).json(Map.of("id", id));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/cerrado", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int operadorID = ((Number) body.get("operadorID")).intValue();
            int pacienteID = ((Number) body.get("pacienteID")).intValue();
            String fecha = (String) body.get("fecha");
            Integer tratPredID = body.get("tratPredID") != null
                    ? ((Number) body.get("tratPredID")).intValue() : null;
            Double monto = body.get("monto") != null
                    ? ((Number) body.get("monto")).doubleValue() : null;
            String tipo = (String) body.get("tipo");

            Map<Integer, Double> materiales = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> rawMateriales = (Map<String, Object>) body.get("materiales");
            if (rawMateriales != null) {
                for (Map.Entry<String, Object> e : rawMateriales.entrySet()) {
                    materiales.put(Integer.parseInt(e.getKey()), ((Number) e.getValue()).doubleValue());
                }
            }

            try {
                int id = service.crearCerrado(operadorID, pacienteID, fecha, tratPredID, monto, tipo,
                        materiales, ControllerUtil.clinicaID(ctx));
                ctx.status(201).json(Map.of("id", id));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/avances", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            String fecha = (String) body.get("fecha");
            Integer unidadID = body.get("unidadID") != null
                    ? ((Number) body.get("unidadID")).intValue() : null;
            Double pago = body.get("pago") != null
                    ? ((Number) body.get("pago")).doubleValue() : null;

            Map<Integer, Double> materiales = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> rawMateriales = (Map<String, Object>) body.get("materiales");
            if (rawMateriales != null) {
                for (Map.Entry<String, Object> e : rawMateriales.entrySet()) {
                    materiales.put(Integer.parseInt(e.getKey()), ((Number) e.getValue()).doubleValue());
                }
            }

            try {
                int avanceID = service.agregarAvance(id, fecha, unidadID,
                        rawMateriales != null ? materiales : null, pago);
                ctx.status(201).json(Map.of("id", avanceID));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/avances/{avanceID}/anular", ctx -> {
            int avanceID = ControllerUtil.parseIdPathParam(ctx, "avanceID");
            var body = ctx.bodyAsClass(Map.class);
            String motivo = (String) body.get("motivo");
            try {
                service.anularAvance(avanceID, motivo);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/avances/{avanceID}/terminar", ctx -> {
            int avanceID = ControllerUtil.parseIdPathParam(ctx, "avanceID");
            try {
                service.terminarAvance(avanceID);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/cerrar", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            try {
                service.cerrar(id);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/anular", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
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
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            try {
                service.reabrir(id);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/pago", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            double abono = ((Number) body.get("abono")).doubleValue();
            String fecha = (String) body.get("fecha");
            try {
                service.registrarPago(id, abono, fecha);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/tratamientos/{id}/cambiar-tipo", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            String tipo = (String) body.get("tipo");
            try {
                service.cambiarTipo(id, tipo);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.get("/api/tratamientos/{id}/pagos", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(service.pagosDe(id));
        });

        app.get("/api/tratamientos/{id}/avances", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(service.listarAvances(id));
        });

        app.get("/api/tratamientos/{id}/consolidado", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(service.obtenerConsolidado(id));
        });

        app.put("/api/tratamientos/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);

            TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
            dto.monto = body.get("monto") != null
                    ? ((Number) body.get("monto")).doubleValue() : null;
            dto.fecha = (String) body.get("fecha");
            dto.nombreTratamiento = (String) body.get("nombreTratamiento");
            dto.operadorID = body.get("operadorID") != null
                    ? ((Number) body.get("operadorID")).intValue() : null;
            dto.pacienteID = body.get("pacienteID") != null
                    ? ((Number) body.get("pacienteID")).intValue() : null;

            try {
                service.editarEnCurso(id, dto);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/tratamientos/pagos/{pagoID}", ctx -> {
            int pagoID = ControllerUtil.parseIdPathParam(ctx, "pagoID");
            var body = ctx.bodyAsClass(Map.class);
            double monto = ((Number) body.get("monto")).doubleValue();
            String fecha = (String) body.get("fecha");
            try {
                service.editarPago(pagoID, monto, fecha);
                ctx.json(Map.of("ok", true));
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.delete("/api/tratamientos/pagos/{pagoID}", ctx -> {
            int pagoID = ControllerUtil.parseIdPathParam(ctx, "pagoID");
            try {
                service.eliminarPago(pagoID);
                ctx.status(204);
            } catch (NegocioException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.put("/api/tratamientos/{id}/editar", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);

            TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
            dto.tipo = (String) body.get("tipo");
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
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(service.materialesConNombre(id));
        });

        app.post("/api/tratamientos/{id}/materiales", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
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
            int mlid = ControllerUtil.parseIdPathParam(ctx, "mlid");
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
            int mlid = ControllerUtil.parseIdPathParam(ctx, "mlid");
            service.quitarMaterial(mlid);
            ctx.status(204);
        });
    }
}
