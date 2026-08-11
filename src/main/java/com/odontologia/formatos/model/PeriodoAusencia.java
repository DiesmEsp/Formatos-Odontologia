package com.odontologia.formatos.model;

public class PeriodoAusencia {

    private int ausenciaID;
    private int asistenciaID;
    private String horaInicio;
    private String horaFin;
    private String motivo;

    public PeriodoAusencia() {
    }

    public PeriodoAusencia(int ausenciaID, int asistenciaID, String horaInicio,
                           String horaFin, String motivo) {
        this.ausenciaID = ausenciaID;
        this.asistenciaID = asistenciaID;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.motivo = motivo;
    }

    public int getAusenciaID() {
        return ausenciaID;
    }

    public void setAusenciaID(int ausenciaID) {
        this.ausenciaID = ausenciaID;
    }

    public int getAsistenciaID() {
        return asistenciaID;
    }

    public void setAsistenciaID(int asistenciaID) {
        this.asistenciaID = asistenciaID;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
