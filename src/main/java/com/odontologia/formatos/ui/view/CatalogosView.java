package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.*;
import com.odontologia.formatos.repository.*;
import com.odontologia.formatos.service.*;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.MaterialTable;
import com.odontologia.formatos.ui.components.SvgIcons;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogosView extends VBox {

    private final MaterialService materialService = new MaterialService();
    private final DocenteService docenteService = new DocenteService();
    private final OperadorService operadorService = new OperadorService();
    private final TratamientoPredefinidoService tratPredService = new TratamientoPredefinidoService();

    private TableView<Materiales> materialesTable;
    private TableView<Docente> docentesTable;
    private TableView<Operador> operadoresTable;
    private TableView<TratamientoPredefinido> tratPredTable;
    private TableView<Tratamiento> tratRealizadosTable;

    private Label materialesCountLabel;
    private Label operadoresCountLabel;

    private Runnable onRefresh;

    private final Set<Integer> expandedTratPred = new HashSet<>();
    private final Set<Integer> expandedTratRealizados = new HashSet<>();
    private VBox tratPredDetailPane;
    private VBox tratRealizadosDetailPane;

    private Map<Integer, String> pacienteCache = new HashMap<>();
    private Map<Integer, Operador> operadorCache = new HashMap<>();
    private Map<Integer, String> unidadCache = new HashMap<>();
    private Map<Integer, List<TratamientoMaterialRepository.MaterialConCantidad>> matRealizadosCache = new HashMap<>();

    public CatalogosView(Runnable onRefresh) {
        this.onRefresh = onRefresh;
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Catalogos");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Administrar materiales, docentes, especialistas, tratamientos predefinidos y realizados");
        subtitle.getStyleClass().add("view-subtitle");

        HBox headerButtons = new HBox(8);
        headerButtons.setAlignment(Pos.CENTER_LEFT);

        Button btnMaterial = new Button("Material");
        btnMaterial.getStyleClass().addAll("btn", "btn-primary");
        btnMaterial.setOnAction(e -> showMaterialDialog(null));

        Button btnDocente = new Button("Docente");
        btnDocente.getStyleClass().addAll("btn", "btn-secondary");
        btnDocente.setOnAction(e -> showDocenteDialog(null));

        Button btnEspecialista = new Button("Especialista");
        btnEspecialista.getStyleClass().addAll("btn", "btn-secondary");
        btnEspecialista.setOnAction(e -> showOperadorDialog(null));

        Button btnTratPred = new Button("Trat. Predef.");
        btnTratPred.getStyleClass().addAll("btn", "btn-secondary");
        btnTratPred.setOnAction(e -> showTratPredDialog(null));

        headerButtons.getChildren().addAll(btnMaterial, btnDocente, btnEspecialista, btnTratPred);
        header.getChildren().addAll(title, subtitle, headerButtons);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
            buildMaterialesTab(),
            buildDocentesTab(),
            buildEspecialistasTab(),
            buildTratamientosPredTab(),
            buildTratamientosRealizadosTab()
        );

        getChildren().addAll(header, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }

    // ======================== MATERIALES TAB ========================

    private Tab buildMaterialesTab() {
        Tab tab = new Tab("Materiales");
        tab.getStyleClass().add("tab");

        materialesTable = new TableView<>();
        materialesTable.getStyleClass().add("table-view");

        TableColumn<Materiales, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getMaterialID())));
        colId.setPrefWidth(60);
        colId.getStyleClass().add("num");
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'IBM Plex Mono';");
                }
            }
        });

        TableColumn<Materiales, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(280);

        TableColumn<Materiales, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidad()));
        colUnidad.setPrefWidth(120);

        TableColumn<Materiales, Void> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Materiales m = getTableView().getItems().get(getIndex());
                    HBox badge = new HBox(6);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    Region led = new Region();
                    led.getStyleClass().add("led");
                    Label lbl = new Label("Activo");
                    if (m.getEstado() == 1) {
                        led.getStyleClass().add("led-ok");
                        badge.getStyleClass().add("badge-success");
                        lbl.setText("Activo");
                    } else {
                        led.getStyleClass().add("led-warn");
                        badge.getStyleClass().add("badge-neutral");
                        lbl.setText("Inactivo");
                    }
                    badge.getStyleClass().add("badge");
                    badge.getChildren().addAll(led, lbl);
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Materiales, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("\u270E");
            private final HBox box = new HBox(6, editBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
                editBtn.setTooltip(new Tooltip("Editar"));
                box.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Materiales m = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showMaterialDialog(m));
                    setGraphic(box);
                }
            }
        });

        materialesTable.getColumns().addAll(colId, colNombre, colUnidad, colEstado, colAcciones);

        HBox filterBar = new HBox(12);
        filterBar.getStyleClass().add("filter-bar");

        HBox searchBox = new HBox(9);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Group searchIcon = SvgIcons.search(16);
        searchIcon.getStyleClass().add("svg-icon-group");
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar materiales...");
        searchField.setPrefWidth(240);
        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        materialesCountLabel = new Label("Mostrando 0 de 0");
        materialesCountLabel.getStyleClass().addAll("text-muted", "text-sm");

        filterBar.getChildren().addAll(searchBox, spacer, materialesCountLabel);

        searchField.textProperty().addListener((obs, o, n) -> loadMateriales(n));

        VBox content = new VBox(12, filterBar, materialesTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(materialesTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) loadMateriales(null); });
        return tab;
    }

    private void showMaterialDialog(Materiales existing) {
        Dialog<Materiales> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo material" : "Editar material");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("card");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));

        Label nameLabel = new Label("Nombre");
        nameLabel.getStyleClass().add("form-label");
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del material");
        nombreField.setPrefWidth(320);

        Label unitLabel = new Label("Unidad");
        unitLabel.getStyleClass().add("form-label");
        TextField unidadField = new TextField();
        unidadField.setPromptText("Unidad (ej: g, ml, uds)");
        unidadField.setPrefWidth(320);

        if (existing != null) {
            nombreField.setText(existing.getNombre());
            unidadField.setText(existing.getUnidad());
        }

        grid.add(nameLabel, 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(unitLabel, 0, 1);
        grid.add(unidadField, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(80);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                String unidad = unidadField.getText().trim();
                if (nombre.isEmpty()) return null;
                try {
                    if (existing == null) {
                        materialService.crear(nombre, unidad.isEmpty() ? "uds" : unidad);
                    } else {
                        existing.setNombre(nombre);
                        existing.setUnidad(unidad.isEmpty() ? "uds" : unidad);
                        materialService.actualizar(existing);
                    }
                    loadMateriales(searchField().map(TextField::getText).orElse(null));
                    if (onRefresh != null) onRefresh.run();
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private Optional<TextField> searchField() {
        if (materialesTable != null && materialesTable.getParent() instanceof VBox) {
            VBox vbox = (VBox) materialesTable.getParent();
            for (Node n : vbox.getChildren()) {
                if (n instanceof HBox && n.getStyleClass().contains("filter-bar")) {
                    HBox bar = (HBox) n;
                    for (Node child : bar.getChildren()) {
                        if (child instanceof HBox && ((HBox) child).getStyleClass().contains("search-box")) {
                            HBox sb = (HBox) child;
                            for (Node inner : sb.getChildren()) {
                                if (inner instanceof TextField) return Optional.of((TextField) inner);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void loadMateriales() { loadMateriales(null); }

    private void loadMateriales(String filter) {
        try {
            List<Materiales> all = new MaterialRepository().findAll();
            List<Materiales> filtered;
            if (filter != null && !filter.isEmpty()) {
                filtered = new MaterialRepository().buscarPorTexto(filter);
            } else {
                filtered = new ArrayList<>(all);
            }
            materialesTable.setItems(FXCollections.observableArrayList(filtered));
            materialesCountLabel.setText("Mostrando " + filtered.size() + " de " + all.size());
        } catch (Exception e) {
            materialesTable.setItems(FXCollections.observableArrayList());
            materialesCountLabel.setText("Mostrando 0 de 0");
        }
    }

    // ======================== DOCENTES TAB ========================

    private Tab buildDocentesTab() {
        Tab tab = new Tab("Docentes");
        tab.getStyleClass().add("tab");

        docentesTable = new TableView<>();
        docentesTable.getStyleClass().add("table-view");

        TableColumn<Docente, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getDocenteID())));
        colId.setPrefWidth(60);
        colId.getStyleClass().add("num");
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setStyle("-fx-font-family: 'IBM Plex Mono';"); }
            }
        });

        TableColumn<Docente, String> colNombres = new TableColumn<>("Nombre");
        colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
        colNombres.setPrefWidth(190);

        TableColumn<Docente, String> colApellidos = new TableColumn<>("Apellido");
        colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        colApellidos.setPrefWidth(190);

        TableColumn<Docente, Void> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Docente d = getTableView().getItems().get(getIndex());
                    HBox badge = new HBox(6);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    Region led = new Region();
                    led.getStyleClass().add("led");
                    Label lbl = new Label("Activo");
                    if (d.getEstado() == 1) {
                        led.getStyleClass().add("led-ok");
                        badge.getStyleClass().add("badge-success");
                    } else {
                        led.getStyleClass().add("led-warn");
                        badge.getStyleClass().add("badge-neutral");
                        lbl.setText("Inactivo");
                    }
                    badge.getStyleClass().add("badge");
                    badge.getChildren().addAll(led, lbl);
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Docente, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("\u270E");
            private final Button delBtn = new Button("\u2716");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
                editBtn.setTooltip(new Tooltip("Editar"));
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                delBtn.setTooltip(new Tooltip("Eliminar"));
                box.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    Docente d = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showDocenteDialog(d));
                    delBtn.setOnAction(e -> {
                        if (ConfirmDialog.confirmDelete(d.getNombres() + " " + d.getApellidos())) {
                            try {
                                docenteService.eliminar(d.getDocenteID());
                                loadDocentes(getSearchText(docentesTable));
                            } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        docentesTable.getColumns().addAll(colId, colNombres, colApellidos, colEstado, colAcciones);

        HBox filterBar = new HBox(12);
        filterBar.getStyleClass().add("filter-bar");

        HBox searchBox = new HBox(9);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Group searchIcon = SvgIcons.search(16);
        searchIcon.getStyleClass().add("svg-icon-group");
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar docentes...");
        searchField.setPrefWidth(240);
        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLabel = new Label("Mostrando 0 de 0");
        countLabel.getStyleClass().addAll("text-muted", "text-sm");

        filterBar.getChildren().addAll(searchBox, spacer, countLabel);

        searchField.textProperty().addListener((obs, o, n) -> {
            loadDocentes(n);
            try {
                List<Docente> all = new DocenteRepository().findAll();
                List<Docente> items = docentesTable.getItems();
                countLabel.setText("Mostrando " + items.size() + " de " + all.size());
            } catch (Exception ex) { /* ignore */ }
        });

        VBox content = new VBox(12, filterBar, docentesTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(docentesTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) loadDocentes(null); });
        return tab;
    }

    private String getSearchText(TableView<?> table) {
        if (table != null && table.getParent() instanceof VBox) {
            VBox vbox = (VBox) table.getParent();
            for (Node n : vbox.getChildren()) {
                if (n instanceof HBox && n.getStyleClass().contains("filter-bar")) {
                    for (Node child : ((HBox) n).getChildren()) {
                        if (child instanceof HBox && ((HBox) child).getStyleClass().contains("search-box")) {
                            for (Node inner : ((HBox) child).getChildren()) {
                                if (inner instanceof TextField) {
                                    String t = ((TextField) inner).getText();
                                    return t != null && !t.isEmpty() ? t : null;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void showDocenteDialog(Docente existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo docente" : "Editar docente");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("card");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));

        Label nomLabel = new Label("Nombres");
        nomLabel.getStyleClass().add("form-label");
        TextField nombresField = new TextField();
        nombresField.setPromptText("Nombres del docente");
        nombresField.setPrefWidth(320);

        Label apeLabel = new Label("Apellidos");
        apeLabel.getStyleClass().add("form-label");
        TextField apellidosField = new TextField();
        apellidosField.setPromptText("Apellidos del docente");
        apellidosField.setPrefWidth(320);

        if (existing != null) {
            nombresField.setText(existing.getNombres());
            apellidosField.setText(existing.getApellidos());
        }

        grid.add(nomLabel, 0, 0);
        grid.add(nombresField, 1, 0);
        grid.add(apeLabel, 0, 1);
        grid.add(apellidosField, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(80);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String n = nombresField.getText().trim();
                String a = apellidosField.getText().trim();
                if (n.isEmpty() || a.isEmpty()) return null;
                try {
                    if (existing == null) {
                        docenteService.crear(n, a);
                    } else {
                        existing.setNombres(n);
                        existing.setApellidos(a);
                        docenteService.actualizar(existing);
                    }
                    loadDocentes(getSearchText(docentesTable));
                } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void loadDocentes() { loadDocentes(null); }

    private void loadDocentes(String filter) {
        try {
            List<Docente> all = new DocenteRepository().findAll();
            List<Docente> filtered;
            if (filter != null && !filter.isEmpty()) {
                filtered = new DocenteRepository().buscarPorTexto(filter);
            } else {
                filtered = new ArrayList<>(all);
            }
            docentesTable.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            docentesTable.setItems(FXCollections.observableArrayList());
        }
    }

    // ======================== ESPECIALISTAS TAB ========================

    private Tab buildEspecialistasTab() {
        Tab tab = new Tab("Especialistas");
        tab.getStyleClass().add("tab");

        operadoresTable = new TableView<>();
        operadoresTable.getStyleClass().add("table-view");

        TableColumn<Operador, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOperadorID())));
        colId.setPrefWidth(60);
        colId.getStyleClass().add("num");
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setStyle("-fx-font-family: 'IBM Plex Mono';"); }
            }
        });

        TableColumn<Operador, String> colNombres = new TableColumn<>("Nombre");
        colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
        colNombres.setPrefWidth(150);

        TableColumn<Operador, String> colApellidos = new TableColumn<>("Apellido");
        colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        colApellidos.setPrefWidth(150);

        TableColumn<Operador, Void> colGrado = new TableColumn<>("Grado");
        colGrado.setPrefWidth(80);
        colGrado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Operador o = getTableView().getItems().get(getIndex());
                    HBox badge = new HBox();
                    badge.getStyleClass().addAll("badge", "badge-info");
                    Label lbl = new Label(o.getGrado());
                    badge.getChildren().add(lbl);
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Operador, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTipo.setPrefWidth(60);

        TableColumn<Operador, String> colPeriodo = new TableColumn<>("Periodo");
        colPeriodo.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPeriodo())));
        colPeriodo.setPrefWidth(70);

        TableColumn<Operador, Void> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Operador o = getTableView().getItems().get(getIndex());
                    HBox badge = new HBox(6);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    Region led = new Region();
                    led.getStyleClass().add("led");
                    Label lbl = new Label("Activo");
                    if (o.getEstado() == 1) {
                        led.getStyleClass().add("led-ok");
                        badge.getStyleClass().add("badge-success");
                    } else {
                        led.getStyleClass().add("led-warn");
                        badge.getStyleClass().add("badge-neutral");
                        lbl.setText("Inactivo");
                    }
                    badge.getStyleClass().add("badge");
                    badge.getChildren().addAll(led, lbl);
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Operador, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("\u270E");
            private final Button delBtn = new Button("\u2716");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
                editBtn.setTooltip(new Tooltip("Editar"));
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                delBtn.setTooltip(new Tooltip("Eliminar"));
                box.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    Operador o = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showOperadorDialog(o));
                    delBtn.setOnAction(e -> {
                        if (ConfirmDialog.confirmDelete(o.getNombres() + " " + o.getApellidos())) {
                            try {
                                operadorService.eliminar(o.getOperadorID());
                                loadOperadores(getOperadorSearchText(), getOperadorPeriodoFilter(), getOperadorEstadoFilter());
                            } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        operadoresTable.getColumns().addAll(colId, colNombres, colApellidos, colGrado, colTipo, colPeriodo, colEstado, colAcciones);

        HBox filterBar = new HBox(12);
        filterBar.getStyleClass().add("filter-bar");

        HBox searchBox = new HBox(9);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Group searchIcon = SvgIcons.search(16);
        searchIcon.getStyleClass().add("svg-icon-group");
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar especialistas...");
        searchField.setPrefWidth(200);
        searchBox.getChildren().addAll(searchIcon, searchField);

        Label anioLabel = new Label("A\u00F1o");
        anioLabel.getStyleClass().addAll("text-muted", "text-sm");
        ComboBox<String> periodoCombo = new ComboBox<>();
        periodoCombo.setPromptText("Periodo");
        periodoCombo.setPrefWidth(100);

        Label estadoFilterLabel = new Label("Estado");
        estadoFilterLabel.getStyleClass().addAll("text-muted", "text-sm");
        ComboBox<String> estadoCombo = new ComboBox<>(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        estadoCombo.setValue("Todos");
        estadoCombo.setPrefWidth(110);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        operadoresCountLabel = new Label("Mostrando 0 de 0");
        operadoresCountLabel.getStyleClass().addAll("text-muted", "text-sm");

        filterBar.getChildren().addAll(searchBox, anioLabel, periodoCombo, estadoFilterLabel, estadoCombo, spacer, operadoresCountLabel);

        Runnable filterOp = () -> loadOperadores(
            searchField.getText().isEmpty() ? null : searchField.getText(),
            periodoCombo.getValue(),
            estadoCombo.getValue());

        searchField.textProperty().addListener((obs, o, n) -> filterOp.run());
        periodoCombo.valueProperty().addListener((obs, o, n) -> filterOp.run());
        estadoCombo.valueProperty().addListener((obs, o, n) -> filterOp.run());

        VBox content = new VBox(12, filterBar, operadoresTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(operadoresTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                try {
                    List<Operador> all = new OperadorRepository().findAll();
                    Set<String> periodos = all.stream()
                        .map(op -> String.valueOf(op.getPeriodo()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                    periodoCombo.setItems(FXCollections.observableArrayList(periodos));
                } catch (Exception ex) { /* ignore */ }
                loadOperadores(null, null, "Todos");
            }
        });
        return tab;
    }

    private String getOperadorSearchText() {
        if (operadoresTable != null && operadoresTable.getParent() instanceof VBox) {
            VBox vbox = (VBox) operadoresTable.getParent();
            for (Node n : vbox.getChildren()) {
                if (n instanceof HBox && n.getStyleClass().contains("filter-bar")) {
                    for (Node child : ((HBox) n).getChildren()) {
                        if (child instanceof HBox && ((HBox) child).getStyleClass().contains("search-box")) {
                            for (Node inner : ((HBox) child).getChildren()) {
                                if (inner instanceof TextField) {
                                    String t = ((TextField) inner).getText();
                                    return t != null && !t.isEmpty() ? t : null;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getOperadorPeriodoFilter() {
        if (operadoresTable != null && operadoresTable.getParent() instanceof VBox) {
            VBox vbox = (VBox) operadoresTable.getParent();
            for (Node n : vbox.getChildren()) {
                if (n instanceof HBox && n.getStyleClass().contains("filter-bar")) {
                    for (Node child : ((HBox) n).getChildren()) {
                        if (child instanceof ComboBox && ((ComboBox<?>) child).getValue() != null) {
                            return ((ComboBox<?>) child).getValue().toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getOperadorEstadoFilter() {
        if (operadoresTable != null && operadoresTable.getParent() instanceof VBox) {
            VBox vbox = (VBox) operadoresTable.getParent();
            boolean foundPeriodo = false;
            for (Node n : vbox.getChildren()) {
                if (n instanceof HBox && n.getStyleClass().contains("filter-bar")) {
                    for (Node child : ((HBox) n).getChildren()) {
                        if (child instanceof ComboBox && ((ComboBox<?>) child).getValue() != null) {
                            if (!foundPeriodo) { foundPeriodo = true; }
                            else { return ((ComboBox<?>) child).getValue().toString(); }
                        }
                    }
                }
            }
        }
        return "Todos";
    }

    private void showOperadorDialog(Operador existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo especialista" : "Editar especialista");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("card");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));

        TextField nombresField = new TextField();
        nombresField.setPromptText("Nombres");
        nombresField.setPrefWidth(280);

        TextField apellidosField = new TextField();
        apellidosField.setPromptText("Apellidos");
        apellidosField.setPrefWidth(280);

        ComboBox<String> gradoCombo = new ComboBox<>(FXCollections.observableArrayList("PRE", "POS"));
        gradoCombo.setPromptText("Grado");
        gradoCombo.setPrefWidth(130);

        ComboBox<String> tipoCombo = new ComboBox<>();
        tipoCombo.setPromptText("Tipo");
        tipoCombo.setPrefWidth(130);

        gradoCombo.valueProperty().addListener((obs, o, n) -> {
            tipoCombo.getItems().clear();
            if ("PRE".equals(n)) {
                tipoCombo.getItems().addAll("4", "5", "6");
            } else if ("POS".equals(n)) {
                tipoCombo.getItems().addAll("R1", "R2", "R3");
            }
        });

        TextField periodoField = new TextField();
        periodoField.setPromptText("Periodo (a\u00F1o)");
        periodoField.setPrefWidth(130);

        if (existing != null) {
            nombresField.setText(existing.getNombres());
            apellidosField.setText(existing.getApellidos());
            gradoCombo.setValue(existing.getGrado());
            tipoCombo.setValue(existing.getTipo());
            periodoField.setText(String.valueOf(existing.getPeriodo()));
        }

        Label nomLabel = new Label("Nombres");
        nomLabel.getStyleClass().add("form-label");
        Label apeLabel = new Label("Apellidos");
        apeLabel.getStyleClass().add("form-label");
        Label gradoLabel = new Label("Grado");
        gradoLabel.getStyleClass().add("form-label");
        Label tipoLabel = new Label("Tipo");
        tipoLabel.getStyleClass().add("form-label");
        Label periodoLabel = new Label("Periodo");
        periodoLabel.getStyleClass().add("form-label");

        grid.add(nomLabel, 0, 0);
        grid.add(nombresField, 1, 0);
        grid.add(apeLabel, 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(gradoLabel, 0, 2);
        grid.add(gradoCombo, 1, 2);
        grid.add(tipoLabel, 0, 3);
        grid.add(tipoCombo, 1, 3);
        grid.add(periodoLabel, 0, 4);
        grid.add(periodoField, 1, 4);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(80);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String n = nombresField.getText().trim();
                String a = apellidosField.getText().trim();
                String grado = gradoCombo.getValue();
                String tipo = tipoCombo.getValue();
                String periodoStr = periodoField.getText().trim();
                if (n.isEmpty() || a.isEmpty() || grado == null || tipo == null || periodoStr.isEmpty()) return null;
                try {
                    int periodo = Integer.parseInt(periodoStr);
                    if (existing == null) {
                        operadorService.crear(n, a, grado, tipo, periodo);
                    } else {
                        existing.setNombres(n);
                        existing.setApellidos(a);
                        existing.setGrado(grado);
                        existing.setTipo(tipo);
                        existing.setPeriodo(periodo);
                        operadorService.actualizar(existing);
                    }
                    loadOperadores(getOperadorSearchText(), getOperadorPeriodoFilter(), getOperadorEstadoFilter());
                } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void loadOperadores() { loadOperadores(null, null, "Todos"); }

    private void loadOperadores(String search, String periodoStr, String estadoStr) {
        try {
            List<Operador> all = new OperadorRepository().findAll();
            List<Operador> filtered = new ArrayList<>(all);

            if (search != null && !search.isEmpty()) {
                filtered = new OperadorRepository().buscarPorTexto(search);
            }

            if (periodoStr != null && !periodoStr.isEmpty()) {
                filtered = filtered.stream()
                    .filter(o -> String.valueOf(o.getPeriodo()).equals(periodoStr))
                    .collect(Collectors.toList());
            }

            if (estadoStr != null && !estadoStr.equals("Todos")) {
                int estadoVal = estadoStr.equals("Activo") ? 1 : 0;
                filtered = filtered.stream()
                    .filter(o -> o.getEstado() == estadoVal)
                    .collect(Collectors.toList());
            }

            operadoresTable.setItems(FXCollections.observableArrayList(filtered));
            operadoresCountLabel.setText("Mostrando " + filtered.size() + " de " + all.size());
        } catch (Exception e) {
            operadoresTable.setItems(FXCollections.observableArrayList());
            operadoresCountLabel.setText("Mostrando 0 de 0");
        }
    }

    // ======================== TRATAMIENTOS PREDEFINIDOS TAB ========================

    private Tab buildTratamientosPredTab() {
        Tab tab = new Tab("Tratamientos Predefinidos");
        tab.getStyleClass().add("tab");

        tratPredTable = new TableView<>();
        tratPredTable.getStyleClass().add("table-view");
        tratPredTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TratamientoPredefinido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (expandedTratPred.contains(item.getTratPredID())) {
                    setStyle("-fx-background-color: #f0f8f7;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<TratamientoPredefinido, Void> colChevron = new TableColumn<>("");
        colChevron.setPrefWidth(36);
        colChevron.setMinWidth(36);
        colChevron.setMaxWidth(36);
        colChevron.setCellFactory(param -> new TableCell<>() {
            private final Button chevBtn = new Button();
            {
                chevBtn.getStyleClass().add("chev-btn");
                Group chevronIcon = SvgIcons.chevronDown(14);
                chevronIcon.getStyleClass().add("svg-icon-group");
                chevBtn.setGraphic(chevronIcon);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(chevBtn);
                    chevBtn.setOnAction(e -> {
                        TratamientoPredefinido tp = getTableView().getItems().get(getIndex());
                        toggleTratPredExpanded(tp);
                    });
                }
            }
        });

        TableColumn<TratamientoPredefinido, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTratPredID())));
        colId.setPrefWidth(60);
        colId.getStyleClass().add("num");
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setStyle("-fx-font-family: 'IBM Plex Mono';"); }
            }
        });

        TableColumn<TratamientoPredefinido, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreTratamiento()));
        colNombre.setPrefWidth(260);

        TableColumn<TratamientoPredefinido, String> colMonto = new TableColumn<>("Costo Sugerido");
        colMonto.setCellValueFactory(d -> {
            Double m = d.getValue().getMontoSugerido();
            return new SimpleStringProperty(m != null ? "S/ " + String.format("%.2f", m) : "-");
        });
        colMonto.setPrefWidth(130);

        TableColumn<TratamientoPredefinido, String> colMateriales = new TableColumn<>("Materiales");
        colMateriales.setCellValueFactory(d -> {
            try {
                List<TratamientoPredefinidoMaterial> mats = tratPredService.materiales(d.getValue().getTratPredID());
                return new SimpleStringProperty(String.valueOf(mats.size()));
            } catch (Exception e) {
                return new SimpleStringProperty("0");
            }
        });
        colMateriales.setPrefWidth(90);
        colMateriales.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item + " mat."); }
            }
        });

        TableColumn<TratamientoPredefinido, Void> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox badge = new HBox(6);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    Region led = new Region();
                    led.getStyleClass().addAll("led", "led-ok");
                    badge.getStyleClass().addAll("badge", "badge-success");
                    badge.getChildren().addAll(led, new Label("Activo"));
                    setGraphic(badge);
                }
            }
        });

        TableColumn<TratamientoPredefinido, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(130);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("\u270E");
            private final Button matBtn = new Button("\u2699");
            private final Button delBtn = new Button("\u2716");
            private final HBox box = new HBox(4, editBtn, matBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
                editBtn.setTooltip(new Tooltip("Editar"));
                matBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
                matBtn.setTooltip(new Tooltip("Materiales"));
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                delBtn.setTooltip(new Tooltip("Eliminar"));
                box.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    TratamientoPredefinido t = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showTratPredDialog(t));
                    matBtn.setOnAction(e -> showTratPredMaterialesDialog(t));
                    delBtn.setOnAction(e -> {
                        if (ConfirmDialog.confirmDelete(t.getNombreTratamiento())) {
                            try {
                                tratPredService.eliminar(t.getTratPredID());
                                loadTratamientos(getTratPredSearchText());
                            } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        tratPredTable.getColumns().addAll(colChevron, colId, colNombre, colMonto, colMateriales, colEstado, colAcciones);

        HBox filterBar = new HBox(12);
        filterBar.getStyleClass().add("filter-bar");

        HBox searchBox = new HBox(9);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Group searchIcon = SvgIcons.search(16);
        searchIcon.getStyleClass().add("svg-icon-group");
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar tratamientos...");
        searchField.setPrefWidth(240);
        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLabel = new Label("Mostrando 0 de 0");
        countLabel.getStyleClass().addAll("text-muted", "text-sm");

        filterBar.getChildren().addAll(searchBox, spacer, countLabel);

        searchField.textProperty().addListener((obs, o, n) -> {
            loadTratamientos(n);
            try {
                List<TratamientoPredefinido> all = new TratamientoPredefinidoRepository().findAll();
                countLabel.setText("Mostrando " + tratPredTable.getItems().size() + " de " + all.size());
            } catch (Exception ex) { /* ignore */ }
        });

        tratPredDetailPane = new VBox();
        tratPredDetailPane.getStyleClass().add("card");
        tratPredDetailPane.setPadding(new Insets(12, 16, 12, 16));
        tratPredDetailPane.setSpacing(6);
        tratPredDetailPane.setVisible(false);
        tratPredDetailPane.setManaged(false);

        VBox content = new VBox(12, filterBar, tratPredTable, tratPredDetailPane);
        content.setPadding(new Insets(16));
        VBox.setVgrow(tratPredTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) loadTratamientos(null); });
        return tab;
    }

    private void toggleTratPredExpanded(TratamientoPredefinido tp) {
        int id = tp.getTratPredID();
        if (expandedTratPred.contains(id)) {
            expandedTratPred.remove(id);
            tratPredDetailPane.setVisible(false);
            tratPredDetailPane.setManaged(false);
        } else {
            expandedTratPred.clear();
            expandedTratPred.add(id);
            try {
                List<TratamientoPredefinidoMaterial> mats = tratPredService.materiales(id);
                List<Materiales> todos = new MaterialRepository().findAll();
                Map<Integer, String> nombreMap = todos.stream()
                    .collect(Collectors.toMap(Materiales::getMaterialID, Materiales::getNombre));
                FlowPane chips = new FlowPane();
                chips.setHgap(8);
                chips.setVgap(6);
                for (TratamientoPredefinidoMaterial m : mats) {
                    String nombre = nombreMap.getOrDefault(m.getMaterialID(), "Material #" + m.getMaterialID());
                    HBox chip = new HBox(4);
                    chip.getStyleClass().add("mat-chip");
                    chip.setAlignment(Pos.CENTER_LEFT);
                    Label nameLbl = new Label(nombre);
                    Label cantLbl = new Label("x" + formatCant(m.getCantidad()));
                    cantLbl.getStyleClass().add("chip-cant");
                    chip.getChildren().addAll(nameLbl, cantLbl);
                    chips.getChildren().add(chip);
                }
                tratPredDetailPane.getChildren().setAll(
                    new Label("Materiales del tratamiento:"),
                    chips
                );
                tratPredDetailPane.setVisible(true);
                tratPredDetailPane.setManaged(true);
            } catch (Exception ex) {
                tratPredDetailPane.setVisible(false);
                tratPredDetailPane.setManaged(false);
            }
        }
        tratPredTable.refresh();
    }

    private String formatCant(double c) {
        if (c == Math.floor(c)) return String.valueOf((int) c);
        return String.format("%.2f", c);
    }

    private String getTratPredSearchText() {
        if (tratPredTable != null && tratPredTable.getParent() instanceof VBox) {
            VBox vbox = (VBox) tratPredTable.getParent();
            for (Node n : vbox.getChildren()) {
                if (n instanceof HBox && n.getStyleClass().contains("filter-bar")) {
                    for (Node child : ((HBox) n).getChildren()) {
                        if (child instanceof HBox && ((HBox) child).getStyleClass().contains("search-box")) {
                            for (Node inner : ((HBox) child).getChildren()) {
                                if (inner instanceof TextField) {
                                    String t = ((TextField) inner).getText();
                                    return t != null && !t.isEmpty() ? t : null;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void showTratPredDialog(TratamientoPredefinido existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo tratamiento predefinido" : "Editar tratamiento");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("card");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));

        Label nomLabel = new Label("Nombre");
        nomLabel.getStyleClass().add("form-label");
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del tratamiento");
        nombreField.setPrefWidth(320);

        Label montoLabel = new Label("Costo sugerido");
        montoLabel.getStyleClass().add("form-label");
        TextField montoField = new TextField();
        montoField.setPromptText("0.00");
        montoField.setPrefWidth(160);

        if (existing != null) {
            nombreField.setText(existing.getNombreTratamiento());
            if (existing.getMontoSugerido() != null) {
                montoField.setText(String.valueOf(existing.getMontoSugerido()));
            }
        }

        grid.add(nomLabel, 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(montoLabel, 0, 1);
        grid.add(montoField, 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(100);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String n = nombreField.getText().trim();
                if (n.isEmpty()) return null;
                Double monto = null;
                try { if (!montoField.getText().trim().isEmpty()) monto = Double.parseDouble(montoField.getText().trim()); }
                catch (NumberFormatException ignored) {}
                try {
                    if (existing == null) {
                        tratPredService.crear(n, monto);
                    } else {
                        existing.setNombreTratamiento(n);
                        existing.setMontoSugerido(monto);
                        tratPredService.actualizar(existing);
                    }
                    loadTratamientos(getTratPredSearchText());
                } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showTratPredMaterialesDialog(TratamientoPredefinido t) {
        try {
            List<TratamientoPredefinidoMaterial> materiales = tratPredService.materiales(t.getTratPredID());
            List<Materiales> todos = new MaterialRepository().findAll();

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Materiales de: " + t.getNombreTratamiento());
            dialog.setHeaderText(null);
            dialog.setResizable(true);
            dialog.getDialogPane().getStyleClass().add("card");

            MaterialTable materialTable = new MaterialTable();
            materialTable.setMaterialOptions(todos.stream()
                .map(m -> new MaterialTable.MaterialOption(m.getMaterialID(), m.getNombre()))
                .toList());

            for (TratamientoPredefinidoMaterial m : materiales) {
                Materiales mat = todos.stream().filter(x -> x.getMaterialID() == m.getMaterialID()).findFirst().orElse(null);
                if (mat != null) {
                    materialTable.addRow(m.getMaterialID(), mat.getNombre(), m.getCantidad());
                }
            }

            VBox content = new VBox(12, materialTable, materialTable.createAddButton("+ Agregar material"));
            content.setPadding(new Insets(20));
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setPrefWidth(550);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        List<TratamientoPredefinidoMaterial> nuevos = materialTable.getEntries().stream()
                            .map(e -> {
                                TratamientoPredefinidoMaterial pm = new TratamientoPredefinidoMaterial();
                                pm.setTratPredID(t.getTratPredID());
                                pm.setMaterialID(e.materialId);
                                pm.setCantidad(e.quantity);
                                return pm;
                            }).toList();
                        tratPredService.guardarMateriales(t.getTratPredID(), nuevos);
                        loadTratamientos(getTratPredSearchText());
                        expandedTratPred.remove(t.getTratPredID());
                        if (tratPredDetailPane != null) {
                            tratPredDetailPane.setVisible(false);
                            tratPredDetailPane.setManaged(false);
                        }
                    } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
    }

    private void loadTratamientos() { loadTratamientos(null); }

    private void loadTratamientos(String filter) {
        try {
            List<TratamientoPredefinido> all = new TratamientoPredefinidoRepository().findAll();
            List<TratamientoPredefinido> filtered;
            if (filter != null && !filter.isEmpty()) {
                filtered = new TratamientoPredefinidoRepository().buscarPorTexto(filter);
            } else {
                filtered = new ArrayList<>(all);
            }
            tratPredTable.setItems(FXCollections.observableArrayList(filtered));
            expandedTratPred.clear();
            if (tratPredDetailPane != null) {
                tratPredDetailPane.setVisible(false);
                tratPredDetailPane.setManaged(false);
            }
        } catch (Exception e) {
            tratPredTable.setItems(FXCollections.observableArrayList());
        }
    }

    // ======================== TRATAMIENTOS REALIZADOS TAB ========================

    private Tab buildTratamientosRealizadosTab() {
        Tab tab = new Tab("Tratamientos Realizados");
        tab.getStyleClass().add("tab");

        tratRealizadosTable = new TableView<>();
        tratRealizadosTable.getStyleClass().add("table-view");
        tratRealizadosTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Tratamiento item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (expandedTratRealizados.contains(item.getTratamientoID())) {
                    setStyle("-fx-background-color: #f0f8f7;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<Tratamiento, Void> colChevron = new TableColumn<>("");
        colChevron.setPrefWidth(36);
        colChevron.setMinWidth(36);
        colChevron.setMaxWidth(36);
        colChevron.setCellFactory(param -> new TableCell<>() {
            private final Button chevBtn = new Button();
            {
                chevBtn.getStyleClass().add("chev-btn");
                Group chevronIcon = SvgIcons.chevronDown(14);
                chevronIcon.getStyleClass().add("svg-icon-group");
                chevBtn.setGraphic(chevronIcon);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(chevBtn);
                    chevBtn.setOnAction(e -> {
                        Tratamiento t = getTableView().getItems().get(getIndex());
                        toggleTratRealizadoExpanded(t);
                    });
                }
            }
        });

        TableColumn<Tratamiento, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTratamientoID())));
        colId.setPrefWidth(55);
        colId.getStyleClass().add("num");
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setStyle("-fx-font-family: 'IBM Plex Mono';"); }
            }
        });

        TableColumn<Tratamiento, String> colPaciente = new TableColumn<>("Paciente");
        colPaciente.setCellValueFactory(d -> {
            String nombre = pacienteCache.get(d.getValue().getPacienteID());
            return new SimpleStringProperty(nombre != null ? nombre : "Paciente #" + d.getValue().getPacienteID());
        });
        colPaciente.setPrefWidth(160);

        TableColumn<Tratamiento, Void> colEspecialista = new TableColumn<>("Especialista");
        colEspecialista.setPrefWidth(190);
        colEspecialista.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    Tratamiento t = getTableView().getItems().get(getIndex());
                    Operador op = operadorCache.get(t.getOperadorID());
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER_LEFT);
                    String nombreOp = op != null ? op.getNombres() + " " + op.getApellidos() : "Op #" + t.getOperadorID();
                    Label nameLbl = new Label(nombreOp);
                    if (op != null && op.getGrado() != null) {
                        HBox gradoBadge = new HBox();
                        gradoBadge.getStyleClass().addAll("badge", "badge-info");
                        gradoBadge.getChildren().add(new Label(op.getGrado()));
                        box.getChildren().addAll(nameLbl, gradoBadge);
                    } else {
                        box.getChildren().add(nameLbl);
                    }
                    setGraphic(box);
                }
            }
        });

        TableColumn<Tratamiento, String> colTratamiento = new TableColumn<>("Tratamiento");
        colTratamiento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreTratamiento()));
        colTratamiento.setPrefWidth(180);

        TableColumn<Tratamiento, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> {
            String un = unidadCache.get(d.getValue().getUnidadID());
            return new SimpleStringProperty(un != null ? un : (d.getValue().getUnidadID() != null ? "U" + d.getValue().getUnidadID() : "-"));
        });
        colUnidad.setPrefWidth(80);

        TableColumn<Tratamiento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha()));
        colFecha.setPrefWidth(100);

        TableColumn<Tratamiento, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(d -> new SimpleStringProperty("S/ " + String.format("%.2f", d.getValue().getMonto())));
        colMonto.setPrefWidth(100);

        TableColumn<Tratamiento, Void> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(110);
        colEstado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Tratamiento t = getTableView().getItems().get(getIndex());
                    HBox badge = new HBox();
                    badge.getStyleClass().add("badge");
                    String estado = t.getEstado();
                    Label lbl = new Label(estado);
                    if ("CERRADO".equals(estado)) {
                        badge.getStyleClass().add("badge-success");
                    } else if ("ANULADO".equals(estado)) {
                        badge.getStyleClass().add("badge-danger");
                    } else if ("PENDIENTE".equals(estado)) {
                        badge.getStyleClass().add("badge-warning");
                    } else {
                        badge.getStyleClass().add("badge-neutral");
                    }
                    badge.getChildren().add(lbl);
                    setGraphic(badge);
                }
            }
        });

        tratRealizadosTable.getColumns().addAll(colChevron, colId, colPaciente, colEspecialista, colTratamiento, colUnidad, colFecha, colMonto, colEstado);

        HBox filterBar = new HBox(12);
        filterBar.getStyleClass().add("filter-bar");

        HBox searchBox = new HBox(9);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Group searchIcon = SvgIcons.search(16);
        searchIcon.getStyleClass().add("svg-icon-group");
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar tratamientos realizados...");
        searchField.setPrefWidth(280);
        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLabel = new Label("Mostrando 0 de 0");
        countLabel.getStyleClass().addAll("text-muted", "text-sm");

        filterBar.getChildren().addAll(searchBox, spacer, countLabel);

        searchField.textProperty().addListener((obs, o, n) -> {
            loadTratamientosRealizados(n);
            countLabel.setText("Mostrando " + tratRealizadosTable.getItems().size());
        });

        tratRealizadosDetailPane = new VBox();
        tratRealizadosDetailPane.getStyleClass().add("card");
        tratRealizadosDetailPane.setPadding(new Insets(12, 16, 12, 16));
        tratRealizadosDetailPane.setSpacing(6);
        tratRealizadosDetailPane.setVisible(false);
        tratRealizadosDetailPane.setManaged(false);

        VBox content = new VBox(12, filterBar, tratRealizadosTable, tratRealizadosDetailPane);
        content.setPadding(new Insets(16));
        VBox.setVgrow(tratRealizadosTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                loadCaches();
                loadTratamientosRealizados(null);
            }
        });
        return tab;
    }

    private void loadCaches() {
        try {
            List<Paciente> pacientes = new PacienteRepository().findAll();
            pacienteCache.clear();
            for (Paciente p : pacientes) {
                pacienteCache.put(p.getPacienteID(), p.getNombres() + " " + p.getApellidos());
            }
        } catch (Exception e) { pacienteCache.clear(); }

        try {
            List<Operador> opes = new OperadorRepository().findAll();
            operadorCache.clear();
            for (Operador o : opes) {
                operadorCache.put(o.getOperadorID(), o);
            }
        } catch (Exception e) { operadorCache.clear(); }

        try {
            List<Unidad> unidades = new UnidadRepository().findAll();
            unidadCache.clear();
            for (Unidad u : unidades) {
                unidadCache.put(u.getUnidadID(), "Unidad " + u.getUnidadNro());
            }
        } catch (Exception e) { unidadCache.clear(); }
    }

    private void toggleTratRealizadoExpanded(Tratamiento t) {
        int id = t.getTratamientoID();
        if (expandedTratRealizados.contains(id)) {
            expandedTratRealizados.remove(id);
            tratRealizadosDetailPane.setVisible(false);
            tratRealizadosDetailPane.setManaged(false);
        } else {
            expandedTratRealizados.clear();
            expandedTratRealizados.add(id);
            try {
                List<TratamientoMaterialRepository.MaterialConCantidad> mats = matRealizadosCache.get(id);
                if (mats == null) {
                    mats = new TratamientoMaterialRepository().findMaterialesConNombre(id);
                    matRealizadosCache.put(id, mats);
                }
                FlowPane chips = new FlowPane();
                chips.setHgap(8);
                chips.setVgap(6);
                for (TratamientoMaterialRepository.MaterialConCantidad m : mats) {
                    HBox chip = new HBox(4);
                    chip.getStyleClass().add("mat-chip");
                    chip.setAlignment(Pos.CENTER_LEFT);
                    Label nameLbl = new Label(m.getNombre());
                    Label cantLbl = new Label("x" + formatCant(m.getCantidad()) + " " + m.getUnidad());
                    cantLbl.getStyleClass().add("chip-cant");
                    chip.getChildren().addAll(nameLbl, cantLbl);
                    chips.getChildren().add(chip);
                }
                tratRealizadosDetailPane.getChildren().setAll(
                    new Label("Materiales utilizados en el tratamiento:"),
                    chips
                );
                tratRealizadosDetailPane.setVisible(true);
                tratRealizadosDetailPane.setManaged(true);
            } catch (Exception ex) {
                tratRealizadosDetailPane.setVisible(false);
                tratRealizadosDetailPane.setManaged(false);
            }
        }
        tratRealizadosTable.refresh();
    }

    private void loadTratamientosRealizados() { loadTratamientosRealizados(null); }

    private void loadTratamientosRealizados(String filter) {
        try {
            List<Tratamiento> cerrados = new TratamientoRepository().findByEstado("CERRADO");
            List<Tratamiento> anulados = new TratamientoRepository().findByEstado("ANULADO");
            List<Tratamiento> todos = new ArrayList<>();
            todos.addAll(cerrados);
            todos.addAll(anulados);
            todos.sort((a, b) -> {
                int cmp = b.getFecha().compareTo(a.getFecha());
                if (cmp != 0) return cmp;
                return Integer.compare(b.getTratamientoID(), a.getTratamientoID());
            });

            List<Tratamiento> filtered;
            if (filter != null && !filter.isEmpty()) {
                String lower = filter.toLowerCase();
                filtered = todos.stream()
                    .filter(t -> {
                        String pac = pacienteCache.getOrDefault(t.getPacienteID(), "").toLowerCase();
                        Operador op = operadorCache.get(t.getOperadorID());
                        String opName = op != null ? (op.getNombres() + " " + op.getApellidos()).toLowerCase() : "";
                        return pac.contains(lower) || opName.contains(lower)
                            || t.getNombreTratamiento().toLowerCase().contains(lower)
                            || t.getEstado().toLowerCase().contains(lower);
                    })
                    .collect(Collectors.toList());
            } else {
                filtered = new ArrayList<>(todos);
            }

            tratRealizadosTable.setItems(FXCollections.observableArrayList(filtered));
            expandedTratRealizados.clear();
            matRealizadosCache.clear();
            if (tratRealizadosDetailPane != null) {
                tratRealizadosDetailPane.setVisible(false);
                tratRealizadosDetailPane.setManaged(false);
            }
        } catch (Exception e) {
            tratRealizadosTable.setItems(FXCollections.observableArrayList());
        }
    }
}
