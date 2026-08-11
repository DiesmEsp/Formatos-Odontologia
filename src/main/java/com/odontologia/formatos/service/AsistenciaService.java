package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.AsistenciaMaterial;
import com.odontologia.formatos.model.PeriodoAusencia;
import com.odontologia.formatos.model.RegistroAnulacion;
import com.odontologia.formatos.repository.AsistenciaMaterialRepository;
import com.odontologia.formatos.repository.AsistenciaRepository;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.PeriodoAusenciaRepository;
import com.odontologia.formatos.repository.RegistroAnulacionRepository;
import com.odontologia.formatos.util.TransaccionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository = new AsistenciaRepository();
    private final AsistenciaMaterialRepository materialRepository = new AsistenciaMaterialRepository();
    private final PeriodoAusenciaRepository ausenciaRepository = new PeriodoAusenciaRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final MaterialRepository materialRepositoryCatalogo = new MaterialRepository();
    private final RegistroAnulacionRepository anulacionRepository = new RegistroAnulacionRepository();

    public Asistencia abrirDia(int docenteID, String fecha, String horaEntrada) throws SQLException {
        validarDocente(docenteID);
        validarFecha(fecha);
        validarHora(horaEntrada, "hora de entrada");
        return TransaccionBD.ejecutarConResultado(con -> {
            Asistencia existente = asistenciaRepository.findActivoPorDocenteYFecha(con, docenteID, fecha);
            if (existente != null) {
                if (existente.getHoraEntrada() == null && horaEntrada != null) {
                    asistenciaRepository.registrarEntrada(con, existente.getAsistenciaID(), horaEntrada);
                    existente.setHoraEntrada(horaEntrada);
                }
                return existente;
            }
            Asistencia nueva = new Asistencia();
            nueva.setDocenteID(docenteID);
            nueva.setFecha(fecha);
            nueva.setEstado("ACTIVO");
            nueva.setHoraEntrada(horaEntrada);
            int id = asistenciaRepository.insert(con, nueva);
            nueva.setAsistenciaID(id);
            return nueva;
        });
    }

    public void registrarEntrada(int asistenciaID, String horaEntrada) throws SQLException {
        validarHora(horaEntrada, "hora de entrada");
        Asistencia a = asistenciaRepository.findById(asistenciaID);
        if (a == null) {
            throw new NegocioException("La asistencia no existe.");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new NegocioException("La asistencia está anulada.");
        }
        TransaccionBD.ejecutar(con -> asistenciaRepository.registrarEntrada(con, asistenciaID, horaEntrada));
    }

    public void registrarSalida(int asistenciaID, String horaSalida) throws SQLException {
        validarHora(horaSalida, "hora de salida");
        Asistencia a = asistenciaRepository.findById(asistenciaID);
        if (a == null) {
            throw new NegocioException("La asistencia no existe.");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new NegocioException("La asistencia está anulada.");
        }
        if (a.getHoraSalida() != null) {
            throw new NegocioException("La asistencia ya tiene una hora de salida registrada.");
        }
        TransaccionBD.ejecutar(con -> {
            PeriodoAusencia abierta = ausenciaRepository.findAbierta(con, asistenciaID);
            if (abierta != null) {
                throw new NegocioException("El docente tiene un periodo de ausencia activo. Debe registrar su regreso antes de finalizar el día.");
            }
            asistenciaRepository.registrarSalida(con, asistenciaID, horaSalida);
        });
    }

    public void revertirSalida(int asistenciaID) throws SQLException {
        Asistencia a = asistenciaRepository.findById(asistenciaID);
        if (a == null) {
            throw new NegocioException("La asistencia no existe.");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new NegocioException("La asistencia está anulada.");
        }
        if (a.getHoraSalida() == null) {
            throw new NegocioException("La asistencia no tiene una hora de salida registrada.");
        }
        TransaccionBD.ejecutar(con -> asistenciaRepository.revertirSalida(con, asistenciaID));
    }

    public PeriodoAusencia iniciarAusencia(int asistenciaID, String horaInicio, String motivo) throws SQLException {
        validarHora(horaInicio, "hora de inicio de ausencia");
        Asistencia a = asistenciaRepository.findById(asistenciaID);
        if (a == null) {
            throw new NegocioException("La asistencia no existe.");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new NegocioException("La asistencia está anulada.");
        }
        if (a.getHoraEntrada() == null) {
            throw new NegocioException("Debe registrar la hora de entrada antes de iniciar una ausencia.");
        }
        return TransaccionBD.ejecutarConResultado(con -> {
            PeriodoAusencia abierta = ausenciaRepository.findAbierta(con, asistenciaID);
            if (abierta != null) {
                throw new NegocioException("El docente ya tiene un periodo de ausencia activo desde las "
                        + formatearHora(abierta.getHoraInicio()) + ". Registre su regreso antes de iniciar uno nuevo.");
            }
            PeriodoAusencia nueva = new PeriodoAusencia();
            nueva.setAsistenciaID(asistenciaID);
            nueva.setHoraInicio(horaInicio);
            if (motivo != null && !motivo.isBlank()) {
                nueva.setMotivo(motivo.trim());
            }
            int id = ausenciaRepository.insert(con, nueva);
            nueva.setAusenciaID(id);
            return nueva;
        });
    }

    public void finalizarAusencia(int ausenciaID, String horaFin) throws SQLException {
        validarHora(horaFin, "hora de regreso");
        PeriodoAusencia ausencia = ausenciaRepository.findById(ausenciaID);
        if (ausencia == null) {
            throw new NegocioException("El periodo de ausencia no existe.");
        }
        if (ausencia.getHoraFin() != null) {
            throw new NegocioException("El periodo de ausencia ya fue finalizado.");
        }
        if (horaFin.compareTo(ausencia.getHoraInicio()) <= 0) {
            throw new NegocioException("La hora de regreso debe ser posterior a la hora de inicio de la ausencia.");
        }
        TransaccionBD.ejecutar(con -> ausenciaRepository.finalizar(con, ausenciaID, horaFin));
    }

    public void eliminarAusencia(int ausenciaID) throws SQLException {
        PeriodoAusencia ausencia = ausenciaRepository.findById(ausenciaID);
        if (ausencia == null) {
            throw new NegocioException("El periodo de ausencia no existe.");
        }
        TransaccionBD.ejecutar(con -> ausenciaRepository.delete(con, ausenciaID));
    }

    public Map<String, Object> obtenerDetalle(int asistenciaID) throws SQLException {
        Asistencia a = asistenciaRepository.findById(asistenciaID);
        if (a == null) {
            throw new NegocioException("La asistencia no existe.");
        }
        List<PeriodoAusencia> ausencias = ausenciaRepository.findByAsistenciaID(asistenciaID);
        List<AsistenciaMaterialRepository.MaterialConCantidad> materiales =
                materialRepository.findMaterialesConNombre(asistenciaID);
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("asistencia", a);
        detalle.put("ausencias", ausencias);
        detalle.put("materiales", materiales);
        return detalle;
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

    public void anular(int asistenciaID, String motivo) throws SQLException {
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException("Debe indicar el motivo de la anulación.");
        }
        Asistencia a = asistenciaRepository.findById(asistenciaID);
        if (a == null) {
            throw new NegocioException("La asistencia no existe.");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new NegocioException("La asistencia ya está anulada.");
        }

        TransaccionBD.ejecutar(con -> {
            asistenciaRepository.anular(con, asistenciaID);

            RegistroAnulacion r = new RegistroAnulacion();
            r.setTablaAfectada("Asistencia");
            r.setIdRegistroAnulado(asistenciaID);
            r.setMotivo(motivo);
            r.setUsuario("SYSTEM");
            anulacionRepository.insert(con, r);
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

    private void validarHora(String hora, String campo) {
        if (hora == null || !hora.matches("\\d{2}:\\d{2}(:\\d{2})?")) {
            throw new NegocioException("La " + campo + " debe tener el formato HH:mm o HH:mm:ss.");
        }
    }

    private String formatearHora(String hora) {
        if (hora != null && hora.length() >= 5) {
            return hora.substring(0, 5);
        }
        return hora;
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
