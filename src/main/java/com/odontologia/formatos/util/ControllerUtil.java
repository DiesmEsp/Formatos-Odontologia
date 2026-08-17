package com.odontologia.formatos.util;

import io.javalin.http.Context;

import java.util.Map;

public final class ControllerUtil {

    private ControllerUtil() {}

    public static int parseIdPathParam(Context ctx, String paramName) {
        String val = ctx.pathParam(paramName);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new ValidationException("El parámetro '" + paramName + "' debe ser un número válido.");
        }
    }

    public static int requireIntBodyField(Context ctx, String fieldName) {
        Object val = ctx.bodyAsClass(Map.class).get(fieldName);
        if (val == null) {
            throw new ValidationException("El campo '" + fieldName + "' es obligatorio.");
        }
        return ((Number) val).intValue();
    }

    public static String requireStringBodyField(Context ctx, String fieldName) {
        Object val = ctx.bodyAsClass(Map.class).get(fieldName);
        if (val == null || ((String) val).isBlank()) {
            throw new ValidationException("El campo '" + fieldName + "' es obligatorio.");
        }
        return (String) val;
    }

    public static String requireStringBodyField(Context ctx, String fieldName, String defaultValue) {
        Object val = ctx.bodyAsClass(Map.class).get(fieldName);
        if (val == null || ((String) val).isBlank()) return defaultValue;
        return (String) val;
    }

    public static int clinicaID(Context ctx) {
        Object val = ctx.attribute("clinicaID");
        if (val == null) {
            throw new ValidationException("Falta el contexto de clínica (header X-Clinica-Nombre).");
        }
        return (int) val;
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
