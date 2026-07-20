package com.secondhand.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FilterDialogController {

    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;

    // در صورت وجود کمبوباکس‌ها یا فیلدهای دیگر، آن‌ها را اینجا نگه دارید
    // @FXML private ComboBox<Category> categoryComboBox;
    // @FXML private ComboBox<City> cityComboBox;

    private FilterListener listener;

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @FXML
    public void initialize() {
        // اعمال فیلتر عددی رویTextFieldها برای جلوگیری از ورود کاراکترهای غیرمجاز و کرش برنامه
        setupNumericFilter(minPriceField);
        setupNumericFilter(maxPriceField);
    }

    @FXML
    private void handleApplyFilter() {
        if (listener != null) {
            // مقادیر پیش‌فرض آیدی‌ها (با توجه به منطق پروژه‌تان مقداردهی کنید)
            Long categoryId = null;
            Long cityId = null;

            // 🟢 پارس کردن مبالغ به صورت Long جهت هماهنگی کامل با کامپایلر و بک‌اند
            Long minPrice = (minPriceField.getText() == null || minPriceField.getText().isBlank())
                    ? null : Long.parseLong(minPriceField.getText().trim());

            Long maxPrice = (maxPriceField.getText() == null || maxPriceField.getText().isBlank())
                    ? null : Long.parseLong(maxPriceField.getText().trim());

            // فراخوانی لیسنر با متد جدید
            listener.onFilterApplied(categoryId, cityId, minPrice, maxPrice);
        }
        closeWindow();
    }

    @FXML
    private void handleClearFilter() {
        if (listener != null) {
            listener.onFilterCleared();
        }
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) minPriceField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void setupNumericFilter(TextField textField) {
        if (textField == null) return;
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * 🟢 اینترفیس به‌روزرسانی شده با نوع داده Long
     * این تغییر باعث برطرف شدن خطاهای اورراید در AdListController می‌شود.
     */
    public interface FilterListener {
        void onFilterApplied(Long categoryId, Long cityId, Long minPrice, Long maxPrice);
        void onFilterCleared();
    }
}