package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.Unidad;
import com.odontologia.formatos.repository.UnidadRepository;
import com.odontologia.formatos.service.UnidadService;
import com.odontologia.formatos.ui.components.ConfirmDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UnidadesView extends VBox {

    private final UnidadService unidadService = new UnidadService();
    private TableView<Unidad> table;

    public UnidadesView() {
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Unidades");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Administrar unidades dentales de la clinica");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Unidad, String> colNumero = new TableColumn<>("Numero de unidad");
        colNumero.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUnidadNro())));

        TableColumn<Unidad, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = new Button("Eliminar");
            {
                delBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    Unidad u = getTableView().getItems().get(getIndex());
                    delBtn.setOnAction(e -> {
                        if (ConfirmDialog.confirmDelete("Unidad " + u.getUnidadNro())) {
                            try {
                                unidadService.eliminar(u.getUnidadID());
                                loadUnidades();
                            } catch (Exception ex) {
                                ConfirmDialog.error("Error", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(delBtn);
                }
            }
        });

        table.getColumns().addAll(colNumero, colAcciones);

        Button addBtn = new Button("+ Nueva unidad");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> {
            try {
                unidadService.crear();
                loadUnidades();
            } catch (Exception ex) {
                ConfirmDialog.error("Error", ex.getMessage());
            }
        });

        HBox toolbar = new HBox(12, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadUnidades();
    }

    private void loadUnidades() {
        try {
            table.setItems(FXCollections.observableArrayList(new UnidadRepository().findAll()));
        } catch (Exception e) {
            table.setItems(FXCollections.observableArrayList());
        }
    }
}
