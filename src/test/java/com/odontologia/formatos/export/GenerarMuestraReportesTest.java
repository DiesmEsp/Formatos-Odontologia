package com.odontologia.formatos.export;

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
import com.odontologia.formatos.repository.TratamientoMaterialRepository;
import com.odontologia.formatos.repository.TratamientoRepository;
import com.odontologia.formatos.repository.UnidadConversionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Genera una muestra real de los reportes Excel con datos de ejemplo en
 * {@code Reportes/muestra/} (carpeta gitignoreada). Sirve para inspeccionar
 * visualmente el formato antes de la UI.
 */
class GenerarMuestraReportesTest extends BaseRepositoryTest {

    private static final Path CARPETA_SALIDA = Paths.get("Reportes", "muestra").toAbsolutePath();
    private static final int ANIO = 2024;

    private final MaterialRepository materialRepository = new MaterialRepository();
    private final OperadorRepository operadorRepository = new OperadorRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();
    private final TratamientoRepository tratamientoRepository = new TratamientoRepository();
    private final TratamientoMaterialRepository tratamientoMaterialRepository = new TratamientoMaterialRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final AsistenciaRepository asistenciaRepository = new AsistenciaRepository();
    private final AsistenciaMaterialRepository asistenciaMaterialRepository = new AsistenciaMaterialRepository();
    private final UnidadConversionRepository conversionRepository = new UnidadConversionRepository();

    private int algodon;
    private int anestesia;
    private int gasa;
    private int guantes;
    private int resina;
    private int hilo;
    private int ana;
    private int luis;
    private int maria;
    private int juan;
    private int pedro;
    private int lucia;
    private int carlos;
    private int elena;

    @BeforeEach
    void sembrarDatos() throws SQLException {
        algodon = materialPorNombre("Algodón 500 g");
        anestesia = materialPorNombre("Anestesia con epinefrina");
        gasa = materialPorNombre("Gasa");
        guantes = materialPorNombre("Guantes descartables M");
        resina = materialPorNombre("Resina compuesta");
        hilo = materialPorNombre("Hilo de sutura nylon 4/0");

        conversion(algodon, "gramo", "bolsa", 500);
        conversion(guantes, "guante", "caja", 100);
        conversion(gasa, "unidad", "paquete", 20);

        ana = operador("Ana", "Perez", "PRE", "4");
        luis = operador("Luis", "Gomez", "PRE", "5");
        maria = operador("Maria", "Diaz", "POS", "R1");

        juan = paciente("Juan", "Lopez");
        pedro = paciente("Pedro", "Sanchez");
        lucia = paciente("Lucia", "Fernandez");

        carlos = docente("Carlos", "Rojas");
        elena = docente("Elena", "Torres");

        // Septiembre
        tratamiento("2024-09-05", "CERRADO", "Exodoncia simple", 120.0, 120.0,
                ana, juan, List.of(material(anestesia, 1.0), material(gasa, 2.0), material(algodon, 0.5)));
        asistencia("2024-09-10", "ACTIVO", carlos, List.of(material(algodon, 1.0), material(gasa, 2.0)));

        // Octubre
        tratamiento("2024-10-03", "CERRADO", "Exodoncia simple", 120.0, 120.0,
                ana, juan, List.of(material(anestesia, 2.0), material(gasa, 3.0), material(algodon, 1.0)));
        tratamiento("2024-10-10", "CERRADO", "Resina anterior", 200.0, 100.0,
                luis, pedro, List.of(material(resina, 1.0), material(algodon, 0.5), material(anestesia, 1.0)));
        tratamiento("2024-10-18", "CERRADO", "Tratamiento continuo", 0.0, 0.0,
                maria, lucia, List.of(material(anestesia, 3.0), material(guantes, 2.0), material(hilo, 1.0)),
                "CONTINUO");
        tratamiento("2024-10-22", "CERRADO", "Periodoncia", 150.0, 0.0,
                luis, pedro, List.of(material(guantes, 2.0), material(anestesia, 2.0)));
        tratamiento("2024-10-28", "ABIERTO", "Ortodoncia inicial", 300.0, 0.0,
                ana, juan, List.of(material(guantes, 1.0)));
        tratamiento("2024-10-20", "ANULADO", "Blanqueamiento", 250.0, 0.0,
                maria, pedro, List.of(material(resina, 2.0)));

        asistencia("2024-10-02", "ACTIVO", carlos, List.of(material(algodon, 2.0), material(guantes, 1.0)));
        asistencia("2024-10-08", "ACTIVO", elena, List.of(material(anestesia, 5.0), material(gasa, 2.0)));
        asistencia("2024-10-16", "ACTIVO", carlos, List.of(material(guantes, 3.0), material(hilo, 2.0)));
        asistencia("2024-10-25", "ANULADO", elena, List.of(material(algodon, 100.0)));

        // Noviembre
        tratamiento("2024-11-12", "CERRADO", "Exodoncia simple", 120.0, 60.0,
                ana, lucia, List.of(material(anestesia, 2.0), material(gasa, 4.0)));
        asistencia("2024-11-05", "ACTIVO", elena, List.of(material(anestesia, 4.0), material(algodon, 1.0)));
    }

