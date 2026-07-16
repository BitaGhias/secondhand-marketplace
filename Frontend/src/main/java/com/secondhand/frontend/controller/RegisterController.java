package com.secondhand.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.service.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
                    .thenAccept(response -> {
                        int statusCode = response.statusCode();
                        String responseBody = response.body();

                        System.out.println("Status Code: " + statusCode);
                        System.out.println("Response Body: " + responseBody);

                        if (statusCode == 201 || statusCode == 200) {
                            handleRegisterSuccess();
                        } else {
                            handleRegisterError(responseBody);
                        }
                    })
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            showError("خطا در ارتباط با سرور: " + e.getMessage());
                            loadingIndicator.setVisible(false);
                            registerButton.setDisable(false);
                        });
                        return null;
                    });

        } catch (Exception e) {
            Platform.runLater(() -> {
                showError("خطا: " + e.getMessage());
                loadingIndicator.setVisible(false);
                registerButton.setDisable(false);
            });
        }
    }

    private void handleRegisterSuccess() {
        Platform.runLater(() -> {
            showSuccess("✅ ثبت‌نام با موفقیت انجام شد! به صفحه ورود بروید.");
            loadingIndicator.setVisible(false);
            registerButton.setDisable(false);

            // رفتن به صفحه ورود بعد از 2 ثانیه
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        try {
                            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "ورود");
                        } catch (Exception e) {
                            showError("خطا در بارگذاری صفحه ورود");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleRegisterError(String responseBody) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            registerButton.setDisable(false);

            if (responseBody.contains("نام کاربری تکراری") || responseBody.contains("duplicate")) {
                showError("این نام کاربری قبلاً ثبت شده است");
            } else if (responseBody.contains("ایمیل تکراری") || responseBody.contains("Email already")) {
                showError("این ایمیل قبلاً ثبت شده است");
            } else if (responseBody.contains("شماره تلفن تکراری") || responseBody.contains("Phone already")) {
                showError("این شماره تلفن قبلاً ثبت شده است");
            } else if (responseBody.contains("validation")) {
                showError("اطلاعات وارد شده معتبر نیست");
            } else {
                showError("خطا در ثبت‌نام: " + responseBody);
            }
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText("❌ " + message);
            errorLabel.setStyle("-fx-text-fill: #ff4757;");
            errorLabel.setVisible(true);
            loadingIndicator.setVisible(false);
            registerButton.setDisable(false);
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            errorLabel.setText("✅ " + message);
            errorLabel.setStyle("-fx-text-fill: #38ef7d;");
            errorLabel.setVisible(true);
        });
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void maximizeWindow() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
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