package com.odontologia.formatos.model;

public class TratamientoPredefinidoMaterial {

    private int materialListPredID;
    private int tratPredID;
    private int materialID;
    private double cantidad;

    public TratamientoPredefinidoMaterial() {
    }

    public TratamientoPredefinidoMaterial(int materialListPredID, int tratPredID, int materialID, double cantidad) {
        this.materialListPredID = materialListPredID;
        this.tratPredID = tratPredID;
        this.materialID = materialID;
        this.cantidad = cantidad;
    }

    public int getMaterialListPredID() {
        return materialListPredID;
    }

    public void setMaterialListPredID(int materialListPredID) {
        this.materialListPredID = materialListPredID;
    }

    public int getTratPredID() {
        return tratPredID;
    }

    public void setTratPredID(int tratPredID) {
        this.tratPredID = tratPredID;
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
