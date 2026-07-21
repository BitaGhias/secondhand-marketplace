package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;

/**
 * کلاس پایه همه کنترلرهای فرانت‌اند.
 * - کنترل پنجره (minimize / maximize / close)
 * - نمایش دیالوگ خطا و موفقیت به صورت یکپارچه
 */
public abstract class BaseController {

    // ─── Window Controls ───────────────────────────────────

    @FXML
    public void minimizeWindow(ActionEvent event) {
        WindowUtil.minimize((Node) event.getSource());
    }

    @FXML
    public void maximizeWindow(ActionEvent event) {
        WindowUtil.toggleMaximize((Node) event.getSource());
    }

    @FXML
    public void closeWindow(ActionEvent event) {
        WindowUtil.close((Node) event.getSource());
    }

    // ─── Dialog Helpers ────────────────────────────────────

    /**
     * نمایش دیالوگ با نوع دلخواه (ERROR یا INFORMATION)
     */
    protected void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "خطا" : "موفق");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /** دیالوگ خطا */
    protected void showError(String message) {
        showAlert(message, Alert.AlertType.ERROR);
    }

    /** دیالوگ موفقیت */
    protected void showSuccess(String message) {
        showAlert(message, Alert.AlertType.INFORMATION);
    }

    /** اعمال استایل مشترک به دیالوگ‌ها */
    protected void styleAlert(Alert alert) {
        try {
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            alert.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        } catch (Exception ignored) {}
    }
}