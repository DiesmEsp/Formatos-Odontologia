package com.odontologia.formatos.model;

public class Paciente {
    private int pacienteID;
    private String nombres;
    private String apellidos;
    private int estado;

    public Paciente() {
    }

    public Paciente(int pacienteID, String nombres, String apellidos) {
        this.pacienteID = pacienteID;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public Paciente(int pacienteID, String nombres, String apellidos, int estado) {
        this.pacienteID = pacienteID;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
    }

    public int getPacienteID() { return pacienteID; }
    public void setPacienteID(int pacienteID) { this.pacienteID = pacienteID; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    @Override
    public String toString() {
        return nombres + " " + apellidos;
    }
}
