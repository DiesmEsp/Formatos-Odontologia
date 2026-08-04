package com.odontologia.formatos.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class ToastUtil {

    private ToastUtil() {}

    public static void success(Scene scene, String message) {
        show(scene, message, "toast-success");
    }

    public static void error(Scene scene, String message) {
        show(scene, message, "toast-error");
    }

    public static void info(Scene scene, String message) {
        show(scene, message, "");
    }

    private static void show(Scene scene, String message, String styleClass) {
        Platform.runLater(() -> {
            Stage stage = (Stage) scene.getWindow();

            Label label = new Label(message);
            label.getStyleClass().add("toast-label");

            HBox bar = new HBox(label);
            bar.getStyleClass().addAll("toast-bar", styleClass);
            bar.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            spacer.setPrefHeight(8);

            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.getContent().addAll(bar);

            bar.setOnMouseClicked(e -> popup.hide());

            double x = stage.getX() + (stage.getWidth() - 360) / 2;
            double y = stage.getY() + 20;
            popup.show(stage, x, y);

            Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.seconds(3.5),
                    new KeyValue(popup.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(4.0),
                    new KeyValue(popup.opacityProperty(), 0.0))
            );
            fadeOut.setOnFinished(e -> popup.hide());
            fadeOut.play();
        });
    }
}
