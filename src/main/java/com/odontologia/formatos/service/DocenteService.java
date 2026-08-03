package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.repository.DocenteRepository;

import java.sql.SQLException;

public class DocenteService {

    private final DocenteRepository repository = new DocenteRepository();

    public int crear(String nombres, String apellidos) throws SQLException {
        validarObligatorios(nombres, apellidos);
        for (Docente d : repository.findAll()) {
            if (mismoNombre(d, nombres, apellidos)) {
                throw new EntidadDuplicadaException(
                        "Ya existe un docente con el nombre '" + nombres.trim() + " " + apellidos.trim() + "'.");
            }
        }
        Docente docente = new Docente();
        docente.setNombres(nombres.trim());
        docente.setApellidos(apellidos.trim());
        docente.setEstado(1);
        return repository.insert(docente);
    }

    public void actualizar(Docente docente) throws SQLException {
        validarObligatorios(docente.getNombres(), docente.getApellidos());
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
