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

    public List<FilaMaterial> materiales(int anio, int mes, int clinicaID) throws SQLException {
        String patron = patronMes(anio, mes);
        String sql = "SELECT m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) AS UnidadBase, "
                + "COALESCE(SUM(consumo.cant * COALESCE(uc.Factor, 1)), 0) AS CantidadTotal "
                + "FROM Materiales m "
                + "LEFT JOIN ( "
                + "  SELECT ml.MaterialID AS MaterialID, ml.Cantidad AS cant "
                + "  FROM Materiales_List ml "
                + "  JOIN Tratamiento t ON t.TratamientoID = ml.TratamientoID "
                + "  WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? AND t.ClinicaID = ? "
                + "  UNION ALL "
                + "  SELECT ma.MaterialesID AS MaterialID, ma.Cantidad AS cant "
                + "  FROM Materiales_Asistencia ma "
                + "  JOIN Asistencia a ON a.AsistenciaID = ma.AsistenciaID "
                + "  WHERE a.Estado = 'ACTIVO' AND a.Fecha LIKE ? AND a.ClinicaID = ? "
                + ") consumo ON consumo.MaterialID = m.MaterialID "
                + "LEFT JOIN Unidad_Conversion uc ON uc.MaterialID = m.MaterialID "
                + "  AND uc.UnidadEmpaque = m.Unidad "
                + "WHERE m.Estado = 1 "
                + "GROUP BY m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) "
                + "ORDER BY m.Nombre";
        List<FilaMaterial> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setInt(2, clinicaID);
            ps.setString(3, patron);
            ps.setInt(4, clinicaID);
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

    public List<FilaIngreso> ingresos(int anio, int mes, int clinicaID) throws SQLException {
        String sql = "SELECT o.Grado, o.Tipo, "
                + "COUNT(t.TratamientoID) AS CantidadTratamientos, "
                + "COALESCE(SUM(t.Monto), 0) AS IngresoTotal, "
                + "COALESCE(SUM(t.MontoPagado), 0) AS MontoPagado, "
                + "COALESCE(SUM(t.Monto - t.MontoPagado), 0) AS MontoPendiente "
                + "FROM Tratamiento t "
                + "JOIN Operadores o ON o.OperadorID = t.OperadorID "
                + "WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? AND t.ClinicaID = ? "
                + "GROUP BY o.Grado, o.Tipo "
                + "ORDER BY o.Grado, o.Tipo";
        List<FilaIngreso> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            ps.setInt(2, clinicaID);
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

    public List<FilaDocente> docenteConsolidado(int anio, int mes, int clinicaID) throws SQLException {
        return docente(anio, mes, false, clinicaID);
    }

    public List<FilaDocente> docenteDetalleDia(int anio, int mes, int clinicaID) throws SQLException {
        return docente(anio, mes, true, clinicaID);
    }

    private List<FilaDocente> docente(int anio, int mes, boolean porDia, int clinicaID) throws SQLException {
        String selectDia = porDia ? "a.Fecha AS Dia, " : "NULL AS Dia, ";
        String groupBy = porDia
                ? "GROUP BY d.DocenteID, d.Nombres, d.Apellidos, a.Fecha, m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) "
                : "GROUP BY d.DocenteID, d.Nombres, d.Apellidos, m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) ";
        String orderBy = porDia
                ? "ORDER BY a.Fecha, d.Apellidos, d.Nombres, m.Nombre"
                : "ORDER BY d.Apellidos, d.Nombres, m.Nombre";
        String sql = "SELECT d.DocenteID, d.Nombres || ' ' || d.Apellidos AS Docente, "
                + selectDia
                + "m.MaterialID, m.Nombre AS Material, COALESCE(uc.UnidadBase, m.Unidad) AS Unidad, "
                + "SUM(ma.Cantidad * COALESCE(uc.Factor, 1)) AS Cantidad "
                + "FROM Materiales_Asistencia ma "
                + "JOIN Asistencia a ON a.AsistenciaID = ma.AsistenciaID "
                + "JOIN Docentes d ON d.DocenteID = a.DocenteID "
                + "JOIN Materiales m ON m.MaterialID = ma.MaterialesID "
                + "LEFT JOIN Unidad_Conversion uc ON uc.MaterialID = m.MaterialID "
                + "  AND uc.UnidadEmpaque = m.Unidad "
                + "WHERE a.Estado = 'ACTIVO' AND a.Fecha LIKE ? AND a.ClinicaID = ? "
                + groupBy + orderBy;
        List<FilaDocente> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            ps.setInt(2, clinicaID);
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

    public List<FilaEspecialista> especialista(int anio, int mes, int clinicaID) throws SQLException {
        String sql = "SELECT o.OperadorID, o.Nombres || ' ' || o.Apellidos AS Especialista, "
                + "o.Grado, o.Tipo, m.MaterialID, m.Nombre AS Material, "
                + "COALESCE(uc.UnidadBase, m.Unidad) AS Unidad, "
                + "SUM(ml.Cantidad * COALESCE(uc.Factor, 1)) AS Cantidad "
                + "FROM Materiales_List ml "
                + "JOIN Tratamiento t ON t.TratamientoID = ml.TratamientoID "
                + "JOIN Operadores o ON o.OperadorID = t.OperadorID "
                + "JOIN Materiales m ON m.MaterialID = ml.MaterialID "
                + "LEFT JOIN Unidad_Conversion uc ON uc.MaterialID = m.MaterialID "
                + "  AND uc.UnidadEmpaque = m.Unidad "
                + "WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? AND t.ClinicaID = ? "
                + "GROUP BY o.OperadorID, o.Nombres, o.Apellidos, o.Grado, o.Tipo, "
                + "m.MaterialID, m.Nombre, COALESCE(uc.UnidadBase, m.Unidad) "
                + "ORDER BY o.Apellidos, o.Nombres, m.Nombre";
        List<FilaEspecialista> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            ps.setInt(2, clinicaID);
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

    public List<FilaIngresoTratamiento> ingresosPorTratamiento(int anio, int mes, int clinicaID) throws SQLException {
        String sql = "SELECT t.NombreTratamiento, "
                + "COUNT(*) AS CantidadTratamientos, "
                + "COALESCE(SUM(t.Monto), 0) AS IngresoTotal, "
                + "COALESCE(SUM(t.MontoPagado), 0) AS MontoPagado, "
                + "COALESCE(SUM(t.Monto - t.MontoPagado), 0) AS MontoPendiente "
                + "FROM Tratamiento t "
                + "WHERE t.Estado = 'CERRADO' AND t.Tipo != 'CONTINUO' AND t.Fecha LIKE ? AND t.ClinicaID = ? "
                + "GROUP BY t.NombreTratamiento "
                + "ORDER BY IngresoTotal DESC";
        List<FilaIngresoTratamiento> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            ps.setInt(2, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaIngresoTratamiento(
                            rs.getString("NombreTratamiento"),
                            rs.getInt("CantidadTratamientos"),
                            rs.getDouble("IngresoTotal"),
                            rs.getDouble("MontoPagado"),
                            rs.getDouble("MontoPendiente")));
                }
            }
        }
        return lista;
    }

    public List<FilaIngresoOperador> ingresosPorOperador(int anio, int mes, int clinicaID) throws SQLException {
        String sql = "SELECT o.OperadorID, o.Nombres || ' ' || o.Apellidos AS Nombre, "
                + "o.Grado, o.Tipo, t.NombreTratamiento, "
                + "COUNT(*) AS Cantidad, "
                + "COALESCE(SUM(t.Monto), 0) AS IngresoTotal, "
                + "COALESCE(SUM(t.MontoPagado), 0) AS MontoPagado, "
                + "COALESCE(SUM(t.Monto - t.MontoPagado), 0) AS MontoPendiente "
                + "FROM Tratamiento t "
                + "JOIN Operadores o ON o.OperadorID = t.OperadorID "
                + "WHERE t.Estado = 'CERRADO' AND t.Tipo != 'CONTINUO' AND t.Fecha LIKE ? AND t.ClinicaID = ? "
                + "GROUP BY o.OperadorID, t.NombreTratamiento "
                + "ORDER BY o.Grado, o.Tipo, IngresoTotal DESC";
        List<FilaIngresoOperador> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            ps.setInt(2, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaIngresoOperador(
                            rs.getInt("OperadorID"),
                            rs.getString("Nombre"),
                            rs.getString("Grado"),
                            rs.getString("Tipo"),
                            rs.getString("NombreTratamiento"),
                            rs.getInt("Cantidad"),
                            rs.getDouble("IngresoTotal"),
                            rs.getDouble("MontoPagado"),
                            rs.getDouble("MontoPendiente")));
                }
            }
        }
        return lista;
    }

    private String patronMes(int anio, int mes) {
        return String.format("%04d-%02d%%", anio, mes);
    }

    public List<FilaAsistencia> datosAsistencia(int anio, int mes, int clinicaID) throws SQLException {
        String sql = """
                SELECT d.DocenteID, d.Nombres || ' ' || d.Apellidos AS Docente,
                       a.Fecha, a.HoraEntrada, a.HoraSalida
                FROM Asistencia a
                JOIN Docentes d ON d.DocenteID = a.DocenteID
                WHERE a.Estado = 'ACTIVO' AND a.Fecha LIKE ? AND a.ClinicaID = ?
                ORDER BY a.Fecha, d.Apellidos, d.Nombres""";
        List<FilaAsistencia> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patronMes(anio, mes));
            ps.setInt(2, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int asistenciaID = rs.getInt("DocenteID");
                    String docente = rs.getString("Docente");
                    String fecha = rs.getString("Fecha");
                    String horaEntrada = rs.getString("HoraEntrada");
                    String horaSalida = rs.getString("HoraSalida");
                    List<FilaAusencia> ausencias = ausenciasPorAsistencia(con, fecha, docente, clinicaID);
                    lista.add(new FilaAsistencia(asistenciaID, docente, fecha, horaEntrada, horaSalida, ausencias));
                }
            }
        }
        return lista;
    }

    private List<FilaAusencia> ausenciasPorAsistencia(Connection con, String fecha, String docente, int clinicaID)
            throws SQLException {
        String sql = """
                SELECT pa.HoraInicio, pa.HoraFin, pa.Motivo
                FROM PeriodoAusencia pa
                JOIN Asistencia a ON a.AsistenciaID = pa.AsistenciaID
                JOIN Docentes d ON d.DocenteID = a.DocenteID
                WHERE d.Nombres || ' ' || d.Apellidos = ? AND a.Fecha = ? AND a.Estado = 'ACTIVO'
                      AND a.ClinicaID = ?
                ORDER BY pa.HoraInicio""";
        List<FilaAusencia> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docente);
            ps.setString(2, fecha);
            ps.setInt(3, clinicaID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaAusencia(
                            rs.getString("HoraInicio"),
                            rs.getString("HoraFin"),
                            rs.getString("Motivo")));
                }
            }
        }
        return lista;
    }

    public List<FilaTratamiento> consumoPorTratamiento(int anio, int mes, int clinicaID) throws SQLException {
        return consumoPorTratamiento(anio, mes, null, null, clinicaID);
    }

    public List<FilaTratamiento> consumoPorTratamiento(int anio, int mes, Integer operadorID, String tipo,
                                                       int clinicaID) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.TratamientoID, t.NombreTratamiento, t.Fecha, "
                + "o.Nombres || ' ' || o.Apellidos AS Operador, o.Grado, o.Tipo, "
                + "m.Nombre AS Material, COALESCE(uc.UnidadBase, m.Unidad) AS Unidad, "
                + "ml.Cantidad * COALESCE(uc.Factor, 1) AS Cantidad "
                + "FROM Materiales_List ml "
                + "JOIN Tratamiento t ON t.TratamientoID = ml.TratamientoID "
                + "JOIN Operadores o ON o.OperadorID = t.OperadorID "
                + "JOIN Materiales m ON m.MaterialID = ml.MaterialID "
                + "LEFT JOIN Unidad_Conversion uc ON uc.MaterialID = m.MaterialID AND uc.UnidadEmpaque = m.Unidad "
                + "WHERE t.Estado = 'CERRADO' AND t.Fecha LIKE ? AND t.ClinicaID = ? ");
        if (operadorID != null) {
            sql.append("AND o.OperadorID = ? ");
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append("AND t.Tipo = ? ");
        }
        sql.append("ORDER BY o.Apellidos, o.Nombres, t.Fecha, t.TratamientoID, m.Nombre");

        List<FilaTratamiento> lista = new ArrayList<>();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, patronMes(anio, mes));
            ps.setInt(idx++, clinicaID);
            if (operadorID != null) {
                ps.setInt(idx++, operadorID);
            }
            if (tipo != null && !tipo.isBlank()) {
                ps.setString(idx++, tipo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaTratamiento(
                            rs.getInt("TratamientoID"),
                            rs.getString("NombreTratamiento"),
                            rs.getString("Fecha"),
                            rs.getString("Operador"),
                            rs.getString("Grado"),
                            rs.getString("Tipo"),
                            rs.getString("Material"),
                            rs.getString("Unidad"),
                            rs.getDouble("Cantidad")));
                }
            }
        }
        return lista;
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

    public static class FilaIngresoTratamiento {
        private final String tratamiento;
        private final int cantidadTratamientos;
        private final double ingresoTotal;
        private final double montoPagado;
        private final double montoPendiente;

        public FilaIngresoTratamiento(String tratamiento, int cantidadTratamientos,
                                      double ingresoTotal, double montoPagado, double montoPendiente) {
            this.tratamiento = tratamiento;
            this.cantidadTratamientos = cantidadTratamientos;
            this.ingresoTotal = ingresoTotal;
            this.montoPagado = montoPagado;
            this.montoPendiente = montoPendiente;
        }

        public String getTratamiento() {
            return tratamiento;
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

    public static class FilaIngresoOperador {
        private final int operadorID;
        private final String nombre;
        private final String grado;
        private final String tipo;
        private final String tratamiento;
        private final int cantidad;
        private final double ingresoTotal;
        private final double montoPagado;
        private final double montoPendiente;

        public FilaIngresoOperador(int operadorID, String nombre, String grado, String tipo,
                                   String tratamiento, int cantidad, double ingresoTotal,
                                   double montoPagado, double montoPendiente) {
            this.operadorID = operadorID;
            this.nombre = nombre;
            this.grado = grado;
            this.tipo = tipo;
            this.tratamiento = tratamiento;
            this.cantidad = cantidad;
            this.ingresoTotal = ingresoTotal;
            this.montoPagado = montoPagado;
            this.montoPendiente = montoPendiente;
        }

        public int getOperadorID() {
            return operadorID;
        }

        public String getNombre() {
            return nombre;
        }

        public String getGrado() {
            return grado;
        }

        public String getTipo() {
            return tipo;
        }

        public String getTratamiento() {
            return tratamiento;
        }

        public int getCantidad() {
            return cantidad;
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

    public static class FilaAsistencia {
        private final int docenteID;
        private final String docente;
        private final String fecha;
        private final String horaEntrada;
        private final String horaSalida;
        private final List<FilaAusencia> ausencias;

        public FilaAsistencia(int docenteID, String docente, String fecha,
                              String horaEntrada, String horaSalida, List<FilaAusencia> ausencias) {
            this.docenteID = docenteID;
            this.docente = docente;
            this.fecha = fecha;
            this.horaEntrada = horaEntrada;
            this.horaSalida = horaSalida;
            this.ausencias = ausencias;
        }

        public int getDocenteID() { return docenteID; }
        public String getDocente() { return docente; }
        public String getFecha() { return fecha; }
        public String getHoraEntrada() { return horaEntrada; }
        public String getHoraSalida() { return horaSalida; }
        public List<FilaAusencia> getAusencias() { return ausencias; }
    }

    public static class FilaAusencia {
        private final String horaInicio;
        private final String horaFin;
        private final String motivo;

        public FilaAusencia(String horaInicio, String horaFin, String motivo) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.motivo = motivo;
        }

        public String getHoraInicio() { return horaInicio; }
        public String getHoraFin() { return horaFin; }
        public String getMotivo() { return motivo; }
    }

    public static class FilaTratamiento {
        private final int tratamientoID;
        private final String nombreTratamiento;
        private final String fecha;
        private final String operador;
        private final String grado;
        private final String tipo;
        private final String material;
        private final String unidad;
        private final double cantidad;

        public FilaTratamiento(int tratamientoID, String nombreTratamiento, String fecha,
                               String operador, String grado, String tipo,
                               String material, String unidad, double cantidad) {
            this.tratamientoID = tratamientoID;
            this.nombreTratamiento = nombreTratamiento;
            this.fecha = fecha;
            this.operador = operador;
            this.grado = grado;
            this.tipo = tipo;
            this.material = material;
            this.unidad = unidad;
            this.cantidad = cantidad;
        }

        public int getTratamientoID() { return tratamientoID; }
        public String getNombreTratamiento() { return nombreTratamiento; }
        public String getFecha() { return fecha; }
        public String getOperador() { return operador; }
        public String getGrado() { return grado; }
        public String getTipo() { return tipo; }
        public String getMaterial() { return material; }
        public String getUnidad() { return unidad; }
        public double getCantidad() { return cantidad; }
    }
}
