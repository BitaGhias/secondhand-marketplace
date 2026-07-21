package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.ValidationUtil;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class RegisterController extends BaseController {

    // ===== FXML Fields =====
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
    @FXML private HBox titleBar;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        System.out.println("✅ RegisterController initialized");
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // ===== اعتبارسنجی متمرکز با استفاده از ValidationUtil =====
        if (!validateForm(fullName, username, email, phone, password, confirmPassword)) {
            return;
        }

        // ===== وضعیت لودینگ UI =====
        setLoadingState(true);

        // انجام عملیات ناهمگام از طریق سرویس احراز هویت بدون پرتاب ارور چک‌شده
        AuthService.register(fullName, username, email, phone, password)
                .thenAccept(responseBody -> handleRegisterSuccess(username, password))
                .exceptionally(ex -> {
                    // استخراج ایمن پیام خطا از داخل زنجیره CompletableFuture
                    String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    handleRegisterError(errorMsg);
                    return null;
                });
    }

    private boolean validateForm(String fullName, String username, String email, String phone, String password, String confirmPassword) {
        if (fullName.isEmpty()) { return showValidationError("لطفاً نام کامل را وارد کنید"); }
        if (username.isEmpty()) { return showValidationError("لطفاً نام کاربری را وارد کنید"); }

        if (!ValidationUtil.isValidEmail(email)) {
            return showValidationError("لطفاً ایمیل معتبر وارد کنید");
        }
        if (!ValidationUtil.isValidIranianPhone(phone)) {
            return showValidationError("شماره تلفن باید با 09 شروع شود و 11 رقم باشد");
        }
        if (!ValidationUtil.isValidPassword(password, 6)) {
            return showValidationError("رمز عبور باید حداقل ۶ کاراکتر باشد");
        }
        if (!password.equals(confirmPassword)) {
            return showValidationError("رمز عبور و تکرار آن مطابقت ندارند");
        }
        return true;
    }

    private boolean showValidationError(String message) {
        showError(message);
        return false;
    }

    private void handleRegisterSuccess(String username, String password) {
        Platform.runLater(() -> showSuccess("✅ ثبت‌نام با موفقیت انجام شد! در حال ورود به حساب شما..."));

        // ورود خودکار پس از ثبت‌نام موفق
        AuthService.login(username, password)
                .thenAccept(loginResponse -> Platform.runLater(() -> {
                    if (loginResponse != null && loginResponse.getUser() != null) {
                        SessionManager.setCurrentUser(loginResponse.getUser());
                    }
                    setLoadingState(false);
                    try {
                        MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "بازار سفید - لیست آگهی‌ها");
                    } catch (Exception e) {
                        e.printStackTrace();
                        goToLogin();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoadingState(false);
                        showSuccess("✅ ثبت‌نام انجام شد! لطفاً وارد شوید.");
                        goToLogin();
                    });
                    return null;
                });
    }

    private void handleRegisterError(String errorMessage) {
        Platform.runLater(() -> {
            setLoadingState(false);

            if (errorMessage.contains("نام کاربری تکراری") || errorMessage.contains("duplicate")) {
                showError("این نام کاربری قبلاً ثبت شده است");
            } else if (errorMessage.contains("ایمیل تکراری") || errorMessage.contains("Email already")) {
                showError("این ایمیل قبلاً ثبت شده است");
            } else if (errorMessage.contains("شماره تلفن تکراری") || errorMessage.contains("Phone already")) {
                showError("این شماره تلفن قبلاً ثبت شده است");
            } else {
                showError("خطا در ثبت‌نام: " + errorMessage);
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (loadingIndicator != null) loadingIndicator.setVisible(isLoading);
        if (registerButton != null) registerButton.setDisable(isLoading);
        if (isLoading && errorLabel != null) errorLabel.setVisible(false);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText("❌ " + message);
                errorLabel.setStyle("-fx-text-fill: #dc2626;");
                errorLabel.setVisible(true);
            }
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setStyle("-fx-text-fill: #16a34a;");
                errorLabel.setVisible(true);
            }
        });
    }

    // ===== Title Bar Controls =====
    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.setIconified(true);
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