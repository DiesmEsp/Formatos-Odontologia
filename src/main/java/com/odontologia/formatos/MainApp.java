package com.odontologia.formatos;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.db.DemoDataLoader;
import com.odontologia.formatos.ui.components.FontLoader;
import com.odontologia.formatos.ui.view.*;
import com.odontologia.formatos.util.LogConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
        DemoDataLoader.loadIfNeeded();
        FontLoader.load();

        MainView mainView = new MainView();

        mainView.registerView("tratamientos", new TratamientosView());
        mainView.registerView("asistencia", new AsistenciaView(() -> mainView.showToastSuccess("Asistencia registrada")));
        mainView.registerView("dashboard", new DashboardView(mainView::selectNav));
        mainView.registerView("catalogos", new CatalogosView(() -> {}));
        mainView.registerView("unidades", new UnidadesView());
        mainView.registerView("reportes", new ReportesView());

        mainView.selectNav("tratamientos");

        Scene scene = new Scene(mainView, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        stage.setTitle("Formatos Odontologicos");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(680);
        stage.show();
    }

    private void configurarErroresGlobales() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOGGER.log(Level.SEVERE, "Error no capturado", e);
            javafx.application.Platform.runLater(() -> {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Error inesperado");
                alerta.setHeaderText("Ocurrio un error inesperado");
                alerta.setContentText(e.getMessage() == null ? e.toString() : e.getMessage());
                alerta.showAndWait();
            });
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
