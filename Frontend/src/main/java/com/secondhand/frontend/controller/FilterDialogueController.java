package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class FilterDialogueController {

    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<City> filterCityComboBox;
    @FXML private ComboBox<Category> filterCategoryComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private Label filterErrorLabel;

    private FilterListener listener;

    public interface FilterListener {
        void onFilterApplied(Long categoryId, Long cityId, Integer minPrice, Integer maxPrice);
        void onFilterCleared();
    }

    @FXML
    public void initialize() {
        loadCities();
        loadCategories();
    }

    private void loadCities() {
        new Thread(() -> {
            try {
                List<City> cities = CityService.getAllCities();
                Platform.runLater(() -> filterCityComboBox.getItems().setAll(cities));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                Platform.runLater(() -> filterCategoryComboBox.getItems().setAll(categories));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @FXML
    private void applyFilter() {
        Integer minPrice;
        Integer maxPrice;
        try {
            minPrice = minPriceField.getText() == null || minPriceField.getText().isBlank()
                    ? null : Integer.parseInt(minPriceField.getText().trim());
            maxPrice = maxPriceField.getText() == null || maxPriceField.getText().isBlank()
                    ? null : Integer.parseInt(maxPriceField.getText().trim());
        } catch (NumberFormatException e) {
            showError("قیمت باید عدد صحیح باشد");
            return;
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            showError("حداقل قیمت نمی‌تواند از حداکثر بیشتر باشد");
            return;
        }

        if (listener != null) {
            Long categoryId = filterCategoryComboBox.getValue() != null
                    ? filterCategoryComboBox.getValue().getId() : null;
            Long cityId = filterCityComboBox.getValue() != null
                    ? filterCityComboBox.getValue().getId() : null;

            listener.onFilterApplied(categoryId, cityId, minPrice, maxPrice);
        }
        closeDialog();
    }

    @FXML
    private void clearFilter() {
        minPriceField.clear();
        maxPriceField.clear();
        filterCategoryComboBox.getSelectionModel().clearSelection();
        filterCityComboBox.getSelectionModel().clearSelection();

        if (listener != null) {
            listener.onFilterCleared();
        }
        closeDialog();
    }

    private void showError(String message) {
        if (filterErrorLabel != null) {
            filterErrorLabel.setText(message);
            filterErrorLabel.setVisible(true);
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) applyFilterButton.getScene().getWindow();
        stage.close();
    }
}
