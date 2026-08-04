package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.*;
import com.odontologia.formatos.repository.*;
import com.odontologia.formatos.service.AsistenciaService;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.SvgIcons;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class AsistenciaView extends VBox {

    private final AsistenciaService asistenciaService = new AsistenciaService();
    private final AsistenciaMaterialRepository asistenciaMaterialRepo = new AsistenciaMaterialRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final MaterialRepository materialRepository = new MaterialRepository();
    private Runnable onToast;

    private VBox step1Card;
    private VBox step2Card;
    private ObservableList<Docente> docentesList;
    private FilteredList<Docente> filteredDocentes;
    private DatePicker datePicker;
    private TextField searchDocenteField;
    private TableView<Docente> docentesTable;

    private List<MaterialRow> materialRows;
    private List<Integer> deletedMatAsistenciaIDs;
    private VBox materialsContainer;
    private Label materialesCountLabel;
    private ComboBox<Materiales> addMaterialCombo;
    private TextField addCantidadField;

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
        Label subtitle = new Label("Registro de asistencia diaria y consumo de materiales");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        step1Card = buildStep1Card();
        step2Card = new VBox();

        getChildren().addAll(header, step1Card);
        loadDocentes();
    }

    private VBox buildStep1Card() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Buscar docente");
        cardTitle.getStyleClass().add("card-header");

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox searchBox = new VBox(6);
        Label searchLabel = new Label("Docente");
        searchLabel.getStyleClass().add("form-label");
        searchDocenteField = new TextField();
        searchDocenteField.setPromptText("Buscar por nombre o apellido...");
        searchDocenteField.setPrefWidth(320);
        searchDocenteField.getStyleClass().add("form-input");
        searchBox.getChildren().addAll(searchLabel, searchDocenteField);

        VBox fechaBox = new VBox(6);
        Label fechaLabel = new Label("Fecha");
        fechaLabel.getStyleClass().add("form-label");
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(150);
        fechaBox.getChildren().addAll(fechaLabel, datePicker);

        searchRow.getChildren().addAll(searchBox, fechaBox);

        searchDocenteField.textProperty().addListener((obs, oldVal, newVal) -> applyDocenteFilter(newVal));

        docentesTable = buildDocentesTable();

        card.getChildren().addAll(cardTitle, searchRow, docentesTable);
        return card;
    }

    private TableView<Docente> buildDocentesTable() {
        TableView<Docente> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Docente, Number> colID = new TableColumn<>("ID");
        colID.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDocenteID()));
        colID.setPrefWidth(60);

        TableColumn<Docente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
        colNombre.setPrefWidth(160);

        TableColumn<Docente, String> colApellido = new TableColumn<>("Apellido");
        colApellido.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        colApellido.setPrefWidth(180);

        TableColumn<Docente, Void> colAccion = new TableColumn<>("");
        colAccion.setPrefWidth(120);
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Seleccionar");
            { btn.getStyleClass().addAll("btn", "btn-sm", "btn-primary"); }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    Docente d = getTableView().getItems().get(getIndex());
                    btn.setOnAction(e -> onSeleccionarDocente(d));
                    setGraphic(btn);
                }
            }
        });

        table.getColumns().addAll(colID, colNombre, colApellido, colAccion);
        return table;
    }

    private void applyDocenteFilter(String text) {
        if (filteredDocentes == null) return;
        if (text == null || text.isBlank()) filteredDocentes.setPredicate(d -> true);
        else {
            String lower = text.toLowerCase();
            filteredDocentes.setPredicate(d -> d.getNombres().toLowerCase().contains(lower) || d.getApellidos().toLowerCase().contains(lower));
        }
    }

    private void loadDocentes() {
        try {
            docentesList = FXCollections.observableArrayList(docenteRepository.findAll());
            filteredDocentes = new FilteredList<>(docentesList, d -> true);
            docentesTable.setItems(filteredDocentes);
        } catch (SQLException e) { /* empty */ }
    }

    private void onSeleccionarDocente(Docente docente) {
        String fecha = datePicker.getValue().toString();
        try {
            Asistencia asistencia = asistenciaService.abrirDia(docente.getDocenteID(), fecha);
            navigateToStep2(docente, fecha, asistencia);
        } catch (SQLException e) {
            ConfirmDialog.error("Error", "No se pudo abrir el registro: " + e.getMessage());
        }
    }

    private void navigateToStep2(Docente docente, String fecha, Asistencia asistencia) {
        getChildren().remove(step1Card);
        step2Card = buildStep2Card(docente, fecha, asistencia);
        getChildren().add(step2Card);
    }

    private void navigateToStep1() { getChildren().remove(step2Card); getChildren().add(step1Card); }

    private static class MaterialRow {
        final int matAsistenciaID, materialID;
        final String nombre, unidad;
        final TextField cantidadField;
        MaterialRow(int matAsistenciaID, int materialID, String nombre, String unidad, double cantidad) {
            this.matAsistenciaID = matAsistenciaID; this.materialID = materialID;
            this.nombre = nombre; this.unidad = unidad;
            this.cantidadField = new TextField(String.valueOf(cantidad));
            this.cantidadField.getStyleClass().add("form-input");
            this.cantidadField.setPrefWidth(100);
        }
    }

    private VBox buildStep2Card(Docente docente, String fecha, Asistencia asistencia) {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");

        HBox cardHeader = new HBox(12);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.getStyleClass().add("card-header");
        Label headerTitle = new Label("Registro del dia — " + docente.getNombres() + " " + docente.getApellidos() + " — " + fecha);
        HBox.setHgrow(headerTitle, Priority.ALWAYS);

        Circle led = new Circle(4);
        led.getStyleClass().addAll("led", "led-ok");
        Label badgeEstado = new Label("Registro activo");
        badgeEstado.getStyleClass().addAll("badge", "badge-success");
        HBox badgeBox = new HBox(6, led, badgeEstado);
        badgeBox.setAlignment(Pos.CENTER_LEFT);
        cardHeader.getChildren().addAll(headerTitle, badgeBox);

        HBox alertInfo = new HBox(10);
        alertInfo.setAlignment(Pos.CENTER_LEFT);
        alertInfo.getStyleClass().addAll("alert-banner", "alert-info");
        alertInfo.getChildren().addAll(SvgIcons.info(16), new Label("Este registro acumula todos los materiales del dia. Los cambios se reflejan en tiempo real al guardar."));

        Label sectionLabel = new Label("Materiales entregados hoy");
        sectionLabel.getStyleClass().addAll("form-label");
        sectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        materialsContainer = buildMaterialsContent(asistencia.getAsistenciaID());
        HBox addMaterialRow = buildAddMaterialRow();
        HBox footer = buildFooter(asistencia);

        card.getChildren().addAll(cardHeader, alertInfo, sectionLabel, materialsContainer,
            new Separator(), addMaterialRow, new Separator(), footer);
        return card;
    }

    private VBox buildMaterialsContent(int asistenciaID) {
        VBox container = new VBox(8);
        materialRows = new ArrayList<>();
        deletedMatAsistenciaIDs = new ArrayList<>();
        try {
            List<AsistenciaMaterial> entities = asistenciaMaterialRepo.findByAsistenciaID(asistenciaID);
            Map<Integer, String> names = new HashMap<>();
            try { for (Materiales m : materialRepository.findAll()) names.put(m.getMaterialID(), m.getNombre() + "|" + m.getUnidad()); } catch (Exception ignored) {}
            for (AsistenciaMaterial am : entities) {
                String[] info = names.getOrDefault(am.getMaterialID(), "Desconocido|").split("\\|", 2);
                materialRows.add(new MaterialRow(am.getMatAsistenciaID(), am.getMaterialID(), info[0], info.length > 1 ? info[1] : "", am.getCantidad()));
            }
        } catch (SQLException ignored) {}

        GridPane headerRow = new GridPane();
        headerRow.setHgap(12);
        Label hMat = new Label("Material"); hMat.getStyleClass().addAll("form-label", "text-muted");
        Label hCant = new Label("Cantidad"); hCant.getStyleClass().addAll("form-label", "text-muted");
        Label hUni = new Label("Unidad"); hUni.getStyleClass().addAll("form-label", "text-muted");
        headerRow.add(hMat, 0, 0); headerRow.add(hCant, 1, 0); headerRow.add(hUni, 2, 0);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(20);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(30);
        headerRow.getColumnConstraints().addAll(c1, c2, c3);

        container.getChildren().add(headerRow);
        rebuildMaterialRows();
        return container;
    }

    private void rebuildMaterialRows() {
        if (materialsContainer == null) return;
        while (materialsContainer.getChildren().size() > 1) materialsContainer.getChildren().remove(1);

        if (materialRows.isEmpty()) {
            Label empty = new Label("No hay materiales registrados hoy.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #5c7178;");
            materialsContainer.getChildren().add(empty);
        } else {
            for (MaterialRow row : materialRows) {
                HBox rowBox = new HBox(8);
                rowBox.setAlignment(Pos.CENTER_LEFT);
                rowBox.setStyle("-fx-padding: 4 0 4 0;");

                Label nombreLabel = new Label(row.nombre);
                nombreLabel.setPrefWidth(280);
                rowBox.getChildren().addAll(nombreLabel, row.cantidadField, new Label(row.unidad));

                Button trashBtn = new Button();
                Group trashIcon = SvgIcons.trash(14);
                trashIcon.setStyle("-fx-stroke:#c2403a;");
                trashBtn.setGraphic(trashIcon);
                trashBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
                trashBtn.setOnAction(e -> {
                    if (row.matAsistenciaID > 0) deletedMatAsistenciaIDs.add(row.matAsistenciaID);
                    materialRows.remove(row);
                    rebuildMaterialRows();
                    updateMaterialCount();
                });
                rowBox.getChildren().add(trashBtn);
                materialsContainer.getChildren().add(rowBox);
            }
        }
        updateMaterialCount();
    }

    private void updateMaterialCount() {
        if (materialesCountLabel == null) return;
        int count = materialRows != null ? materialRows.size() : 0;
        materialesCountLabel.setText(count + " material" + (count != 1 ? "es" : "") + " registrado" + (count != 1 ? "s" : ""));
    }

    private HBox buildAddMaterialRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        addMaterialCombo = new ComboBox<>();
        addMaterialCombo.setPromptText("Buscar material...");
        addMaterialCombo.setEditable(true);
        addMaterialCombo.setPrefWidth(260);
        try {
            List<Materiales> mats = materialRepository.findAll();
            addMaterialCombo.setItems(FXCollections.observableArrayList(mats));
            addMaterialCombo.setConverter(new StringConverter<>() {
                public String toString(Materiales m) { return m == null ? "" : m.getNombre(); }
                public Materiales fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}

        addCantidadField = new TextField();
        addCantidadField.setPromptText("Cantidad");
        addCantidadField.setPrefWidth(100);

        Button addBtn = new Button("Agregar");
        addBtn.getStyleClass().addAll("btn", "btn-success", "btn-sm");
        addBtn.setOnAction(e -> agregarMaterial());

        row.getChildren().addAll(addMaterialCombo, addCantidadField, addBtn);
        return row;
    }

    private void agregarMaterial() {
        Materiales mat = addMaterialCombo.getValue();
        if (mat == null) { ConfirmDialog.error("Error", "Seleccione un material de la lista."); return; }
        String ct = addCantidadField.getText().trim();
        if (ct.isEmpty()) { ConfirmDialog.error("Error", "Ingrese una cantidad."); return; }
        double cantidad;
        try { cantidad = Double.parseDouble(ct); } catch (NumberFormatException ex) {
            ConfirmDialog.error("Error", "Cantidad invalida."); return;
        }
        if (cantidad <= 0) { ConfirmDialog.error("Error", "La cantidad debe ser mayor a 0."); return; }

        MaterialRow existing = null;
        for (MaterialRow r : materialRows) { if (r.materialID == mat.getMaterialID()) { existing = r; break; } }
        if (existing != null) {
            double prev = Double.parseDouble(existing.cantidadField.getText());
            existing.cantidadField.setText(String.valueOf(prev + cantidad));
        } else {
            materialRows.add(new MaterialRow(0, mat.getMaterialID(), mat.getNombre(), mat.getUnidad(), cantidad));
        }
        rebuildMaterialRows();
        addMaterialCombo.setValue(null); addMaterialCombo.getEditor().clear();
        addCantidadField.clear();
    }

    private HBox buildFooter(Asistencia asistencia) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER);
        materialesCountLabel = new Label();
        updateMaterialCount();
        materialesCountLabel.getStyleClass().add("text-muted");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button volverBtn = new Button("Volver");
        volverBtn.getStyleClass().addAll("btn", "btn-ghost");
        volverBtn.setOnAction(e -> navigateToStep1());

        Button anularBtn = new Button("Anular asistencia");
        anularBtn.getStyleClass().addAll("btn", "btn-danger");
        anularBtn.setOnAction(e -> anularAsistencia(asistencia));

        Button guardarBtn = new Button("Guardar cambios");
        guardarBtn.getStyleClass().addAll("btn", "btn-primary");
        guardarBtn.setOnAction(e -> guardarCambios(asistencia.getAsistenciaID()));

        HBox rightBox = new HBox(8, volverBtn, anularBtn, guardarBtn);
        footer.getChildren().addAll(materialesCountLabel, spacer, rightBox);
        return footer;
    }

    private void guardarCambios(int asistenciaID) {
        try {
            for (int id : deletedMatAsistenciaIDs) asistenciaMaterialRepo.delete(id);
            deletedMatAsistenciaIDs.clear();

            for (MaterialRow row : materialRows) {
                double cantidad;
                try { cantidad = Double.parseDouble(row.cantidadField.getText().trim()); }
                catch (NumberFormatException ex) { ConfirmDialog.error("Error", "Cantidad invalida para \"" + row.nombre + "\"."); return; }
                if (cantidad <= 0) { ConfirmDialog.error("Error", "La cantidad de \"" + row.nombre + "\" debe ser mayor a 0."); return; }
                if (row.matAsistenciaID > 0) {
                    AsistenciaMaterial am = new AsistenciaMaterial(row.matAsistenciaID, asistenciaID, row.materialID, cantidad);
                    asistenciaMaterialRepo.update(am);
                } else {
                    asistenciaService.acumularMaterial(asistenciaID, row.materialID, cantidad);
                }
            }
            refreshMaterialsAfterSave(asistenciaID);
            if (onToast != null) onToast.run();
            ConfirmDialog.info("Guardado", "Los cambios se guardaron correctamente.");
        } catch (SQLException e) {
            ConfirmDialog.error("Error", "No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    private void refreshMaterialsAfterSave(int asistenciaID) {
        materialRows.clear();
        try {
            Map<Integer, String> names = new HashMap<>();
            for (Materiales m : materialRepository.findAll()) names.put(m.getMaterialID(), m.getNombre() + "|" + m.getUnidad());
            for (AsistenciaMaterial am : asistenciaMaterialRepo.findByAsistenciaID(asistenciaID)) {
                String[] info = names.getOrDefault(am.getMaterialID(), "Desconocido|").split("\\|", 2);
                materialRows.add(new MaterialRow(am.getMatAsistenciaID(), am.getMaterialID(), info[0], info.length > 1 ? info[1] : "", am.getCantidad()));
            }
        } catch (Exception ignored) {}
        rebuildMaterialRows();
    }

    private void anularAsistencia(Asistencia asistencia) {
        String docenteName = "Docente #" + asistencia.getDocenteID();
        for (Docente d : docentesList) { if (d.getDocenteID() == asistencia.getDocenteID()) { docenteName = d.getNombres() + " " + d.getApellidos(); break; } }

        var motivoOpt = ConfirmDialog.confirmWithReason("Anular asistencia", "Anular asistencia de " + docenteName, "Motivo de la anulacion:");
        motivoOpt.ifPresent(motivo -> {
            try {
                asistenciaService.anular(asistencia.getAsistenciaID(), motivo);
                if (onToast != null) onToast.run();
                ConfirmDialog.info("Anulado", "La asistencia fue anulada correctamente.");
                navigateToStep1();
            } catch (SQLException e) {
                ConfirmDialog.error("Error", "No se pudo anular la asistencia: " + e.getMessage());
            }
        });
    }
}