    @Test
    void generarMuestraOctubre2024YAnual() throws Exception {
        Files.createDirectories(CARPETA_SALIDA);

        Path materialesMensual = new ReporteMaterialesGenerator().generar(ANIO, 10, CARPETA_SALIDA);
        Path materialesAnual = new ReporteMaterialesGenerator().generar(ANIO, 1, 12, CARPETA_SALIDA);
        Path economicoMensual = new ReporteEconomicoGenerator().generar(ANIO, 10, CARPETA_SALIDA);
        Path economicoAnual = new ReporteEconomicoGenerator().generar(ANIO, 1, 12, CARPETA_SALIDA);

        assertTrue(Files.exists(materialesMensual));
        assertTrue(Files.exists(materialesAnual));
        assertTrue(Files.exists(economicoMensual));
        assertTrue(Files.exists(economicoAnual));
    }

    private int materialPorNombre(String nombre) throws SQLException {
        Materiales m = materialRepository.findByNombre(nombre);
        if (m == null) {
            throw new IllegalStateException("Material no encontrado en seed: " + nombre);
        }
        return m.getMaterialID();
    }

    private void conversion(int materialID, String unidadBase, String unidadEmpaque, double factor)
            throws SQLException {
        UnidadConversion c = new UnidadConversion();
        c.setMaterialID(materialID);
        c.setUnidadBase(unidadBase);
        c.setUnidadEmpaque(unidadEmpaque);
        c.setFactor(factor);
        conversionRepository.insert(c);
    }

    private int operador(String nombres, String apellidos, String grado, String tipo) throws SQLException {
        Operador o = new Operador();
        o.setNombres(nombres);
        o.setApellidos(apellidos);
        o.setGrado(grado);
        o.setTipo(tipo);
        o.setPeriodo(2026);
        o.setEstado(1);
        return operadorRepository.insert(o);
    }

    private int paciente(String nombres, String apellidos) throws SQLException {
        Paciente p = new Paciente();
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        return pacienteRepository.insert(p);
    }

    private int docente(String nombres, String apellidos) throws SQLException {
        Docente d = new Docente();
        d.setNombres(nombres);
        d.setApellidos(apellidos);
        d.setEstado(1);
        return docenteRepository.insert(d);
    }

    private TratamientoMaterial material(int materialID, double cantidad) {
        TratamientoMaterial item = new TratamientoMaterial();
        item.setMaterialID(materialID);
        item.setCantidad(cantidad);
        return item;
    }

    private void tratamiento(String fecha, String estado, String nombre, double monto, double pagado,
                             int operadorID, int pacienteID, List<TratamientoMaterial> materiales)
            throws SQLException {
        tratamiento(fecha, estado, nombre, monto, pagado, operadorID, pacienteID, materiales, "NORMAL");
    }

    private void tratamiento(String fecha, String estado, String nombre, double monto, double pagado,
                             int operadorID, int pacienteID, List<TratamientoMaterial> materiales, String tipo)
            throws SQLException {
        Tratamiento t = new Tratamiento();
        t.setOperadorID(operadorID);
        t.setPacienteID(pacienteID);
        t.setUnidadID(null);
        t.setFecha(fecha);
        t.setNombreTratamiento(nombre);
        t.setMonto(monto);
        t.setTipo(tipo);
        t.setEstadoPago(pagado <= 0 ? "PENDIENTE" : (pagado >= monto ? "PAGADO" : "PARCIAL"));
        t.setMontoPagado(pagado);
        t.setEstado(estado);
        t.setCerradoEn("CERRADO".equals(estado) ? fecha + " 18:00:00" : null);
        int id = tratamientoRepository.insert(t);
        for (TratamientoMaterial item : materiales) {
            item.setTratamientoID(id);
            tratamientoMaterialRepository.insert(item);
        }
    }

    private void asistencia(String fecha, String estado, int docenteID, List<TratamientoMaterial> materiales)
            throws SQLException {
        Asistencia a = new Asistencia();
        a.setDocenteID(docenteID);
        a.setFecha(fecha);
        a.setEstado(estado);
        int id = asistenciaRepository.insert(a);
        for (TratamientoMaterial item : materiales) {
            AsistenciaMaterial am = new AsistenciaMaterial();
            am.setAsistenciaID(id);
            am.setMaterialID(item.getMaterialID());
            am.setCantidad(item.getCantidad());
            asistenciaMaterialRepository.insert(am);
        }
    }
}
