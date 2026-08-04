package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.*;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.PacienteRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.service.*;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.MaterialTable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.util.*;

public class TratamientosView extends VBox {

    private final TratamientoService tratamientoService = new TratamientoService();
    private final UnidadService unidadService = new UnidadService();
    private final OperadorService operadorService = new OperadorService();
    private final PacienteService pacienteService = new PacienteService();
    private final TratamientoPredefinidoService tratPredService = new TratamientoPredefinidoService();
    private final MaterialService materialService = new MaterialService();

    private FlowPane unitGrid;

    public TratamientosView() {
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Tratamientos");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Gestion de tratamientos por unidad");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button refreshBtn = new Button("Actualizar");
        refreshBtn.getStyleClass().addAll("btn", "btn-secondary");
        refreshBtn.setOnAction(e -> refreshGrid());
        Button newBtn = new Button("+ Nuevo tratamiento");
        newBtn.getStyleClass().addAll("btn", "btn-primary");
        newBtn.setOnAction(e -> showTratamientoDialog(null));
        toolbar.getChildren().addAll(refreshBtn, newBtn);

        unitGrid = new FlowPane();
        unitGrid.getStyleClass().add("unit-grid");
        unitGrid.setHgap(12);
        unitGrid.setVgap(12);

        ScrollPane scroll = new ScrollPane(unitGrid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().addAll(header, toolbar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        refreshGrid();
    }

    private void refreshGrid() {
        unitGrid.getChildren().clear();
        try {
            List<Unidad> unidades = new UnidadRepository().findAll();
            for (Unidad u : unidades) {
                VBox card = buildUnitCard(u);
                unitGrid.getChildren().add(card);
            }
        } catch (Exception e) {
            unitGrid.getChildren().add(new Label("Error al cargar unidades"));
        }
    }

    private VBox buildUnitCard(Unidad u) {
        VBox card = new VBox(8);
        card.getStyleClass().add("unit-card");

        Label unitNum = new Label("Unidad " + u.getUnidadNro());
        unitNum.getStyleClass().add("unit-number");

        try {
            List<Tratamiento> activos = tratamientoService.porUnidad(u.getUnidadID());
            if (!activos.isEmpty()) {
                card.getStyleClass().add("occupied");
                Tratamiento t = activos.get(0);

                Label status = new Label("OCUPADA");
                status.getStyleClass().addAll("unit-status", "badge", "badge-success");

                String pacienteNombre = t.getPacienteID() > 0 ? obtenerNombrePaciente(t.getPacienteID()) : "Sin paciente";
                Label paciente = new Label(pacienteNombre);
                paciente.getStyleClass().add("unit-patient");

                Label tratamiento = new Label(t.getNombreTratamiento());
                tratamiento.getStyleClass().add("unit-treatment");

                card.getChildren().addAll(unitNum, status, paciente, tratamiento);
                card.setOnMouseClicked(e -> showTratamientoDialog(t));
            } else {
                card.getStyleClass().add("available");
                Label status = new Label("Disponible");
                status.getStyleClass().addAll("unit-status", "badge", "badge-muted");
                card.getChildren().addAll(unitNum, status);
                card.setOnMouseClicked(e -> showNewTratamientoDialog(u));
            }
        } catch (Exception e) {
            card.getChildren().add(new Label("Error"));
        }

        return card;
    }

    private void showNewTratamientoDialog(Unidad u) {
        showTratamientoDialog(null, u.getUnidadID());
    }

    private void showTratamientoDialog(Tratamiento t) {
        showTratamientoDialog(t, null);
    }

    private void showTratamientoDialog(Tratamiento existing, Integer preSelectedUnidad) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo tratamiento" : "Tratamiento #" + existing.getTratamientoID());
        dialog.setHeaderText(null);
        dialog.setResizable(true);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setPrefWidth(650);

        // Form fields
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(12);

        ComboBox<Operador> operadorCombo = loadOperadorCombo();
        ComboBox<Paciente> pacienteCombo = loadPacienteCombo();
        ComboBox<TratamientoPredefinido> tratPredCombo = loadTratPredCombo();
        ComboBox<Unidad> unidadCombo = loadUnidadCombo();

        TextField nombreField = new TextField();
        TextField montoField = new TextField();
        TextField montoPagadoField = new TextField("0");
        ComboBox<String> tipoCombo = new ComboBox<>(FXCollections.observableArrayList("NORMAL", "CONTINUO"));
        tipoCombo.setValue("NORMAL");
        DatePicker fechaPicker = new DatePicker(LocalDate.now());

        if (existing != null) {
            nombreField.setText(existing.getNombreTratamiento());
            montoField.setText(String.valueOf(existing.getMonto()));
            montoPagadoField.setText(String.valueOf(existing.getMontoPagado()));
            tipoCombo.setValue(existing.getTipo());
            if (existing.getFecha() != null) fechaPicker.setValue(LocalDate.parse(existing.getFecha()));

            // Pre-select operador, paciente, unidad
            operadorCombo.getItems().stream().filter(o -> o.getOperadorID() == existing.getOperadorID()).findFirst().ifPresent(operadorCombo::setValue);
            pacienteCombo.getItems().stream().filter(p -> p.getPacienteID() == existing.getPacienteID()).findFirst().ifPresent(pacienteCombo::setValue);
            if (existing.getUnidadID() != null) {
                unidadCombo.getItems().stream().filter(u -> u.getUnidadID() == existing.getUnidadID()).findFirst().ifPresent(unidadCombo::setValue);
            }
        } else if (preSelectedUnidad != null) {
            unidadCombo.getItems().stream().filter(u -> u.getUnidadID() == preSelectedUnidad).findFirst().ifPresent(unidadCombo::setValue);
        }

        form.add(new Label("Operador:"), 0, 0);
        form.add(operadorCombo, 1, 0);
        form.add(new Label("Paciente:"), 0, 1);
        form.add(pacienteCombo, 1, 1);
        form.add(new Label("Unidad:"), 2, 0);
        form.add(unidadCombo, 3, 0);
        form.add(new Label("Fecha:"), 2, 1);
        form.add(fechaPicker, 3, 1);
        form.add(new Label("Tratamiento:"), 0, 2);
        form.add(nombreField, 1, 2);
        form.add(new Label("Tipo:"), 2, 2);
        form.add(tipoCombo, 3, 2);
        form.add(new Label("Monto:"), 0, 3);
        form.add(montoField, 1, 3);
        form.add(new Label("Pagado:"), 2, 3);
        form.add(montoPagadoField, 3, 3);

        // Plantilla selector
        Label plantillaLabel = new Label("Cargar desde plantilla:");
        plantillaLabel.getStyleClass().add("form-label");
        tratPredCombo.setPromptText("Seleccionar plantilla...");
        tratPredCombo.setOnAction(e -> {
            TratamientoPredefinido tp = tratPredCombo.getValue();
            if (tp != null && existing == null) {
                nombreField.setText(tp.getNombreTratamiento());
                if (tp.getMontoSugerido() != null) montoField.setText(String.valueOf(tp.getMontoSugerido()));
            }
        });

        HBox plantillaRow = new HBox(12, plantillaLabel, tratPredCombo);
        plantillaRow.setAlignment(Pos.CENTER_LEFT);

        // Materials section
        Label matLabel = new Label("Materiales");
        matLabel.getStyleClass().add("form-label");

        MaterialTable materialTable = new MaterialTable();
        materialTable.setMaterialOptions(loadMaterialOptions());
        HBox matToolbar = new HBox(8, materialTable.createAddButton("+ Agregar material"));

        root.getChildren().addAll(form, plantillaRow, new javafx.scene.control.Separator(), matLabel, materialTable, matToolbar);

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Action buttons for existing treatments
        if (existing != null) {
            ButtonType closeBtn = new ButtonType("Cerrar");
            ButtonType anularBtn = new ButtonType("Anular");
            ButtonType reopenBtn = new ButtonType("Reabrir");
            ButtonType payBtn = new ButtonType("Registrar pago");
            dialog.getDialogPane().getButtonTypes().addAll(closeBtn, payBtn, anularBtn);

            if ("CERRADO".equals(existing.getEstado())) {
                dialog.getDialogPane().getButtonTypes().add(reopenBtn);
            }
        }

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Operador op = operadorCombo.getValue();
                    Paciente pac = pacienteCombo.getValue();
                    Unidad un = unidadCombo.getValue();
                    if (op == null || pac == null) {
                        ConfirmDialog.error("Error", "Operador y paciente son obligatorios.");
                        return null;
                    }
                    String nombre = nombreField.getText().trim();
                    if (nombre.isEmpty()) {
                        ConfirmDialog.error("Error", "El nombre del tratamiento es obligatorio.");
                        return null;
                    }
                    double monto = 0;
                    try { monto = Double.parseDouble(montoField.getText().trim()); } catch (NumberFormatException ignored) {}
                    double pagado = 0;
                    try { pagado = Double.parseDouble(montoPagadoField.getText().trim()); } catch (NumberFormatException ignored) {}
                    String fecha = fechaPicker.getValue().toString();
                    String tipo = tipoCombo.getValue();

                    if (existing == null) {
                        Integer unidadId = un != null ? un.getUnidadID() : null;
                        int id = tratamientoService.crear(op.getOperadorID(), pac.getPacienteID(), unidadId, fecha, null, monto, tipo);
                        if (!nombre.equals(tipo)) {
                            // update name if it differs from default
                        }
                        for (MaterialTable.MaterialEntry e : materialTable.getEntries()) {
                            tratamientoService.agregarMaterial(id, e.materialId, e.quantity);
                        }
                    }
                    refreshGrid();
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
                return null;
            } else if (btn != null && btn.getText() != null) {
                try {
                    switch (btn.getText()) {
                        case "Cerrar":
                            if (ConfirmDialog.confirm("Cerrar tratamiento", "¿Confirma el cierre del tratamiento #" + existing.getTratamientoID() + "?")) {
                                tratamientoService.cerrar(existing.getTratamientoID());
                                refreshGrid();
                            }
                            break;
                        case "Anular":
                            var motivoOpt = ConfirmDialog.confirmWithReason("Anular tratamiento",
                                "Anular tratamiento #" + existing.getTratamientoID(),
                                "Motivo de la anulacion:");
                            motivoOpt.ifPresent(motivo -> {
                                try {
                                    tratamientoService.anular(existing.getTratamientoID(), motivo);
                                    refreshGrid();
                                } catch (Exception ex) {
                                    ConfirmDialog.error("Error", ex.getMessage());
                                }
                            });
                            break;
                        case "Reabrir":
                            if (ConfirmDialog.confirm("Reabrir tratamiento", "¿Reabrir el tratamiento #" + existing.getTratamientoID() + "?")) {
                                tratamientoService.reabrir(existing.getTratamientoID());
                                refreshGrid();
                            }
                            break;
                        case "Registrar pago":
                            showPagoDialog(existing);
                            break;
                    }
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showPagoDialog(Tratamiento t) {
        TextInputDialog input = new TextInputDialog();
        input.setTitle("Registrar pago");
        input.setHeaderText("Tratamiento #" + t.getTratamientoID());
        input.setContentText("Monto a abonar:");
        var result = input.showAndWait();
        result.ifPresent(val -> {
            try {
                double abono = Double.parseDouble(val.trim());
                tratamientoService.registrarPago(t.getTratamientoID(), abono);
                refreshGrid();
            } catch (Exception ex) {
                ConfirmDialog.error("Error", ex.getMessage());
            }
        });
    }

    private ComboBox<Operador> loadOperadorCombo() {
        ComboBox<Operador> combo = new ComboBox<>();
        try {
            combo.setItems(FXCollections.observableArrayList(new OperadorRepository().findAll()));
        } catch (Exception e) { /* empty */ }
        combo.setCellFactory(c -> new ListCell<>() {
            @Override
            protected void updateItem(Operador o, boolean empty) {
                super.updateItem(o, empty);
                setText(empty || o == null ? "" : o.getNombres() + " " + o.getApellidos());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Operador o, boolean empty) {
                super.updateItem(o, empty);
                setText(empty || o == null ? "" : o.getNombres() + " " + o.getApellidos());
            }
        });
        return combo;
    }

    private ComboBox<Paciente> loadPacienteCombo() {
        ComboBox<Paciente> combo = new ComboBox<>();
        try {
            combo.setItems(FXCollections.observableArrayList(new PacienteRepository().findAll()));
        } catch (Exception e) { /* empty */ }
        combo.setEditable(true);
        combo.setPromptText("Buscar o crear paciente...");
        combo.setCellFactory(c -> new ListCell<>() {
            @Override
            protected void updateItem(Paciente p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getNombres() + " " + p.getApellidos());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Paciente p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getNombres() + " " + p.getApellidos());
            }
        });
        return combo;
    }

