package com.odontologia.formatos.model;

public class Asistencia {

    private int asistenciaID;
    private int docenteID;
    private String fecha;
    private String estado;

    public Asistencia() {
    }

    public Asistencia(int asistenciaID, int docenteID, String fecha, String estado) {
        this.asistenciaID = asistenciaID;
        this.docenteID = docenteID;
        this.fecha = fecha;
        this.estado = estado;
    }

    public int getAsistenciaID() {
        return asistenciaID;
    }

    public void setAsistenciaID(int asistenciaID) {
        this.asistenciaID = asistenciaID;
    }

    public int getDocenteID() {
        return docenteID;
    }

    public void setDocenteID(int docenteID) {
        this.docenteID = docenteID;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
