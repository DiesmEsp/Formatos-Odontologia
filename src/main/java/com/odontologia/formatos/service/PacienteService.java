package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.repository.PacienteRepository;

import java.sql.SQLException;

public class PacienteService {

    private final PacienteRepository repository = new PacienteRepository();

    public int crear(String nombres, String apellidos) throws SQLException {
        validarObligatorios(nombres, apellidos);
        for (Paciente p : repository.findAll()) {
            if (mismoNombre(p, nombres, apellidos)) {
                throw new EntidadDuplicadaException(
                        "Ya existe un paciente con el nombre '" + nombres.trim() + " " + apellidos.trim() + "'.");
            }
        }
        Paciente paciente = new Paciente();
        paciente.setNombres(nombres.trim());
        paciente.setApellidos(apellidos.trim());
        return repository.insert(paciente);
    }

    public void actualizar(Paciente paciente) throws SQLException {
        validarObligatorios(paciente.getNombres(), paciente.getApellidos());
        repository.update(paciente);
    }

    public void eliminar(int pacienteID) throws SQLException {
        repository.delete(pacienteID);
    }

    private boolean mismoNombre(Paciente existente, String nombres, String apellidos) {
        return existente.getNombres().equalsIgnoreCase(nombres.trim())
                && existente.getApellidos().equalsIgnoreCase(apellidos.trim());
    }

    private void validarObligatorios(String nombres, String apellidos) {
        if (nombres == null || nombres.isBlank()) {
            throw new NegocioException("Los nombres del paciente son obligatorios.");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new NegocioException("Los apellidos del paciente son obligatorios.");
        }
    }
}
