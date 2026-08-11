package com.odontologia.formatos.service;

import com.odontologia.formatos.model.TratamientoPredefinido;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;
import com.odontologia.formatos.repository.TratamientoPredefinidoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.util.TransaccionBD;

import java.sql.SQLException;
import java.util.List;

public class TratamientoPredefinidoService {

    private final TratamientoPredefinidoRepository repository = new TratamientoPredefinidoRepository();
    private final TratamientoPredefinidoMaterialRepository materialRepository =
            new TratamientoPredefinidoMaterialRepository();

    public int crear(String nombre, Double montoSugerido) throws SQLException {
        validarObligatorios(nombre);
        if (repository.findByNombre(nombre.trim()) != null) {
            throw new EntidadDuplicadaException(
                    "Ya existe un tratamiento predefinido con el nombre '" + nombre.trim() + "'.");
        }
        TratamientoPredefinido tp = new TratamientoPredefinido();
        tp.setNombreTratamiento(nombre.trim());
        tp.setMontoSugerido(montoSugerido);
        return repository.insert(tp);
    }

    public void actualizar(TratamientoPredefinido tp) throws SQLException {
        validarObligatorios(tp.getNombreTratamiento());
        repository.update(tp);
    }

    public void eliminar(int tratPredID) throws SQLException {
        TransaccionBD.ejecutar(con -> {
            materialRepository.deleteByTratPredID(con, tratPredID);
            repository.delete(con, tratPredID);
        });
    }

    public List<TratamientoPredefinidoMaterial> materiales(int tratPredID) throws SQLException {
        return materialRepository.findByTratPredID(tratPredID);
    }

    public void guardarMateriales(int tratPredID, List<TratamientoPredefinidoMaterial> materiales) throws SQLException {
        materialRepository.deleteByTratPredID(tratPredID);
        for (TratamientoPredefinidoMaterial item : materiales) {
            if (item.getCantidad() <= 0) {
                throw new NegocioException("La cantidad sugerida debe ser mayor a 0.");
            }
            item.setTratPredID(tratPredID);
            materialRepository.insert(item);
        }
    }

    private void validarObligatorios(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new NegocioException("El nombre del tratamiento predefinido es obligatorio.");
        }
    }
}
