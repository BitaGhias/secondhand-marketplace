package com.secondhand.frontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() ||
        password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("لطفاً همه فیلدها را پر کنید");
            return;
        }

        if (!confirmPassword.equals(password)) {
            errorLabel.setText("رمز عبور و تکرار آن یکسان نیستند");
            return;
        }

        errorLabel.setText("");
        // بعداً به backend وصل می‌کنیم
        System.out.println("Register: " + fullName + " - " + username);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/secondhand/frontend/adlist.fxml")
            );
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 600, 500));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 500, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}