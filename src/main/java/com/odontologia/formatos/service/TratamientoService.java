package com.odontologia.formatos.service;

import com.odontologia.formatos.model.MaterialAvance;
import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.model.Pago;
import com.odontologia.formatos.model.RegistroAnulacion;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.TratamientoAvance;
import com.odontologia.formatos.model.TratamientoMaterial;
import com.odontologia.formatos.model.TratamientoPredefinido;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;
import com.odontologia.formatos.repository.MaterialAvanceRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.PagoRepository;
import com.odontologia.formatos.repository.RegistroAnulacionRepository;
import com.odontologia.formatos.repository.TratamientoAvanceRepository;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.repository.TratamientoRepository;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.util.TransaccionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Flujo de tratamientos (RF-1.4.x).
 * Creación con carga de plantilla, consumo dinámico acumulativo,
 * cierre con consolidación, reapertura, tipo CONTINUO y pagos.
 */
public class TratamientoService {

    private static final double EPSILON = 0.0001;

    private final TratamientoRepository repository = new TratamientoRepository();
    private final TratamientoMaterialRepository materialRepository = new TratamientoMaterialRepository();
    private final TratamientoPredefinidoRepository predefinidoRepository = new TratamientoPredefinidoRepository();
    private final TratamientoPredefinidoMaterialRepository predefinidoMaterialRepository =
            new TratamientoPredefinidoMaterialRepository();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private final UnidadRepository unidadRepository = new UnidadRepository();
    private final MaterialRepository materialRepositoryCatalogo = new MaterialRepository();
    private final RegistroAnulacionRepository anulacionRepository = new RegistroAnulacionRepository();
    private final PagoRepository pagoRepository = new PagoRepository();
    private final TratamientoAvanceRepository avanceRepository = new TratamientoAvanceRepository();
    private final MaterialAvanceRepository materialAvanceRepository = new MaterialAvanceRepository();

public int crear(int operadorID, int pacienteID, Integer unidadID, String fecha,
                 Integer tratPredID, Double monto, String tipo) throws SQLException {
        return crear(operadorID, pacienteID, unidadID, fecha, tratPredID, monto, tipo, null, 1);
    }

    public int crear(int operadorID, int pacienteID, Integer unidadID, String fecha,
                     Integer tratPredID, Double monto, String tipo,
                     Map<Integer, Double> materiales) throws SQLException {
        return crear(operadorID, pacienteID, unidadID, fecha, tratPredID, monto, tipo, materiales, 1);
    }

    public int crear(int operadorID, int pacienteID, Integer unidadID, String fecha,
                     Integer tratPredID, Double monto, String tipo,
                     Map<Integer, Double> materiales, int clinicaID) throws SQLException {
        validarEspecialista(operadorID);
        validarPaciente(pacienteID);
        if (unidadID != null && unidadRepository.findById(unidadID) == null) {
            throw new NegocioException("La unidad de tratamiento seleccionada no existe.");
        }
        validarFecha(fecha);
        String tipoNormalizado = normalizarTipo(tipo);

        return TransaccionBD.ejecutarConResultado(con -> {
            Tratamiento tratamiento = new Tratamiento();
            tratamiento.setOperadorID(operadorID);
            tratamiento.setPacienteID(pacienteID);
            tratamiento.setUnidadID(unidadID);
            tratamiento.setFecha(fecha);
            tratamiento.setTipo(tipoNormalizado);
            tratamiento.setEstado("ABIERTO");
            tratamiento.setCerradoEn(null);
            tratamiento.setClinicaID(clinicaID);

            List<TratamientoPredefinidoMaterial> sugeridos = List.of();
            if (tratPredID != null) {
                sugeridos = cargarPlantilla(tratamiento, tratPredID, monto);
            } else {
                tratamiento.setNombreTratamiento(tratamiento.getNombreTratamiento() != null
                        ? tratamiento.getNombreTratamiento() : "Tratamiento general");
            }

            if ("CONTINUO".equals(tipoNormalizado)) {
                tratamiento.setMonto(0);
                tratamiento.setEstadoPago("PAGADO");
                tratamiento.setMontoPagado(0);
            } else {
                double montoReal = monto != null ? monto : tratamiento.getMonto();
                if (montoReal < 0) {
                    throw new NegocioException("El monto del tratamiento no puede ser negativo.");
                }
                tratamiento.setMonto(montoReal);
                tratamiento.setEstadoPago("PENDIENTE");
                tratamiento.setMontoPagado(0);
            }

            int tratamientoID = repository.insert(con, tratamiento);
            tratamiento.setTratamientoID(tratamientoID);

            if (materiales != null) {
                for (Map.Entry<Integer, Double> entrada : materiales.entrySet()) {
                    if (entrada.getValue() == null || entrada.getValue() <= 0) {
                        continue;
                    }
                    validarMaterialExiste(entrada.getKey());
                    TratamientoMaterial item = new TratamientoMaterial();
                    item.setTratamientoID(tratamientoID);
                    item.setMaterialID(entrada.getKey());
                    item.setCantidad(entrada.getValue());
                    materialRepository.insert(con, item);
                }
            } else {
                for (TratamientoPredefinidoMaterial sugerido : sugeridos) {
                    TratamientoMaterial item = new TratamientoMaterial();
                    item.setTratamientoID(tratamientoID);
                    item.setMaterialID(sugerido.getMaterialID());
                    item.setCantidad(sugerido.getCantidad());
                    materialRepository.insert(con, item);
                }
            }
            return tratamientoID;
        });
    }

