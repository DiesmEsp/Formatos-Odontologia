package com.odontologia.formatos.controller;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Asistencia;
import com.odontologia.formatos.repository.AsistenciaRepository;
import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    public void register(Javalin app) {
        app.get("/api/dashboard/kpis", ctx -> ctx.json(obtenerKpis()));

        app.get("/api/dashboard/ingresos-mensuales", ctx -> ctx.json(ingresosMensuales()));

        app.get("/api/dashboard/tratamientos-estado", ctx -> ctx.json(tratamientosEstado()));

        app.get("/api/dashboard/top-materiales", ctx -> ctx.json(topMateriales()));

        app.get("/api/dashboard/asistencia-hoy", ctx -> ctx.json(asistenciaHoy()));
    }

    private Map<String, Object> obtenerKpis() throws SQLException {
        String hoy = java.time.LocalDate.now().toString();
        String mesActual = hoy.substring(0, 7);
        String hace7Dias = java.time.LocalDate.now().minusDays(7).toString();

        Map<String, Object> kpis = new LinkedHashMap<>();

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(SUM(MontoPagado), 0) FROM Tratamiento WHERE Fecha LIKE ? AND Estado = 'CERRADO'")) {
                ps.setString(1, mesActual + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("ingresosMes", rs.next() ? rs.getDouble(1) : 0);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(SUM(MontoPagado), 0) FROM Tratamiento WHERE Fecha >= ? AND Estado = 'CERRADO'")) {
                ps.setString(1, hace7Dias);
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("ingresosSemana", rs.next() ? rs.getDouble(1) : 0);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM Tratamiento WHERE Estado = 'ABIERTO'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("tratamientosCurso", rs.next() ? rs.getInt(1) : 0);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM Asistencia WHERE Fecha = ? AND Estado = 'ACTIVO'")) {
                ps.setString(1, hoy);
                try (ResultSet rs = ps.executeQuery()) {
                    kpis.put("docentesHoy", rs.next() ? rs.getInt(1) : 0);
                }
            }
        }

        return kpis;
    }

    private List<Map<String, Object>> ingresosMensuales() throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String base = java.time.LocalDate.now().getYear() + "-";

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            for (int m = 1; m <= 12; m++) {
                String prefijo = base + String.format("%02d", m);
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(SUM(MontoPagado), 0) FROM Tratamiento WHERE Fecha LIKE ? AND Estado = 'CERRADO'")) {
                    ps.setString(1, prefijo + "%");
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

    private List<Map<String, Object>> tratamientosEstado() throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String mesActual = java.time.LocalDate.now().toString().substring(0, 7);

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            for (String estado : new String[]{"CERRADO", "ABIERTO", "ANULADO"}) {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM Tratamiento WHERE Estado = ? AND Fecha LIKE ?")) {
                    ps.setString(1, estado);
                    ps.setString(2, mesActual + "%");
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

    private List<Map<String, Object>> topMateriales() throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            String sql = """
                    SELECT m.Nombre, COALESCE(SUM(ml.Cantidad), 0) as total
                    FROM Materiales m
                    LEFT JOIN Materiales_List ml ON m.MaterialID = ml.MaterialID
                    LEFT JOIN Tratamiento t ON ml.TratamientoID = t.TratamientoID AND t.Estado = 'CERRADO'
                    WHERE m.Estado = 1
                    GROUP BY m.MaterialID
                    ORDER BY total DESC
                    LIMIT 5""";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("nombre", rs.getString(1));
                    entry.put("cantidad", rs.getDouble(2));
                    resultado.add(entry);
                }
            }
        }

        return resultado;
    }

    private List<Map<String, Object>> asistenciaHoy() throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String hoy = java.time.LocalDate.now().toString();

        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            String sql = """
                    SELECT d.DocenteID, d.Nombres, d.Apellidos, a.Estado, a.AsistenciaID,
                           a.HoraEntrada, a.HoraSalida
                    FROM Docentes d
                    LEFT JOIN Asistencia a ON d.DocenteID = a.DocenteID AND a.Fecha = ? AND a.Estado = 'ACTIVO'
                    WHERE d.Estado = 1""";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hoy);
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

                        boolean enAusencia = false;
                        if (estado != null) {
                            try (PreparedStatement psAus = con.prepareStatement(
                                    "SELECT 1 FROM PeriodoAusencia WHERE AsistenciaID = ? AND HoraFin IS NULL")) {
                                psAus.setInt(1, rs.getInt(5));
                                try (ResultSet rsAus = psAus.executeQuery()) {
                                    enAusencia = rsAus.next();
                                }
                            }
                        }
                        entry.put("enAusencia", enAusencia);
                        resultado.add(entry);
                    }
                }
            }
        }

        return resultado;
    }
}
