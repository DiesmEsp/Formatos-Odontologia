package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.repository.MaterialRepository;

import java.sql.SQLException;

public class MaterialService {

    private final MaterialRepository repository = new MaterialRepository();

    public int crear(String nombre, String unidad) throws SQLException {
        validarObligatorios(nombre, unidad);
        if (repository.findByNombre(nombre.trim()) != null) {
            throw new EntidadDuplicadaException("Ya existe un material con el nombre '" + nombre.trim() + "'.");
        }
        Materiales material = new Materiales();
        material.setNombre(nombre.trim());
        material.setUnidad(unidad.trim());
        material.setEstado(1);
        return repository.insert(material);
    }

    public void actualizar(Materiales material) throws SQLException {
        validarObligatorios(material.getNombre(), material.getUnidad());
        Materiales existente = repository.findByNombre(material.getNombre().trim());
        if (existente != null && existente.getMaterialID() != material.getMaterialID()) {
            throw new EntidadDuplicadaException("Ya existe un material con el nombre '" + material.getNombre().trim() + "'.");
        }
        repository.update(material);
    }

    public void eliminar(int materialID) throws SQLException {
        repository.delete(materialID);
    }

    public Materiales buscarPorId(int materialID) throws SQLException {
        return repository.findById(materialID);
    }

    private void validarObligatorios(String nombre, String unidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new NegocioException("El nombre del material es obligatorio.");
        }
        if (unidad == null || unidad.isBlank()) {
            throw new NegocioException("La unidad de medida del material es obligatoria.");
        }
    }
}
