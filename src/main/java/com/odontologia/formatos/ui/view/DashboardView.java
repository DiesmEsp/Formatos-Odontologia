package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.repository.*;
import com.odontologia.formatos.ui.components.SvgIcons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.function.Consumer;

public class DashboardView extends VBox {

    private final TratamientoRepository tratamientoRepo = new TratamientoRepository();
    private final UnidadRepository unidadRepo = new UnidadRepository();
    private final OperadorRepository operadorRepo = new OperadorRepository();
    private final Consumer<String> onNavigate;

    public DashboardView(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(28, 32, 48, 32));
        setSpacing(20);

        VBox headerBox = new VBox(4);
        headerBox.getStyleClass().add("view-header");
        headerBox.getChildren().addAll(
            title("Dashboard"),
            subtitle("Panel principal del sistema")
        );

        HBox kpiRow = new HBox(14);
        kpiRow.getChildren().addAll(
            kpiCard("Ingresos del mes", "S/ --", "+0% vs. anterior", SvgIcons.reportes(15)),
            kpiCard("Tratamientos en curso", String.valueOf(fetchAbiertos()), fetchUnidadesOcupadas() + " unidades en uso", SvgIcons.tooth(15)),
            kpiCard("Docentes presentes hoy", String.valueOf(fetchAsistenciaHoy()), "activos", SvgIcons.asistencia(15)),
            kpiCard("Operadores activos", String.valueOf(fetchOperadores()), "en el periodo actual", SvgIcons.user(15))
        );

        HBox chartRow = new HBox(14);
        VBox doughnutCard = chartCard("Tratamientos del mes", "Julio 2026", doughnutPlaceholder());
        VBox hbarCard = chartCard("Materiales mas usados", "Julio 2026", barPlaceholder());
        chartRow.getChildren().addAll(doughnutCard, hbarCard);
        HBox.setHgrow(doughnutCard, Priority.ALWAYS);
        HBox.setHgrow(hbarCard, Priority.ALWAYS);

        Label qaTitle = new Label("Accesos rapidos");
        qaTitle.setStyle("-fx-font-size:14px; -fx-font-weight:700; -fx-text-fill:#142a33; -fx-padding:8 0 4 0;");

        TilePane quickGrid = new TilePane();
        quickGrid.setHgap(12);
        quickGrid.setVgap(12);
        quickGrid.setPrefColumns(4);
        quickGrid.getChildren().addAll(
            dashCard("Tratamientos", "Registro de atencion con materiales predefinidos y adicionales", "tratamientos", SvgIcons.tooth(20)),
            dashCard("Asistencia Docente", "Control diario de entrega de materiales a docentes", "asistencia", SvgIcons.asistencia(20)),
            dashCard("Catalogos", "Materiales, docentes, especialistas y tratamientos predefinidos", "catalogos", SvgIcons.catalogo(20)),
            dashCard("Reportes", "Exportacion Excel de materiales, ingresos y docentes", "reportes", SvgIcons.reportes(20)),
            dashCard("Unidades", "Gestion de unidades de atencion de la clinica", "unidades", SvgIcons.modulos(20))
        );

        getChildren().addAll(headerBox, kpiRow, chartRow, new Separator(), qaTitle, quickGrid);
    }

    private VBox kpiCard(String label, String value, String sub, javafx.scene.Group icon) {
        VBox card = new VBox(8);
        card.getStyleClass().add("kpi-card");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(top, Priority.ALWAYS);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("kpi-label");
        StackPane ico = new StackPane(icon);
        ico.getStyleClass().add("kpi-icon");
        icon.getStyleClass().add("svg-icon-group");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(lbl, spacer, ico);

        Label val = new Label(value);
        val.getStyleClass().add("kpi-value");
        Label subLbl = new Label(sub);
        subLbl.getStyleClass().add("kpi-sub");

        card.getChildren().addAll(top, val, subLbl);
        return card;
    }

    private VBox chartCard(String title, String meta, Region body) {
        VBox card = new VBox(10);
        card.getStyleClass().add("chart-card");

        VBox head = new VBox(2);
        head.getStyleClass().add("chart-head");
        Label t = new Label(title);
        t.getStyleClass().add("chart-title");
        Label m = new Label(meta);
        m.getStyleClass().add("chart-meta");
        head.getChildren().addAll(t, m);

        card.getChildren().addAll(head, body);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox doughnutPlaceholder() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        Label center = new Label("0");
        center.setStyle("-fx-font-family:'IBM Plex Mono';-fx-font-weight:700;-fx-font-size:28px;-fx-text-fill:#142a33;");
        box.getChildren().add(center);
        return box;
    }

    private VBox barPlaceholder() {
        VBox box = new VBox(8);
        box.setSpacing(8);
        box.getChildren().add(new Label("Sin datos del periodo"));
        return box;
    }

    private VBox dashCard(String title, String desc, String navKey, javafx.scene.Group icon) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dash-card");
        icon.getStyleClass().add("svg-icon-group");

        StackPane ico = new StackPane(icon);
        ico.getStyleClass().add("dash-icon");

        Label t = new Label(title);
        t.getStyleClass().add("dash-card-title");
        Label d = new Label(desc);
        d.getStyleClass().add("dash-card-desc");
        d.setWrapText(true);

        card.getChildren().addAll(ico, t, d);
        card.setOnMouseClicked(e -> onNavigate.accept(navKey));
        return card;
    }

    private Label title(String t) { Label l = new Label(t); l.getStyleClass().add("view-title"); return l; }
    private Label subtitle(String t) { Label l = new Label(t); l.getStyleClass().add("view-subtitle"); return l; }

    private int fetchAbiertos() { try { return tratamientoRepo.findByEstado("ABIERTO").size(); } catch (Exception e) { return 0; } }
    private int fetchOperadores() { try { return operadorRepo.findAll().size(); } catch (Exception e) { return 0; } }

    private String fetchUnidadesOcupadas() { try {
        int ocupadas = 0;
        for (var u : new UnidadRepository().findAll()) {
            var abiertos = tratamientoRepo.findAbiertoPorUnidad(u.getUnidadID());
            if (abiertos != null) ocupadas++;
        }
        return String.valueOf(ocupadas);
    } catch (Exception e) { return "0"; } }

    private int fetchAsistenciaHoy() { try {
        String hoy = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) FROM Asistencia WHERE Fecha = ? AND Estado = 'ACTIVO'";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoy);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    } catch (Exception e) { return 0; } }
}
