package com.odontologia.formatos.model;

public class RegistroAnulacion {

    private int anulacionID;
    private String tablaAfectada;
    private int idRegistroAnulado;
    private String motivo;
    private String usuario;
    private String timestamp;

    public RegistroAnulacion() {
    }

    public RegistroAnulacion(int anulacionID, String tablaAfectada, int idRegistroAnulado, String motivo, String usuario, String timestamp) {
        this.anulacionID = anulacionID;
        this.tablaAfectada = tablaAfectada;
        this.idRegistroAnulado = idRegistroAnulado;
        this.motivo = motivo;
        this.usuario = usuario;
        this.timestamp = timestamp;
    }

    public int getAnulacionID() {
        return anulacionID;
    }

    public void setAnulacionID(int anulacionID) {
        this.anulacionID = anulacionID;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public int getIdRegistroAnulado() {
        return idRegistroAnulado;
    }

    public void setIdRegistroAnulado(int idRegistroAnulado) {
        this.idRegistroAnulado = idRegistroAnulado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
