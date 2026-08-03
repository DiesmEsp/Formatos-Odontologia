package com.odontologia.formatos.export;

import com.odontologia.formatos.repository.ReporteRepository.FilaDocente;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Reporte de Consumo Docente (RF-1.7.4, tarea 4.5).
 * Un solo archivo con dos hojas: consolidado mensual y detalle diario.
 */
public class ReporteDocenteGenerator extends ReporteGeneradorBase {

    @Override
    protected String nombreArchivo(int anio, int mes) {
        return ReporteNomenclatura.nombre(ReporteNomenclatura.TIPO_DOCENTE, anio, mes);
    }

    @Override
    protected void construir(Workbook libro, int anio, int mes) throws SQLException {
        Sheet consolidado = crearHoja(libro, "Consolidado");
        encabezado(consolidado, Arrays.asList("Docente", "Material", "Unidad", "Cantidad Total"));
        List<FilaDocente> filasConsolidadas = service.docenteConsolidado(anio, mes);
        int filaIndex = 1;
        for (FilaDocente f : filasConsolidadas) {
            fila(consolidado, filaIndex++, Arrays.asList(
                    f.getDocente(), f.getMaterial(), f.getUnidad(), redondear2(f.getCantidad())));
        }
        autoAjustar(consolidado, 4);

        Sheet detalle = crearHoja(libro, "Detalle diario");
        encabezado(detalle, Arrays.asList("Docente", "Día", "Material", "Unidad", "Cantidad"));
        List<FilaDocente> filasDetalle = service.docenteDetalleDia(anio, mes);
        filaIndex = 1;
        for (FilaDocente f : filasDetalle) {
            fila(detalle, filaIndex++, Arrays.asList(
                    f.getDocente(), f.getDia(), f.getMaterial(), f.getUnidad(), redondear2(f.getCantidad())));
        }
        autoAjustar(detalle, 5);
    }
}
