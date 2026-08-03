package com.odontologia.formatos.util;

import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

/**
 * Traduce errores de exportación a mensajes claros con sugerencia (checklist 8.3).
 */
public final class ExportErrorUtil {

    private ExportErrorUtil() {
    }

    public static String mensajeArchivoOcupado(String archivo) {
        return "No se pudo guardar el archivo '" + archivo
                + "' porque está abierto en otro programa. Ciérrelo e inténtelo de nuevo.";
    }

    public static String mensajeSinPermisos(String carpeta) {
        return "No se tienen permisos para escribir en la carpeta '" + carpeta
                + "'. Elija otra ubicación para guardar el reporte.";
    }

    public static String mensaje(Throwable e, String archivo) {
        if (e instanceof AccessDeniedException) {
            return mensajeSinPermisos(archivo);
        }
        if (e instanceof NoSuchFileException || esArchivoEnUso(e)) {
            return mensajeArchivoOcupado(archivo);
        }
        return "Ocurrió un error al generar el reporte. Verifique la ubicación e inténtelo de nuevo.";
    }

    private static boolean esArchivoEnUso(Throwable e) {
        String msg = e.getMessage();
        return msg != null && (msg.toLowerCase().contains("being used by another process")
                || msg.toLowerCase().contains("sharing violation")
                || msg.toLowerCase().contains("permission denied"));
    }
}
