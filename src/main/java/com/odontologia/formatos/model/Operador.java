package com.odontologia.formatos.model;

public class Operador {

    private int operadorID;
    private String nombres;
    private String apellidos;
    private String dni;
    private String grado;
    private String tipo;
    private int periodo;
    private int estado;
    private int clinicaID = 1;

    public Operador() {
    }

    public Operador(int operadorID, String nombres, String apellidos, String dni, String grado, String tipo, int periodo, int estado) {
        this(operadorID, nombres, apellidos, dni, grado, tipo, periodo, estado, 1);
    }

    public Operador(int operadorID, String nombres, String apellidos, String dni, String grado, String tipo, int periodo, int estado, int clinicaID) {
        this.operadorID = operadorID;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.grado = grado;
        this.tipo = tipo;
        this.periodo = periodo;
        this.estado = estado;
        this.clinicaID = clinicaID;
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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public int getClinicaID() {
        return clinicaID;
    }

    public void setClinicaID(int clinicaID) {
        this.clinicaID = clinicaID;
    }

    @Override
    public String toString() {
        return nombres + " " + apellidos;
    }
}
