package com.secondhand.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;
import com.secondhand.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        // اگر قبلاً وارد شده بود، مستقیم به صفحه اصلی بره
        if (SessionManager.isLoggedIn()) {
            try {
                MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "بازار سفید - لیست آگهی‌ها");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // فوکوس هوشمند با اینتر
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void maximizeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("لطفاً نام کاربری و رمز عبور را وارد کنید");
            return;
        }

        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(this::handleLoginResponse)
                    .exceptionally(e -> {
                        e.printStackTrace();
                        showError("خطا در ارتباط با سرور: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            showError("خطا: " + e.getMessage());
        }
    }

    private void handleLoginResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        String responseBody = response.body();

        System.out.println("Status Code: " + statusCode);
        System.out.println("Response Body: " + responseBody);

        if (statusCode == 401 || statusCode == 403) {
            showError("نام کاربری یا رمز عبور اشتباه است");
            return;
        }
        if (statusCode != 200) {
            showError("خطای سرور: کد وضعیت " + statusCode);
            return;
        }

        try {
            ObjectMapper mapper = ApiClient.getMapper();
            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);

            // استخراج و ذخیره‌سازی توکن
            String token = (String) responseMap.get("token");
            ApiClient.setToken(token);

            // استخراج آبجکت کاربر
            Map<String, Object> userMap = (Map<String, Object>) responseMap.get("user");
            if (userMap != null) {
                User user = new User(
                        ((Number) userMap.get("id")).longValue(),
                        (String) userMap.get("fullName"),
                        (String) userMap.get("username"),
                        (String) userMap.get("role"),
                        (boolean) userMap.get("blocked"),
                        (String) userMap.get("phoneNumber"),
                        (String) userMap.get("email")
                );
                SessionManager.setCurrentUser(user);
                System.out.println("✅ کاربر وارد شد: " + user.getFullName());
            }

            // تغییر به صفحه لیست آگهی‌ها
            Platform.runLater(() -> {
                try {
                    MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "بازار سفید - لیست آگهی‌ها");
                } catch (Exception e) {
                    System.err.println("❌ خطا در تغییر صفحه به adlist.fxml:");
                    e.printStackTrace();
                    showError("خطا در رندر و بارگذاری صفحه اصلی");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("خطا در پردازش اطلاعات دریافتی از سرور");
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #ff576c;");
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
            e.printStackTrace();
            showError("خطا در بارگذاری صفحه ثبت‌نام");
        }
    }
}