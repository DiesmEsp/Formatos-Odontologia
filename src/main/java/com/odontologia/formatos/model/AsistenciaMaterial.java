package com.odontologia.formatos.model;

public class AsistenciaMaterial {

    private int matAsistenciaID;
    private int asistenciaID;
    private int materialID;
    private double cantidad;

    public AsistenciaMaterial() {
    }

    public AsistenciaMaterial(int matAsistenciaID, int asistenciaID, int materialID, double cantidad) {
        this.matAsistenciaID = matAsistenciaID;
        this.asistenciaID = asistenciaID;
        this.materialID = materialID;
        this.cantidad = cantidad;
    }

    public int getMatAsistenciaID() {
        return matAsistenciaID;
    }

    public void setMatAsistenciaID(int matAsistenciaID) {
        this.matAsistenciaID = matAsistenciaID;
    }

    public int getAsistenciaID() {
        return asistenciaID;
    }

    public void setAsistenciaID(int asistenciaID) {
        this.asistenciaID = asistenciaID;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
}
