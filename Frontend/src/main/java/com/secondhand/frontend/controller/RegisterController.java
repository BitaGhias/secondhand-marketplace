package com.secondhand.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.service.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // ===== اعتبارسنجی =====
        if (fullName.isEmpty()) {
            showError("لطفاً نام کامل را وارد کنید");
            return;
        }
        if (username.isEmpty()) {
            showError("لطفاً نام کاربری را وارد کنید");
            return;
        }
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            showError("لطفاً ایمیل معتبر وارد کنید");
            return;
        }
        if (phone.isEmpty() || !phone.matches("^09[0-9]{9}$")) {
            showError("شماره تلفن باید با 09 شروع شود و 11 رقم باشد");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            showError("رمز عبور باید حداقل ۶ کاراکتر باشد");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("رمز عبور و تکرار آن مطابقت ندارند");
            return;
        }

        loadingIndicator.setVisible(true);
        registerButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            String json = String.format(
                    "{\"fullName\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"phoneNumber\":\"%s\",\"email\":\"%s\"}",
                    fullName, username, password, phone, email
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleRegisterResponse)
                    .exceptionally(e -> {
                        showError("خطا در ارتباط با سرور: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            showError("خطا: " + e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
            registerButton.setDisable(false);
        }
    }

    private void handleRegisterResponse(String responseBody) {
        try {
            // ثبت‌نام موفق
            javafx.application.Platform.runLater(() -> {
                try {
                    showSuccess("✅ ثبت‌نام با موفقیت انجام شد! به صفحه ورود بروید.");
                    MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "ورود");
                } catch (Exception e) {
                    showError("خطا در بارگذاری صفحه ورود");
                }
            });
        } catch (Exception e) {
            // بررسی خطای سرور (مثل تکراری بودن)
            if (responseBody.contains("نام کاربری تکراری")) {
                showError("این نام کاربری قبلاً ثبت شده است");
            } else if (responseBody.contains("ایمیل تکراری")) {
                showError("این ایمیل قبلاً ثبت شده است");
            } else if (responseBody.contains("شماره تلفن تکراری")) {
                showError("این شماره تلفن قبلاً ثبت شده است");
            } else {
                showError("خطا در ثبت‌نام: " + responseBody);
            }
        }
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            loadingIndicator.setVisible(false);
            registerButton.setDisable(false);
        });
    }

    private void showSuccess(String message) {
        javafx.application.Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #51cf66;");
            errorLabel.setVisible(true);
        });
    }

    @FXML
    private void goToLogin() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "ورود");
        } catch (Exception e) {
            showError("خطا در بارگذاری صفحه ورود");
        }
    }
}