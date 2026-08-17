package com.odontologia.formatos.service;

import com.odontologia.formatos.model.RegistroAnulacion;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.TratamientoMaterial;
import com.odontologia.formatos.model.TratamientoPredefinido;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.RegistroAnulacionRepository;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.repository.TratamientoRepository;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.util.TransaccionBD;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Flujo de tratamientos (RF-1.4.x).
 * Creación con carga de plantilla, consumo dinámico acumulativo,
 * cierre con consolidación, reapertura, tipo CONTINUO y pagos.
 */
public class TratamientoService {

    private static final double EPSILON = 0.005;

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

    public int crear(int operadorID, int pacienteID, Integer unidadID, String fecha,
                     Integer tratPredID, Double monto, String tipo) throws SQLException {
        return crear(operadorID, pacienteID, unidadID, fecha, tratPredID, monto, tipo, null);
    }

    public int crear(int operadorID, int pacienteID, Integer unidadID, String fecha,
                     Integer tratPredID, Double monto, String tipo,
                     Map<Integer, Double> materiales) throws SQLException {
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
        return repository.findByEstado("ABIERTO");
    }

    public List<Tratamiento> cerrados() throws SQLException {
        return repository.findByEstado("CERRADO");
    }

    public int crearCerrado(int operadorID, int pacienteID, String fecha, Integer tratPredID,
                            Double monto, String tipo, Map<Integer, Double> materiales) throws SQLException {
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
                t.setMonto(0);
                t.setEstadoPago("PAGADO");
                t.setMontoPagado(0);
            } else {
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
        if (abono <= 0) {
            throw new NegocioException("El abono debe ser mayor a 0.");
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
        TransaccionBD.ejecutar(con -> {
            double nuevoPagado = t.getMontoPagado() + abono;
            if (nuevoPagado > t.getMonto() + EPSILON) {
                throw new NegocioException("El pago supera el monto total del tratamiento.");
            }
            t.setMontoPagado(nuevoPagado);
            t.setEstadoPago(derivarEstadoPago(nuevoPagado, t.getMonto()));
            repository.update(con, t);
        });
    }

    public boolean requiereAdvertenciaPago(Tratamiento t) {
        if ("CONTINUO".equals(t.getTipo())) {
            return false;
        }
        return !"PAGADO".equals(t.getEstadoPago());
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
        if (materialRepositoryCatalogo.findById(materialID) == null) {
            throw new NegocioException("El material seleccionado no existe.");
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
}
