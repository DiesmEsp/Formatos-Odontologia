package com.odontologia.formatos.service;

import com.odontologia.formatos.model.ConsumoClinica;
import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.repository.ConsumoRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.util.TransaccionBD;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumoService {

    private final ConsumoRepository consumoRepo = new ConsumoRepository();
    private final MaterialRepository materialRepo = new MaterialRepository();

    public List<ConsumoClinica> listarPorMes(int anio, int mes, int clinicaID) throws SQLException {
        validarPeriodo(anio, mes);
        return consumoRepo.findByMes(anio, mes, clinicaID);
    }

    public List<Integer> crearLote(List<ConsumoClinica> items, int clinicaID) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new NegocioException("Debe registrar al menos un material.");
        }
        for (ConsumoClinica item : items) {
            validarItem(item.getFecha(), item.getMaterialID(), item.getCantidad());
        }
        return TransaccionBD.ejecutarConResultado(con -> {
            List<Integer> ids = new ArrayList<>();
            for (ConsumoClinica item : items) {
                item.setClinicaID(clinicaID);
                ids.add(consumoRepo.insert(item, con));
            }
            return ids;
        });
    }

    public void actualizar(int consumoID, String fecha, int materialID, double cantidad,
                           int clinicaID) throws SQLException {
        validarItem(fecha, materialID, cantidad);
        ConsumoClinica registro = new ConsumoClinica();
        registro.setConsumoID(consumoID);
        registro.setFecha(fecha);
        registro.setMaterialID(materialID);
        registro.setCantidad(cantidad);
        registro.setClinicaID(clinicaID);
        if (!consumoRepo.update(registro, null)) {
            throw new NegocioException("Registro de consumo no encontrado.");
        }
    }

    public void eliminar(int consumoID, int clinicaID) throws SQLException {
        if (!consumoRepo.delete(consumoID, clinicaID, null)) {
            throw new NegocioException("Registro de consumo no encontrado.");
        }
    }

    private void validarItem(String fecha, int materialID, double cantidad) throws SQLException {
        if (fecha == null || !fecha.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new NegocioException("La fecha debe tener el formato AAAA-MM-DD.");
        }
        try {
            LocalDate.parse(fecha);
        } catch (java.time.format.DateTimeParseException e) {
            throw new NegocioException("La fecha indicada no es válida.");
        }
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad debe ser mayor a cero.");
        }
        Materiales material = materialRepo.findById(materialID);
        if (material == null || material.getEstado() != 1) {
            throw new NegocioException("El material indicado no existe o está inactivo.");
        }
    }

    private void validarPeriodo(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new NegocioException("El mes debe estar entre 1 y 12.");
        }
        int anioActual = LocalDate.now().getYear();
        if (anio < 2000 || anio > anioActual + 1) {
            throw new NegocioException("El año indicado no es válido.");
        }
    }
}
