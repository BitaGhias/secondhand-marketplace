package com.secondhand.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.util.ApiClient;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Phase 6: LoginController extends BaseController
 * (minimizeWindow / closeWindow از BaseController به ارث می‌رسند — تکرار حذف شد)
 */
public class LoginController extends BaseController {

    @FXML private TextField         usernameField;
    @FXML private PasswordField     passwordField;
    @FXML private Button            loginButton;
    @FXML private Hyperlink         registerLink;
    @FXML private Label             errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private HBox              titleBar;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);

        // اگر قبلاً وارد شده بود، مستقیم به صفحه اصلی بره
        if (SessionManager.isLoggedIn()) {
            try {
                navigateAfterLogin();
            } catch (Exception e) { e.printStackTrace(); }
            return;
        }

        // فوکوس هوشمند با اینتر
        usernameField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) passwordField.requestFocus(); });
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    // ─ window controls ─ BaseController پالیش کرده (ActionEvent)
    // login.fxml از onAction="#minimizeWindow" / onAction="#closeWindow" استفاده می‌کند
    // BaseController هر دو را دارد — هیچ overrideای لازم نیست

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
                        showError("خطا در ارتباط با سرور: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            showError("خطا: " + e.getMessage());
        }
    }

    private void handleLoginResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status == 401 || status == 403) { showError("نام کاربری یا رمز عبور اشتباه است"); return; }
        if (status != 200) { showError("خطای سرور: " + status); return; }

        try {
            ObjectMapper mapper = ApiClient.getMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> res = mapper.readValue(response.body(), Map.class);

            String token = (String) res.get("token");
            ApiClient.setToken(token);

            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) res.get("user");
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
            }
            Platform.runLater(() -> {
                try { navigateAfterLogin(); }
                catch (Exception e) { showError("خطا در بارگذاری صفحه"); }
            });
        } catch (Exception e) {
            showError("خطا در پردازش پاسخ سرور");
        }
    }

    private void navigateAfterLogin() throws Exception {
        User u = SessionManager.getCurrentUser();
        if (u != null && "ADMIN".equalsIgnoreCase(u.getRole()))
            MainApplication.changeScene("/com/secondhand/frontend/admin_panel.fxml", "پنل مدیریت");
        else
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "بازار سفید — لیست آگهی‌ها");
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #dc2626;");
            errorLabel.setVisible(true);
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
        });
    }

    @FXML
    private void goToRegister() {
        try { MainApplication.changeScene("/com/secondhand/frontend/register.fxml", "ثبت‌نام"); }
        catch (Exception e) { showError("خطا در بارگذاری صفحه ثبت‌نام"); }
    }
}