package com.odontologia.formatos.ui.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class ConfirmDialog {

    private ConfirmDialog() {}

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean confirmDelete(String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminacion");
        alert.setHeaderText("Eliminar \"" + itemName + "\"");
        alert.setContentText("Esta accion no se puede deshacer. ¿Desea continuar?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static Optional<String> confirmWithReason(String title, String header, String prompt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(prompt);

        TextArea textArea = new TextArea();
        textArea.setPromptText("Escriba el motivo...");
        textArea.setPrefRowCount(3);
        textArea.setPrefWidth(380);
        textArea.setMaxWidth(400);

        VBox content = new VBox(8, new javafx.scene.control.Label(prompt), textArea);
        alert.getDialogPane().setContent(content);

        ButtonType confirmBtn = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmBtn) {
            String reason = textArea.getText().trim();
            if (reason.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(reason);
        }
        return Optional.empty();
    }

    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
