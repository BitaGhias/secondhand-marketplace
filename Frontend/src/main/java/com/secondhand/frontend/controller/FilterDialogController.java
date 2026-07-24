package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import com.secondhand.frontend.util.CategoryPicker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller of the advanced filter dialog (category, city, price range and sort order).
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class FilterDialogController {

    private static final String ALL_CATEGORIES_LABEL = "🗂 همه دسته‌بندی‌ها";

    @FXML private MenuButton filterCategoryMenuButton;
    @FXML private ComboBox<City> filterCityComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label filterErrorLabel;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    private FilterListener listener;
    private Category selectedCategory;

    private static final Map<String, String> SORT_OPTIONS = new LinkedHashMap<>();
    static {
        SORT_OPTIONS.put("جدیدترین", "newest");
        SORT_OPTIONS.put("قدیمی‌ترین", "oldest");
        SORT_OPTIONS.put("ارزان‌ترین", "price_asc");
        SORT_OPTIONS.put("گران‌ترین", "price_desc");
    }
    private static final String DEFAULT_SORT_LABEL = "جدیدترین";

    /**
     * Listener notified when a filter is applied or cleared; implemented by the ad-list screen.
     */
    public interface FilterListener {
        void onFilterApplied(Long categoryId, Long cityId, Long minPrice, Long maxPrice, String sortBy);
        void onFilterCleared();
    }

    /**
     * Sets listener.
     *
     * @param listener the callback listener
     */
    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        setupSortComboBox();
        loadOptionsInBackground();
    }

    /**
     * Sets up sort combo box.
     */
    private void setupSortComboBox() {
        if (sortComboBox == null) return;
        sortComboBox.getItems().setAll(SORT_OPTIONS.keySet());
        sortComboBox.setValue(DEFAULT_SORT_LABEL);
    }

    /**
     * Loads options in background.
     */
    private void loadOptionsInBackground() {
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                List<City> cities = CityService.getAllCities();

                Platform.runLater(() -> {
                    // فلای‌اوت مشترک دسته‌بندی — زیردسته‌ها در زیرمنوی کناری باز می‌شوند
                    CategoryPicker.populate(filterCategoryMenuButton, categories, ALL_CATEGORIES_LABEL,
                            cat -> selectedCategory = cat);
                    filterCityComboBox.getItems().setAll(cities);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در بارگذاری دسته‌بندی‌ها و شهرها"));
            }
        }).start();
    }

    /**
     * Applies filter.
     */
    @FXML
    private void applyFilter() {
        hideError();

        Long minPrice, maxPrice;
        try {
            minPrice = parsePrice(minPriceField.getText());
            maxPrice = parsePrice(maxPriceField.getText());
        } catch (NumberFormatException e) {
            showError("مقدار قیمت باید عدد باشد!");
            return;
        }

        if (minPrice != null && minPrice < 0 || maxPrice != null && maxPrice < 0) {
            showError("قیمت نمی‌تواند منفی باشد!");
            return;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            showError("حداقل قیمت نمی‌تواند از حداکثر بیشتر باشد!");
            return;
        }

        City selectedCity = filterCityComboBox.getValue();
        String sortBy = sortComboBox != null
                ? SORT_OPTIONS.getOrDefault(sortComboBox.getValue(), "newest")
                : "newest";

        if (listener != null) {
            listener.onFilterApplied(
                    selectedCategory != null ? selectedCategory.getId() : null,
                    selectedCity != null ? selectedCity.getId() : null,
                    minPrice,
                    maxPrice,
                    sortBy
            );
        }
        close();
    }

    /**
     * Clears filter.
     */
    @FXML
    private void clearFilter() {
        selectedCategory = null;
        if (filterCategoryMenuButton != null) filterCategoryMenuButton.setText(ALL_CATEGORIES_LABEL);
        filterCityComboBox.getSelectionModel().clearSelection();
        filterCityComboBox.setValue(null);
        minPriceField.clear();
        maxPriceField.clear();
        if (sortComboBox != null) sortComboBox.setValue(DEFAULT_SORT_LABEL);
        hideError();

        if (listener != null) {
            listener.onFilterCleared();
        }
        close();
    }

    /**
     * Parses price.
     *
     * @param text the text value
     * @return the resulting numeric value
     * @throws NumberFormatException if the text cannot be parsed as a valid number
     */
    private Long parsePrice(String text) throws NumberFormatException {
        if (text == null || text.isBlank()) return null;
        String normalized = text.trim()
                .replace(",", "").replace("،", "").replace(" ", "");
        StringBuilder sb = new StringBuilder();
        for (char ch : normalized.toCharArray()) {
            if (ch >= '۰' && ch <= '۹') sb.append((char) ('0' + (ch - '۰')));
            else if (ch >= '٠' && ch <= '٩') sb.append((char) ('0' + (ch - '٠')));
            else sb.append(ch);
        }
        return Long.parseLong(sb.toString());
    }

    /**
     * Shows error.
     *
     * @param message the message text
     */
    private void showError(String message) {
        filterErrorLabel.setText(message);
        filterErrorLabel.setVisible(true);
    }

    /**
     * Hides error.
     */
    private void hideError() {
        filterErrorLabel.setVisible(false);
    }

    /**
     * Closes.
     */
    private void close() {
        Stage stage = (Stage) applyFilterButton.getScene().getWindow();
        stage.close();
    }
}
