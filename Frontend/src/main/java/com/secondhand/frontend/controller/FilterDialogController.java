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
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * کنترلر دیالوگ فیلتر جست‌وجو — مطابق filter_dialogue.fxml
 * (دسته‌بندی، شهر، محدوده قیمت). نتیجه از طریق FilterListener به AdListController برمی‌گردد
 * و جست‌وجو با POST /api/items/search/advanced انجام می‌شود.
 */
public class FilterDialogController {

    @FXML private ComboBox<Category> filterCategoryComboBox;
    @FXML private ComboBox<City> filterCityComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private Label filterErrorLabel;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    private FilterListener listener;

    public interface FilterListener {
        void onFilterApplied(Long categoryId, Long cityId, Long minPrice, Long maxPrice);
        void onFilterCleared();
    }

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @FXML
    public void initialize() {
        setupCategoryComboDisplay();
        loadOptionsInBackground();
    }

    /** نمایش سلسله‌مراتبی دسته‌بندی‌ها (زیردسته‌ها با نام والد) */
    private void setupCategoryComboDisplay() {
        filterCategoryComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayName(item));
            }
        });
        filterCategoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayName(item));
            }
        });
    }

    private String displayName(Category c) {
        if (c.getParentName() != null && !c.getParentName().isBlank()) {
            return c.getParentName() + " › " + c.getName();
        }
        return c.getName();
    }

    private void loadOptionsInBackground() {
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                List<City> cities = CityService.getAllCities();

                // مرتب‌سازی: ابتدا دسته‌های اصلی، سپس زیردسته‌ها کنار والد خودشان
                List<Category> ordered = new ArrayList<>();
                List<Category> roots = categories.stream()
                        .filter(Category::isRoot)
                        .sorted(Comparator.comparing(Category::getName))
                        .toList();
                for (Category root : roots) {
                    ordered.add(root);
                    categories.stream()
                            .filter(c -> !c.isRoot() && root.getId().equals(c.getParentId()))
                            .sorted(Comparator.comparing(Category::getName))
                            .forEach(ordered::add);
                }
                // دسته‌هایی که در سلسله‌مراتب جا نماندند
                for (Category c : categories) {
                    if (!ordered.contains(c)) ordered.add(c);
                }

                Platform.runLater(() -> {
                    filterCategoryComboBox.getItems().setAll(ordered);
                    filterCityComboBox.getItems().setAll(cities);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در بارگذاری دسته‌بندی‌ها و شهرها"));
            }
        }).start();
    }

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

        Category selectedCategory = filterCategoryComboBox.getValue();
        City selectedCity = filterCityComboBox.getValue();

        if (listener != null) {
            listener.onFilterApplied(
                    selectedCategory != null ? selectedCategory.getId() : null,
                    selectedCity != null ? selectedCity.getId() : null,
                    minPrice,
                    maxPrice
            );
        }
        close();
    }

    @FXML
    private void clearFilter() {
        filterCategoryComboBox.getSelectionModel().clearSelection();
        filterCategoryComboBox.setValue(null);
        filterCityComboBox.getSelectionModel().clearSelection();
        filterCityComboBox.setValue(null);
        minPriceField.clear();
        maxPriceField.clear();
        hideError();

        if (listener != null) {
            listener.onFilterCleared();
        }
        close();
    }

    /** تبدیل متن قیمت به عدد — ارقام فارسی و جداکننده‌ها را هم می‌پذیرد */
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

    private void showError(String message) {
        filterErrorLabel.setText(message);
        filterErrorLabel.setVisible(true);
    }

    private void hideError() {
        filterErrorLabel.setVisible(false);
    }

    private void close() {
        Stage stage = (Stage) applyFilterButton.getScene().getWindow();
        stage.close();
    }
}
