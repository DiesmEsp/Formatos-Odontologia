package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.*;
import com.odontologia.formatos.repository.DocenteRepository;
import com.odontologia.formatos.repository.MaterialRepository;
import com.odontologia.formatos.repository.OperadorRepository;
import com.odontologia.formatos.repository.TratamientoPredefinidoRepository;
import com.odontologia.formatos.service.*;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.MaterialTable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class CatalogosView extends VBox {

    private final MaterialService materialService = new MaterialService();
    private final DocenteService docenteService = new DocenteService();
    private final OperadorService operadorService = new OperadorService();
    private final TratamientoPredefinidoService tratPredService = new TratamientoPredefinidoService();

    private TableView<Materiales> materialesTable;
    private TableView<Docente> docentesTable;
    private TableView<Operador> operadoresTable;
    private TableView<TratamientoPredefinido> tratPredTable;

    private Runnable onRefresh;

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
        Label subtitle = new Label("Administrar materiales, docentes, operadores y tratamientos predefinidos");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
            buildMaterialesTab(),
            buildDocentesTab(),
            buildOperadoresTab(),
            buildTratamientosTab()
        );

        getChildren().addAll(header, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }

    private Tab buildMaterialesTab() {
        Tab tab = new Tab("Materiales");

        materialesTable = new TableView<>();
        materialesTable.getStyleClass().add("table-view");

        TableColumn<Materiales, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(300);

        TableColumn<Materiales, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidad()));
        colUnidad.setPrefWidth(150);

        TableColumn<Materiales, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button delBtn = new Button("Eliminar");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Materiales m = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showMaterialDialog(m));
                    delBtn.setOnAction(e -> {
                        if (ConfirmDialog.confirmDelete(m.getNombre())) {
                            try {
                                materialService.eliminar(m.getMaterialID());
                                loadMateriales();
                            } catch (Exception ex) {
                                ConfirmDialog.error("Error", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        materialesTable.getColumns().addAll(colNombre, colUnidad, colAcciones);

        Button addBtn = new Button("+ Nuevo material");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showMaterialDialog(null));

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, o, n) -> loadMateriales(n));

        HBox toolbar = new HBox(12, searchField, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, toolbar, materialesTable);
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

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del material");
        TextField unidadField = new TextField();
        unidadField.setPromptText("Unidad (ej: g, ml, uds)");

        if (existing != null) {
            nombreField.setText(existing.getNombre());
            unidadField.setText(existing.getUnidad());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Unidad:"), 0, 1);
        grid.add(unidadField, 1, 1);

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
                    loadMateriales();
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

    private void loadMateriales() { loadMateriales(null); }

    private void loadMateriales(String filter) {
        try {
            if (filter != null && !filter.isEmpty()) {
                materialesTable.setItems(FXCollections.observableArrayList(new MaterialRepository().buscarPorTexto(filter)));
            } else {
                materialesTable.setItems(FXCollections.observableArrayList(new MaterialRepository().findAll()));
            }
        } catch (Exception e) {
            materialesTable.setItems(FXCollections.observableArrayList());
        }
    }

    private Tab buildDocentesTab() {
        Tab tab = new Tab("Docentes");

        docentesTable = new TableView<>();

        TableColumn<Docente, String> colNombres = new TableColumn<>("Nombres");
        colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
        colNombres.setPrefWidth(200);

        TableColumn<Docente, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        colApellidos.setPrefWidth(200);

        TableColumn<Docente, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button delBtn = new Button("Eliminar");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
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
                                loadDocentes();
                            } catch (Exception ex) {
                                ConfirmDialog.error("Error", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        docentesTable.getColumns().addAll(colNombres, colApellidos, colAcciones);

        Button addBtn = new Button("+ Nuevo docente");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showDocenteDialog(null));

        HBox toolbar = new HBox(12, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, toolbar, docentesTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(docentesTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) loadDocentes(); });
        return tab;
    }

    private void showDocenteDialog(Docente existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo docente" : "Editar docente");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nombresField = new TextField();
        TextField apellidosField = new TextField();
        if (existing != null) {
            nombresField.setText(existing.getNombres());
            apellidosField.setText(existing.getApellidos());
        }

        grid.add(new Label("Nombres:"), 0, 0);
        grid.add(nombresField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);

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
                    loadDocentes();
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void loadDocentes() {
        try {
            docentesTable.setItems(FXCollections.observableArrayList(new DocenteRepository().findAll()));
        } catch (Exception e) {
            docentesTable.setItems(FXCollections.observableArrayList());
        }
    }

    private Tab buildOperadoresTab() {
        Tab tab = new Tab("Operadores");

        operadoresTable = new TableView<>();

        TableColumn<Operador, String> colNombres = new TableColumn<>("Nombres");
        colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
        colNombres.setPrefWidth(150);

        TableColumn<Operador, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        colApellidos.setPrefWidth(150);

        TableColumn<Operador, String> colGrado = new TableColumn<>("Grado");
        colGrado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGrado()));
        colGrado.setPrefWidth(80);

        TableColumn<Operador, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTipo.setPrefWidth(80);

        TableColumn<Operador, String> colPeriodo = new TableColumn<>("Periodo");
        colPeriodo.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPeriodo())));
        colPeriodo.setPrefWidth(80);

        TableColumn<Operador, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button delBtn = new Button("Eliminar");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
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
                                loadOperadores();
                            } catch (Exception ex) {
                                ConfirmDialog.error("Error", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        operadoresTable.getColumns().addAll(colNombres, colApellidos, colGrado, colTipo, colPeriodo, colAcciones);

        Button addBtn = new Button("+ Nuevo operador");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showOperadorDialog(null));

        HBox toolbar = new HBox(12, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, toolbar, operadoresTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(operadoresTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) loadOperadores(); });
        return tab;
    }

    private void showOperadorDialog(Operador existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo operador" : "Editar operador");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nombresField = new TextField();
        TextField apellidosField = new TextField();
        ComboBox<String> gradoCombo = new ComboBox<>(FXCollections.observableArrayList("PRE", "POS"));
        ComboBox<String> tipoCombo = new ComboBox<>();
        TextField periodoField = new TextField();

        gradoCombo.valueProperty().addListener((obs, o, n) -> {
            tipoCombo.getItems().clear();
            if ("PRE".equals(n)) {
                tipoCombo.getItems().addAll("4", "5", "6");
            } else if ("POS".equals(n)) {
                tipoCombo.getItems().addAll("R1", "R2", "R3");
            }
        });

        if (existing != null) {
            nombresField.setText(existing.getNombres());
            apellidosField.setText(existing.getApellidos());
            gradoCombo.setValue(existing.getGrado());
            tipoCombo.setValue(existing.getTipo());
            periodoField.setText(String.valueOf(existing.getPeriodo()));
        }

        grid.add(new Label("Nombres:"), 0, 0);
        grid.add(nombresField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(new Label("Grado:"), 0, 2);
        grid.add(gradoCombo, 1, 2);
        grid.add(new Label("Tipo:"), 0, 3);
        grid.add(tipoCombo, 1, 3);
        grid.add(new Label("Periodo:"), 0, 4);
        grid.add(periodoField, 1, 4);

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
                    loadOperadores();
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void loadOperadores() {
        try {
            operadoresTable.setItems(FXCollections.observableArrayList(new OperadorRepository().findAll()));
        } catch (Exception e) {
            operadoresTable.setItems(FXCollections.observableArrayList());
        }
    }

    private Tab buildTratamientosTab() {
        Tab tab = new Tab("Tratamientos Predefinidos");

        tratPredTable = new TableView<>();

        TableColumn<TratamientoPredefinido, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreTratamiento()));
        colNombre.setPrefWidth(300);

        TableColumn<TratamientoPredefinido, String> colMonto = new TableColumn<>("Monto sugerido");
        colMonto.setCellValueFactory(d -> {
            Double m = d.getValue().getMontoSugerido();
            return new SimpleStringProperty(m != null ? String.format("%.2f", m) : "-");
        });
        colMonto.setPrefWidth(120);

        TableColumn<TratamientoPredefinido, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(120);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button matBtn = new Button("Materiales");
            private final Button delBtn = new Button("Eliminar");
            private final HBox box = new HBox(4, editBtn, matBtn, delBtn);
            {
                editBtn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
                matBtn.getStyleClass().addAll("btn", "btn-sm", "btn-primary");
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
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
                                loadTratamientos();
                            } catch (Exception ex) {
                                ConfirmDialog.error("Error", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(box);
                }
            }
        });

        tratPredTable.getColumns().addAll(colNombre, colMonto, colAcciones);

        Button addBtn = new Button("+ Nuevo tratamiento");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showTratPredDialog(null));

        HBox toolbar = new HBox(12, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, toolbar, tratPredTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(tratPredTable, Priority.ALWAYS);

        tab.setContent(content);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) loadTratamientos(); });
        return tab;
    }

    private void showTratPredDialog(TratamientoPredefinido existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo tratamiento predefinido" : "Editar tratamiento");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nombreField = new TextField();
        TextField montoField = new TextField();

        if (existing != null) {
            nombreField.setText(existing.getNombreTratamiento());
            if (existing.getMontoSugerido() != null) {
                montoField.setText(String.valueOf(existing.getMontoSugerido()));
            }
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Monto sugerido:"), 0, 1);
        grid.add(montoField, 1, 1);

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
                    loadTratamientos();
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
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

            MaterialTable materialTable = new MaterialTable();
            materialTable.setMaterialOptions(todos.stream()
                .map(m -> new MaterialTable.MaterialOption(m.getMaterialID(), m.getNombre()))
                .toList());

            // Pre-load existing materials
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
                        loadTratamientos();
                    } catch (Exception ex) {
                        ConfirmDialog.error("Error", ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception ex) {
            ConfirmDialog.error("Error", ex.getMessage());
        }
    }

    private void loadTratamientos() {
        try {
            tratPredTable.setItems(FXCollections.observableArrayList(new TratamientoPredefinidoRepository().findAll()));
        } catch (Exception e) {
            tratPredTable.setItems(FXCollections.observableArrayList());
        }
    }
}
