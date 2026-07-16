package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
        try {
            List<City> cities = CityService.getAllCities();
            filterCityComboBox.getItems().addAll(cities);
            filterCityComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = CategoryService.getAllCategories();
            filterCategoryComboBox.getItems().addAll(categories);
            filterCategoryComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @FXML
    private void applyFilter() {
        if (listener != null) {
            Long categoryId = filterCategoryComboBox.getValue() != null
                    ? filterCategoryComboBox.getValue().getId() : null;
            Long cityId = filterCityComboBox.getValue() != null
                    ? filterCityComboBox.getValue().getId() : null;
            Integer minPrice = minPriceField.getText().isEmpty()
                    ? null : Integer.parseInt(minPriceField.getText());
            Integer maxPrice = maxPriceField.getText().isEmpty()
                    ? null : Integer.parseInt(maxPriceField.getText());

            listener.onFilterApplied(categoryId, cityId, minPrice, maxPrice);
        }
        closeDialog();
    }

    @FXML
    private void clearFilter() {
        if (listener != null) {
            listener.onFilterCleared();
        }
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) applyFilterButton.getScene().getWindow();
        stage.close();
    }
}