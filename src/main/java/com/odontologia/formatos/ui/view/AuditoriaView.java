package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.model.RegistroAnulacion;
import com.odontologia.formatos.repository.RegistroAnulacionRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AuditoriaView extends VBox {

    private final RegistroAnulacionRepository repo = new RegistroAnulacionRepository();
    private TableView<RegistroAnulacion> table;

    public AuditoriaView() {
        build();
    }

    private void build() {
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);

        VBox header = new VBox(8);
        header.getStyleClass().add("view-header");
        Label title = new Label("Auditoria");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Registro de anulaciones de tratamientos y asistencias");
        subtitle.getStyleClass().add("view-subtitle");
        header.getChildren().addAll(title, subtitle);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RegistroAnulacion, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp()));
        colFecha.setPrefWidth(160);

        TableColumn<RegistroAnulacion, String> colTabla = new TableColumn<>("Tabla");
        colTabla.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTablaAfectada()));
        colTabla.setPrefWidth(120);

        TableColumn<RegistroAnulacion, String> colId = new TableColumn<>("ID Registro");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdRegistroAnulado())));
        colId.setPrefWidth(100);

        TableColumn<RegistroAnulacion, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario()));
        colUsuario.setPrefWidth(120);

        TableColumn<RegistroAnulacion, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));
        colMotivo.setPrefWidth(400);

        table.getColumns().addAll(colFecha, colTabla, colId, colUsuario, colMotivo);

        Button refreshBtn = new Button("Actualizar");
        refreshBtn.getStyleClass().addAll("btn", "btn-secondary");
        refreshBtn.setOnAction(e -> loadAuditoria());

        getChildren().addAll(header, refreshBtn, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadAuditoria();
    }

    private void loadAuditoria() {
        try {
            table.setItems(FXCollections.observableArrayList(repo.findAll()));
        } catch (Exception e) {
            table.setItems(FXCollections.observableArrayList());
        }
    }
}
