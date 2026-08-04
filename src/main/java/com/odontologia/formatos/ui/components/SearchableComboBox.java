package com.odontologia.formatos.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchableComboBox<T> extends ComboBox<T> {

    private final ObservableList<T> allItems = FXCollections.observableArrayList();
    private final Function<T, String> displayFunction;

    public SearchableComboBox(Function<T, String> displayFunction) {
        this.displayFunction = displayFunction;
        setEditable(true);
        setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : displayFunction.apply(item);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });

        TextField editor = getEditor();
        editor.textProperty().addListener((obs, old, text) -> {
            if (text == null || text.isEmpty()) {
                setItems(allItems);
                hide();
                return;
            }
            String lower = text.toLowerCase();
            List<T> filtered = allItems.stream()
                .filter(item -> displayFunction.apply(item).toLowerCase().contains(lower))
                .collect(Collectors.toList());
            setItems(FXCollections.observableArrayList(filtered));
            if (!filtered.isEmpty()) {
                show();
            } else {
                hide();
            }
        });

        setOnShowing(e -> {
            if (editor.getText() == null || editor.getText().isEmpty()) {
                setItems(allItems);
            }
        });
    }

    public void setAllItems(List<T> items) {
        allItems.setAll(items);
        setItems(allItems);
    }

    public void setAllItems(ObservableList<T> items) {
        allItems.setAll(items);
        setItems(allItems);
    }
}
