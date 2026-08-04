package com.odontologia.formatos.export;

/**
 * Nomenclatura dinámica de archivos de reportes (RNF-2.3.2).
 * <p>
 * Formato mensual:   {Tipo}_{Mes}_{Anio}.xlsx    → Materiales_Octubre_2024.xlsx
 * Formato semestral: {Tipo}_{Inicio}_{Fin}_{Anio}.xlsx → Materiales_Ene_Jun_2024.xlsx
 * Formato anual:     {Tipo}_{Anio}.xlsx           → Materiales_2024.xlsx
 */
public final class ReporteNomenclatura {

    public static final String TIPO_MATERIALES = "Materiales";
    public static final String TIPO_ECONOMICO = "Economico";

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

    public static String nombreMateriales(int anio, int mes) {
        return nombreMensual(TIPO_MATERIALES, anio, mes);
    }

    public static String nombreMateriales(int anio, int mesInicio, int mesFin) {
        return nombreRango(TIPO_MATERIALES, anio, mesInicio, mesFin);
    }

    public static String nombreEconomico(int anio, int mes) {
        return nombreMensual(TIPO_ECONOMICO, anio, mes);
    }

    public static String nombreEconomico(int anio, int mesInicio, int mesFin) {
        return nombreRango(TIPO_ECONOMICO, anio, mesInicio, mesFin);
    }

    private static String nombreMensual(String tipo, int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        return tipo + "_" + MESES[mes - 1] + "_" + anio + ".xlsx";
    }

    private static String nombreRango(String tipo, int anio, int mesInicio, int mesFin) {
        if (mesInicio < 1 || mesInicio > 12 || mesFin < 1 || mesFin > 12) {
            throw new IllegalArgumentException("Los meses deben estar entre 1 y 12.");
        }
        if (mesInicio > mesFin) {
            throw new IllegalArgumentException("mesInicio debe ser <= mesFin.");
        }
        if (mesInicio == 1 && mesFin == 12) {
            return tipo + "_" + anio + ".xlsx";
        }
        return tipo + "_" + MESES_CORTOS[mesInicio - 1] + "_" + MESES_CORTOS[mesFin - 1] + "_" + anio + ".xlsx";
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
