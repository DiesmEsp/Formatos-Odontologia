package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.repository.DocenteRepository;

import java.sql.SQLException;

public class DocenteService {

    private final DocenteRepository repository = new DocenteRepository();

    public int crear(String nombres, String apellidos, String telefono, int clinicaID) throws SQLException {
        validarObligatorios(nombres, apellidos);
        for (Docente d : repository.findAll(clinicaID)) {
            if (mismoNombre(d, nombres, apellidos)) {
                throw new EntidadDuplicadaException(
                        "Ya existe un docente con el nombre '" + nombres.trim() + " " + apellidos.trim() + "'.");
            }
        }
        Docente docente = new Docente();
        docente.setNombres(nombres.trim());
        docente.setApellidos(apellidos.trim());
        docente.setTelefono(telefono == null ? null : telefono.trim());
        docente.setEstado(1);
        docente.setClinicaID(clinicaID);
        return repository.insert(docente);
    }

    public void actualizar(Docente docente) throws SQLException {
        validarObligatorios(docente.getNombres(), docente.getApellidos());
        for (Docente d : repository.findAll(docente.getClinicaID())) {
            if (d.getDocenteID() != docente.getDocenteID() && mismoNombre(d, docente.getNombres(), docente.getApellidos())) {
                throw new EntidadDuplicadaException(
                        "Ya existe un docente con el nombre '" + docente.getNombres().trim() + " " + docente.getApellidos().trim() + "'.");
            }
        }
        repository.update(docente);
    }

    public void eliminar(int docenteID) throws SQLException {
        repository.delete(docenteID);
    }

    private boolean mismoNombre(Docente existente, String nombres, String apellidos) {
        return existente.getNombres().equalsIgnoreCase(nombres.trim())
                && existente.getApellidos().equalsIgnoreCase(apellidos.trim());
    }

    private void validarObligatorios(String nombres, String apellidos) {
        if (nombres == null || nombres.isBlank()) {
            throw new NegocioException("Los nombres del docente son obligatorios.");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new NegocioException("Los apellidos del docente son obligatorios.");
        }
    }
}
