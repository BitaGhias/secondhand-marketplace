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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * کنترلر دیالوگ فیلتر جست‌وجو — مطابق filter_dialogue.fxml
 * (دسته‌بندی، شهر، محدوده قیمت و مرتب‌سازی). نتیجه از طریق FilterListener به AdListController برمی‌گردد
 * و جست‌وجو با POST /api/items/search/advanced انجام می‌شود.
 */
public class FilterDialogController {

    @FXML private ComboBox<Category> filterCategoryComboBox;
    @FXML private ComboBox<City> filterCityComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label filterErrorLabel;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    private FilterListener listener;

    /** برچسب فارسی مرتب‌سازی ← کد مورد انتظار بک‌اند (ItemSearchRequest.sortBy) */
    private static final Map<String, String> SORT_OPTIONS = new LinkedHashMap<>();
    static {
        SORT_OPTIONS.put("جدیدترین", "newest");
        SORT_OPTIONS.put("قدیمی‌ترین", "oldest");
        SORT_OPTIONS.put("ارزان‌ترین", "price_asc");
        SORT_OPTIONS.put("گران‌ترین", "price_desc");
    }
    private static final String DEFAULT_SORT_LABEL = "جدیدترین";

    public interface FilterListener {
        void onFilterApplied(Long categoryId, Long cityId, Long minPrice, Long maxPrice, String sortBy);
        void onFilterCleared();
    }

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @FXML
    public void initialize() {
        setupCategoryComboDisplay();
        setupSortComboBox();
        loadOptionsInBackground();
    }

    private void setupSortComboBox() {
        if (sortComboBox == null) return;
        sortComboBox.getItems().setAll(SORT_OPTIONS.keySet());
        sortComboBox.setValue(DEFAULT_SORT_LABEL);
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

    @FXML
    private void clearFilter() {
        filterCategoryComboBox.getSelectionModel().clearSelection();
        filterCategoryComboBox.setValue(null);
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