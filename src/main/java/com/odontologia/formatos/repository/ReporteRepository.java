package com.odontologia.formatos.repository;

import com.odontologia.formatos.db.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas agregadas para los reportes (Fase 4).
 * <p>
 * El mes se filtra sobre la columna Fecha (TEXT 'AAAA-MM-DD') mediante el patrón
 * LIKE 'AAAA-MM%'. Solo cuentan los registros activos: Tratamiento CERRADO y
 * Asistencia ACTIVO (RD-3.1.5, RD-3.1.6).
 * <p>
 * Conversión de unidades (RD-3.1.14): el consumo de un material se expresa en su
 * UnidadBase multiplicando por el Factor de Unidad_Conversion cuando el empaque
 * coincide con la unidad declarada del material; si no hay conversión, se usa
 * factor 1 (el material se suma en su propia unidad).
 */
public class ReporteRepository {

    public List<FilaMaterial> materiales(int anio, int mes) throws SQLException {
        String patron = patronMes(anio, mes);
        String sql = "SELECT m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) AS UnidadBase, "
                + "SUM(consumo.cant * COALESCE(uc.Factor, 1)) AS CantidadTotal "
                + "FROM ( "
                + "  SELECT ml.MaterialID AS MaterialID, ml.Cantidad AS cant "
                + "  FROM Materiales_List ml "
                + "  JOIN Tratamiento t ON t.TratamientoID = ml.TratamientoID "
                + "  WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? "
                + "  UNION ALL "
                + "  SELECT ma.MaterialesID AS MaterialID, ma.Cantidad AS cant "
                + "  FROM Materiales_Asistencia ma "
                + "  JOIN Asistencia a ON a.AsistenciaID = ma.AsistenciaID "
                + "  WHERE a.Estado = 'ACTIVO' AND a.Fecha LIKE ? "
                + ") consumo "
                + "JOIN Materiales m ON m.MaterialID = consumo.MaterialID "
                + "LEFT JOIN Unidad_Conversion uc ON uc.MaterialID = m.MaterialID "
                + "  AND uc.UnidadEmpaque = m.Unidad "
                + "GROUP BY m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) "
                + "ORDER BY m.Nombre";
        List<FilaMaterial> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaMaterial(
                            rs.getInt("MaterialID"),
                            rs.getString("Nombre"),
                            rs.getString("UnidadBase"),
                            rs.getDouble("CantidadTotal")));
                }
            }
        }
        return lista;
    }

    public List<FilaIngreso> ingresos(int anio, int mes) throws SQLException {
        String sql = "SELECT o.Grado, o.Tipo, "
                + "COUNT(t.TratamientoID) AS CantidadTratamientos, "
                + "COALESCE(SUM(t.Monto), 0) AS IngresoTotal, "
                + "COALESCE(SUM(t.MontoPagado), 0) AS MontoPagado, "
                + "COALESCE(SUM(t.Monto - t.MontoPagado), 0) AS MontoPendiente "
                + "FROM Tratamiento t "
                + "JOIN Operadores o ON o.OperadorID = t.OperadorID "
                + "WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? "
                + "GROUP BY o.Grado, o.Tipo "
                + "ORDER BY o.Grado, o.Tipo";
        List<FilaIngreso> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaIngreso(
                            rs.getString("Grado"),
                            rs.getString("Tipo"),
                            rs.getInt("CantidadTratamientos"),
                            rs.getDouble("IngresoTotal"),
                            rs.getDouble("MontoPagado"),
                            rs.getDouble("MontoPendiente")));
                }
            }
        }
        return lista;
    }

    public List<FilaDocente> docenteConsolidado(int anio, int mes) throws SQLException {
        return docente(anio, mes, false);
    }

    public List<FilaDocente> docenteDetalleDia(int anio, int mes) throws SQLException {
        return docente(anio, mes, true);
    }

    private List<FilaDocente> docente(int anio, int mes, boolean porDia) throws SQLException {
        String selectDia = porDia ? "a.Fecha AS Dia, " : "NULL AS Dia, ";
        String groupBy = porDia
                ? "GROUP BY d.DocenteID, d.Nombres, d.Apellidos, a.Fecha, m.MaterialID, m.Nombre, m.Unidad "
                : "GROUP BY d.DocenteID, d.Nombres, d.Apellidos, m.MaterialID, m.Nombre, m.Unidad ";
        String orderBy = porDia
                ? "ORDER BY a.Fecha, d.Apellidos, d.Nombres, m.Nombre"
                : "ORDER BY d.Apellidos, d.Nombres, m.Nombre";
        String sql = "SELECT d.DocenteID, d.Nombres || ' ' || d.Apellidos AS Docente, "
                + selectDia
                + "m.MaterialID, m.Nombre AS Material, m.Unidad, SUM(ma.Cantidad) AS Cantidad "
                + "FROM Materiales_Asistencia ma "
                + "JOIN Asistencia a ON a.AsistenciaID = ma.AsistenciaID "
                + "JOIN Docentes d ON d.DocenteID = a.DocenteID "
                + "JOIN Materiales m ON m.MaterialID = ma.MaterialesID "
                + "WHERE a.Estado = 'ACTIVO' AND a.Fecha LIKE ? "
                + groupBy + orderBy;
        List<FilaDocente> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String dia = rs.getString("Dia");
                    lista.add(new FilaDocente(
                            rs.getInt("DocenteID"),
                            rs.getString("Docente"),
                            dia,
                            rs.getInt("MaterialID"),
                            rs.getString("Material"),
                            rs.getString("Unidad"),
                            rs.getDouble("Cantidad")));
                }
            }
        }
        return lista;
    }

    public List<FilaEspecialista> especialista(int anio, int mes) throws SQLException {
        String sql = "SELECT o.OperadorID, o.Nombres || ' ' || o.Apellidos AS Especialista, "
                + "o.Grado, o.Tipo, m.MaterialID, m.Nombre AS Material, m.Unidad, "
                + "SUM(ml.Cantidad) AS Cantidad "
                + "FROM Materiales_List ml "
                + "JOIN Tratamiento t ON t.TratamientoID = ml.TratamientoID "
                + "JOIN Operadores o ON o.OperadorID = t.OperadorID "
                + "JOIN Materiales m ON m.MaterialID = ml.MaterialID "
                + "WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? "
                + "GROUP BY o.OperadorID, o.Nombres, o.Apellidos, o.Grado, o.Tipo, "
                + "m.MaterialID, m.Nombre, m.Unidad "
                + "ORDER BY o.Apellidos, o.Nombres, m.Nombre";
        List<FilaEspecialista> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaEspecialista(
                            rs.getInt("OperadorID"),
                            rs.getString("Especialista"),
                            rs.getString("Grado"),
                            rs.getString("Tipo"),
                            rs.getInt("MaterialID"),
                            rs.getString("Material"),
                            rs.getString("Unidad"),
                            rs.getDouble("Cantidad")));
                }
            }
        }
        return lista;
    }

    private String patronMes(int anio, int mes) {
        return String.format("%04d-%02d%%", anio, mes);
    }

    public static class FilaMaterial {
        private final int materialID;
        private final String nombre;
        private final String unidadBase;
        private final double cantidadTotal;

        public FilaMaterial(int materialID, String nombre, String unidadBase, double cantidadTotal) {
            this.materialID = materialID;
            this.nombre = nombre;
            this.unidadBase = unidadBase;
            this.cantidadTotal = cantidadTotal;
        }

        public int getMaterialID() {
            return materialID;
        }

        public String getNombre() {
            return nombre;
        }

        public String getUnidadBase() {
            return unidadBase;
        }

        public double getCantidadTotal() {
            return cantidadTotal;
        }
    }

    public static class FilaIngreso {
        private final String grado;
        private final String tipo;
        private final int cantidadTratamientos;
        private final double ingresoTotal;
        private final double montoPagado;
        private final double montoPendiente;

        public FilaIngreso(String grado, String tipo, int cantidadTratamientos, double ingresoTotal,
                           double montoPagado, double montoPendiente) {
            this.grado = grado;
            this.tipo = tipo;
            this.cantidadTratamientos = cantidadTratamientos;
            this.ingresoTotal = ingresoTotal;
            this.montoPagado = montoPagado;
            this.montoPendiente = montoPendiente;
        }

        public String getGrado() {
            return grado;
        }

        public String getTipo() {
            return tipo;
        }

        public int getCantidadTratamientos() {
            return cantidadTratamientos;
        }

        public double getIngresoTotal() {
            return ingresoTotal;
        }

        public double getMontoPagado() {
            return montoPagado;
        }

        public double getMontoPendiente() {
            return montoPendiente;
        }
    }

    public static class FilaDocente {
        private final int docenteID;
        private final String docente;
        private final String dia;
        private final int materialID;
        private final String material;
        private final String unidad;
        private final double cantidad;

        public FilaDocente(int docenteID, String docente, String dia, int materialID,
                           String material, String unidad, double cantidad) {
            this.docenteID = docenteID;
            this.docente = docente;
            this.dia = dia;
            this.materialID = materialID;
            this.material = material;
            this.unidad = unidad;
            this.cantidad = cantidad;
        }

        public int getDocenteID() {
            return docenteID;
        }

        public String getDocente() {
            return docente;
        }

        public String getDia() {
            return dia;
        }

        public int getMaterialID() {
            return materialID;
        }

        public String getMaterial() {
            return material;
        }

        public String getUnidad() {
            return unidad;
        }

        public double getCantidad() {
            return cantidad;
        }
    }

    public static class FilaEspecialista {
        private final int operadorID;
        private final String especialista;
        private final String grado;
        private final String tipo;
        private final int materialID;
        private final String material;
        private final String unidad;
        private final double cantidad;

        public FilaEspecialista(int operadorID, String especialista, String grado, String tipo,
                                int materialID, String material, String unidad, double cantidad) {
            this.operadorID = operadorID;
            this.especialista = especialista;
            this.grado = grado;
            this.tipo = tipo;
            this.materialID = materialID;
            this.material = material;
            this.unidad = unidad;
            this.cantidad = cantidad;
        }

        public int getOperadorID() {
            return operadorID;
        }

        public String getEspecialista() {
            return especialista;
        }

        public String getGrado() {
            return grado;
        }

        public String getTipo() {
            return tipo;
        }

        public int getMaterialID() {
            return materialID;
        }

        public String getMaterial() {
            return material;
        }

        public String getUnidad() {
            return unidad;
        }

        public double getCantidad() {
            return cantidad;
        }
    }
}
