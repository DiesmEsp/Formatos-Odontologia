package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.TratamientoPredefinido;
import com.odontologia.formatos.model.TratamientoPredefinidoMaterial;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.repository.TratamientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

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
    void crearConPlantillaCargaMateriales() throws SQLException {
        int predID = crearPlantillaConMateriales();

        int id = service.crear(operadorID, pacienteID, 1, "2026-08-03", predID, null, "NORMAL");

        Tratamiento t = tratamientoRepository.findById(id);
        assertEquals("Exodoncia simple", t.getNombreTratamiento());
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

    private int crearPlantillaConMateriales() throws SQLException {
        TratamientoPredefinido pred = new TratamientoPredefinido();
        pred.setNombreTratamiento("Exodoncia simple");
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
