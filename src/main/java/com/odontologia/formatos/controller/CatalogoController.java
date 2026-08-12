package com.odontologia.formatos.controller;

import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.TratamientoPredefinido;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;
import com.odontologia.formatos.model.UnidadConversion;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.repository.UnidadConversionRepository;
import com.odontologia.formatos.service.MaterialService;
import com.odontologia.formatos.service.DocenteService;
import com.odontologia.formatos.service.OperadorService;
import com.odontologia.formatos.service.PacienteService;
import com.odontologia.formatos.service.TratamientoPredefinidoService;
import com.odontologia.formatos.service.UnidadConversionService;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;

import java.util.List;
import java.util.Map;

public class CatalogoController {

    private final OperadorRepository operadorRepo = new OperadorRepository();
    private final DocenteRepository docenteRepo = new DocenteRepository();
    private final PacienteRepository pacienteRepo = new PacienteRepository();
    private final MaterialRepository materialRepo = new MaterialRepository();
    private final MaterialService materialService = new MaterialService();
    private final TratamientoPredefinidoRepository tratPredRepo = new TratamientoPredefinidoRepository();
    private final TratamientoPredefinidoService tratPredService = new TratamientoPredefinidoService();
    private final UnidadConversionRepository conversionRepo = new UnidadConversionRepository();
    private final UnidadConversionService conversionService = new UnidadConversionService();
    private final OperadorService operadorService = new OperadorService();
    private final DocenteService docenteService = new DocenteService();
    private final PacienteService pacienteService = new PacienteService();

    public void register(Javalin app) {
        registerOperadores(app);
        registerDocentes(app);
        registerPacientes(app);
        registerMateriales(app);
        registerTratamientosPred(app);
        registerConversiones(app);
    }

