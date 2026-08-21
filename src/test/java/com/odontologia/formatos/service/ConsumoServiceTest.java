package com.odontologia.formatos.service;

import com.odontologia.formatos.model.ConsumoClinica;
import com.odontologia.formatos.repository.BaseRepositoryTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumoServiceTest extends BaseRepositoryTest {

    private final ConsumoService service = new ConsumoService();
    private final MaterialService materialService = new MaterialService();

    private int crearMaterial(String nombre) throws SQLException {
        return materialService.crear(nombre, "unidad");
    }

    @Test
    void creaLoteValidoYRetornaIds() throws SQLException {
        int materialId = crearMaterial("Guante de prueba");
        List<ConsumoClinica> items = List.of(
                item("2026-08-05", materialId, 3),
                item("2026-08-10", materialId, 1.5));
        List<Integer> ids = service.crearLote(items, 1);
        assertEquals(2, ids.size());
    }

    @Test
    void listaPorMesDevuelveSoloRegistrosDelRango() throws SQLException {
        int materialId = crearMaterial("Gasa de prueba");
        service.crearLote(List.of(
                item("2026-07-31", materialId, 2),
                item("2026-08-01", materialId, 1),
                item("2026-08-31", materialId, 4),
                item("2026-09-01", materialId, 5)), 1);
        List<ConsumoClinica> agosto = service.listarPorMes(2026, 8, 1);
        assertEquals(2, agosto.size());
        assertTrue(agosto.stream().allMatch(c -> c.getFecha().startsWith("2026-08")));
    }

    @Test
    void listaOrdenaPorFechaDescendente() throws SQLException {
        int materialId = crearMaterial("Algodon de prueba");
        service.crearLote(List.of(
                item("2026-08-02", materialId, 1),
                item("2026-08-20", materialId, 2)), 1);
        List<ConsumoClinica> lista = service.listarPorMes(2026, 8, 1);
        assertEquals("2026-08-20", lista.get(0).getFecha());
    }

    @Test
    void listaJuntaNombreYUnidadDelMaterial() throws SQLException {
        int materialId = materialService.crear("Seda dental prueba", "caja");
        service.crearLote(List.of(item("2026-08-15", materialId, 1)), 1);
        ConsumoClinica registro = service.listarPorMes(2026, 8, 1).get(0);
        assertEquals("Seda dental prueba", registro.getNombreMaterial());
        assertEquals("caja", registro.getUnidad());
    }

    @Test
    void rechazaLoteVacio() {
        assertThrows(NegocioException.class, () -> service.crearLote(List.of(), 1));
    }

    @Test
    void rechazaCantidadNegativaOCero() throws SQLException {
        int materialId = crearMaterial("Babero prueba");
        assertThrows(NegocioException.class,
                () -> service.crearLote(List.of(item("2026-08-05", materialId, 0)), 1));
        assertThrows(NegocioException.class,
                () -> service.crearLote(List.of(item("2026-08-05", materialId, -1)), 1));
    }

    @Test
    void rechazaFechaInvalida() throws SQLException {
        int materialId = crearMaterial("Pinzas prueba");
        assertThrows(NegocioException.class,
                () -> service.crearLote(List.of(item("05/08/2026", materialId, 1)), 1));
        assertThrows(NegocioException.class,
                () -> service.crearLote(List.of(item("2026-13-40", materialId, 1)), 1));
    }

    @Test
    void rechazaMesFueraDeRangoYAnioInvalido() {
        assertThrows(NegocioException.class, () -> service.listarPorMes(2026, 0, 1));
        assertThrows(NegocioException.class, () -> service.listarPorMes(2026, 13, 1));
        assertThrows(NegocioException.class, () -> service.listarPorMes(1999, 8, 1));
    }

    @Test
    void rechazaMaterialInexistenteOInactivo() throws SQLException {
        assertThrows(NegocioException.class,
                () -> service.crearLote(List.of(item("2026-08-05", 99999, 1)), 1));
        int materialId = materialService.crear("Material a inactivar", "unidad");
        MaterialService matSvc = new MaterialService();
        com.odontologia.formatos.model.Materiales m = new com.odontologia.formatos.model.Materiales();
        m.setMaterialID(materialId);
        m.setNombre("Material a inactivar");
        m.setUnidad("unidad");
        m.setEstado(0);
        matSvc.actualizar(m);
        assertThrows(NegocioException.class,
                () -> service.crearLote(List.of(item("2026-08-05", materialId, 1)), 1));
    }

    @Test
    void actualizaRegistroExistente() throws SQLException {
        int materialId = crearMaterial("Espejo prueba");
        int id = service.crearLote(List.of(item("2026-08-05", materialId, 1)), 1).get(0);
        service.actualizar(id, "2026-08-06", materialId, 7, 1);
        ConsumoClinica registro = service.listarPorMes(2026, 8, 1).get(0);
        assertEquals(7, registro.getCantidad(), 0.001);
        assertEquals("2026-08-06", registro.getFecha());
    }

    @Test
    void eliminarQuitaElRegistro() throws SQLException {
        int materialId = crearMaterial("Cubeta prueba");
        int id = service.crearLote(List.of(item("2026-08-05", materialId, 1)), 1).get(0);
        service.eliminar(id, 1);
        assertTrue(service.listarPorMes(2026, 8, 1).isEmpty());
    }

    @Test
    void actualizarYEliminarDeOtraClinicaFalla() throws SQLException {
        int materialId = crearMaterial("Fresa prueba");
        int id = service.crearLote(List.of(item("2026-08-05", materialId, 1)), 1).get(0);
        assertThrows(NegocioException.class,
                () -> service.actualizar(id, "2026-08-06", materialId, 2, 2));
        assertThrows(NegocioException.class, () -> service.eliminar(id, 2));
    }

    @Test
    void clinicasNoSeMezclanAlListar() throws SQLException {
        int materialId = crearMaterial("Micromotor prueba");
        service.crearLote(List.of(item("2026-08-05", materialId, 1)), 1);
        assertTrue(service.listarPorMes(2026, 8, 2).isEmpty());
        assertEquals(1, service.listarPorMes(2026, 8, 1).size());
    }

    private ConsumoClinica item(String fecha, int materialId, double cantidad) {
        ConsumoClinica c = new ConsumoClinica();
        c.setFecha(fecha);
        c.setMaterialID(materialId);
        c.setCantidad(cantidad);
        return c;
    }
}
