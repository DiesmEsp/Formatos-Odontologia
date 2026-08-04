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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void materialesGeneraArchivoMensual() throws Exception {
        Path archivo = new ReporteMaterialesGenerator().generar(2024, 10, carpeta);

        assertEquals("Materiales_Octubre_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            assertEquals(3, libro.getNumberOfSheets());

            Sheet general = libro.getSheet("General");
            assertNotNull(general);
            assertEquals("Material", general.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Algodón prueba", general.getRow(1).getCell(0).getStringCellValue());
            assertEquals("gramo", general.getRow(1).getCell(1).getStringCellValue());
            assertEquals(1500.0, general.getRow(1).getCell(2).getNumericCellValue(), 0.001);

            Sheet detalleDocente = libro.getSheet("Detalle Docente");
            assertNotNull(detalleDocente);
            assertEquals("Docente: Carlos Rojas", detalleDocente.getRow(0).getCell(0).getStringCellValue());

            Sheet operador = libro.getSheet("Por Operador");
            assertNotNull(operador);
            assertEquals("Operador: Ana Perez (PRE-4)", operador.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Material", operador.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Algodón prueba", operador.getRow(2).getCell(0).getStringCellValue());
            assertEquals("gramo", operador.getRow(2).getCell(1).getStringCellValue());
            assertEquals(1000.0, operador.getRow(2).getCell(2).getNumericCellValue(), 0.001);
        }
    }

    @Test
    void materialesGeneraArchivoAnual() throws Exception {
        Path archivo = new ReporteMaterialesGenerator().generar(2024, 1, 12, carpeta);

        assertEquals("Materiales_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            assertEquals(3, libro.getNumberOfSheets());
            Sheet general = libro.getSheet("General");
            assertNotNull(general);
            assertEquals("Ene", general.getRow(0).getCell(2).getStringCellValue());
            assertEquals("Total", general.getRow(0).getCell(14).getStringCellValue());
        }
    }

    @Test
    void materialesGeneraArchivoSemestral() throws Exception {
        Path archivo = new ReporteMaterialesGenerator().generar(2024, 1, 6, carpeta);

        assertEquals("Materiales_Ene_Jun_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            assertTrue(libro.getNumberOfSheets() >= 3);
        }
    }

    @Test
    void economicoGeneraArchivoMensual() throws Exception {
        Path archivo = new ReporteEconomicoGenerator().generar(2024, 10, carpeta);

        assertEquals("Economico_Octubre_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            assertEquals(2, libro.getNumberOfSheets());

            Sheet general = libro.getSheet("General");
            assertNotNull(general);
            assertEquals("Tratamiento", general.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Tratamiento test", general.getRow(1).getCell(0).getStringCellValue());
            assertEquals(2.0, general.getRow(1).getCell(1).getNumericCellValue(), 0.001);
            assertEquals(150.0, general.getRow(1).getCell(2).getNumericCellValue(), 0.001);
            assertEquals(90.0, general.getRow(1).getCell(3).getNumericCellValue(), 0.001);
            assertEquals(60.0, general.getRow(1).getCell(4).getNumericCellValue(), 0.001);

            Sheet operador = libro.getSheet("Por Operador");
            assertNotNull(operador);
            assertEquals("Operador: Ana Perez (PRE-4)", operador.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Tratamiento", operador.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Tratamiento test", operador.getRow(2).getCell(0).getStringCellValue());
            assertEquals(2.0, operador.getRow(2).getCell(1).getNumericCellValue(), 0.001);
        }
    }

    @Test
    void economicoGeneraArchivoAnual() throws Exception {
        Path archivo = new ReporteEconomicoGenerator().generar(2024, 1, 12, carpeta);

        assertEquals("Economico_2024.xlsx", archivo.getFileName().toString());
        try (Workbook libro = WorkbookFactory.create(archivo.toFile())) {
            assertEquals(2, libro.getNumberOfSheets());
            Sheet general = libro.getSheet("General");
            assertNotNull(general);
            assertEquals("Ene", general.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Total", general.getRow(0).getCell(13).getStringCellValue());
        }
    }

    @Test
    void archivoSeEscribeEnCarpetaIndicada() throws Exception {
        Path archivo = new ReporteMaterialesGenerator().generar(2024, 10, carpeta);

        assertTrue(Files.exists(archivo));
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
}