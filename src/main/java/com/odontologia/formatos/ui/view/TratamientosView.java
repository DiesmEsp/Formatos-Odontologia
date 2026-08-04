package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.*;
import com.odontologia.formatos.repository.*;
import com.odontologia.formatos.service.*;
import com.odontologia.formatos.ui.components.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class TratamientosView extends VBox {

    private final TratamientoService tratamientoService = new TratamientoService();
    private final UnidadService unidadService = new UnidadService();
    private final OperadorService operadorService = new OperadorService();
    private final PacienteService pacienteService = new PacienteService();
    private final TratamientoPredefinidoService tratPredService = new TratamientoPredefinidoService();
    private final MaterialService materialService = new MaterialService();

    private GridPane unitGrid;
    private StackPane detailOverlay;

    public TratamientosView() { build(); }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        header.getChildren().addAll(title("Tratamientos"), subtitle("Seleccione una unidad libre para iniciar un nuevo tratamiento"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button refreshBtn = new Button("Actualizar");
        refreshBtn.getStyleClass().addAll("btn", "btn-secondary");
        refreshBtn.setOnAction(e -> refreshGrid());
        toolbar.getChildren().add(refreshBtn);

        unitGrid = new GridPane();
        unitGrid.setHgap(12);
        unitGrid.setVgap(12);

        detailOverlay = new StackPane();
        detailOverlay.setVisible(false);
        detailOverlay.setManaged(false);
        detailOverlay.getStyleClass().add("content-area");

        ScrollPane scroll = new ScrollPane(unitGrid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        StackPane mainArea = new StackPane(scroll, detailOverlay);
        detailOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        getChildren().addAll(header, toolbar, mainArea);
        VBox.setVgrow(mainArea, Priority.ALWAYS);
        refreshGrid();
    }

    private void refreshGrid() {
        unitGrid.getChildren().clear();
        try {
            List<Unidad> unidades = new UnidadRepository().findAll();
            int col = 0;
            for (Unidad u : unidades) {
                VBox card = buildStationCard(u);
                unitGrid.add(card, col % 4, col / 4);
                col++;
            }
        } catch (Exception e) {
            unitGrid.add(new Label("Error al cargar unidades"), 0, 0);
        }
    }

    private VBox buildStationCard(Unidad u) {
        VBox card = new VBox(8);
        card.getStyleClass().add("station-card");

        try {
            Tratamiento activo = new TratamientoRepository().findAbiertoPorUnidad(u.getUnidadID());

            Label numBadge = new Label(String.format("#%02d", u.getUnidadNro()));
            numBadge.getStyleClass().add("station-num");
            HBox numRow = new HBox(numBadge);
            numRow.setAlignment(Pos.TOP_RIGHT);

            if (activo != null) {
                card.getStyleClass().add("ocupado");

                Region led = new Region();
                led.getStyleClass().addAll("led", "led-warn");
                Label status = new Label("EN CURSO");
                status.getStyleClass().add("station-status");
                HBox statusRow = new HBox(6, led, status);
                statusRow.setAlignment(Pos.CENTER_LEFT);

                VBox ticket = new VBox(6);
                ticket.getStyleClass().add("station-ticket");
                ticket.getChildren().addAll(
                    newLabel(activo.getNombreTratamiento(), "ticket-tipo"),
                    newLabel(obtenerNombreOperador(activo.getOperadorID()), "ticket-meta"),
                    newLabel(obtenerNombrePaciente(activo.getPacienteID()), "ticket-meta"),
                    newLabel("S/ " + String.format("%.2f", activo.getMonto()), "ticket-monto"));

                Button verBtn = new Button("Ver datos");
                verBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
                verBtn.setOnAction(e -> showDetailPanel(activo));
                ticket.getChildren().add(verBtn);

                card.getChildren().addAll(numRow, statusRow, ticket);

                Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
                card.getChildren().add(spacer);
            } else {
                card.getStyleClass().add("libre");

                Region led = new Region();
                led.getStyleClass().addAll("led", "led-ok");
                Label status = new Label("LIBRE");
                status.getStyleClass().add("station-status");
                HBox statusRow = new HBox(6, led, status);
                statusRow.setAlignment(Pos.CENTER_LEFT);

                card.getChildren().addAll(numRow, statusRow);

                Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
                card.getChildren().add(spacer);

                VBox emptyArea = new VBox();
                emptyArea.setAlignment(Pos.CENTER);

                Group plusIcon = SvgIcons.plus(15);
                plusIcon.getStyleClass().add("svg-icon-group");
                Button newBtn = new Button("Nuevo tratamiento");
                newBtn.getStyleClass().add("btn-station");
                newBtn.setGraphic(plusIcon);
                newBtn.setMaxWidth(Double.MAX_VALUE);
                newBtn.setOnAction(e -> showNewTratamientoDialog(u));
                emptyArea.getChildren().add(newBtn);
                card.getChildren().add(emptyArea);
            }
        } catch (Exception e) {
            card.getChildren().add(new Label("Error"));
        }
        return card;
    }

    private void showNewTratamientoDialog(Unidad u) { showTratamientoDialog(null, u.getUnidadID()); }

    private void showTratamientoDialog(Tratamiento existing, Integer preSelectedUnidad) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo tratamiento" : "Tratamiento #" + existing.getTratamientoID());
        dialog.setHeaderText(null);
        dialog.setResizable(true);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setPrefWidth(650);

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

        tipoCombo.setOnAction(e -> {
            if ("CONTINUO".equals(tipoCombo.getValue())) {
                montoField.setText("0"); montoField.setDisable(true);
            } else { montoField.setDisable(false); }
        });

        if (existing != null) {
            nombreField.setText(existing.getNombreTratamiento());
            montoField.setText(String.valueOf(existing.getMonto()));
            montoPagadoField.setText(String.valueOf(existing.getMontoPagado()));
            tipoCombo.setValue(existing.getTipo());
            if (existing.getFecha() != null) fechaPicker.setValue(LocalDate.parse(existing.getFecha()));
            selectById(operadorCombo, existing.getOperadorID(), Operador::getOperadorID);
            selectById(pacienteCombo, existing.getPacienteID(), Paciente::getPacienteID);
            if (existing.getUnidadID() != null) selectById(unidadCombo, existing.getUnidadID(), Unidad::getUnidadID);
        } else if (preSelectedUnidad != null) {
            selectById(unidadCombo, preSelectedUnidad, Unidad::getUnidadID);
        }

        addFormRow(form, "Operador", 0, operadorCombo);
        addFormRow(form, "Paciente", 1, pacienteCombo);
        addFormRow(form, "Unidad", 2, unidadCombo);
        addFormRow(form, "Fecha", 3, fechaPicker);
        addFormRow(form, "Nombre tratamiento", 4, nombreField);
        addFormRow(form, "Tipo", 5, tipoCombo);
        addFormRow(form, "Monto", 6, montoField);
        addFormRow(form, "Pagado", 7, montoPagadoField);

        Label plantillaLabel = new Label("Cargar desde plantilla:");
        plantillaLabel.getStyleClass().add("form-label");
        tratPredCombo.setPromptText("Seleccionar plantilla...");

        MaterialTable materialTable = new MaterialTable();
        materialTable.setMaterialOptions(loadMaterialOptions());

        tratPredCombo.setOnAction(e -> {
            TratamientoPredefinido tp = tratPredCombo.getValue();
            if (tp != null && existing == null) {
                nombreField.setText(tp.getNombreTratamiento());
                if (tp.getMontoSugerido() != null && !"CONTINUO".equals(tipoCombo.getValue())) montoField.setText(String.valueOf(tp.getMontoSugerido()));
                try {
                    materialTable.clear();
                    for (TratamientoPredefinidoMaterial m : tratPredService.materiales(tp.getTratPredID()))
                        materialTable.addRow(m.getMaterialID(), obtenerNombreMaterial(m.getMaterialID()), m.getCantidad());
                } catch (Exception ignored) {}
            }
        });

        if (existing != null) {
            try {
                for (var m : tratamientoService.materialesConNombre(existing.getTratamientoID()))
                    materialTable.addRow(m.getMaterialID(), m.getNombre(), m.getCantidad());
            } catch (Exception ignored) {}
        }

        HBox plantillaRow = new HBox(12, plantillaLabel, tratPredCombo);
        plantillaRow.setAlignment(Pos.CENTER_LEFT);

        Button newMatBtn = new Button("+ Nuevo material");
        newMatBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
        newMatBtn.setOnAction(e -> {
            quickNewMaterial();
            materialTable.setMaterialOptions(loadMaterialOptions());
        });

        HBox matToolbar = new HBox(8, materialTable.createAddButton("+ Agregar material"), newMatBtn);

        root.getChildren().addAll(form, plantillaRow, new Separator(), new Label("Materiales"), materialTable, matToolbar);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                Operador op = operadorCombo.getValue();
                Paciente pac = pacienteCombo.getValue();
                if (op == null || pac == null) { ConfirmDialog.error("Error", "Operador y paciente son obligatorios."); return null; }
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) { ConfirmDialog.error("Error", "El nombre del tratamiento es obligatorio."); return null; }
                double monto = parseDouble(montoField.getText(), 0);
                double pagado = parseDouble(montoPagadoField.getText(), 0);
                String fecha = fechaPicker.getValue().toString();
                String tipo = tipoCombo.getValue();
                Integer unidadId = unidadCombo.getValue() != null ? unidadCombo.getValue().getUnidadID() : null;

                if (existing == null) {
                    TratamientoPredefinido tp = tratPredCombo.getValue();
                    Integer tratPredID = tp != null ? tp.getTratPredID() : null;
                    int id = tratamientoService.crear(op.getOperadorID(), pac.getPacienteID(), unidadId, fecha, tratPredID, monto, tipo);
                    if (tratPredID == null) {
                        for (var e : materialTable.getEntries())
                            tratamientoService.agregarMaterial(id, e.materialId, e.quantity);
                    }
                    if (pagado > 0 && !"CONTINUO".equals(tipo)) tratamientoService.registrarPago(id, pagado);
                }
                refreshGrid();
                hideDetailPanel();
            } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
            return null;
        });
        dialog.showAndWait();
    }

    private void showDetailPanel(Tratamiento t) {
        detailOverlay.getChildren().clear();
        VBox panel = new VBox(16);
        panel.getStyleClass().add("content-area");
        panel.setPadding(new Insets(24, 28, 24, 28));
        panel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        try {
            Tratamiento r = tratamientoService.buscarPorId(t.getTratamientoID());

            HBox hRow = new HBox(12);
            hRow.setAlignment(Pos.CENTER_LEFT);
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            Button closeBtn = new Button();
            closeBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
            closeBtn.setGraphic(icon(SvgIcons.close(16)));
            closeBtn.setOnAction(e -> hideDetailPanel());
            hRow.getChildren().addAll(newLabel("Detalle del Tratamiento #" + r.getTratamientoID(), "view-title"), spacer, closeBtn);

            Operador op = new OperadorRepository().findById(r.getOperadorID());
            String paciente = obtenerNombrePaciente(r.getPacienteID());

            VBox infoCard = new VBox(8);
            infoCard.getStyleClass().add("card");
            infoCard.getStyleClass().add("station-ticket");

            infoCard.getChildren().addAll(
                infoRow("Paciente", paciente),
                infoRow("Especialista", op.getNombres() + " " + op.getApellidos() + " (" + op.getGrado() + "-" + op.getTipo() + ")"),
                infoRow("Tratamiento", r.getNombreTratamiento()),
                infoRow("Monto", "S/ " + String.format("%.2f", r.getMonto())),
                infoRow("Estado", r.getEstado()),
                infoRow("Tipo", r.getTipo()),
                infoRow("Pago", r.getEstadoPago() + " (S/ " + String.format("%.2f", r.getMontoPagado()) + ")")
            );

            MaterialTable materialTable = new MaterialTable();
            materialTable.setMaterialOptions(loadMaterialOptions());
            try {
                for (var m : tratamientoService.materialesConNombre(r.getTratamientoID()))
                    materialTable.addRow(m.getMaterialID(), m.getNombre(), m.getCantidad());
            } catch (Exception ignored) {}

            HBox matToolbar = new HBox(8, materialTable.createAddButton("+ Agregar material"));

            HBox footer = new HBox(12);
            footer.setAlignment(Pos.CENTER_RIGHT);

            Button anularBtn = new Button("Anular");
            anularBtn.getStyleClass().addAll("btn", "btn-danger");
            anularBtn.setOnAction(ev -> {
                var motivoOpt = ConfirmDialog.confirmWithReason("Anular", "Anular tratamiento #" + r.getTratamientoID(), "Motivo:");
                motivoOpt.ifPresent(motivo -> {
                    try { tratamientoService.anular(r.getTratamientoID(), motivo); hideDetailPanel(); refreshGrid(); }
                    catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                });
            });

            Button payBtn = new Button("Registrar pago");
            payBtn.getStyleClass().addAll("btn", "btn-secondary");
            payBtn.setOnAction(ev -> {
                TextInputDialog inp = new TextInputDialog();
                inp.setTitle("Registrar pago");
                inp.setContentText("Monto:");
                inp.showAndWait().ifPresent(val -> {
                    try {
                        double abono = Double.parseDouble(val.trim());
                        tratamientoService.registrarPago(r.getTratamientoID(), abono);
                        showDetailPanel(r);
                    } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                });
            });

            Button toggleBtn;
            if ("CERRADO".equals(r.getEstado())) {
                toggleBtn = new Button("Reabrir");
                toggleBtn.getStyleClass().addAll("btn", "btn-secondary");
                toggleBtn.setOnAction(ev -> {
                    if (ConfirmDialog.confirm("Reabrir", "Reabrir #" + r.getTratamientoID() + "?")) {
                        try { tratamientoService.reabrir(r.getTratamientoID()); showDetailPanel(r); }
                        catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                    }
                });
            } else {
                toggleBtn = new Button("Cerrar");
                toggleBtn.getStyleClass().addAll("btn", "btn-secondary");
                toggleBtn.setOnAction(ev -> {
                    if (ConfirmDialog.confirm("Cerrar", "Cerrar #" + r.getTratamientoID() + "?")) {
                        try { tratamientoService.cerrar(r.getTratamientoID()); hideDetailPanel(); refreshGrid(); }
                        catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                    }
                });
            }

            footer.getChildren().addAll(anularBtn, payBtn, toggleBtn);

            panel.getChildren().addAll(hRow, infoCard, newLabel("Materiales", "form-label"), materialTable, matToolbar, footer);
        } catch (Exception e) {
            panel.getChildren().add(new Label("Error al cargar detalle"));
        }

        detailOverlay.getChildren().add(panel);
        detailOverlay.setVisible(true);
        detailOverlay.setManaged(true);
    }

    private void hideDetailPanel() { detailOverlay.setVisible(false); detailOverlay.setManaged(false); detailOverlay.getChildren().clear(); }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(12);
        Label l = new Label(label + ":");
        l.getStyleClass().add("form-label");
        Label v = new Label(value);
        row.getChildren().addAll(l, v);
        return row;
    }

    private void quickNewMaterial() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Nuevo material");
        d.setHeaderText(null);
        VBox c = new VBox(12);
        c.setPadding(new Insets(16));
        TextField n = new TextField(); n.setPromptText("Nombre");
        TextField u = new TextField(); u.setPromptText("Unidad"); u.setText("u.");
        c.getChildren().addAll(new Label("Nombre:"), n, new Label("Unidad:"), u);
        d.getDialogPane().setContent(c);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !n.getText().trim().isEmpty()) {
                try { materialService.crear(n.getText().trim(), u.getText().trim().isEmpty() ? "u." : u.getText().trim()); }
                catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
            }
            return null;
        });
        d.showAndWait();
    }

    private <T> void selectById(ComboBox<T> combo, int id, java.util.function.ToIntFunction<T> getter) {
        combo.getItems().stream().filter(x -> getter.applyAsInt(x) == id).findFirst().ifPresent(combo::setValue);
    }

    private void addFormRow(GridPane form, String label, int row, Control field) {
        form.add(new Label(label + ":"), 0, row);
        form.add(field, 1, row);
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return def; }
    }

    private Group icon(Group g) { g.getStyleClass().add("svg-icon-group"); return g; }
    private Label title(String t) { Label l = new Label(t); l.getStyleClass().add("view-title"); return l; }
    private Label subtitle(String t) { Label l = new Label(t); l.getStyleClass().add("view-subtitle"); return l; }
    private Label newLabel(String t, String styleClass) { Label l = new Label(t); l.getStyleClass().add(styleClass); return l; }

    private ComboBox<Operador> loadOperadorCombo() {
        ComboBox<Operador> c = new ComboBox<>();
        try { c.setItems(FXCollections.observableArrayList(new OperadorRepository().findAll())); } catch (Exception e) {}
        c.setCellFactory(lv -> cell(o -> o.getNombres() + " " + o.getApellidos()));
        c.setButtonCell(cell(o -> o.getNombres() + " " + o.getApellidos()));
        return c;
    }

    private ComboBox<Paciente> loadPacienteCombo() {
        ComboBox<Paciente> c = new ComboBox<>();
        try { c.setItems(FXCollections.observableArrayList(new PacienteRepository().findAll())); } catch (Exception e) {}
        c.setCellFactory(lv -> cell(p -> p.getNombres() + " " + p.getApellidos()));
        c.setButtonCell(cell(p -> p.getNombres() + " " + p.getApellidos()));
        return c;
    }

    private ComboBox<TratamientoPredefinido> loadTratPredCombo() {
        ComboBox<TratamientoPredefinido> c = new ComboBox<>();
        try { c.setItems(FXCollections.observableArrayList(new TratamientoPredefinidoRepository().findAll())); } catch (Exception e) {}
        c.setCellFactory(lv -> cell(TratamientoPredefinido::getNombreTratamiento));
        c.setButtonCell(cell(TratamientoPredefinido::getNombreTratamiento));
        return c;
    }

    private ComboBox<Unidad> loadUnidadCombo() {
        ComboBox<Unidad> c = new ComboBox<>();
        try { c.setItems(FXCollections.observableArrayList(new UnidadRepository().findAll())); } catch (Exception e) {}
        c.setCellFactory(lv -> cell(u -> "Unidad " + u.getUnidadNro()));
        c.setButtonCell(cell(u -> "Unidad " + u.getUnidadNro()));
        return c;
    }

    private <T> javafx.scene.control.ListCell<T> cell(java.util.function.Function<T, String> fn) {
        return new javafx.scene.control.ListCell<>() {
            protected void updateItem(T item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : fn.apply(item)); }
        };
    }

    private List<MaterialTable.MaterialOption> loadMaterialOptions() {
        try { return new MaterialRepository().findAll().stream().map(m -> new MaterialTable.MaterialOption(m.getMaterialID(), m.getNombre())).toList(); }
        catch (Exception e) { return List.of(); }
    }

    private String obtenerNombrePaciente(int id) {
        try { Paciente p = new PacienteRepository().findById(id); return p != null ? p.getNombres() + " " + p.getApellidos() : "Paciente #" + id; }
        catch (Exception e) { return "Paciente #" + id; }
    }

    private String obtenerNombreOperador(int id) {
        try { Operador o = new OperadorRepository().findById(id); return o != null ? o.getNombres() + " " + o.getApellidos() : "Operador #" + id; }
        catch (Exception e) { return "Operador #" + id; }
    }

    private String obtenerNombreMaterial(int id) {
        try { Materiales m = new MaterialRepository().findById(id); return m != null ? m.getNombre() : "Material #" + id; }
        catch (Exception e) { return "Material #" + id; }
    }
}
