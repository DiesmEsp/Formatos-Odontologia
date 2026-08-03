package com.odontologia.formatos.service;

import com.odontologia.formatos.model.UnidadConversion;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.UnidadConversionRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Conversiones de empaque a unidad base (RD-3.1.14).
 * Reglas: material y unidades obligatorias, factor > 0 y
 * un solo registro por (MaterialID, UnidadEmpaque).
 */
public class UnidadConversionService {

    private final UnidadConversionRepository repository = new UnidadConversionRepository();
    private final MaterialRepository materialRepository = new MaterialRepository();

    public int crear(int materialID, String unidadBase, String unidadEmpaque, double factor) throws SQLException {
        validarMaterial(materialID);
        validarObligatorios(unidadBase, unidadEmpaque);
        validarFactor(factor);
        String base = unidadBase.trim();
        String empaque = unidadEmpaque.trim();
        if (repository.findByMaterialEmpaque(materialID, empaque) != null) {
            throw new EntidadDuplicadaException(
                    "Ya existe una conversión para el material y empaque '" + empaque + "'.");
        }
        UnidadConversion conversion = new UnidadConversion();
        conversion.setMaterialID(materialID);
        conversion.setUnidadBase(base);
        conversion.setUnidadEmpaque(empaque);
        conversion.setFactor(factor);
        return repository.insert(conversion);
    }

    public void actualizar(UnidadConversion conversion) throws SQLException {
        if (conversion == null || repository.findById(conversion.getConversionID()) == null) {
            throw new NegocioException("La conversión indicada no existe.");
        }
        validarMaterial(conversion.getMaterialID());
        validarObligatorios(conversion.getUnidadBase(), conversion.getUnidadEmpaque());
        validarFactor(conversion.getFactor());
        UnidadConversion existente = repository.findByMaterialEmpaque(
                conversion.getMaterialID(), conversion.getUnidadEmpaque());
        if (existente != null && existente.getConversionID() != conversion.getConversionID()) {
            throw new EntidadDuplicadaException(
                    "Ya existe una conversión para el material y empaque '"
                            + conversion.getUnidadEmpaque() + "'.");
        }
        repository.update(conversion);
    }

    public void eliminar(int conversionID) throws SQLException {
        repository.delete(conversionID);
    }

    public UnidadConversion buscarPorId(int conversionID) throws SQLException {
        return repository.findById(conversionID);
    }

    public List<UnidadConversion> buscarPorMaterial(int materialID) throws SQLException {
        return repository.findByMaterial(materialID);
    }

    private void validarMaterial(int materialID) throws SQLException {
        if (materialRepository.findById(materialID) == null) {
            throw new NegocioException("El material seleccionado no existe.");
        }
    }

    private void validarObligatorios(String unidadBase, String unidadEmpaque) {
        if (unidadBase == null || unidadBase.isBlank()) {
            throw new NegocioException("La unidad base es obligatoria.");
        }
        if (unidadEmpaque == null || unidadEmpaque.isBlank()) {
            throw new NegocioException("La unidad de empaque es obligatoria.");
        }
    }

    private void validarFactor(double factor) {
        if (factor <= 0) {
            throw new NegocioException("El factor de conversión debe ser mayor a 0.");
        }
    }
}
