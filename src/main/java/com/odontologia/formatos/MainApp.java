package com.odontologia.formatos;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.util.LogConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger("com.odontologia.formatos");

    @Override
    public void start(Stage stage) {
        configurarErroresGlobales();
        LogConfig.configurar();
        ConnectionManager.getInstance();
        stage.setTitle("Formatos Odontología");
        stage.setScene(new Scene(new Label("Aplicación en construcción"), 1280, 720));
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.show();
    }

    private void configurarErroresGlobales() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOGGER.log(Level.SEVERE, "Error no capturado", e);
            javafx.application.Platform.runLater(() -> {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Error inesperado");
                alerta.setHeaderText("Ocurrió un error inesperado");
                alerta.setContentText(e.getMessage() == null ? e.toString() : e.getMessage());
                alerta.showAndWait();
            });
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
