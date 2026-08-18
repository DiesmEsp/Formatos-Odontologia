package com.odontologia.formatos.model;

public class MaterialAvance {

    private int materialesListAvanceID;
    private int avanceID;
    private int materialID;
    private double cantidad;

    public MaterialAvance() {
    }

    public MaterialAvance(int materialesListAvanceID, int avanceID, int materialID, double cantidad) {
        this.materialesListAvanceID = materialesListAvanceID;
        this.avanceID = avanceID;
        this.materialID = materialID;
        this.cantidad = cantidad;
    }

    public int getMaterialesListAvanceID() {
        return materialesListAvanceID;
    }

    public void setMaterialesListAvanceID(int materialesListAvanceID) {
        this.materialesListAvanceID = materialesListAvanceID;
    }

    public int getAvanceID() {
        return avanceID;
    }

    public void setAvanceID(int avanceID) {
        this.avanceID = avanceID;
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
