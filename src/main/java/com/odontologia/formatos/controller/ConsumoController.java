package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.ConsumoClinica;
import com.odontologia.formatos.service.ConsumoService;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsumoController {

    private final ConsumoService consumoService = new ConsumoService();

    public void register(Javalin app) {
        app.get("/api/consumos", this::listar);
        app.post("/api/consumos", this::crearLote);
        app.put("/api/consumos/{id}", this::actualizar);
        app.delete("/api/consumos/{id}", this::eliminar);
    }

    private void listar(Context ctx) throws Exception {
        int anio = parseQueryParam(ctx, "anio");
        int mes = parseQueryParam(ctx, "mes");
        ctx.json(consumoService.listarPorMes(anio, mes, ControllerUtil.clinicaID(ctx)));
    }

    @SuppressWarnings("unchecked")
    private void crearLote(Context ctx) throws Exception {
        var body = ctx.bodyAsClass(Map.class);
        Object itemsObj = body.get("items");
        if (!(itemsObj instanceof List)) {
            throw new ControllerUtil.ValidationException("El campo 'items' es obligatorio.");
        }
        List<ConsumoClinica> items = new ArrayList<>();
        for (Object obj : (List<Object>) itemsObj) {
            Map<String, Object> item = (Map<String, Object>) obj;
            items.add(mapToConsumo(item));
        }
        List<Integer> ids = consumoService.crearLote(items, ControllerUtil.clinicaID(ctx));
        ctx.status(201).json(Map.of("ids", ids));
    }

    private void actualizar(Context ctx) throws Exception {
        int id = ControllerUtil.parseIdPathParam(ctx, "id");
        var body = ctx.bodyAsClass(Map.class);
        ConsumoClinica item = mapToConsumo(body);
        consumoService.actualizar(id, item.getFecha(), item.getMaterialID(),
                item.getCantidad(), ControllerUtil.clinicaID(ctx));
        ctx.json(Map.of("ok", true));
    }

    private void eliminar(Context ctx) throws Exception {
        int id = ControllerUtil.parseIdPathParam(ctx, "id");
        consumoService.eliminar(id, ControllerUtil.clinicaID(ctx));
        ctx.json(Map.of("ok", true));
    }

    private ConsumoClinica mapToConsumo(Map<String, Object> item) {
        Object fecha = item.get("fecha");
        Object materialId = item.get("materialId");
        Object cantidad = item.get("cantidad");
        if (fecha == null || materialId == null || cantidad == null) {
            throw new ControllerUtil.ValidationException(
                    "Los campos 'fecha', 'materialId' y 'cantidad' son obligatorios.");
        }
        ConsumoClinica consumo = new ConsumoClinica();
        consumo.setFecha(fecha.toString());
        consumo.setMaterialID(((Number) materialId).intValue());
        consumo.setCantidad(((Number) cantidad).doubleValue());
        return consumo;
    }

    private int parseQueryParam(Context ctx, String name) {
        String val = ctx.queryParam(name);
        if (val == null || val.isBlank()) {
            throw new ControllerUtil.ValidationException(
                    "El parámetro '" + name + "' es obligatorio.");
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            throw new ControllerUtil.ValidationException(
                    "El parámetro '" + name + "' debe ser un número válido.");
        }
    }
}
