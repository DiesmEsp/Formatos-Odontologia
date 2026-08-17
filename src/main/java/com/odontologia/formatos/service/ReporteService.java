package com.odontologia.formatos.service;

import com.odontologia.formatos.repository.ReporteRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de datos para reportes (RF-1.7.x, Fase 4).
 * Valida el período y delega las consultas agregadas en ReporteRepository.
 */
public class ReporteService {

    private final ReporteRepository repository = new ReporteRepository();

    public List<ReporteRepository.FilaMaterial> materiales(int anio, int mes) throws SQLException {
        return materiales(anio, mes, 1);
    }

    public List<ReporteRepository.FilaMaterial> materiales(int anio, int mes, int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.materiales(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaIngreso> ingresos(int anio, int mes) throws SQLException {
        return ingresos(anio, mes, 1);
    }

    public List<ReporteRepository.FilaIngreso> ingresos(int anio, int mes, int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.ingresos(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaDocente> docenteConsolidado(int anio, int mes) throws SQLException {
        return docenteConsolidado(anio, mes, 1);
    }

    public List<ReporteRepository.FilaDocente> docenteConsolidado(int anio, int mes, int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.docenteConsolidado(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaDocente> docenteDetalleDia(int anio, int mes) throws SQLException {
        return docenteDetalleDia(anio, mes, 1);
    }

    public List<ReporteRepository.FilaDocente> docenteDetalleDia(int anio, int mes, int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.docenteDetalleDia(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaEspecialista> especialista(int anio, int mes) throws SQLException {
        return especialista(anio, mes, 1);
    }

    public List<ReporteRepository.FilaEspecialista> especialista(int anio, int mes, int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.especialista(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaIngresoTratamiento> ingresosPorTratamiento(int anio, int mes) throws SQLException {
        return ingresosPorTratamiento(anio, mes, 1);
    }

    public List<ReporteRepository.FilaIngresoTratamiento> ingresosPorTratamiento(int anio, int mes, int clinicaID)
            throws SQLException {
        validarMes(mes);
        return repository.ingresosPorTratamiento(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaIngresoOperador> ingresosPorOperador(int anio, int mes) throws SQLException {
        return ingresosPorOperador(anio, mes, 1);
    }

    public List<ReporteRepository.FilaIngresoOperador> ingresosPorOperador(int anio, int mes, int clinicaID)
            throws SQLException {
        validarMes(mes);
        return repository.ingresosPorOperador(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaAsistencia> datosAsistencia(int anio, int mes) throws SQLException {
        return datosAsistencia(anio, mes, 1);
    }

    public List<ReporteRepository.FilaAsistencia> datosAsistencia(int anio, int mes, int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.datosAsistencia(anio, mes, clinicaID);
    }

    public List<ReporteRepository.FilaTratamiento> consumoPorTratamiento(int anio, int mes) throws SQLException {
        return consumoPorTratamiento(anio, mes, null, null, 1);
    }

    public List<ReporteRepository.FilaTratamiento> consumoPorTratamiento(int anio, int mes, Integer operadorID, String tipo)
            throws SQLException {
        return consumoPorTratamiento(anio, mes, operadorID, tipo, 1);
    }

    public List<ReporteRepository.FilaTratamiento> consumoPorTratamiento(int anio, int mes, Integer operadorID, String tipo,
                                                                         int clinicaID) throws SQLException {
        validarMes(mes);
        return repository.consumoPorTratamiento(anio, mes, operadorID, tipo, clinicaID);
    }

    private void validarMes(int mes) {
        if (mes < 1 || mes > 12) {
            throw new NegocioException("El mes debe estar entre 1 y 12.");
        }
    }
}
