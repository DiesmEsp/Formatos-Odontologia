package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.AsistenciaMaterial;
import com.odontologia.formatos.repository.AsistenciaMaterialRepository;
import com.odontologia.formatos.repository.AsistenciaRepository;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.util.TransaccionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Control diario de docentes (RF-1.2.1, RF-1.2.2, RD-3.1.4).
 * Un registro por docente y día; los materiales se acumulan sobre el registro ACTIVO.
 */
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository = new AsistenciaRepository();
    private final AsistenciaMaterialRepository materialRepository = new AsistenciaMaterialRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final MaterialRepository materialRepositoryCatalogo = new MaterialRepository();

    public Asistencia abrirDia(int docenteID, String fecha) throws SQLException {
        validarDocente(docenteID);
        validarFecha(fecha);
        return TransaccionBD.ejecutarConResultado(con -> {
            Asistencia existente = asistenciaRepository.findActivoPorDocenteYFecha(con, docenteID, fecha);
            if (existente != null) {
                return existente;
            }
            Asistencia nueva = new Asistencia();
            nueva.setDocenteID(docenteID);
            nueva.setFecha(fecha);
            nueva.setEstado("ACTIVO");
            int id = asistenciaRepository.insert(con, nueva);
            nueva.setAsistenciaID(id);
            return nueva;
        });
    }

    public void registrarMateriales(int docenteID, String fecha, Map<Integer, Double> materiales)
            throws SQLException {
        validarDocente(docenteID);
        validarFecha(fecha);
        if (materiales == null || materiales.isEmpty()) {
            throw new NegocioException("Debe registrar al menos un material.");
        }
        validarMaterialesExisten(materiales);
        validarCantidades(materiales);

        TransaccionBD.ejecutar(con -> {
            Asistencia asistencia = asistenciaRepository.findActivoPorDocenteYFecha(con, docenteID, fecha);
            if (asistencia == null) {
                asistencia = new Asistencia();
                asistencia.setDocenteID(docenteID);
                asistencia.setFecha(fecha);
                asistencia.setEstado("ACTIVO");
                int id = asistenciaRepository.insert(con, asistencia);
                asistencia.setAsistenciaID(id);
            }
            for (Map.Entry<Integer, Double> entrada : materiales.entrySet()) {
                acumular(con, asistencia.getAsistenciaID(), entrada.getKey(), entrada.getValue());
            }
        });
    }

    public void acumularMaterial(int asistenciaID, int materialID, double cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad debe ser mayor a 0.");
        }
        validarMaterialExiste(materialID);
        TransaccionBD.ejecutar(con -> acumular(con, asistenciaID, materialID, cantidad));
    }

    public List<AsistenciaMaterialRepository.MaterialConCantidad> materialesDelDia(int asistenciaID)
            throws SQLException {
        return materialRepository.findMaterialesConNombre(asistenciaID);
    }

    public List<AsistenciaMaterial> materialesCrudos(int asistenciaID) throws SQLException {
        return materialRepository.findByAsistenciaID(asistenciaID);
    }

    private void acumular(Connection con, int asistenciaID, int materialID, double cantidad)
            throws SQLException {
        AsistenciaMaterial existente = materialRepository.findByMaterial(con, asistenciaID, materialID);
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
            materialRepository.update(con, existente);
        } else {
            AsistenciaMaterial nuevo = new AsistenciaMaterial();
            nuevo.setAsistenciaID(asistenciaID);
            nuevo.setMaterialID(materialID);
            nuevo.setCantidad(cantidad);
            materialRepository.insert(con, nuevo);
        }
    }

    private void validarDocente(int docenteID) throws SQLException {
        if (docenteRepository.findById(docenteID) == null) {
            throw new NegocioException("El docente seleccionado no existe.");
        }
    }

    private void validarFecha(String fecha) {
        if (fecha == null || !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new NegocioException("La fecha debe tener el formato AAAA-MM-DD.");
        }
    }

    private void validarMaterialesExisten(Map<Integer, Double> materiales) throws SQLException {
        for (Integer materialID : materiales.keySet()) {
            validarMaterialExiste(materialID);
        }
    }

    private void validarMaterialExiste(int materialID) throws SQLException {
        if (materialRepositoryCatalogo.findById(materialID) == null) {
            throw new NegocioException("El material seleccionado no existe.");
        }
    }

    private void validarCantidades(Map<Integer, Double> materiales) {
        for (Double cantidad : materiales.values()) {
            if (cantidad == null || cantidad <= 0) {
                throw new NegocioException("La cantidad de cada material debe ser mayor a 0.");
            }
        }
    }
}
