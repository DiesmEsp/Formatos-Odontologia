package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.config.AppConfig;
import com.odontologia.formatos.export.ReporteEconomicoGenerator;
import com.odontologia.formatos.export.ReporteMaterialesGenerator;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.MonthYearPicker;
import com.odontologia.formatos.ui.components.SvgIcons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.application.Platform;

public class ReportesView extends VBox {

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
        header.getChildren().addAll(title("Reportes"), subtitle("Exportacion de reportes mensuales y anuales a Excel"));

        monthPicker = new MonthYearPicker();

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("view-subtitle");
        statusLabel.setVisible(false);

        VBox periodCard = new VBox(16);
        periodCard.getStyleClass().add("card");
        periodCard.getChildren().addAll(cardTitle("Periodo del reporte"), monthPicker);

        VBox reportsCard = new VBox(16);
        reportsCard.getStyleClass().add("card");
        String periodo = monthPicker.getMonthName() + " " + monthPicker.getYear();
        reportsCard.getChildren().add(cardTitle("Reportes mensuales — " + periodo));

        GridPane reportGrid = new GridPane();
        reportGrid.setHgap(12);
        reportGrid.setVgap(12);
        ColumnConstraints rc1 = new ColumnConstraints(); rc1.setPercentWidth(50);
        ColumnConstraints rc2 = new ColumnConstraints(); rc2.setPercentWidth(50);
        reportGrid.getColumnConstraints().addAll(rc1, rc2);

        reportGrid.add(reportCard("Materiales Generales",
            "Consolidado de materiales consumidos en el mes, convertidos a unidad base",
            SvgIcons.reportes(18), () -> generarMateriales()), 0, 0);

        reportGrid.add(reportCard("Ingresos Financieros",
            "Montos facturados por grado y tipo de especialista, con total, pagado y pendiente",
            SvgIcons.reportes(18), () -> generarEconomico()), 1, 0);

        reportGrid.add(reportCard("Consumo Docente",
            "Materiales entregados por docente (consolidado + detalle diario)",
            SvgIcons.docente(18), () -> generarMateriales()), 0, 1);

        reportGrid.add(reportCard("Consumo Especialista",
            "Materiales consumidos por especialista en tratamientos cerrados",
            SvgIcons.user(18), () -> generarMateriales()), 1, 1);

        reportsCard.getChildren().add(reportGrid);

        VBox annualCard = new VBox(16);
        annualCard.getStyleClass().add("card");
        annualCard.getChildren().add(cardTitle("Reporte anual"));

        VBox annualReport = new VBox(10);
        annualReport.getStyleClass().add("report-card");
        annualReport.getStyleClass().add("report-icon");

        Group annualIcon = SvgIcons.clock(18);
        StackPane annualIconBox = new StackPane(annualIcon);
        annualIconBox.getStyleClass().add("report-icon");

        Label annualTitle = new Label("Consolidado Anual");
        annualTitle.getStyleClass().add("report-title");
        Label annualDesc = new Label("Los 4 reportes anteriores desglosados mes por mes para el ano seleccionado");
        annualDesc.getStyleClass().add("report-desc");
        annualDesc.setWrapText(true);

        Button annualBtn = new Button("Generar Reporte Anual " + monthPicker.getYear());
        annualBtn.getStyleClass().addAll("btn", "btn-primary", "btn-lg");
        annualBtn.setGraphic(SvgIcons.download(16));
        annualBtn.setOnAction(e -> generarAnual());

        annualReport.getChildren().addAll(annualIconBox, annualTitle, annualDesc, annualBtn);
        annualCard.getChildren().add(annualReport);

        VBox recentCard = new VBox(16);
        recentCard.getStyleClass().add("card");
        recentCard.getChildren().add(cardTitle("Reportes generados recientemente"));
        TableView<FileEntry> recentTable = buildRecentTable();
        recentCard.getChildren().add(recentTable);

        Button refreshRecentBtn = new Button("Actualizar lista");
        refreshRecentBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
        refreshRecentBtn.setOnAction(e -> loadRecentFiles(recentTable));
        recentCard.getChildren().add(refreshRecentBtn);