    private void registerOperadores(Javalin app) {
        app.get("/api/catalogos/operadores", ctx -> {
            String q = ctx.queryParam("q");
            if (q != null && !q.isBlank()) {
                ctx.json(operadorRepo.buscarPorTexto(q));
            } else {
                ctx.json(operadorRepo.findAll());
            }
        });

        app.post("/api/catalogos/operadores", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int id = operadorService.crear(
                    (String) body.get("nombres"),
                    (String) body.get("apellidos"),
                    (String) body.get("dni"),
                    (String) body.get("grado"),
                    (String) body.get("tipo"),
                    ((Number) body.get("periodo")).intValue());
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/catalogos/operadores/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            Operador o = new Operador();
            o.setOperadorID(id);
            o.setNombres((String) body.getOrDefault("nombres", ""));
            o.setApellidos((String) body.getOrDefault("apellidos", ""));
            o.setDni((String) body.get("dni"));
            o.setGrado((String) body.getOrDefault("grado", ""));
            o.setTipo((String) body.getOrDefault("tipo", ""));
            o.setPeriodo(body.get("periodo") != null ? ((Number) body.get("periodo")).intValue() : 0);
            o.setEstado(body.containsKey("estado") ? ((Number) body.get("estado")).intValue() : operadorRepo.findById(id).getEstado());
            operadorService.actualizar(o);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/catalogos/operadores/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            operadorService.eliminar(id);
            ctx.status(204);
        });
    }

    private void registerDocentes(Javalin app) {
        app.get("/api/catalogos/docentes", ctx -> {
            String q = ctx.queryParam("q");
            if (q != null && !q.isBlank()) {
                ctx.json(docenteRepo.buscarPorTexto(q));
            } else {
                ctx.json(docenteRepo.findAll());
            }
        });

        app.post("/api/catalogos/docentes", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int id = docenteService.crear(
                    (String) body.get("nombres"),
                    (String) body.get("apellidos"),
                    (String) body.get("telefono"));
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/catalogos/docentes/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            Docente d = new Docente();
            d.setDocenteID(id);
            d.setNombres((String) body.getOrDefault("nombres", ""));
            d.setApellidos((String) body.getOrDefault("apellidos", ""));
            d.setTelefono((String) body.getOrDefault("telefono", ""));
            d.setEstado(body.containsKey("estado") ? ((Number) body.get("estado")).intValue() : docenteRepo.findById(id).getEstado());
            docenteService.actualizar(d);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/catalogos/docentes/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            docenteService.eliminar(id);
            ctx.status(204);
        });
    }

    private void registerPacientes(Javalin app) {
        app.get("/api/catalogos/pacientes", ctx -> {
            String q = ctx.queryParam("q");
            if (q != null && !q.isBlank()) {
                ctx.json(pacienteRepo.buscarPorTexto(q));
            } else {
                ctx.json(pacienteRepo.findAll());
            }
        });

        app.post("/api/catalogos/pacientes", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int id = pacienteService.crear(
                    (String) body.get("nombres"),
                    (String) body.get("apellidos"));
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/catalogos/pacientes/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            Paciente p = new Paciente();
            p.setPacienteID(id);
            p.setNombres((String) body.getOrDefault("nombres", ""));
            p.setApellidos((String) body.getOrDefault("apellidos", ""));
            p.setEstado(body.containsKey("estado") ? ((Number) body.get("estado")).intValue() : pacienteRepo.findById(id).getEstado());
            pacienteService.actualizar(p);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/catalogos/pacientes/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            pacienteService.eliminar(id);
            ctx.status(204);
        });
    }

    private void registerMateriales(Javalin app) {
        app.get("/api/catalogos/materiales", ctx -> {
            String q = ctx.queryParam("q");
            if (q != null && !q.isBlank()) {
                ctx.json(materialRepo.buscarPorTexto(q));
            } else {
                ctx.json(materialRepo.findAll());
            }
        });

        app.get("/api/catalogos/materiales/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            Materiales m = materialService.buscarPorId(id);
            if (m == null) {
                ctx.status(404).json(Map.of("error", "Material no encontrado"));
            } else {
                ctx.json(m);
            }
        });

        app.post("/api/catalogos/materiales", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int id = materialService.crear(
                    (String) body.get("nombre"),
                    (String) body.get("unidad"));
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/catalogos/materiales/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            Materiales m = new Materiales();
            m.setMaterialID(id);
            m.setNombre((String) body.getOrDefault("nombre", ""));
            m.setUnidad((String) body.getOrDefault("unidad", ""));
            m.setEstado(body.containsKey("estado") ? ((Number) body.get("estado")).intValue() : materialService.buscarPorId(id).getEstado());
            materialService.actualizar(m);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/catalogos/materiales/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            materialService.eliminar(id);
            ctx.status(204);
        });
    }

    private void registerTratamientosPred(Javalin app) {
        app.get("/api/catalogos/tratamientos-pred", ctx -> {
            String q = ctx.queryParam("q");
            if (q != null && !q.isBlank()) {
                ctx.json(tratPredRepo.buscarPorTexto(q));
            } else {
                ctx.json(tratPredRepo.findAll());
            }
        });

        app.post("/api/catalogos/tratamientos-pred", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            String nombre = (String) body.get("nombreTratamiento");
            Double montoSugerido = body.get("montoSugerido") != null
                    ? ((Number) body.get("montoSugerido")).doubleValue() : null;
            int id = tratPredService.crear(nombre, montoSugerido);
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/catalogos/tratamientos-pred/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            TratamientoPredefinido tp = new TratamientoPredefinido();
            tp.setTratPredID(id);
            tp.setNombreTratamiento((String) body.getOrDefault("nombreTratamiento", ""));
            tp.setMontoSugerido(body.get("montoSugerido") != null ? ((Number) body.get("montoSugerido")).doubleValue() : 0.0);
            tp.setEstado(body.containsKey("estado") ? ((Number) body.get("estado")).intValue() : tratPredRepo.findById(id).getEstado());
            tratPredService.actualizar(tp);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/catalogos/tratamientos-pred/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            tratPredService.eliminar(id);
            ctx.status(204);
        });

        app.get("/api/catalogos/tratamientos-pred/{id}/materiales", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(tratPredService.materiales(id));
        });

        app.put("/api/catalogos/tratamientos-pred/{id}/materiales", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("materiales");
            List<TratamientoPredefinidoMaterial> list = raw.stream().map(m -> {
                TratamientoPredefinidoMaterial item = new TratamientoPredefinidoMaterial();
                item.setMaterialID(((Number) m.get("materialID")).intValue());
                item.setCantidad(((Number) m.get("cantidad")).doubleValue());
                return item;
            }).toList();
            tratPredService.guardarMateriales(id, list);
            ctx.json(Map.of("ok", true));
        });
    }

    private void registerConversiones(Javalin app) {
        app.get("/api/catalogos/conversiones", ctx -> {
            ctx.json(conversionRepo.findAll());
        });

        app.get("/api/catalogos/conversiones/material/{id}", ctx -> {
            int materialId = ControllerUtil.parseIdPathParam(ctx, "id");
            ctx.json(conversionService.buscarPorMaterial(materialId));
        });

        app.post("/api/catalogos/conversiones", ctx -> {
            var body = ctx.bodyAsClass(Map.class);
            int id = conversionService.crear(
                    ((Number) body.get("materialID")).intValue(),
                    (String) body.get("unidadBase"),
                    (String) body.get("unidadEmpaque"),
                    ((Number) body.get("factor")).doubleValue());
            ctx.status(201).json(Map.of("id", id));
        });

        app.put("/api/catalogos/conversiones/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            var body = ctx.bodyAsClass(UnidadConversion.class);
            body.setConversionID(id);
            conversionService.actualizar(body);
            ctx.json(Map.of("ok", true));
        });

        app.delete("/api/catalogos/conversiones/{id}", ctx -> {
            int id = ControllerUtil.parseIdPathParam(ctx, "id");
            conversionService.eliminar(id);
            ctx.status(204);
        });
    }
}
