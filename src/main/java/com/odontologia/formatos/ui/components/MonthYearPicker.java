package com.odontologia.formatos.ui.components;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.YearMonth;

public class MonthYearPicker extends HBox {

    private final ComboBox<String> monthCombo;
    private final ComboBox<Integer> yearCombo;

    private static final String[] MONTHS = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public MonthYearPicker() {
        super(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("form-field");

        monthCombo = new ComboBox<>(FXCollections.observableArrayList(MONTHS));
        monthCombo.setPrefWidth(140);
        monthCombo.setPromptText("Mes");

        yearCombo = new ComboBox<>();
        yearCombo.setPrefWidth(90);
        yearCombo.setPromptText("Año");

        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 2; y++) {
            yearCombo.getItems().add(y);
        }

        monthCombo.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
        yearCombo.getSelectionModel().select(Integer.valueOf(currentYear));

        Label separator = new Label("");

        getChildren().addAll(new Label(""), monthCombo, yearCombo);
    }

    public int getMonth() {
        Integer idx = monthCombo.getSelectionModel().getSelectedIndex();
        return idx == null || idx < 0 ? LocalDate.now().getMonthValue() : idx + 1;
    }

    public int getYear() {
        Integer year = yearCombo.getSelectionModel().getSelectedItem();
        return year == null ? LocalDate.now().getYear() : year;
    }

    public YearMonth getYearMonth() {
        return YearMonth.of(getYear(), getMonth());
    }

    public String getMonthName() {
        return monthCombo.getSelectionModel().getSelectedItem();
    }

    public void setMonth(int month) {
        if (month >= 1 && month <= 12) {
            monthCombo.getSelectionModel().select(month - 1);
        }
    }

    public void setYear(int year) {
        yearCombo.getSelectionModel().select(Integer.valueOf(year));
    }

    public ComboBox<String> getMonthCombo() {
        return monthCombo;
    }

    public ComboBox<Integer> getYearCombo() {
        return yearCombo;
    }
}
