package com.odontologia.formatos.model;

public class Materiales {

    private int materialID;
    private String nombre;
    private String unidad;
    private int estado;

    public Materiales() {
    }

    public Materiales(int materialID, String nombre, String unidad, int estado) {
        this.materialID = materialID;
        this.nombre = nombre;
        this.unidad = unidad;
        this.estado = estado;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
