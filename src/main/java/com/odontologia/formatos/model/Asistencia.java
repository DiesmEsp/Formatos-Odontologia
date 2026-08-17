package com.odontologia.formatos.model;

public class Asistencia {

    private int asistenciaID;
    private int docenteID;
    private String fecha;
    private String estado;
    private String horaEntrada;
    private String horaSalida;
    private int clinicaID = 1;

    public Asistencia() {
    }

    public Asistencia(int asistenciaID, int docenteID, String fecha, String estado) {
        this(asistenciaID, docenteID, fecha, estado, null, null, 1);
    }

    public Asistencia(int asistenciaID, int docenteID, String fecha, String estado,
                      String horaEntrada, String horaSalida) {
        this(asistenciaID, docenteID, fecha, estado, horaEntrada, horaSalida, 1);
    }

    public Asistencia(int asistenciaID, int docenteID, String fecha, String estado,
                      String horaEntrada, String horaSalida, int clinicaID) {
        this.asistenciaID = asistenciaID;
        this.docenteID = docenteID;
        this.fecha = fecha;
        this.estado = estado;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.clinicaID = clinicaID;
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

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public int getClinicaID() {
        return clinicaID;
    }

    public void setClinicaID(int clinicaID) {
        this.clinicaID = clinicaID;
    }
}
