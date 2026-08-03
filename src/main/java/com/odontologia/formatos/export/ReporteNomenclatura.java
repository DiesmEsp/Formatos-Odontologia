package com.odontologia.formatos.export;

/**
 * Nomenclatura dinámica de archivos de reportes (RNF-2.3.2).
 * Formato: {Materiales,Ingresos,Docente,Especialista,Anual}_{Mes}_{Año}.xlsx
 * Ejemplo: Materiales_Octubre_2024.xlsx, Anual_2024.xlsx
 */
public final class ReporteNomenclatura {

    public static final String TIPO_MATERIALES = "Materiales";
    public static final String TIPO_INGRESOS = "Ingresos";
    public static final String TIPO_DOCENTE = "Docente";
    public static final String TIPO_ESPECIALISTA = "Especialista";
    public static final String TIPO_ANUAL = "Anual";

    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private static final String[] MESES_CORTOS = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private ReporteNomenclatura() {
    }

    public static String nombre(String tipo, int anio, int mes) {
        return tipo + "_" + mes(mes) + "_" + anio + ".xlsx";
    }

    public static String nombreAnual(int anio) {
        return TIPO_ANUAL + "_" + anio + ".xlsx";
    }

    public static String mes(int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        return MESES[mes - 1];
    }

    public static String mesCorto(int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        return MESES_CORTOS[mes - 1];
    }
}
