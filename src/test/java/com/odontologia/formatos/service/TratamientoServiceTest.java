package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.TratamientoPredefinido;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.repository.TratamientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TratamientoServiceTest extends BaseRepositoryTest {

    private final TratamientoService service = new TratamientoService();
    private final TratamientoRepository tratamientoRepository = new TratamientoRepository();
    private final TratamientoMaterialRepository materialRepository = new TratamientoMaterialRepository();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private final TratamientoPredefinidoRepository predRepository = new TratamientoPredefinidoRepository();
    private final TratamientoPredefinidoMaterialRepository predMaterialRepository =
            new TratamientoPredefinidoMaterialRepository();
    private final MaterialRepository materialCatalogo = new MaterialRepository();
    private int operadorID;
    private int pacienteID;

    @BeforeEach
    void crearReferencias() throws SQLException {
        Operador o = new Operador();
        o.setNombres("Ana");
        o.setApellidos("Perez");
        o.setGrado("PRE");
        o.setTipo("4");
        o.setPeriodo(2026);
        o.setEstado(1);
        operadorID = operadorRepository.insert(o);

        Paciente p = new Paciente();
        p.setNombres("Juan");
        p.setApellidos("Lopez");
        pacienteID = pacienteRepository.insert(p);
    }

    @Test
    void crearNormalConMonto() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 120.0, "NORMAL");

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals(120.0, t.getMonto(), 0.001);
        assertEquals("ABIERTO", t.getEstado());
        assertEquals("PENDIENTE", t.getEstadoPago());
    }

    @Test
    void crearContinuoFijaMontoCeroYPagado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 999.0, "CONTINUO");

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals(0.0, t.getMonto(), 0.001);
        assertEquals("PAGADO", t.getEstadoPago());
        assertEquals(0.0, t.getMontoPagado(), 0.001);
    }

    @Test
    void crearCerradoConMateriales() throws SQLException {
        Map<Integer, Double> materiales = new HashMap<>();
        materiales.put(1, 2.0);
        materiales.put(2, 3.0);

        int id = service.crearCerrado(operadorID, pacienteID, "2026-08-01", null, 120.0, "NORMAL", materiales);

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("CERRADO", t.getEstado());
        assertEquals(120.0, t.getMonto(), 0.001);
        assertEquals("PAGADO", t.getEstadoPago());
        assertEquals(120.0, t.getMontoPagado(), 0.001);

        List<TratamientoMaterialRepository.MaterialConCantidad> mats = service.materialesConNombre(id);
        assertEquals(2, mats.size());
    }

    @Test
    void crearCerradoConPlantillaUsaNombreYMonto() throws SQLException {
        int predID = crearPlantillaConMateriales();

        int id = service.crearCerrado(operadorID, pacienteID, "2026-08-01", predID, null, "NORMAL", Map.of());

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("Tratamiento de prueba unico", t.getNombreTratamiento());
        assertEquals(150.0, t.getMonto(), 0.001);
        assertEquals("CERRADO", t.getEstado());
    }

    @Test
    void crearCerradoRechazaMontoNegativo() {
        assertThrows(NegocioException.class, () ->
                service.crearCerrado(operadorID, pacienteID, "2026-08-01", null, -5.0, "NORMAL", Map.of()));
    }

    @Test
    void crearCerradoRechazaFechaInvalida() {
        assertThrows(NegocioException.class, () ->
                service.crearCerrado(operadorID, pacienteID, "2026/08/01", null, 120.0, "NORMAL", Map.of()));
    }

    @Test
    void crearConPlantillaCargaMateriales() throws SQLException {
        int predID = crearPlantillaConMateriales();

        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", predID, null, "NORMAL");

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("Tratamiento de prueba unico", t.getNombreTratamiento());
        assertEquals(150.0, t.getMonto(), 0.001);

        List<TratamientoMaterialRepository.MaterialConCantidad> materiales = service.materialesConNombre(id);
        assertEquals(2, materiales.size());
    }

    @Test
    void agregarMaterialAcumulaCantidad() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        service.agregarMaterial(id, 1, 2.0);
        service.agregarMaterial(id, 1, 3.0);

        List<TratamientoMaterialRepository.MaterialConCantidad> materiales = service.materialesConNombre(id);
        assertEquals(1, materiales.size());
        assertEquals(5.0, materiales.get(0).getCantidad(), 0.001);
    }

    @Test
    void agregarMaterialNuevoNoSobrescribe() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        service.agregarMaterial(id, 1, 2.0);
        service.agregarMaterial(id, 2, 1.5);

        assertEquals(2, service.materiales(id).size());
    }

    @Test
    void cerrarConsolida() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.agregarMaterial(id, 1, 2.0);

        service.cerrar(id);

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("CERRADO", t.getEstado());
        assertTrue(t.getCerradoEn() != null && !t.getCerradoEn().isBlank());
    }

    @Test
    void reabrirTratamientoCerrado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.cerrar(id);

        service.reabrir(id);

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("ABIERTO", t.getEstado());
        assertNull(t.getCerradoEn());
    }

    @Test
    void reabrirSoloCerrado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        assertThrows(NegocioException.class, () -> service.reabrir(id));
    }

    @Test
    void noAgregaMaterialACerrado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.cerrar(id);

        assertThrows(NegocioException.class, () -> service.agregarMaterial(id, 1, 1.0));
    }

    @Test
    void registrarPagoParcialDerivaParcial() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        service.registrarPago(id, 40.0);

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("PARCIAL", t.getEstadoPago());
        assertEquals(40.0, t.getMontoPagado(), 0.001);
    }

    @Test
    void registrarPagoCompletoDerivaPagado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        service.registrarPago(id, 60.0);
        service.registrarPago(id, 40.0);

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("PAGADO", t.getEstadoPago());
        assertEquals(100.0, t.getMontoPagado(), 0.001);
    }

    @Test
    void continuoNoRequiereAdvertenciaDePago() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, null, "CONTINUO");

        Tratamiento t = tratamientoRepository.findById(id);
        assertFalse(service.requiereAdvertenciaPago(t));
    }

    @Test
    void normalSinPagoRequiereAdvertencia() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        Tratamiento t = tratamientoRepository.findById(id);
        assertTrue(service.requiereAdvertenciaPago(t));
    }

    @Test
    void rechazaEspecialistaInexistente() {
        assertThrows(NegocioException.class,
                () -> service.crear(9999, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL"));
    }

    @Test
    void rechazaPacienteInexistente() {
        assertThrows(NegocioException.class,
                () -> service.crear(operadorID, 9999, 1, "2026-08-03", null, 100.0, "NORMAL"));
    }

    @Test
    void rechazaFechaInvalida() {
        assertThrows(NegocioException.class,
                () -> service.crear(operadorID, pacienteID, 1, "03/08/2026", null, 100.0, "NORMAL"));
    }

    @Test
    void rechazaTipoInvalido() {
        assertThrows(NegocioException.class,
                () -> service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "OTRO"));
    }

    @Test
    void rechazaAbonoDeContinuo() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, null, "CONTINUO");

        assertThrows(NegocioException.class, () -> service.registrarPago(id, 10.0));
    }

    @Test
    void anularCambiaEstado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        service.anular(id, "Motivo de prueba");

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("ANULADO", t.getEstado());
    }

    @Test
    void anularCerradoCambiaEstado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.cerrar(id);

        service.anular(id, "Motivo de prueba");

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("ANULADO", t.getEstado());
    }

    @Test
    void anularYaAnuladoArrojaError() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.anular(id, "Motivo");

        assertThrows(NegocioException.class, () -> service.anular(id, "Otra vez"));
    }

    @Test
    void anularSinMotivoArrojaError() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        assertThrows(NegocioException.class, () -> service.anular(id, null));
        assertThrows(NegocioException.class, () -> service.anular(id, "  "));
    }

    @Test
    void reabrirConUnidadOcupadaArrojaError() throws SQLException {
        int id1 = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.cerrar(id1);

        int id2 = service.crear(operadorID, pacienteID, 1, "2026-08-05", null, 50.0, "NORMAL");

        assertThrows(NegocioException.class, () -> service.reabrir(id1));
    }

    @Test
    void editarRetroactivoMontoYGasto() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.cerrar(id);

        TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
        dto.monto = 200.0;
        dto.montoPagado = 150.0;
        service.editarRetroactivo(id, dto);

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals(200.0, t.getMonto(), 0.001);
        assertEquals(150.0, t.getMontoPagado(), 0.001);
        assertEquals("PARCIAL", t.getEstadoPago());
    }

    @Test
    void editarRetroactivoMateriales() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.agregarMaterial(id, 1, 2.0);
        service.agregarMaterial(id, 2, 1.0);
        service.cerrar(id);

        Map<Integer, Double> materiales = new HashMap<>();
        materiales.put(1, 5.0);
        materiales.put(2, -1.0);
        TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
        dto.cantidadesMateriales = materiales;
        service.editarRetroactivo(id, dto);

        List<TratamientoMaterialRepository.MaterialConCantidad> lista = service.materialesConNombre(id);
        assertEquals(1, lista.size());
        assertEquals(1, lista.get(0).getMaterialID());
        assertEquals(5.0, lista.get(0).getCantidad(), 0.001);
    }

    @Test
    void editarRetroactivoAgregarMaterial() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");
        service.agregarMaterial(id, 1, 2.0);
        service.cerrar(id);

        Map<Integer, Double> materiales = new HashMap<>();
        materiales.put(2, 3.0);
        TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
        dto.cantidadesMateriales = materiales;
        service.editarRetroactivo(id, dto);

        List<TratamientoMaterialRepository.MaterialConCantidad> lista = service.materialesConNombre(id);
        assertEquals(2, lista.size());
    }

    @Test
    void editarRetroactivoSoloCerrado() throws SQLException {
        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", null, 100.0, "NORMAL");

        TratamientoService.EditarRetroactivoDto dto = new TratamientoService.EditarRetroactivoDto();
        dto.monto = 200.0;
        assertThrows(NegocioException.class, () -> service.editarRetroactivo(id, dto));
    }

    private int crearMaterial(String nombre, String unidad) throws SQLException {
        Materiales m = new Materiales();
        m.setNombre(nombre);
        m.setUnidad(unidad);
        m.setEstado(1);
        return materialCatalogo.insert(m);
    }

    private int crearPlantillaConMateriales() throws SQLException {
        TratamientoPredefinido pred = new TratamientoPredefinido();
        pred.setNombreTratamiento("Tratamiento de prueba unico");
        pred.setMontoSugerido(150.0);
        int predID = predRepository.insert(pred);

        TratamientoPredefinidoMaterial m1 = new TratamientoPredefinidoMaterial();
        m1.setTratPredID(predID);
        m1.setMaterialID(1);
        m1.setCantidad(1.0);
        predMaterialRepository.insert(m1);

        TratamientoPredefinidoMaterial m2 = new TratamientoPredefinidoMaterial();
        m2.setTratPredID(predID);
        m2.setMaterialID(2);
        m2.setCantidad(2.0);
        predMaterialRepository.insert(m2);

        return predID;
    }
}
