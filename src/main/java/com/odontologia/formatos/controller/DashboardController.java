package com.odontologia.formatos.controller;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.util.ControllerUtil;
import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DashboardController {

    public void register(Javalin app) {
        app.get("/api/dashboard/kpis", ctx -> ctx.json(obtenerKpis(ControllerUtil.clinicaID(ctx))));
        app.get("/api/dashboard/ingresos-mensuales", ctx -> ctx.json(ingresosMensuales(ControllerUtil.clinicaID(ctx))));
        app.get("/api/dashboard/tratamientos-estado", ctx -> ctx.json(tratamientosEstado(ControllerUtil.clinicaID(ctx))));
        app.get("/api/dashboard/top-materiales", ctx -> ctx.json(topMateriales(ControllerUtil.clinicaID(ctx))));
        app.get("/api/dashboard/asistencia-hoy", ctx -> ctx.json(asistenciaHoy(ControllerUtil.clinicaID(ctx))));
    }

    private Map<String, Object> obtenerKpis(int clinicaID) throws SQLException {
        String hoy = java.time.LocalDate.now().toString();
        String mesActual = hoy.substring(0, 7);
        String hace7Dias = java.time.LocalDate.now().minusDays(7).toString();

        Map<String, Object> kpis = new LinkedHashMap<>();

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(SUM(Monto), 0) FROM Tratamiento WHERE Fecha LIKE ? AND Estado = 'CERRADO' AND ClinicaID = ?")) {
                ps.setString(1, mesActual + "%");
                ps.setInt(2, clinicaID);
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("ingresosMes", rs.next() ? rs.getDouble(1) : 0);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(SUM(Monto), 0) FROM Tratamiento WHERE Fecha >= ? AND Estado = 'CERRADO' AND ClinicaID = ?")) {
                ps.setString(1, hace7Dias);
                ps.setInt(2, clinicaID);
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("ingresosSemana", rs.next() ? rs.getDouble(1) : 0);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM Tratamiento WHERE Estado = 'ABIERTO' AND ClinicaID = ?")) {
                ps.setInt(1, clinicaID);
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("tratamientosCurso", rs.next() ? rs.getInt(1) : 0);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM Asistencia WHERE Fecha = ? AND Estado = 'ACTIVO' AND ClinicaID = ?")) {
                ps.setString(1, hoy);
                ps.setInt(2, clinicaID);
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("docentesHoy", rs.next() ? rs.getInt(1) : 0);
                }
            }
        }

        return kpis;
    }

    private List<Map<String, Object>> ingresosMensuales(int clinicaID) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String base = java.time.LocalDate.now().getYear() + "-";

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            for (int m = 1; m <= 12; m++) {
                String prefijo = base + String.format("%02d", m);
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(SUM(Monto), 0) FROM Tratamiento WHERE Fecha LIKE ? AND Estado = 'CERRADO' AND ClinicaID = ?")) {
                    ps.setString(1, prefijo + "%");
                    ps.setInt(2, clinicaID);
                    try (ResultSet rs = ps.executeQuery()) {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("mes", m);
                        entry.put("monto", rs.next() ? rs.getDouble(1) : 0);
                        resultado.add(entry);
                    }
                }
            }
        }

        return resultado;
    }

    private List<Map<String, Object>> tratamientosEstado(int clinicaID) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String mesActual = java.time.LocalDate.now().toString().substring(0, 7);

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            for (String estado : new String[]{"CERRADO", "ABIERTO", "ANULADO"}) {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM Tratamiento WHERE Estado = ? AND Fecha LIKE ? AND ClinicaID = ?")) {
                    ps.setString(1, estado);
                    ps.setString(2, mesActual + "%");
                    ps.setInt(3, clinicaID);
                    try (ResultSet rs = ps.executeQuery()) {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("estado", estado);
                        entry.put("count", rs.next() ? rs.getInt(1) : 0);
                        resultado.add(entry);
                    }
                }
            }
        }

        return resultado;
    }

    private List<Map<String, Object>> topMateriales(int clinicaID) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            String sql = """
                    SELECT m.Nombre, COALESCE(SUM(consumo.cant), 0) AS total
                    FROM Materiales m
                    LEFT JOIN (
                      SELECT ml.MaterialID AS MaterialID, ml.Cantidad AS cant
                      FROM Materiales_List ml
                      JOIN Tratamiento t ON t.TratamientoID = ml.TratamientoID
                      WHERE t.Estado = 'CERRADO' AND t.ClinicaID = ?
                      UNION ALL
                      SELECT ma.MaterialesID AS MaterialID, ma.Cantidad AS cant
                      FROM Materiales_Asistencia ma
                      JOIN Asistencia a ON a.AsistenciaID = ma.AsistenciaID
                      WHERE a.Estado = 'ACTIVO' AND a.ClinicaID = ?
                    ) consumo ON consumo.MaterialID = m.MaterialID
                    WHERE m.Estado = 1
                    GROUP BY m.MaterialID, m.Nombre
                    ORDER BY total DESC
                    LIMIT 5""";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, clinicaID);
                ps.setInt(2, clinicaID);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("nombre", rs.getString(1));
                        entry.put("cantidad", rs.getDouble(2));
                        resultado.add(entry);
                    }
                }
            }
        }

        return resultado;
    }

    private List<Map<String, Object>> asistenciaHoy(int clinicaID) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String hoy = java.time.LocalDate.now().toString();

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            String sql = """
                    SELECT d.DocenteID, d.Nombres, d.Apellidos, a.Estado, a.AsistenciaID,
                           a.HoraEntrada, a.HoraSalida
                    FROM Docentes d
                    LEFT JOIN Asistencia a ON d.DocenteID = a.DocenteID AND a.Fecha = ?
                                             AND a.Estado = 'ACTIVO' AND a.ClinicaID = ?
                    WHERE d.Estado = 1 AND d.ClinicaID = ?""";

            Map<Integer, Boolean> ausenciaCache = new HashMap<>();
            String sqlAusencias = "SELECT DISTINCT AsistenciaID FROM PeriodoAusencia WHERE HoraFin IS NULL";
            try (PreparedStatement psAus = con.prepareStatement(sqlAusencias);
                 ResultSet rsAus = psAus.executeQuery()) {
                while (rsAus.next()) {
                    ausenciaCache.put(rsAus.getInt(1), true);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hoy);
                ps.setInt(2, clinicaID);
                ps.setInt(3, clinicaID);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        int id = rs.getInt(1);
                        entry.put("docenteID", id);
                        entry.put("nombres", rs.getString(2));
                        entry.put("apellidos", rs.getString(3));
                        String estado = rs.getString(4);
                        entry.put("presente", estado != null);
                        entry.put("asistenciaID", estado != null ? rs.getInt(5) : null);
                        entry.put("horaEntrada", estado != null ? rs.getString(6) : null);
                        entry.put("horaSalida", estado != null ? rs.getString(7) : null);
                        entry.put("enAusencia", estado != null && ausenciaCache.containsKey(rs.getInt(5)));
                        resultado.add(entry);
                    }
                }
            }
        }

        return resultado;
    }
}