        getChildren().addAll(header, periodCard, reportsCard, annualCard, progressBar, statusLabel, recentCard);
        Platform.runLater(() -> loadRecentFiles(recentTable));
    }

    private VBox reportCard(String title, String desc, Group icon, Runnable action) {
        VBox card = new VBox(10);
        card.getStyleClass().add("report-card");

        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("report-icon");
        Label t = new Label(title);
        t.getStyleClass().add("report-title");
        Label d = new Label(desc);
        d.getStyleClass().add("report-desc");
        d.setWrapText(true);

        Button btn = new Button("Generar Excel");
        btn.getStyleClass().addAll("btn", "btn-sm", "btn-primary");
        btn.setGraphic(SvgIcons.download(14));
        btn.setOnAction(e -> startAsync(action));

        card.getChildren().addAll(iconBox, t, d, btn);
        return card;
    }

    private TableView<FileEntry> buildRecentTable() {
        TableView<FileEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FileEntry, String> colArchivo = new TableColumn<>("Archivo");
        colArchivo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().name));
        colArchivo.setPrefWidth(300);

        TableColumn<FileEntry, String> colPeriodo = new TableColumn<>("Periodo");
        colPeriodo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().periodo));
        colPeriodo.setPrefWidth(120);

        TableColumn<FileEntry, String> colFecha = new TableColumn<>("Generado");
        colFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().generado));
        colFecha.setPrefWidth(150);

        TableColumn<FileEntry, Void> colAccion = new TableColumn<>("");
        colAccion.setPrefWidth(80);
        colAccion.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final Button btn = new Button("Abrir");
            { btn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary"); }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    FileEntry fe = getTableView().getItems().get(getIndex());
                    btn.setOnAction(e -> {
                        try { java.awt.Desktop.getDesktop().open(fe.file); }
                        catch (IOException ex) { ConfirmDialog.error("Error", "No se pudo abrir el archivo."); }
                    });
                    setGraphic(btn);
                }
            }
        });

        table.getColumns().addAll(colArchivo, colPeriodo, colFecha, colAccion);
        return table;
    }

    private void loadRecentFiles(TableView<FileEntry> table) {
        File[] files = carpetaReportes.toFile().listFiles(f -> f.getName().endsWith(".xlsx"));
        if (files == null) { table.setItems(javafx.collections.FXCollections.observableArrayList()); return; }
        List<FileEntry> entries = Arrays.stream(files)
            .sorted((a, b) -> Long.compare(b.lastModified(), a.lastModified()))
            .limit(10)
            .map(f -> {
                String periodo = f.getName().replaceAll(".*_(\\d{4})\\.xlsx$", "$1");
                String generado = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(f.lastModified()));
                return new FileEntry(f.getName(), periodo, generado, f);
            })
            .toList();
        table.setItems(javafx.collections.FXCollections.observableArrayList(entries));
    }

    private void startAsync(Runnable action) {
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
                    ConfirmDialog.error("Error", ex.getMessage());
                });
            }
        }).start();
    }

    private void generarMateriales() {
        int anio = monthPicker.getYear();
        int mes = monthPicker.getMonth();
        try { matGenerator.generar(anio, mes, carpetaReportes); }
        catch (Exception e) { throw new RuntimeException("Error al generar reporte de materiales: " + e.getMessage(), e); }
    }

    private void generarEconomico() {
        int anio = monthPicker.getYear();
        int mes = monthPicker.getMonth();
        try { ecoGenerator.generar(anio, mes, carpetaReportes); }
        catch (Exception e) { throw new RuntimeException("Error al generar reporte economico: " + e.getMessage(), e); }
    }

    private void generarAnual() {
        int anio = monthPicker.getYear();
        try {
            matGenerator.generar(anio, 1, 12, carpetaReportes);
            ecoGenerator.generar(anio, 1, 12, carpetaReportes);
        } catch (Exception e) { throw new RuntimeException("Error al generar reporte anual: " + e.getMessage(), e); }
    }

    private Label title(String t) { Label l = new Label(t); l.getStyleClass().add("view-title"); return l; }
    private Label subtitle(String t) { Label l = new Label(t); l.getStyleClass().add("view-subtitle"); return l; }
    private Label cardTitle(String t) { Label l = new Label(t); l.getStyleClass().add("card-title"); return l; }

    private record FileEntry(String name, String periodo, String generado, File file) {}
}
