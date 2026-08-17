package com.odontologia.formatos.model;

public class Tratamiento {

    private int tratamientoID;
    private int operadorID;
    private int pacienteID;
    private Integer unidadID;
    private String fecha;
    private String nombreTratamiento;
    private double monto;
    private String tipo;
    private String estadoPago;
    private double montoPagado;
    private String estado;
    private String cerradoEn;
    private Integer tratamientoPadreID;
    private Double montoAnterior;
    private int clinicaID = 1;

    public Tratamiento() {
    }

    public int getTratamientoID() {
        return tratamientoID;
    }

    public void setTratamientoID(int tratamientoID) {
        this.tratamientoID = tratamientoID;
    }

    public int getOperadorID() {
        return operadorID;
    }

    public void setOperadorID(int operadorID) {
        this.operadorID = operadorID;
    }

    public int getPacienteID() {
        return pacienteID;
    }

    public void setPacienteID(int pacienteID) {
        this.pacienteID = pacienteID;
    }

    public Integer getUnidadID() {
        return unidadID;
    }

    public void setUnidadID(Integer unidadID) {
        this.unidadID = unidadID;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombreTratamiento() {
        return nombreTratamiento;
    }

    public void setNombreTratamiento(String nombreTratamiento) {
        this.nombreTratamiento = nombreTratamiento;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCerradoEn() {
        return cerradoEn;
    }

    public void setCerradoEn(String cerradoEn) {
        this.cerradoEn = cerradoEn;
    }

    public Integer getTratamientoPadreID() {
        return tratamientoPadreID;
    }

    public void setTratamientoPadreID(Integer tratamientoPadreID) {
        this.tratamientoPadreID = tratamientoPadreID;
    }

    public Double getMontoAnterior() {
        return montoAnterior;
    }

    public void setMontoAnterior(Double montoAnterior) {
        this.montoAnterior = montoAnterior;
    }

    public int getClinicaID() {
        return clinicaID;
    }

    public void setClinicaID(int clinicaID) {
        this.clinicaID = clinicaID;
    }
}
