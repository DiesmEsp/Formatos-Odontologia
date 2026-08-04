package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.Docente;
import com.odontologia.formatos.model.Materiales;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.service.AsistenciaService;
import com.odontologia.formatos.service.DocenteService;
import com.odontologia.formatos.service.MaterialService;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.MaterialTable;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaView extends VBox {

    private final AsistenciaService asistenciaService = new AsistenciaService();
    private final DocenteService docenteService = new DocenteService();
    private final MaterialService materialService = new MaterialService();

    private ComboBox<Docente> docenteCombo;
    private MaterialTable materialTable;
    private Runnable onToast;

    public AsistenciaView(Runnable onToast) {
        this.onToast = onToast;
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Asistencia Docente");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Registrar asistencia diaria y consumo de materiales");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card");

        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox docenteBox = new VBox(6);
        Label docenteLabel = new Label("Docente");
        docenteLabel.getStyleClass().add("form-label");
        docenteCombo = new ComboBox<>();
        docenteCombo.setPromptText("Seleccionar docente...");
        docenteCombo.setPrefWidth(320);
        docenteBox.getChildren().addAll(docenteLabel, docenteCombo);

        VBox fechaBox = new VBox(6);
        Label fechaLabel = new Label("Fecha");
        fechaLabel.getStyleClass().add("form-label");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(150);
        fechaBox.getChildren().addAll(fechaLabel, datePicker);

        topRow.getChildren().addAll(docenteBox, fechaBox);

        Label matLabel = new Label("Materiales consumidos");
        matLabel.getStyleClass().add("form-label");

        materialTable = new MaterialTable();
        materialTable.setMaterialOptions(loadMaterialOptions());

        HBox matToolbar = new HBox(8, materialTable.createAddButton("+ Agregar material"));

        Button abrirBtn = new Button("Abrir / Registrar asistencia");
        abrirBtn.getStyleClass().addAll("btn", "btn-primary", "btn-lg");
        abrirBtn.setMaxWidth(300);
        abrirBtn.setOnAction(e -> {
            Docente d = docenteCombo.getValue();
            if (d == null) {
                ConfirmDialog.error("Error", "Seleccione un docente.");
                return;
            }
            String fecha = datePicker.getValue().toString();
            try {
                asistenciaService.abrirDia(d.getDocenteID(), fecha);
                Map<Integer, Double> materiales = new HashMap<>();
                for (MaterialTable.MaterialEntry entry : materialTable.getEntries()) {
                    materiales.merge(entry.materialId, entry.quantity, Double::sum);
                }
                if (!materiales.isEmpty()) {
                    asistenciaService.registrarMateriales(d.getDocenteID(), fecha, materiales);
                }
                if (onToast != null) onToast.run();
                materialTable.clear();
            } catch (Exception ex) {
                ConfirmDialog.error("Error", ex.getMessage());
            }
        });

        Button anularBtn = new Button("Anular asistencia de hoy");
        anularBtn.getStyleClass().addAll("btn", "btn-danger");
        anularBtn.setMaxWidth(300);
        anularBtn.setOnAction(e -> {
            Docente d = docenteCombo.getValue();
            if (d == null) {
                ConfirmDialog.error("Error", "Seleccione un docente.");
                return;
            }
            String fecha = datePicker.getValue().toString();
            try {
                var asistencia = asistenciaService.abrirDia(d.getDocenteID(), fecha);
                var motivoOpt = ConfirmDialog.confirmWithReason("Anular asistencia",
                    "Anular asistencia de " + d.getNombres() + " " + d.getApellidos(),
                    "Motivo de la anulacion:");
                motivoOpt.ifPresent(motivo -> {
                    try {
                        asistenciaService.anular(asistencia.getAsistenciaID(), motivo);
                        if (onToast != null) onToast.run();
                    } catch (Exception ex) {
                        ConfirmDialog.error("Error", ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                ConfirmDialog.error("Error", ex.getMessage());
            }
        });

        HBox actions = new HBox(16, abrirBtn, anularBtn);

        formCard.getChildren().addAll(topRow, new javafx.scene.control.Separator(), matLabel, materialTable, matToolbar, actions);

        getChildren().addAll(header, formCard);

        loadDocentes();
    }

    private List<MaterialTable.MaterialOption> loadMaterialOptions() {
        try {
            return new MaterialRepository().findAll().stream()
                .map(m -> new MaterialTable.MaterialOption(m.getMaterialID(), m.getNombre()))
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private void loadDocentes() {
        try {
            List<Docente> docentes = new DocenteRepository().findAll();
            docenteCombo.setItems(FXCollections.observableArrayList(docentes));
        } catch (Exception e) {
            docenteCombo.setItems(FXCollections.observableArrayList());
        }
    }
}