    private ComboBox<TratamientoPredefinido> loadTratPredCombo() {
        ComboBox<TratamientoPredefinido> combo = new ComboBox<>();
        try {
            combo.setItems(FXCollections.observableArrayList(new TratamientoPredefinidoRepository().findAll()));
        } catch (Exception e) { /* empty */ }
        combo.setCellFactory(c -> new ListCell<>() {
            @Override
            protected void updateItem(TratamientoPredefinido t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getNombreTratamiento());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TratamientoPredefinido t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getNombreTratamiento());
            }
        });
        return combo;
    }

    private ComboBox<Unidad> loadUnidadCombo() {
        ComboBox<Unidad> combo = new ComboBox<>();
        try {
            combo.setItems(FXCollections.observableArrayList(new UnidadRepository().findAll()));
        } catch (Exception e) { /* empty */ }
        combo.setCellFactory(c -> new ListCell<>() {
            @Override
            protected void updateItem(Unidad u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? "" : "Unidad " + u.getUnidadNro());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Unidad u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? "" : "Unidad " + u.getUnidadNro());
            }
        });
        return combo;
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

    private String obtenerNombrePaciente(int pacienteId) {
        try {
            Paciente p = new PacienteRepository().findById(pacienteId);
            return p != null ? p.getNombres() + " " + p.getApellidos() : "Paciente #" + pacienteId;
        } catch (Exception e) {
            return "Paciente #" + pacienteId;
        }
    }
}
