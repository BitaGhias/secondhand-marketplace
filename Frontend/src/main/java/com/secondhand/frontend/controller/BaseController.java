package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;

/**
 * JavaFX controller of the "base" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public abstract class BaseController {

    // ─── Window Controls ───────────────────────────────────

    /**
     * Performs the "minimize window" operation.
     *
     * @param event the UI event
     */
    @FXML
    public void minimizeWindow(ActionEvent event) {
        WindowUtil.minimize((Node) event.getSource());
    }

    /**
     * Performs the "maximize window" operation.
     *
     * @param event the UI event
     */
    @FXML
    public void maximizeWindow(ActionEvent event) {
        WindowUtil.toggleMaximize((Node) event.getSource());
    }

    /**
     * Closes window.
     *
     * @param event the UI event
     */
    @FXML
    public void closeWindow(ActionEvent event) {
        WindowUtil.close((Node) event.getSource());
    }

    // ─── Dialog Helpers ────────────────────────────────────

    /**
     * Shows alert.
     *
     * @param message the message text
     * @param type the "type" value of type {@code Alert.AlertType}
     */
    protected void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "خطا" : "موفق");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows error.
     *
     * @param message the message text
     */
    protected void showError(String message) {
        showAlert(message, Alert.AlertType.ERROR);
    }

    /**
     * Shows success.
     *
     * @param message the message text
     */
    protected void showSuccess(String message) {
        showAlert(message, Alert.AlertType.INFORMATION);
    }

    /**
     * Styles alert.
     *
     * @param alert the "alert" value of type {@code Alert}
     */
    protected void styleAlert(Alert alert) {
        try {
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            alert.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
    }
}