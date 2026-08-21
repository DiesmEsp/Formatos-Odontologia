package com.odontologia.formatos.model;

public class ConsumoClinica {

    private int consumoID;
    private String fecha;
    private int materialID;
    private String nombreMaterial;
    private String unidad;
    private double cantidad;
    private int clinicaID;

    public ConsumoClinica() {
    }

    public ConsumoClinica(int consumoID, String fecha, int materialID, String nombreMaterial,
                          String unidad, double cantidad, int clinicaID) {
        this.consumoID = consumoID;
        this.fecha = fecha;
        this.materialID = materialID;
        this.nombreMaterial = nombreMaterial;
        this.unidad = unidad;
        this.cantidad = cantidad;
        this.clinicaID = clinicaID;
    }

    public int getConsumoID() {
        return consumoID;
    }

    public void setConsumoID(int consumoID) {
        this.consumoID = consumoID;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

    public String getNombreMaterial() {
        return nombreMaterial;
    }

    public void setNombreMaterial(String nombreMaterial) {
        this.nombreMaterial = nombreMaterial;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public int getClinicaID() {
        return clinicaID;
    }

    public void setClinicaID(int clinicaID) {
        this.clinicaID = clinicaID;
    }
}
