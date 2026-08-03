package com.odontologia.formatos.model;

public class Unidad {

    private int unidadID;
    private int unidadNro;

    public Unidad() {
    }

    public Unidad(int unidadID, int unidadNro) {
        this.unidadID = unidadID;
        this.unidadNro = unidadNro;
    }

    public int getUnidadID() {
        return unidadID;
    }

    public void setUnidadID(int unidadID) {
        this.unidadID = unidadID;
    }

    public int getUnidadNro() {
        return unidadNro;
    }

    public void setUnidadNro(int unidadNro) {
        this.unidadNro = unidadNro;
    }

    @Override
    public String toString() {
        return "Unidad " + unidadNro;
    }
}