    public Tratamiento buscarPorId(int tratamientoID) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        return t;
    }

    public List<Tratamiento> activos() throws SQLException {
        return activos(1);
    }

    public List<Tratamiento> activos(int clinicaID) throws SQLException {
        return repository.findByEstado("ABIERTO", clinicaID);
    }

    public List<Tratamiento> cerrados() throws SQLException {
        return cerrados(1);
    }

    public List<Tratamiento> cerrados(int clinicaID) throws SQLException {
        return repository.findByEstado("CERRADO", clinicaID);
    }

    public int crearCerrado(int operadorID, int pacienteID, String fecha, Integer tratPredID,
                            Double monto, String tipo, Map<Integer, Double> materiales) throws SQLException {
        return crearCerrado(operadorID, pacienteID, fecha, tratPredID, monto, tipo, materiales, 1);
    }

    public int crearCerrado(int operadorID, int pacienteID, String fecha, Integer tratPredID,
                            Double monto, String tipo, Map<Integer, Double> materiales,
                            int clinicaID) throws SQLException {
        validarEspecialista(operadorID);
        validarPaciente(pacienteID);
        validarFecha(fecha);
        String tipoNormalizado = normalizarTipo(tipo);

        return TransaccionBD.ejecutarConResultado(con -> {
            Tratamiento tratamiento = new Tratamiento();
            tratamiento.setOperadorID(operadorID);
            tratamiento.setPacienteID(pacienteID);
            tratamiento.setUnidadID(null);
            tratamiento.setFecha(fecha);
            tratamiento.setTipo(tipoNormalizado);
            tratamiento.setEstado("CERRADO");
            tratamiento.setCerradoEn(timestampLocal());
            tratamiento.setClinicaID(clinicaID);

            if (tratPredID != null) {
                TratamientoPredefinido predefinido = predefinidoRepository.findById(tratPredID);
                if (predefinido == null) {
                    throw new NegocioException("El tipo de tratamiento seleccionado no existe.");
                }
                tratamiento.setNombreTratamiento(predefinido.getNombreTratamiento());
                if (monto == null && predefinido.getMontoSugerido() != null) {
                    tratamiento.setMonto(predefinido.getMontoSugerido());
                }
            } else {
                tratamiento.setNombreTratamiento("Tratamiento general");
            }

            if ("CONTINUO".equals(tipoNormalizado)) {
                tratamiento.setMonto(0);
                tratamiento.setEstadoPago("PAGADO");
                tratamiento.setMontoPagado(0);
            } else {
                double montoReal = monto != null ? monto : tratamiento.getMonto();
                if (montoReal < 0) {
                    throw new NegocioException("El monto del tratamiento no puede ser negativo.");
                }
                tratamiento.setMonto(montoReal);
                tratamiento.setEstadoPago("PAGADO");
                tratamiento.setMontoPagado(montoReal);
            }

            int tratamientoID = repository.insert(con, tratamiento);
            tratamiento.setTratamientoID(tratamientoID);

            if (materiales != null) {
                for (Map.Entry<Integer, Double> entrada : materiales.entrySet()) {
                    if (entrada.getValue() == null || entrada.getValue() <= 0) {
                        continue;
                    }
                    validarMaterialExiste(entrada.getKey());
                    TratamientoMaterial item = new TratamientoMaterial();
                    item.setTratamientoID(tratamientoID);
                    item.setMaterialID(entrada.getKey());
                    item.setCantidad(entrada.getValue());
                    materialRepository.insert(con, item);
                }
            }
            return tratamientoID;
        });
    }

    public List<Tratamiento> porUnidad(int unidadID) throws SQLException {
        return repository.findByUnidad(unidadID);
    }

    public int agregarAvance(int tratamientoID, String fecha, Integer unidadID,
                             Map<Integer, Double> materiales, Double pago) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if ("ANULADO".equals(t.getEstado())) {
            throw new NegocioException("No se puede agregar un avance a un tratamiento anulado.");
        }
        if (!"ABIERTO".equals(t.getEstado())) {
            throw new NegocioException("Solo se pueden agregar avances a tratamientos en estado ABIERTO.");
        }
        if ("CONTINUO".equals(t.getTipo())) {
            throw new NegocioException("Un tratamiento continuo no admite avances.");
        }
        validarFecha(fecha);
        if (unidadID != null && unidadRepository.findById(unidadID) == null) {
            throw new NegocioException("La unidad de tratamiento seleccionada no existe.");
        }

        return TransaccionBD.ejecutarConResultado(con -> {
            TratamientoAvance avance = new TratamientoAvance();
            avance.setTratamientoID(tratamientoID);
            avance.setFecha(fecha);
            avance.setUnidadID(unidadID);
            int avanceID = avanceRepository.insert(con, avance);

            if (materiales != null) {
                for (Map.Entry<Integer, Double> entrada : materiales.entrySet()) {
                    if (entrada.getValue() == null || entrada.getValue() <= 0) {
                        continue;
                    }
                    validarMaterialExiste(entrada.getKey());
                    MaterialAvance item = new MaterialAvance();
                    item.setAvanceID(avanceID);
                    item.setMaterialID(entrada.getKey());
                    item.setCantidad(entrada.getValue());
                    materialAvanceRepository.insert(con, item);
                }
            }

            if (pago != null && pago > 0) {
                if (pagoRepository.sumByTratamiento(con, tratamientoID) + pago > t.getMonto() + EPSILON) {
                    throw new NegocioException("El pago supera el monto total del tratamiento.");
                }
                Pago nuevoPago = new Pago();
                nuevoPago.setTratamientoID(tratamientoID);
                nuevoPago.setAvanceID(avanceID);
                nuevoPago.setFecha(fechaHoy());
                nuevoPago.setMonto(pago);
                pagoRepository.insert(con, nuevoPago);
                recalcularPagos(con, t);
            }

            return avanceID;
        });
    }

    public List<TratamientoAvance> listarAvances(int tratamientoID) throws SQLException {
        return avanceRepository.findByTratamiento(tratamientoID);
    }

    public void terminarAvance(int avanceID) throws SQLException {
        TratamientoAvance avance = avanceRepository.findById(avanceID);
        if (avance == null) {
            throw new NegocioException("El avance no existe.");
        }
        if ("TERMINADO".equals(avance.getEstado())) {
            throw new NegocioException("El avance ya está terminado.");
        }
        if ("ANULADO".equals(avance.getEstado())) {
            throw new NegocioException("No se puede terminar un avance anulado.");
        }
        TransaccionBD.ejecutar(con -> {
            avanceRepository.terminar(con, avanceID);
        });
    }

    public void anularAvance(int avanceID, String motivo) throws SQLException {
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException("Debe indicar el motivo de la anulación.");
        }
        TratamientoAvance avance = avanceRepository.findById(avanceID);
        if (avance == null) {
            throw new NegocioException("El avance no existe.");
        }
        if ("ANULADO".equals(avance.getEstado())) {
            throw new NegocioException("El avance ya está anulado.");
        }

        TransaccionBD.ejecutar(con -> {
            avanceRepository.anular(con, avanceID);
            pagoRepository.deleteByAvanceID(con, avanceID);
            Tratamiento t = repository.findById(avance.getTratamientoID());
            if (t != null) {
                recalcularPagos(con, t);
            }

            RegistroAnulacion r = new RegistroAnulacion();
            r.setTablaAfectada("Tratamiento_Avance");
            r.setIdRegistroAnulado(avanceID);
            r.setMotivo(motivo);
            r.setUsuario("SYSTEM");
            r.setClinicaID(t != null ? t.getClinicaID() : 1);
            anulacionRepository.insert(con, r);
        });
    }

    public ConsolidadoDto obtenerConsolidado(int tratamientoID) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        ConsolidadoDto dto = new ConsolidadoDto();
        dto.tratamiento = t;
        dto.materiales = materialAvanceRepository.consolidarPorTratamiento(tratamientoID);
        dto.pagos = pagoRepository.findByTratamiento(tratamientoID);
        return dto;
    }

    public void agregarMaterial(int tratamientoID, int materialID, double cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad del material debe ser mayor a 0.");
        }
        validarMaterialExiste(materialID);
        validarAbierto(tratamientoID);

        TransaccionBD.ejecutar(con -> {
            TratamientoMaterial existente = materialRepository.findByMaterial(con, tratamientoID, materialID);
            if (existente != null) {
                existente.setCantidad(existente.getCantidad() + cantidad);
                materialRepository.update(con, existente);
            } else {
                TratamientoMaterial nuevo = new TratamientoMaterial();
                nuevo.setTratamientoID(tratamientoID);
                nuevo.setMaterialID(materialID);
                nuevo.setCantidad(cantidad);
                materialRepository.insert(con, nuevo);
            }
        });
    }

    public void actualizarCantidad(int materialesListID, double cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad del material debe ser mayor a 0.");
        }
        TratamientoMaterial item = materialRepository.findById(materialesListID);
        if (item == null) {
            throw new NegocioException("El material indicado no existe.");
        }
        validarAbierto(item.getTratamientoID());
        item.setCantidad(cantidad);
        materialRepository.update(item);
    }

    public void quitarMaterial(int materialesListID) throws SQLException {
        TratamientoMaterial item = materialRepository.findById(materialesListID);
        if (item == null) {
            throw new NegocioException("El material indicado no existe.");
        }
        validarAbierto(item.getTratamientoID());
        materialRepository.delete(materialesListID);
    }

    public List<TratamientoMaterialRepository.MaterialConCantidad> materialesConNombre(int tratamientoID)
            throws SQLException {
        return materialRepository.findMaterialesConNombre(tratamientoID);
    }

    public List<TratamientoMaterial> materiales(int tratamientoID) throws SQLException {
        return materialRepository.findByTratamientoID(tratamientoID);
    }

    public void cerrar(int tratamientoID) throws SQLException {
        validarAbierto(tratamientoID);
        TransaccionBD.ejecutar(con -> {
            Tratamiento t = repository.findById(tratamientoID);
            t.setEstado("CERRADO");
            t.setCerradoEn(timestampLocal());
            repository.update(con, t);
        });
    }

    public void anular(int tratamientoID, String motivo) throws SQLException {
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException("Debe indicar el motivo de la anulación.");
        }
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if ("ANULADO".equals(t.getEstado())) {
            throw new NegocioException("El tratamiento ya está anulado.");
        }

        TransaccionBD.ejecutar(con -> {
            t.setEstado("ANULADO");
            repository.update(con, t);

            RegistroAnulacion r = new RegistroAnulacion();
            r.setTablaAfectada("Tratamiento");
            r.setIdRegistroAnulado(tratamientoID);
            r.setMotivo(motivo);
            r.setUsuario("SYSTEM");
            r.setClinicaID(t.getClinicaID());
            anulacionRepository.insert(con, r);
        });
    }

    public void reabrir(int tratamientoID) throws SQLException {
        TransaccionBD.ejecutar(con -> {
            Tratamiento t = repository.findById(tratamientoID);
            if (t == null) throw new NegocioException("El tratamiento no existe.");
            if (!"CERRADO".equals(t.getEstado())) throw new NegocioException("Solo se puede reabrir un tratamiento cerrado.");
            if (t.getUnidadID() != null && repository.existeOtroAbiertoEnUnidad(t.getUnidadID(), tratamientoID))
                throw new NegocioException("La unidad de tratamiento ya está ocupada por otro tratamiento activo.");
            t.setEstado("ABIERTO");
            t.setCerradoEn(null);
            repository.update(con, t);
        });
    }

    public void cambiarTipo(int tratamientoID, String tipo) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if (!"ABIERTO".equals(t.getEstado())) {
            throw new NegocioException("Solo se puede cambiar el tipo en tratamientos abiertos.");
        }
        String tipoNormalizado = normalizarTipo(tipo);
        if (tipoNormalizado.equals(t.getTipo())) {
            throw new NegocioException("El tratamiento ya es de tipo " + tipoNormalizado + ".");
        }

        TransaccionBD.ejecutar(con -> {
            t.setTipo(tipoNormalizado);
            if ("CONTINUO".equals(tipoNormalizado)) {
                t.setMontoAnterior(t.getMonto());
                t.setMonto(0);
                t.setEstadoPago("PAGADO");
                t.setMontoPagado(0);
            } else {
                double anterior = t.getMontoAnterior() != null ? t.getMontoAnterior() : 0;
                t.setMonto(anterior);
                t.setMontoAnterior(null);
                t.setEstadoPago("PENDIENTE");
                t.setMontoPagado(0);
            }
            repository.update(con, t);
        });
    }

    public void editarRetroactivo(int tratamientoID, EditarRetroactivoDto dto) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if (!"CERRADO".equals(t.getEstado())) {
            throw new NegocioException("Solo se pueden editar tratamientos cerrados de forma retroactiva.");
        }

        TransaccionBD.ejecutar(con -> {
            boolean continuo;
            if (dto.tipo != null) {
                String tipoNormalizado = normalizarTipo(dto.tipo);
                t.setTipo(tipoNormalizado);
                continuo = "CONTINUO".equals(tipoNormalizado);
            } else {
                continuo = "CONTINUO".equals(t.getTipo());
            }

            if (continuo) {
                t.setMonto(0);
                t.setMontoPagado(0);
                t.setEstadoPago("PAGADO");
            } else {
                if (dto.monto != null) {
                    if (dto.monto < 0) {
                        throw new NegocioException("El monto del tratamiento no puede ser negativo.");
                    }
                    t.setMonto(dto.monto);
                }
                if (dto.montoPagado != null) {
                    if (dto.montoPagado < 0) {
                        throw new NegocioException("El monto pagado no puede ser negativo.");
                    }
                    t.setMontoPagado(dto.montoPagado);
                }
                if (t.getMontoPagado() > t.getMonto() + EPSILON) {
                    throw new NegocioException("El monto pagado no puede superar el monto total.");
                }
                t.setEstadoPago(derivarEstadoPago(t.getMontoPagado(), t.getMonto()));
            }

            if (dto.fecha != null) {
                validarFecha(dto.fecha);
                t.setFecha(dto.fecha);
            }
            if (dto.nombreTratamiento != null) {
                t.setNombreTratamiento(dto.nombreTratamiento);
            }
            if (dto.operadorID != null) {
                validarEspecialista(dto.operadorID);
                t.setOperadorID(dto.operadorID);
            }
            if (dto.pacienteID != null) {
                validarPaciente(dto.pacienteID);
                t.setPacienteID(dto.pacienteID);
            }

            repository.update(con, t);

            if (dto.cantidadesMateriales != null) {
                List<TratamientoMaterial> actuales = materialRepository.findByTratamientoID(tratamientoID);
                for (Map.Entry<Integer, Double> entry : dto.cantidadesMateriales.entrySet()) {
                    int materialID = entry.getKey();
                    double nuevaCantidad = entry.getValue();
                    if (nuevaCantidad <= 0) {
                        TratamientoMaterial aEliminar = actuales.stream()
                                .filter(m -> m.getMaterialID() == materialID)
                                .findFirst().orElse(null);
                        if (aEliminar != null) {
                            materialRepository.delete(con, aEliminar.getMaterialesListID());
                        }
                    } else {
                        TratamientoMaterial existente = actuales.stream()
                                .filter(m -> m.getMaterialID() == materialID)
                                .findFirst().orElse(null);
                        if (existente != null) {
                            existente.setCantidad(nuevaCantidad);
                            materialRepository.update(con, existente);
                        } else {
                            TratamientoMaterial nuevo = new TratamientoMaterial();
                            nuevo.setTratamientoID(tratamientoID);
                            nuevo.setMaterialID(materialID);
                            nuevo.setCantidad(nuevaCantidad);
                            materialRepository.insert(con, nuevo);
                        }
                    }
                }
            }
        });
    }

    public void registrarPago(int tratamientoID, double abono) throws SQLException {
        registrarPago(tratamientoID, abono, null);
    }

    public void registrarPago(int tratamientoID, double abono, String fecha) throws SQLException {
        if (abono <= 0) {
            throw new NegocioException("El abono debe ser mayor a 0.");
        }
        if (fecha != null) {
            validarFecha(fecha);
        }
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if ("ANULADO".equals(t.getEstado())) {
            throw new NegocioException("No se puede registrar pago sobre un tratamiento anulado.");
        }
        if ("CONTINUO".equals(t.getTipo())) {
            throw new NegocioException("Un tratamiento continuo no requiere pago.");
        }

        if ("CONTINUO".equals(t.getTipo())) {
            throw new NegocioException("Un tratamiento continuo no requiere pago.");
        }

        final Tratamiento target = t;
        TransaccionBD.ejecutar(con -> {
            double nuevoPagado = pagoRepository.sumByTratamiento(con, target.getTratamientoID()) + abono;
            if (nuevoPagado > target.getMonto() + EPSILON) {
                throw new NegocioException("El pago supera el monto total del tratamiento.");
            }
            Pago pago = new Pago();
            pago.setTratamientoID(target.getTratamientoID());
            pago.setFecha(fecha != null ? fecha : fechaHoy());
            pago.setMonto(abono);
            pagoRepository.insert(con, pago);
            recalcularPagos(con, target);
        });
    }

    public void editarPago(int pagoID, double monto, String fecha) throws SQLException {
        if (monto <= 0) {
            throw new NegocioException("El monto del pago debe ser mayor a 0.");
        }
        if (fecha != null) {
            validarFecha(fecha);
        }
        Pago pago = pagoRepository.findById(pagoID);
        if (pago == null) {
            throw new NegocioException("El pago no existe.");
        }
        Tratamiento t = repository.findById(pago.getTratamientoID());
        if (t == null) {
            throw new NegocioException("El tratamiento del pago no existe.");
        }
        double totalSinPago = pagoRepository.sumByTratamiento(t.getTratamientoID()) - pago.getMonto();
        double nuevoPagado = totalSinPago + monto;
        if (nuevoPagado > t.getMonto() + EPSILON) {
            throw new NegocioException("El pago editado supera el monto total del tratamiento.");
        }
        TransaccionBD.ejecutar(con -> {
            pago.setMonto(monto);
            if (fecha != null) {
                pago.setFecha(fecha);
            }
            pagoRepository.update(con, pago);
            recalcularPagos(con, t);
        });
    }

    public void eliminarPago(int pagoID) throws SQLException {
        Pago pago = pagoRepository.findById(pagoID);
        if (pago == null) {
            throw new NegocioException("El pago no existe.");
        }
        Tratamiento t = repository.findById(pago.getTratamientoID());
        if (t == null) {
            throw new NegocioException("El tratamiento del pago no existe.");
        }
        TransaccionBD.ejecutar(con -> {
            pagoRepository.delete(con, pagoID);
            recalcularPagos(con, t);
        });
    }

    public void editarEnCurso(int tratamientoID, EditarRetroactivoDto dto) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if (!"ABIERTO".equals(t.getEstado())) {
            throw new NegocioException("Solo se pueden editar tratamientos en curso (abiertos).");
        }

        TransaccionBD.ejecutar(con -> {
            if (dto.fecha != null) {
                validarFecha(dto.fecha);
                t.setFecha(dto.fecha);
            }
            if (dto.nombreTratamiento != null) {
                t.setNombreTratamiento(dto.nombreTratamiento);
            }
            if (dto.operadorID != null) {
                validarEspecialista(dto.operadorID);
                t.setOperadorID(dto.operadorID);
            }
            if (dto.pacienteID != null) {
                validarPaciente(dto.pacienteID);
                t.setPacienteID(dto.pacienteID);
            }
            if (dto.monto != null) {
                if (dto.monto < 0) {
                    throw new NegocioException("El monto del tratamiento no puede ser negativo.");
                }
                if ("CONTINUO".equals(t.getTipo())) {
                    throw new NegocioException("Un tratamiento continuo no tiene monto editable.");
                }
                t.setMonto(dto.monto);
                t.setEstadoPago(derivarEstadoPago(t.getMontoPagado(), dto.monto));
            }
            repository.update(con, t);
        });
    }

    public List<Tratamiento> cerradosConSaldo() throws SQLException {
        return cerradosConSaldo(1);
    }

    public List<Tratamiento> cerradosConSaldo(int clinicaID) throws SQLException {
        List<Tratamiento> resultado = new ArrayList<>();
        for (Tratamiento t : repository.findByEstado("CERRADO", clinicaID)) {
            if (requiereAdvertenciaPago(t) && t.getMonto() - t.getMontoPagado() > EPSILON) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    public List<Pago> pagosDe(int tratamientoID) throws SQLException {
        return pagoRepository.findByTratamiento(tratamientoID);
    }

    public boolean requiereAdvertenciaPago(Tratamiento t) {
        if ("CONTINUO".equals(t.getTipo())) {
            return false;
        }
        return !"PAGADO".equals(t.getEstadoPago());
    }

    private void recalcularPagos(Connection con, Tratamiento t) throws SQLException {
        double pagado = pagoRepository.sumByTratamiento(con, t.getTratamientoID());
        t.setMontoPagado(pagado);
        t.setEstadoPago(derivarEstadoPago(pagado, t.getMonto()));
        repository.update(con, t);
    }

    private List<TratamientoPredefinidoMaterial> cargarPlantilla(Tratamiento tratamiento, int tratPredID,
                                                                  Double monto) throws SQLException {
        TratamientoPredefinido predefinido = predefinidoRepository.findById(tratPredID);
        if (predefinido == null) {
            throw new NegocioException("El tipo de tratamiento seleccionado no existe.");
        }
        tratamiento.setNombreTratamiento(predefinido.getNombreTratamiento());
        if ("NORMAL".equals(tratamiento.getTipo())) {
            double montoReal = monto != null ? monto
                    : (predefinido.getMontoSugerido() != null ? predefinido.getMontoSugerido() : 0);
            if (montoReal < 0) {
                throw new NegocioException("El monto del tratamiento no puede ser negativo.");
            }
            tratamiento.setMonto(montoReal);
        }
        return predefinidoMaterialRepository.findByTratPredID(tratPredID);
    }

    private void validarAbierto(int tratamientoID) throws SQLException {
        Tratamiento t = repository.findById(tratamientoID);
        if (t == null) {
            throw new NegocioException("El tratamiento no existe.");
        }
        if (!"ABIERTO".equals(t.getEstado())) {
            throw new NegocioException("El tratamiento no está abierto; no se pueden modificar sus materiales.");
        }
    }

    private void validarEspecialista(int operadorID) throws SQLException {
        if (operadorRepository.findById(operadorID) == null) {
            throw new NegocioException("El especialista seleccionado no existe.");
        }
    }

    private void validarPaciente(int pacienteID) throws SQLException {
        if (pacienteRepository.findById(pacienteID) == null) {
            throw new NegocioException("El paciente seleccionado no existe.");
        }
    }

    private void validarMaterialExiste(int materialID) throws SQLException {
        Materiales m = materialRepositoryCatalogo.findById(materialID);
        if (m == null) {
            throw new NegocioException("El material seleccionado no existe.");
        }
        if (m.getEstado() != 1) {
            throw new NegocioException("El material '" + m.getNombre() + "' está inactivo y no puede usarse.");
        }
    }

    private void validarFecha(String fecha) {
        if (fecha == null || !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new NegocioException("La fecha debe tener el formato AAAA-MM-DD.");
        }
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null) {
            return "NORMAL";
        }
        String t = tipo.trim().toUpperCase();
        if (!t.equals("NORMAL") && !t.equals("CONTINUO")) {
            throw new NegocioException("El tipo de tratamiento debe ser NORMAL o CONTINUO.");
        }
        return t;
    }

    private String fechaHoy() {
        return java.time.LocalDate.now().toString();
    }

    private String derivarEstadoPago(double pagado, double monto) {
        if (pagado <= EPSILON) {
            return "PENDIENTE";
        }
        if (pagado + EPSILON < monto) {
            return "PARCIAL";
        }
        return "PAGADO";
    }

    private String timestampLocal() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static class EditarRetroactivoDto {
        public String tipo;
        public Double monto;
        public Double montoPagado;
        public String estadoPago;
        public String fecha;
        public String nombreTratamiento;
        public Integer operadorID;
        public Integer pacienteID;
        public Map<Integer, Double> cantidadesMateriales;
    }

    public static class ConsolidadoDto {
        public Tratamiento tratamiento;
        public List<MaterialAvanceRepository.MaterialConsolidado> materiales;
        public List<Pago> pagos;
    }
}
