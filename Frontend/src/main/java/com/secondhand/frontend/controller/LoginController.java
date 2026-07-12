package com.secondhand.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink; //همون لینک ثبت نام کنید
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        // Enter key listener
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("لطفاً نام کاربری و رمز عبور را وارد کنید");
            return;
        }

        loadingIndicator.setVisible(true);//چرخ دنده رو نشون بده
        loginButton.setDisable(true);//دکمه ورود رو غیرفعال میکنه تا کاربر دوباره کلیک نکنه
        errorLabel.setVisible(false);//خطای قبلی رو پنهان کن

        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)//وقتی جواب اومد بدنه پاسخ رو بگیر
                    .thenAccept(this::handleLoginResponse)//نتیجه رو به متد قید شده بده
                    .exceptionally(e -> {
                        showError("خطا در ارتباط با سرور: " + e.getMessage());
                        return null;
                    });//اگر خطایی رخ داد پیام خطارو نشون بده

        } catch (Exception e) {
            showError("خطا: " + e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
        }
    }

    private void handleLoginResponse(String responseBody) {
        try {
            ObjectMapper mapper = ApiClient.getMapper();
            Map<String, Object> response = mapper.readValue(responseBody, Map.class);

            // استخراج توکن و اطلاعات کاربر
            String token = (String) response.get("token");
            ApiClient.setToken(token);

            // تبدیل user Map به User object
            Map<String, Object> userMap = (Map<String, Object>) response.get("user");
            User user = new User(
                    ((Number) userMap.get("id")).longValue(),
                    (String) userMap.get("fullName"),
                    (String) userMap.get("username"),
                    (String) userMap.get("role"),
                    (boolean) userMap.get("blocked"),
                    (String) userMap.get("phoneNumber"),
                    (String) userMap.get("email")
            );

            javafx.application.Platform.runLater(() -> {
                try {
                    MainApplication.changeScene("/com/secondhand/frontend/main.fxml", "صفحه اصلی");
                } catch (Exception e) {
                    showError("خطا در بارگذاری صفحه اصلی");
                }
            });

        } catch (Exception e) {
            showError("نام کاربری یا رمز عبور اشتباه است");
        }
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
        });
    }

    @FXML
    private void goToRegister() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/register.fxml", "ثبت‌نام");
        } catch (Exception e) {
            showError("خطا در بارگذاری صفحه ثبت‌نام");
        }
    }
}