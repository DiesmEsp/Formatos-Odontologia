package com.odontologia.formatos;

import com.odontologia.formatos.db.ConnectionManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        ConnectionManager.getInstance();
        stage.setTitle("Formatos Odontología");
        stage.setScene(new Scene(new Label("Aplicación en construcción"), 1280, 720));
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
