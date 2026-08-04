package com.odontologia.formatos.ui.view;

import com.odontologia.formatos.ui.components.SvgIcons;
import com.odontologia.formatos.ui.components.ToastUtil;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainView extends BorderPane {

    private final StackPane contentArea;
    private final Map<String, NavEntry> navEntries = new LinkedHashMap<>();
    private NavEntry activeEntry;

    public MainView() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        setLeft(buildSidebar());
        setCenter(contentArea);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Brand
        VBox brand = new VBox();
        brand.getStyleClass().add("sidebar-brand");

        HBox brandRow = new HBox();
        StackPane brandMark = new StackPane(SvgIcons.tooth(19));
        brandMark.getStyleClass().add("brand-mark");
        brandMark.getChildren().forEach(c -> { if (c instanceof javafx.scene.Group) c.getStyleClass().add("svg-icon-group"); });

        VBox brandText = new VBox(0);
        Label brandTitle = new Label("Formatos Odontologicos");
        brandTitle.getStyleClass().add("brand-title");
        Label brandSub = new Label("Clinica UNMSM");
        brandSub.getStyleClass().add("brand-subtitle");
        brandText.getChildren().addAll(brandTitle, brandSub);

        brandRow.getChildren().addAll(brandMark, brandText);
        brand.getChildren().add(brandRow);

        // Nav
        VBox nav = new VBox(0);
        nav.getStyleClass().add("sidebar-nav");

        addEyebrow(nav, "Atencion");
        addNavEntry(nav, "tratamientos", "Tratamientos", SvgIcons.tooth(18));
        addNavEntry(nav, "asistencia", "Asistencia Docente", SvgIcons.asistencia(18));

        addEyebrow(nav, "Gestion");
        addNavEntry(nav, "dashboard", "Dashboard", SvgIcons.dashboard(18));
        addNavEntry(nav, "catalogos", "Catalogos", SvgIcons.catalogo(18));
        addNavEntry(nav, "unidades", "Unidades", SvgIcons.modulos(18));
        addNavEntry(nav, "reportes", "Reportes", SvgIcons.reportes(18));

        Label footer = new Label("v1.0.0");
        footer.getStyleClass().add("sidebar-footer");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brand, nav, spacer, footer);
        return sidebar;
    }

    private void addEyebrow(VBox nav, String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.getStyleClass().add("nav-eyebrow");
        nav.getChildren().add(lbl);
    }

    private void addNavEntry(VBox nav, String key, String label, javafx.scene.Group icon) {
        icon.getStyleClass().add("svg-icon-group");
        Button btn = new Button(label);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setGraphic(icon);
        btn.setOnAction(e -> selectNav(key));
        NavEntry entry = new NavEntry(key, btn);
        navEntries.put(key, entry);
        nav.getChildren().add(btn);
    }

    public void registerView(String key, Node view) {
        NavEntry entry = navEntries.get(key);
        if (entry != null) entry.view = view;
    }

    public void selectNav(String key) {
        NavEntry entry = navEntries.get(key);
        if (entry == null || entry == activeEntry) return;
        if (activeEntry != null) activeEntry.button.getStyleClass().remove("active");
        activeEntry = entry;
        entry.button.getStyleClass().add("active");
        if (entry.view != null) contentArea.getChildren().setAll(entry.view);
    }

    public void showToast(String message) { ToastUtil.info(getScene(), message); }
    public void showToastError(String message) { ToastUtil.error(getScene(), message); }
    public void showToastSuccess(String message) { ToastUtil.success(getScene(), message); }

    public StackPane getContentArea() { return contentArea; }

    private static class NavEntry {
        final String key;
        final Button button;
        Node view;
        NavEntry(String key, Button button) { this.key = key; this.button = button; }
    }
}
