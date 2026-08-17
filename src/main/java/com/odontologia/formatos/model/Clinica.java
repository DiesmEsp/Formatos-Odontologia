package com.odontologia.formatos.model;

public class Clinica {

    private int clinicaID;
    private String nombre;
    private String grupo;
    private int estado;

    public Clinica() {
    }

    public Clinica(int clinicaID, String nombre, String grupo, int estado) {
        this.clinicaID = clinicaID;
        this.nombre = nombre;
        this.grupo = grupo;
        this.estado = estado;
    }

    public int getClinicaID() {
        return clinicaID;
    }

    public void setClinicaID(int clinicaID) {
        this.clinicaID = clinicaID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
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