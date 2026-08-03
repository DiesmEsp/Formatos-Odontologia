package com.odontologia.formatos.model;

public class TratamientoPredefinido {

    private int tratPredID;
    private String nombreTratamiento;
    private Double montoSugerido;

    public TratamientoPredefinido() {
    }

    public TratamientoPredefinido(int tratPredID, String nombreTratamiento, Double montoSugerido) {
        this.tratPredID = tratPredID;
        this.nombreTratamiento = nombreTratamiento;
        this.montoSugerido = montoSugerido;
    }

    public int getTratPredID() {
        return tratPredID;
    }

    public void setTratPredID(int tratPredID) {
        this.tratPredID = tratPredID;
    }

    public String getNombreTratamiento() {
        return nombreTratamiento;
    }

    public void setNombreTratamiento(String nombreTratamiento) {
        this.nombreTratamiento = nombreTratamiento;
    }

    public Double getMontoSugerido() {
        return montoSugerido;
    }

    public void setMontoSugerido(Double montoSugerido) {
        this.montoSugerido = montoSugerido;
    }

    @Override
    public String toString() {
        return nombreTratamiento;
    }
}
