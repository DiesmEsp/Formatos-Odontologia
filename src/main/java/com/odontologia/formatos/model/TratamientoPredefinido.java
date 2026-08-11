package com.odontologia.formatos.model;

public class TratamientoPredefinido {

    private int tratPredID;
    private String nombreTratamiento;
    private Double montoSugerido;
    private int estado;

    public TratamientoPredefinido() {
    }

    public TratamientoPredefinido(int tratPredID, String nombreTratamiento, Double montoSugerido) {
        this.tratPredID = tratPredID;
        this.nombreTratamiento = nombreTratamiento;
        this.montoSugerido = montoSugerido;
    }

    public TratamientoPredefinido(int tratPredID, String nombreTratamiento, Double montoSugerido, int estado) {
        this.tratPredID = tratPredID;
        this.nombreTratamiento = nombreTratamiento;
        this.montoSugerido = montoSugerido;
        this.estado = estado;
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreTratamiento;
    }
}
