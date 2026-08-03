package com.odontologia.formatos.model;

public class TratamientoMaterial {

    private int materialesListID;
    private int tratamientoID;
    private int materialID;
    private double cantidad;

    public TratamientoMaterial() {
    }

    public TratamientoMaterial(int materialesListID, int tratamientoID, int materialID, double cantidad) {
        this.materialesListID = materialesListID;
        this.tratamientoID = tratamientoID;
        this.materialID = materialID;
        this.cantidad = cantidad;
    }

    public int getMaterialesListID() {
        return materialesListID;
    }

    public void setMaterialesListID(int materialesListID) {
        this.materialesListID = materialesListID;
    }

    public int getTratamientoID() {
        return tratamientoID;
    }

    public void setTratamientoID(int tratamientoID) {
        this.tratamientoID = tratamientoID;
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
