package com.odontologia.formatos.ui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Consumer;

public class MaterialTable extends VBox {

    private final ObservableList<MaterialRow> rows = FXCollections.observableArrayList();
    private final VBox rowsContainer;
    private final Consumer<Integer> onRowCountChanged;
    private List<MaterialOption> materialOptions = List.of();

    public MaterialTable() {
        this(null);
    }

    public MaterialTable(Consumer<Integer> onRowCountChanged) {
        this.onRowCountChanged = onRowCountChanged;
        getStyleClass().add("material-table");
        setSpacing(4);

        rowsContainer = new VBox(4);
        getChildren().add(rowsContainer);
    }

    public void setMaterialOptions(List<MaterialOption> options) {
        this.materialOptions = options;
        for (MaterialRow row : rows) {
            row.getMaterialCombo().setAllItems(options);
        }
    }

    private MaterialRow createRow() {
        final MaterialRow[] ref = new MaterialRow[1];
        ref[0] = new MaterialRow(rows.size() + 1, () -> {
            rows.remove(ref[0]);
            rowsContainer.getChildren().remove(ref[0]);
            renumberRows();
            if (onRowCountChanged != null) {
                onRowCountChanged.accept(rows.size());
            }
        });
        ref[0].getMaterialCombo().setAllItems(materialOptions);
        return ref[0];
    }

    public void addRow() {
        MaterialRow row = createRow();
        rows.add(row);
        rowsContainer.getChildren().add(row);
        if (onRowCountChanged != null) {
            onRowCountChanged.accept(rows.size());
        }
    }

    public void addRow(int materialId, String materialName, double quantity) {
        MaterialRow row = createRow();
        row.setMaterial(materialId, materialName);
        row.setQuantity(quantity);
        rows.add(row);
        rowsContainer.getChildren().add(row);
    }

    public void setMaterials(List<MaterialEntry> entries) {
        clear();
        for (MaterialEntry e : entries) {
            addRow(e.materialId, e.materialName, e.quantity);
        }
    }

    public ObservableList<MaterialEntry> getEntries() {
        ObservableList<MaterialEntry> entries = FXCollections.observableArrayList();
        for (MaterialRow row : rows) {
            Integer id = row.getMaterialId();
            double qty = row.getQuantity();
            if (id != null && qty > 0) {
                entries.add(new MaterialEntry(id, row.getMaterialName(), qty));
            }
        }
        return entries;
    }

    public boolean isValid() {
        if (rows.isEmpty()) return true;
        for (MaterialRow row : rows) {
            if (row.getMaterialId() == null || row.getQuantity() <= 0) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        rows.clear();
        rowsContainer.getChildren().clear();
    }

    private void renumberRows() {
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setRowNumber(i + 1);
        }
    }

    public static class MaterialRow extends HBox {

        private final TextField quantityField;
        private final SearchableComboBox<MaterialOption> materialCombo;
        private final Button removeBtn;
        private MaterialOption selectedMaterial;

        public MaterialRow(int rowNumber, Runnable onRemove) {
            super(8);
            getStyleClass().add("material-row");
            setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            materialCombo = new SearchableComboBox<>(MaterialOption::label);
            materialCombo.setPromptText("Seleccionar material...");
            materialCombo.setPrefWidth(260);
            materialCombo.valueProperty().addListener((obs, old, val) -> {
                selectedMaterial = val;
            });

            quantityField = new TextField();
            quantityField.setPromptText("Cant.");
            quantityField.setPrefWidth(80);
            quantityField.setTextFormatter(new TextFormatter<>(change -> {
                String text = change.getControlNewText();
                if (text.isEmpty() || text.matches("\\d*\\.?\\d*")) {
                    return change;
                }
                return null;
            }));

            removeBtn = new Button("-");
            removeBtn.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
            removeBtn.setOnAction(e -> onRemove.run());

            getChildren().addAll(materialCombo, quantityField, removeBtn);
        }

        public void setRowNumber(int n) {
        }

        public Integer getMaterialId() {
            return selectedMaterial != null ? selectedMaterial.id() : null;
        }

        public String getMaterialName() {
            return selectedMaterial != null ? selectedMaterial.label() : "";
        }

        public double getQuantity() {
            try {
                return Double.parseDouble(quantityField.getText().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public void setMaterial(int materialId, String materialName) {
            this.selectedMaterial = new MaterialOption(materialId, materialName);
            materialCombo.setValue(selectedMaterial);
        }

        public void setQuantity(double qty) {
            quantityField.setText(String.valueOf(qty));
        }

        public SearchableComboBox<MaterialOption> getMaterialCombo() {
            return materialCombo;
        }

        public TextField getQuantityField() {
            return quantityField;
        }
    }

    public record MaterialOption(int id, String label) {}

    public static class MaterialEntry {
        public final int materialId;
        public final String materialName;
        public final double quantity;

        public MaterialEntry(int materialId, String materialName, double quantity) {
            this.materialId = materialId;
            this.materialName = materialName;
            this.quantity = quantity;
        }
    }

    public VBox getRowsContainer() {
        return rowsContainer;
    }

    public Button createAddButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
        btn.setOnAction(e -> addRow());
        return btn;
    }
}
