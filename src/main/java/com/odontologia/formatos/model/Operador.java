package com.odontologia.formatos.model;

public class Operador {

    private int operadorID;
    private String nombres;
    private String apellidos;
    private String grado;
    private String tipo;
    private int periodo;
    private int estado;

    public Operador() {
    }

    public Operador(int operadorID, String nombres, String apellidos, String grado, String tipo, int periodo, int estado) {
        this.operadorID = operadorID;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.grado = grado;
        this.tipo = tipo;
        this.periodo = periodo;
        this.estado = estado;
    }

    public int getOperadorID() {
        return operadorID;
    }

    public void setOperadorID(int operadorID) {
        this.operadorID = operadorID;
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

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
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
