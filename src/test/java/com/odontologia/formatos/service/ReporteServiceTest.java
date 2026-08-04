package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.model.AsistenciaMaterial;
import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.model.Paciente;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.model.TratamientoMaterial;
import com.odontologia.formatos.model.UnidadConversion;
import com.odontologia.formatos.repository.AsistenciaMaterialRepository;
import com.odontologia.formatos.repository.AsistenciaRepository;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.ReporteRepository;
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoRepository;
import com.odontologia.formatos.repository.UnidadConversionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica las agregaciones de reportes: filtro de mes, conversión a unidad base
 * (factor 1 si no hay conversión) y exclusión de estados ABIERTO/ANULADO.
 */
class ReporteServiceTest extends BaseRepositoryTest {

    private final ReporteService service = new ReporteService();
    private final MaterialRepository materialRepository = new MaterialRepository();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private final TratamientoRepository tratamientoRepository = new TratamientoRepository();
    private final TratamientoMaterialRepository tratamientoMaterialRepository = new TratamientoMaterialRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final AsistenciaRepository asistenciaRepository = new AsistenciaRepository();
    private final AsistenciaMaterialRepository asistenciaMaterialRepository = new AsistenciaMaterialRepository();
    private final UnidadConversionRepository conversionRepository = new UnidadConversionRepository();

    private int algodon;   // unidad 'bolsa', conversión -> gramo (factor 500)
    private int anestesia; // unidad 'cartucho', sin conversión
    private int operadorID;
    private int pacienteID;
    private int docenteID;

    @BeforeEach
    void sembrarDatos() throws SQLException {
        algodon = insertarMaterial("Algodón prueba", "bolsa");
        anestesia = insertarMaterial("Anestesia prueba", "cartucho");

        UnidadConversion c = new UnidadConversion();
        c.setMaterialID(algodon);
        c.setUnidadBase("gramo");
        c.setUnidadEmpaque("bolsa");
        c.setFactor(500);
        conversionRepository.insert(c);

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

        Docente d = new Docente();
        d.setNombres("Carlos");
        d.setApellidos("Rojas");
        d.setEstado(1);
        docenteID = docenteRepository.insert(d);

        insertarTratamiento("2024-10-15", "CERRADO", 100.0, 40.0, true);
        insertarTratamiento("2024-10-20", "CERRADO", 50.0, 50.0, false);
        insertarTratamiento("2024-11-05", "CERRADO", 80.0, 0.0, true);
        insertarTratamiento("2024-10-10", "ABIERTO", 60.0, 0.0, true);
        insertarTratamiento("2024-10-12", "ANULADO", 999.0, 0.0, true);

        insertarAsistencia("2024-10-15", "ACTIVO", true);
        insertarAsistencia("2024-10-16", "ANULADO", true);
        insertarAsistencia("2024-11-02", "ACTIVO", false);
    }

    @Test
    void materialesConvierteAUndadBase() throws SQLException {
        List<ReporteRepository.FilaMaterial> filas = service.materiales(2024, 10);

        FilaMaterialHelper alg = fila(filas, algodon);
        assertEquals("gramo", alg.unidadBase());
        assertEquals(1500.0, alg.cantidadTotal(), 0.001);

        FilaMaterialHelper anes = fila(filas, anestesia);
        assertEquals("cartucho", anes.unidadBase());
        assertEquals(5.0, anes.cantidadTotal(), 0.001);
    }

    @Test
    void materialesIgnoraOtroMesYEstados() throws SQLException {
        List<ReporteRepository.FilaMaterial> filasNov = service.materiales(2024, 11);
        FilaMaterialHelper anesNov = fila(filasNov, anestesia);
        assertEquals(3.0, anesNov.cantidadTotal(), 0.001);
        assertTrue(fila(filasNov, algodon).cantidadTotal() > 0);
    }

    @Test
    void ingresosAgrupaPorGradoYTipo() throws SQLException {
        List<ReporteRepository.FilaIngreso> filas = service.ingresos(2024, 10);

        assertEquals(1, filas.size());
        ReporteRepository.FilaIngreso f = filas.get(0);
        assertEquals("PRE", f.getGrado());
        assertEquals("4", f.getTipo());
        assertEquals(2, f.getCantidadTratamientos());
        assertEquals(150.0, f.getIngresoTotal(), 0.001);
        assertEquals(90.0, f.getMontoPagado(), 0.001);
        assertEquals(60.0, f.getMontoPendiente(), 0.001);
    }

    @Test
    void ingresosDeMesSinDatosEsVacio() throws SQLException {
        assertTrue(service.ingresos(2024, 7).isEmpty());
    }

    @Test
    void docenteConsolidado() throws SQLException {
        List<ReporteRepository.FilaDocente> filas = service.docenteConsolidado(2024, 10);

        assertEquals(2, filas.size());
        ReporteRepository.FilaDocente alg = filaDocente(filas, algodon);
        assertEquals("Carlos Rojas", alg.getDocente());
        assertEquals("gramo", alg.getUnidad());
        assertEquals(500.0, alg.getCantidad(), 0.001);
        assertTrue(filaDocente(filas, anestesia).getCantidad() > 0);
    }

    @Test
    void docenteDetalleDia() throws SQLException {
        List<ReporteRepository.FilaDocente> filas = service.docenteDetalleDia(2024, 10);

        assertEquals(2, filas.size());
        assertTrue(filas.stream().allMatch(f -> "2024-10-15".equals(f.getDia())));
    }

