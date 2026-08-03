package com.odontologia.formatos.export;

/**
 * Error al generar o guardar un reporte. El mensaje ya viene traducido
 * para el usuario (ExportErrorUtil) indicando causa y sugerencia.
 */
public class ReporteException extends Exception {

    public ReporteException(String mensaje) {
        super(mensaje);
    }
}
