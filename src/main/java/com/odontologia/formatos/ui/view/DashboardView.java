package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Tratamiento;
import com.odontologia.formatos.repository.TratamientoRepository;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.repository.AsistenciaRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class DashboardView extends VBox {

    private final TratamientoRepository tratamientoRepo = new TratamientoRepository();
    private final UnidadRepository unidadRepo = new UnidadRepository();
    private final AsistenciaRepository asistenciaRepo = new AsistenciaRepository();
    private final OperadorRepository operadorRepo = new OperadorRepository();

    public DashboardView() {
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(24);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Inicio");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Resumen del dia — " + LocalDate.now());
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            buildStatCard("Tratamientos activos", String.valueOf(fetchAbiertos()), "blue"),
            buildStatCard("Unidades", String.valueOf(fetchUnidades()), "teal"),
            buildStatCard("Operadores", String.valueOf(fetchOperadores()), "purple"),
            buildStatCard("Asistencia hoy", String.valueOf(fetchAsistenciaHoy()), "amber")
        );

        HBox actionsRow = new HBox(20);
        actionsRow.getChildren().addAll(
            buildQuickAction("Nuevo tratamiento", "Registrar un tratamiento en una unidad"),
            buildQuickAction("Asistencia docente", "Marcar asistencia para hoy"),
            buildQuickAction("Ver reportes", "Generar reportes del mes")
        );

        getChildren().addAll(header, statsRow, new javafx.scene.control.Separator(), actionsRow);
    }

    private VBox buildStatCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");

        Label val = new Label(value);
        val.getStyleClass().add("stat-value");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(val, lbl);

        String borderColor = switch (color) {
            case "teal" -> "#0D9488";
            case "purple" -> "#7C3AED";
            case "amber" -> "#D97706";
            default -> "#2563EB";
        };
        card.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2 0 0 0; -fx-border-radius: 8 8 0 0;");
        return card;
    }

    private VBox buildQuickAction(String title, String description) {
        VBox box = new VBox(6);
        box.getStyleClass().add("card");
        box.setPrefWidth(280);

        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        Label d = new Label(description);
        d.getStyleClass().add("view-subtitle");
        d.setWrapText(true);

        box.getChildren().addAll(t, d);
        return box;
    }

    private int fetchAbiertos() {
        try {
            return tratamientoRepo.findByEstado("ABIERTO").size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int fetchUnidades() {
        try {
            return unidadRepo.findAll().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int fetchOperadores() {
        try {
            return operadorRepo.findAll().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int fetchAsistenciaHoy() {
        try {
            String hoy = LocalDate.now().toString();
            String sql = "SELECT COUNT(*) FROM Asistencia WHERE Fecha = ? AND Estado = 'ACTIVO'";
            try (Connection con = ConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hoy);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
