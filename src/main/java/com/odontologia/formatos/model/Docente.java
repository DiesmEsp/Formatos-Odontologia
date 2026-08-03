package com.odontologia.formatos.model;

public class Docente {

    private int docenteID;
    private String nombres;
    private String apellidos;
    private int estado;

    public Docente() {
    }

    public Docente(int docenteID, String nombres, String apellidos, int estado) {
        this.docenteID = docenteID;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
    }

    public int getDocenteID() {
        return docenteID;
    }

    public void setDocenteID(int docenteID) {
        this.docenteID = docenteID;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombres + " " + apellidos;
    }
}
