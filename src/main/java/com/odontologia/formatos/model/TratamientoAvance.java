package com.odontologia.formatos.model;

public class TratamientoAvance {

    private int avanceID;
    private int tratamientoID;
    private String fecha;
    private Integer unidadID;
    private String estado;
    private String timestamp;

    public TratamientoAvance() {
    }

    public int getAvanceID() {
        return avanceID;
    }

    public void setAvanceID(int avanceID) {
        this.avanceID = avanceID;
    }

    public int getTratamientoID() {
        return tratamientoID;
    }

    public void setTratamientoID(int tratamientoID) {
        this.tratamientoID = tratamientoID;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Integer getUnidadID() {
        return unidadID;
    }

    public void setUnidadID(Integer unidadID) {
        this.unidadID = unidadID;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
