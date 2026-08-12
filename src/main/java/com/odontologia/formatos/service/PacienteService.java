package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.repository.PacienteRepository;

import java.sql.SQLException;

public class PacienteService {

    private final PacienteRepository repository = new PacienteRepository();

    public int crear(String nombres, String apellidos) throws SQLException {
        if (nombres == null || nombres.isBlank()) throw new NegocioException("Los nombres son obligatorios.");
        if (apellidos == null || apellidos.isBlank()) throw new NegocioException("Los apellidos son obligatorios.");
        for (Paciente p : repository.findAll()) {
            if (p.getNombres().equalsIgnoreCase(nombres.trim()) && p.getApellidos().equalsIgnoreCase(apellidos.trim())) {
                throw new EntidadDuplicadaException("Ya existe un paciente con ese nombre.");
            }
        }
        Paciente paciente = new Paciente();
        paciente.setNombres(nombres.trim());
        paciente.setApellidos(apellidos.trim());
        return repository.insert(paciente);
    }

    public void actualizar(Paciente paciente) throws SQLException {
        if (paciente.getNombres() == null || paciente.getNombres().isBlank()) throw new NegocioException("Los nombres son obligatorios.");
        if (paciente.getApellidos() == null || paciente.getApellidos().isBlank()) throw new NegocioException("Los apellidos son obligatorios.");
        for (Paciente p : repository.findAll()) {
            if (p.getPacienteID() != paciente.getPacienteID()
                    && p.getNombres().equalsIgnoreCase(paciente.getNombres().trim())
                    && p.getApellidos().equalsIgnoreCase(paciente.getApellidos().trim())) {
                throw new EntidadDuplicadaException("Ya existe un paciente con ese nombre.");
            }
        }
        repository.update(paciente);
    }

    public void eliminar(int id) throws SQLException {
        repository.delete(id);
    }
}
