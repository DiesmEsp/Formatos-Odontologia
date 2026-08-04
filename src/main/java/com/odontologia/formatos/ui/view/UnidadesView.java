package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.Unidad;
import com.odontologia.formatos.repository.TratamientoRepository;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.service.UnidadService;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import com.odontologia.formatos.ui.components.SvgIcons;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UnidadesView extends VBox {

    private final UnidadService unidadService = new UnidadService();
    private TableView<Unidad> table;

    public UnidadesView() { build(); }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Unidades");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Administre las unidades de atencion de la clinica");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Unidad, String> colNro = new TableColumn<>("#");
        colNro.setCellValueFactory(d -> {
            int n = d.getValue().getUnidadNro();
            return new SimpleStringProperty(String.format("%02d", n));
        });
        colNro.setPrefWidth(60);

        TableColumn<Unidad, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty("Unidad " + d.getValue().getUnidadNro()));
        colNombre.setPrefWidth(300);

        TableColumn<Unidad, String> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(150);
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(estaOcupada(d.getValue().getUnidadID()) ? "Ocupado" : "Libre"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER_LEFT);
                    javafx.scene.shape.Circle led = new javafx.scene.shape.Circle(4);
                    led.getStyleClass().add("led");
                    if ("Ocupado".equals(item)) led.getStyleClass().add("led-warn");
                    else led.getStyleClass().add("led-ok");
                    Label lbl = new Label(item);
                    if ("Ocupado".equals(item)) lbl.getStyleClass().addAll("badge", "badge-warning");
                    else lbl.getStyleClass().addAll("badge", "badge-success");
                    box.getChildren().addAll(led, lbl);
                    setGraphic(box);
                }
            }
        });

        TableColumn<Unidad, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = new Button("Eliminar");
            { delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger"); }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    Unidad u = getTableView().getItems().get(getIndex());
                    if (estaOcupada(u.getUnidadID())) {
                        Button disabled = new Button("Eliminar");
                        disabled.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
                        disabled.setDisable(true);
                        disabled.setTooltip(new Tooltip("No se puede eliminar: unidad ocupada"));
                        setGraphic(disabled);
                    } else {
                        delBtn.setOnAction(e -> {
                            if (ConfirmDialog.confirmDelete("Unidad " + u.getUnidadNro())) {
                                try { unidadService.eliminar(u.getUnidadID()); loadUnidades(); }
                                catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
                            }
                        });
                        setGraphic(delBtn);
                    }
                }
            }
        });

        table.getColumns().addAll(colNro, colNombre, colEstado, colAcciones);

        Button addBtn = new Button("Nueva Unidad");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setGraphic(SvgIcons.plus(15));
        SvgIcons.plus(15).getStyleClass().add("svg-icon-group");
        addBtn.setOnAction(e -> {
            try { unidadService.crear(); loadUnidades(); }
            catch (Exception ex) { ConfirmDialog.error("Error", ex.getMessage()); }
        });

        HBox toolbar = new HBox(12, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        loadUnidades();
    }

    private boolean estaOcupada(int unidadID) {
        try { return new TratamientoRepository().findAbiertoPorUnidad(unidadID) != null; }
        catch (Exception e) { return false; }
    }

    private void loadUnidades() {
        try { table.setItems(FXCollections.observableArrayList(new UnidadRepository().findAll())); }
        catch (Exception e) { table.setItems(FXCollections.observableArrayList()); }
    }
}
