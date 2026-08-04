package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.config.AppConfig;
import com.odontologia.formatos.export.ReporteEconomicoGenerator;
import com.odontologia.formatos.export.ReporteMaterialesGenerator;
import com.odontologia.formatos.service.ReporteService;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.MonthYearPicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.nio.file.Path;

public class ReportesView extends VBox {

    private final ReporteService reporteService = new ReporteService();
    private final ReporteMaterialesGenerator matGenerator = new ReporteMaterialesGenerator();
    private final ReporteEconomicoGenerator ecoGenerator = new ReporteEconomicoGenerator();

    private MonthYearPicker monthPicker;
    private ProgressBar progressBar;
    private Label statusLabel;
    private final Path carpetaReportes;

    public ReportesView() {
        carpetaReportes = Path.of(AppConfig.carpetaInicialReportes());
        carpetaReportes.toFile().mkdirs();
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(20);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Reportes");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Generar reportes en formato Excel");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        monthPicker = new MonthYearPicker();

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("view-subtitle");
        statusLabel.setVisible(false);

        VBox reportCard = new VBox(16);
        reportCard.getStyleClass().add("card");

        Label cardTitle = new Label("Reportes disponibles");
        cardTitle.getStyleClass().add("card-title");

        Button matBtn = buildReportButton("Reporte de Materiales",
            "Consumo de materiales por especialista, docente y operador (3 hojas)",
            () -> generarMateriales());

        Button ecoBtn = buildReportButton("Reporte Economico",
            "Ingresos generales y por operador (2 hojas)",
            () -> generarEconomico());

        reportCard.getChildren().addAll(cardTitle, monthPicker,
            new javafx.scene.control.Separator(),
            matBtn, ecoBtn, progressBar, statusLabel);

        getChildren().addAll(header, reportCard);
    }

    private Button buildReportButton(String title, String description, Runnable action) {
        Button btn = new Button();
        btn.getStyleClass().addAll("btn", "btn-primary", "btn-lg");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label d = new Label(description);
        d.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
        content.getChildren().addAll(t, d);

        btn.setGraphic(content);
        btn.setOnAction(e -> {
            progressBar.setVisible(true);
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            statusLabel.setVisible(true);
            statusLabel.setText("Generando reporte...");
            new Thread(() -> {
                try {
                    action.run();
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(1);
                        progressBar.setVisible(false);
                        statusLabel.setText("Reporte generado exitosamente.");
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        statusLabel.setText("Error: " + ex.getMessage());
                        ConfirmDialog.error("Error al generar reporte", ex.getMessage());
                    });
                }
            }).start();
        });
        return btn;
    }

    private void generarMateriales() {
        int anio = monthPicker.getYear();
        int mes = monthPicker.getMonth();
        try {
            matGenerator.generar(anio, mes, carpetaReportes);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte de materiales: " + e.getMessage(), e);
        }
    }

    private void generarEconomico() {
        int anio = monthPicker.getYear();
        int mes = monthPicker.getMonth();
        try {
            ecoGenerator.generar(anio, mes, carpetaReportes);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte economico: " + e.getMessage(), e);
        }
    }
}
