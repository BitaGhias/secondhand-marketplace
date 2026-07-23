package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.util.ApiClient;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * JavaFX controller of the "login" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class LoginController extends BaseController {

    @FXML private TextField         usernameField;
    @FXML private PasswordField     passwordField;
    @FXML private Button            loginButton;
    @FXML private Hyperlink         registerLink;
    @FXML private Label             errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private HBox              titleBar;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        if (SessionManager.isLoggedIn()) {
            try { navigateAfterLogin(); } catch (Exception e) { FrontendErrorHandler.log(e); }
            return;
        }
        usernameField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) passwordField.requestFocus(); });
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    /**
     * Handles login.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorLabel("لطفاً نام کاربری و رمز عبور را وارد کنید");
            return;
        }

        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            String json = ApiClient.getMapper().writeValueAsString(
                    Map.of("username", username, "password", password));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(this::handleLoginResponse)
                    .exceptionally(e -> {
                        showErrorLabel("خطا در ارتباط با سرور: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            showErrorLabel("خطا: " + e.getMessage());
        }
    }

    /**
     * Handles login response.
     *
     * @param response the received response
     */
    private void handleLoginResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        // FIX: به‌جای نمایش پیام یکسان برای 401 و 403، پیام واقعی سرور (مثلاً مسدود بودن کاربر) نمایش داده شود
        if (status == 401) { showErrorLabel(extractServerMessage(response.body(), "نام کاربری یا رمز عبور اشتباه است")); return; }
        if (status == 403) { showErrorLabel(extractServerMessage(response.body(), "حساب کاربری شما مسدود یا غیرفعال است")); return; }
        if (status != 200) { showErrorLabel("خطای سرور: " + status); return; }


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
                catch (Exception e) { showErrorLabel("خطا در بارگذاری صفحه"); }
            });
        } catch (Exception e) {
            showErrorLabel("خطا در پردازش پاسخ سرور");
        }
    }

    // FIX: استخراج پیام دقیق خطا (مثلاً مسدود بودن کاربر) از پاسخ سرور
    /**
     * Extracts server message.
     *
     * @param responseBody the "response body" value of type {@code String}
     * @param fallback the "fallback" value of type {@code String}
     * @return the resulting string
     */
    private String extractServerMessage(String responseBody, String fallback) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> errorMap = ApiClient.getMapper().readValue(responseBody, Map.class);
            Object message = errorMap.get("message");
            if (message != null && !message.toString().trim().isEmpty()) return message.toString();
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        return fallback;
    }

    /**
     * Navigates to after login.
     *
     * @throws Exception if the request fails or the server cannot be reached
     */
    private void navigateAfterLogin() throws Exception {
        User u = SessionManager.getCurrentUser();
        if (u != null && "ADMIN".equalsIgnoreCase(u.getRole()))
            MainApplication.changeScene(Routes.ADMIN_PANEL, "پنل مدیریت");
        else
            // Regular users must land on the marketplace panel, not the create-ad form.
            MainApplication.changeScene(Routes.USER_PANEL, "دیباچه — پنل کاربر");
    }

    /**
     * Navigates to to register.
     */
    @FXML
    private void goToRegister() {
        try { MainApplication.changeScene(Routes.REGISTER, "ثبت‌نام"); }
        catch (Exception e) { showErrorLabel("خطا در بارگذاری صفحه ثبت‌نام"); }
    }

    // ─── Label helper (≠ BaseController.showError که Alert باز می‌کنه) ───────

    /**
     * Shows error label.
     *
     * @param message the message text
     */
    private void showErrorLabel(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #dc2626;");
            errorLabel.setVisible(true);
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
        });
    }
}