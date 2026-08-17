package com.odontologia.formatos.model;

public class Unidad {

    private int unidadID;
    private int unidadNro;
    private int clinicaID = 1;

    public Unidad() {
    }

    public Unidad(int unidadID, int unidadNro) {
        this(unidadID, unidadNro, 1);
    }

    public Unidad(int unidadID, int unidadNro, int clinicaID) {
        this.unidadID = unidadID;
        this.unidadNro = unidadNro;
        this.clinicaID = clinicaID;
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

    public int getClinicaID() {
        return clinicaID;
    }

    public void setClinicaID(int clinicaID) {
        this.clinicaID = clinicaID;
    }

    @Override
    public String toString() {
        return "Unidad " + unidadNro;
    }
}
