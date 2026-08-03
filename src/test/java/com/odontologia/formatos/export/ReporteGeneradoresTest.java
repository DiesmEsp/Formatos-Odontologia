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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que los generadores escriban archivos Excel válidos con el nombre
 * correcto (RNF-2.3.2) y con los datos esperados (RF-1.7.2 a RF-1.7.6).
 */
class ReporteGeneradoresTest extends BaseRepositoryTest {

    @TempDir
    Path carpeta;

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

        insertarAsistencia("2024-10-15", "ACTIVO", true);
    }

    @Test
    void materialesGeneraArchivoConNombreYContenido() throws Exception {
        Path archivo = new ReporteMaterialesGenerator().generar(2024, 10, carpeta);

        assertEquals("Materiales_Octubre_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            Sheet hoja = libro.getSheet("Materiales");
            assertNotNull(hoja);
            assertEquals("Material", texto(hoja, 0, 0));
            assertEquals("Algodón prueba", texto(hoja, 1, 0));
            assertEquals("gramo", texto(hoja, 1, 1));
            assertEquals(1500.0, numero(hoja, 1, 2), 0.001);
        }
    }

    @Test
    void ingresosGeneraArchivoConAgrupacion() throws Exception {
        Path archivo = new ReporteIngresosGenerator().generar(2024, 10, carpeta);

        assertEquals("Ingresos_Octubre_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            Sheet hoja = libro.getSheet("Ingresos");
            assertNotNull(hoja);
            assertEquals("PRE", texto(hoja, 1, 0));
            assertEquals("4", texto(hoja, 1, 1));
            assertEquals(2.0, numero(hoja, 1, 2), 0.001);
            assertEquals(150.0, numero(hoja, 1, 3), 0.001);
            assertEquals(90.0, numero(hoja, 1, 4), 0.001);
            assertEquals(60.0, numero(hoja, 1, 5), 0.001);
        }
    }

    @Test
    void docenteGeneraConsolidadoYDetalleDiario() throws Exception {
        Path archivo = new ReporteDocenteGenerator().generar(2024, 10, carpeta);

        assertEquals("Docente_Octubre_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            Sheet consolidado = libro.getSheet("Consolidado");
            Sheet detalle = libro.getSheet("Detalle diario");
            assertNotNull(consolidado);
            assertNotNull(detalle);
            assertEquals("Carlos Rojas", texto(consolidado, 1, 0));
            assertEquals(1.0, numero(consolidado, 1, 3), 0.001);
            assertEquals("2024-10-15", texto(detalle, 1, 1));
        }
    }

    @Test
    void especialistaGeneraArchivo() throws Exception {
        Path archivo = new ReporteEspecialistaGenerator().generar(2024, 10, carpeta);

        assertEquals("Especialista_Octubre_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            Sheet hoja = libro.getSheet("Especialista");
            assertNotNull(hoja);
            assertEquals("Ana Perez", texto(hoja, 1, 0));
            assertEquals(2.0, numero(hoja, 1, 5), 0.001);
        }
    }

    @Test
    void anualGeneraCuatroHojasConTotales() throws Exception {
        Path archivo = new ReporteAnualGenerator().generar(2024, 1, carpeta);

        assertEquals("Anual_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            assertEquals(4, libro.getNumberOfSheets());
            Sheet materiales = libro.getSheet("Materiales");
            assertNotNull(materiales);
            assertEquals("Ene", texto(materiales, 0, 2));
            assertEquals("Total", texto(materiales, 0, 14));
            int filaAlgodon = buscarFila(materiales, 0, "Algodón prueba");
            assertTrue(filaAlgodon > 0);
            assertEquals(1500.0, numero(materiales, filaAlgodon, 2 + 9), 0.001);
            assertEquals(2500.0, numero(materiales, filaAlgodon, 14), 0.001);
            assertNotNull(libro.getSheet("Ingresos"));
            assertNotNull(libro.getSheet("Docente"));
            assertNotNull(libro.getSheet("Especialista"));
        }
    }

    @Test
    void archivoSeEscribeEnCarpetaIndicada() throws Exception {
        Path archivo = new ReporteMaterialesGenerator().generar(2024, 10, carpeta);

        assertTrue(Files.exists(archivo));
        try (Stream<Path> contenido = Files.list(carpeta)) {
            assertEquals(1, contenido.count());
        }
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

    private String texto(Sheet hoja, int fila, int col) {
        Cell celda = hoja.getRow(fila).getCell(col);
        return switch (celda.getCellType()) {
            case STRING -> celda.getStringCellValue();
            case NUMERIC -> String.valueOf(celda.getNumericCellValue());
            default -> "";
        };
    }

    private double numero(Sheet hoja, int fila, int col) {
        return hoja.getRow(fila).getCell(col).getNumericCellValue();
    }

    private int buscarFila(Sheet hoja, int col, String valor) {
        for (int i = 1; i <= hoja.getLastRowNum(); i++) {
            if (valor.equals(texto(hoja, i, col))) {
                return i;
            }
        }
        return -1;
    }
}