    @Test
    void especialista() throws SQLException {
        List<ReporteRepository.FilaEspecialista> filas = service.especialista(2024, 10);

        assertEquals(2, filas.size());
        ReporteRepository.FilaEspecialista alg = filaEspecialista(filas, algodon);
        assertEquals("Ana Perez", alg.getEspecialista());
        assertEquals("PRE", alg.getGrado());
        assertEquals("gramo", alg.getUnidad());
        assertEquals(1000.0, alg.getCantidad(), 0.001);
    }

    @Test
    void ingresosPorTratamiento() throws SQLException {
        List<ReporteRepository.FilaIngresoTratamiento> filas = service.ingresosPorTratamiento(2024, 10);

        assertEquals(1, filas.size());
        ReporteRepository.FilaIngresoTratamiento f = filas.get(0);
        assertEquals("Tratamiento test", f.getTratamiento());
        assertEquals(2, f.getCantidadTratamientos());
        assertEquals(150.0, f.getIngresoTotal(), 0.001);
        assertEquals(90.0, f.getMontoPagado(), 0.001);
        assertEquals(60.0, f.getMontoPendiente(), 0.001);
    }

    @Test
    void ingresosPorOperador() throws SQLException {
        List<ReporteRepository.FilaIngresoOperador> filas = service.ingresosPorOperador(2024, 10);

        assertEquals(1, filas.size());
        ReporteRepository.FilaIngresoOperador f = filas.get(0);
        assertEquals("Ana Perez", f.getNombre());
        assertEquals("PRE", f.getGrado());
        assertEquals("4", f.getTipo());
        assertEquals("Tratamiento test", f.getTratamiento());
        assertEquals(2, f.getCantidad());
        assertEquals(150.0, f.getIngresoTotal(), 0.001);
        assertEquals(90.0, f.getMontoPagado(), 0.001);
        assertEquals(60.0, f.getMontoPendiente(), 0.001);
    }

    @Test
    void rechazaMesInvalido() {
        assertThrows(NegocioException.class, () -> service.materiales(2024, 0));
        assertThrows(NegocioException.class, () -> service.materiales(2024, 13));
    }

    private int insertarMaterial(String nombre, String unidad) throws SQLException {
        Materiales m = new Materiales();
        m.setNombre(nombre);
        m.setUnidad(unidad);
        m.setEstado(1);
        return materialRepository.insert(m);
    }

    private void insertarTratamiento(String fecha, String estado, double monto, double pagado,
                                     boolean conMateriales) throws SQLException {
        Tratamiento t = new Tratamiento();
        t.setOperadorID(operadorID);
        t.setPacienteID(pacienteID);
        t.setUnidadID(null);
        t.setFecha(fecha);
        t.setNombreTratamiento("Tratamiento test");
        t.setMonto(monto);
        t.setTipo("NORMAL");
        t.setEstadoPago(pagado <= 0 ? "PENDIENTE" : (pagado >= monto ? "PAGADO" : "PARCIAL"));
        t.setMontoPagado(pagado);
        t.setEstado(estado);
        t.setCerradoEn("CERRADO".equals(estado) ? "2026-01-01 10:00:00" : null);
        int id = tratamientoRepository.insert(t);
        if (conMateriales) {
            insertarConsumoTratamiento(id, algodon, 2.0);
            insertarConsumoTratamiento(id, anestesia, 3.0);
        }
    }

    private void insertarConsumoTratamiento(int tratamientoID, int materialID, double cantidad)
            throws SQLException {
        TratamientoMaterial item = new TratamientoMaterial();
        item.setTratamientoID(tratamientoID);
        item.setMaterialID(materialID);
        item.setCantidad(cantidad);
        tratamientoMaterialRepository.insert(item);
    }

    private void insertarAsistencia(String fecha, String estado, boolean conMateriales) throws SQLException {
        Asistencia a = new Asistencia();
        a.setDocenteID(docenteID);
        a.setFecha(fecha);
        a.setEstado(estado);
        int id = asistenciaRepository.insert(a);
        if (conMateriales) {
            insertarConsumoAsistencia(id, algodon, 1.0);
            insertarConsumoAsistencia(id, anestesia, 2.0);
        }
    }

    private void insertarConsumoAsistencia(int asistenciaID, int materialID, double cantidad)
            throws SQLException {
        AsistenciaMaterial item = new AsistenciaMaterial();
        item.setAsistenciaID(asistenciaID);
        item.setMaterialID(materialID);
        item.setCantidad(cantidad);
        asistenciaMaterialRepository.insert(item);
    }

    private FilaMaterialHelper fila(List<ReporteRepository.FilaMaterial> filas, int materialID) {
        ReporteRepository.FilaMaterial f = filas.stream()
                .filter(item -> item.getMaterialID() == materialID)
                .findFirst()
                .orElseThrow();
        return new FilaMaterialHelper(f.getUnidadBase(), f.getCantidadTotal());
    }

    private ReporteRepository.FilaDocente filaDocente(List<ReporteRepository.FilaDocente> filas, int materialID) {
        return filas.stream()
                .filter(f -> f.getMaterialID() == materialID)
                .findFirst()
                .orElseThrow();
    }

    private ReporteRepository.FilaEspecialista filaEspecialista(
            List<ReporteRepository.FilaEspecialista> filas, int materialID) {
        return filas.stream()
                .filter(f -> f.getMaterialID() == materialID)
                .findFirst()
                .orElseThrow();
    }

    private record FilaMaterialHelper(String unidadBase, double cantidadTotal) {
    }
}
