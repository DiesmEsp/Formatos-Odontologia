package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.ui.components.ToastUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainView extends BorderPane {

    private final StackPane contentArea;
    private final Map<String, NavEntry> navEntries = new LinkedHashMap<>();
    private NavEntry activeEntry;

    private static final String SIDEBAR_STYLE = "sidebar";
    private static final String NAV_BUTTON = "nav-button";

    public MainView() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        VBox sidebar = buildSidebar();

        setLeft(sidebar);
        setCenter(contentArea);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add(SIDEBAR_STYLE);

        VBox header = new VBox();
        header.getStyleClass().add("sidebar-header");
        Label title = new Label("Formatos");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Clinica Odontologica");
        subtitle.getStyleClass().add("sidebar-subtitle");
        header.getChildren().addAll(title, subtitle);

        VBox nav = new VBox(2);
        nav.getStyleClass().add("sidebar-nav");

        addNavSection(nav, null, "Principal");
        addNavEntry(nav, "dashboard", "Inicio", "M 4 8 H 20 V 10 H 4 Z M 4 14 H 20 V 22 H 4 Z");

        addNavSection(nav, null, "Gestion");
        addNavEntry(nav, "catalogos", "Catalogos", "M 3 13 H 9 V 6 H 3 Z M 11 13 H 21 V 3 H 11 Z M 3 21 H 9 V 14 H 3 Z M 11 21 H 21 V 14 H 11 Z");
        addNavEntry(nav, "unidades", "Unidades", "M 4 3 H 10 V 10 H 4 Z M 14 3 H 20 V 10 H 14 Z M 4 14 H 10 V 21 H 4 Z M 14 14 H 20 V 21 H 14 Z");
        addNavEntry(nav, "asistencia", "Asistencia", "M 7 11 V 7 A 5 5 0 1 1 10 12 M 22 4 V 16 H 14.5 M 2 6 H 7 M 2 10 H 7 M 14 20 V 15 A 5 5 0 0 1 19 10 M 22 20 H 17");
        addNavEntry(nav, "tratamientos", "Tratamientos", "M 7 2 H 17 V 6 H 7 Z M 2 7 H 22 V 17 A 4 4 0 0 1 18 21 H 6 A 4 4 0 0 1 2 17 Z");

        addNavSection(nav, null, "Reportes");
        addNavEntry(nav, "reportes", "Reportes", "M 14 2 H 6 A 2 2 0 0 0 4 4 V 20 A 2 2 0 0 0 6 22 H 18 A 2 2 0 0 0 20 20 V 8 Z M 14 2 V 8 H 20 M 16 13 H 8 M 16 17 H 8 M 10 9 H 8");

        addNavSection(nav, null, "Sistema");
        addNavEntry(nav, "auditoria", "Auditoria", "M 12 22 C 17.523 22 22 17.523 22 12 S 17.523 2 12 2 S 2 6.477 2 12 S 6.477 22 12 22 Z M 12 16 V 12 M 12 8 H 12.01");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(header, nav, spacer);
        return sidebar;
    }

    private void addNavSection(VBox nav, String label, String sectionLabel) {
        if (sectionLabel != null) {
            Label secLabel = new Label(sectionLabel.toUpperCase());
            secLabel.getStyleClass().add("nav-separator-label");
            VBox wrapper = new VBox(secLabel);
            wrapper.getStyleClass().add("nav-separator");
            nav.getChildren().add(wrapper);
        }
    }

    private void addNavEntry(VBox nav, String key, String label, String svgPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("nav-icon");

        Button btn = new Button(label);
        btn.getStyleClass().add(NAV_BUTTON);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setGraphic(icon);
        btn.setOnAction(e -> selectNav(key));

        NavEntry entry = new NavEntry(key, btn);
        navEntries.put(key, entry);
        nav.getChildren().add(btn);
    }

    public void registerView(String key, Node view) {
        NavEntry entry = navEntries.get(key);
        if (entry != null) {
            entry.view = view;
        }
    }

    public void selectNav(String key) {
        NavEntry entry = navEntries.get(key);
        if (entry == null || entry == activeEntry) return;

        if (activeEntry != null) {
            activeEntry.button.getStyleClass().remove("active");
        }

        activeEntry = entry;
        entry.button.getStyleClass().add("active");

        if (entry.view != null) {
            contentArea.getChildren().setAll(entry.view);
        }
    }

    public void showToast(String message) {
        ToastUtil.info(getScene(), message);
    }

    public void showToastError(String message) {
        ToastUtil.error(getScene(), message);
    }

    public void showToastSuccess(String message) {
        ToastUtil.success(getScene(), message);
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    private static class NavEntry {
        final String key;
        final Button button;
        Node view;

        NavEntry(String key, Button button) {
            this.key = key;
            this.button = button;
        }
    }
}
