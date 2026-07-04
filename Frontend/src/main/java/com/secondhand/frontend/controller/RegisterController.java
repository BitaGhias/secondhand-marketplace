package com.secondhand.frontend.controller;

import com.secondhand.frontend.service.AuthService;
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

    // تعریف سرویس احراز هویت برای ارتباط با بک‌اِند
    private final AuthService authService = new AuthService();

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

        errorLabel.setText("در حال ثبت نام...");

        // ساخت تسک برای اجرای متد شبکه در ترد پس‌زمینه برای جلوگیری از فریز شدن UI
        javafx.concurrent.Task<Boolean> registerTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // صدا زدن متد ثبت‌نام با ۳ پارامتر ورودی
                return authService.register(fullName, username, password);
            }
        };

        // هندل کردن نتیجه موفقیت‌آمیز
        registerTask.setOnSucceeded(event -> {
            boolean success = registerTask.getValue();
            if (success) {
                System.out.println("ثبت‌نام با موفقیت انجام شد: " + username);
                // هدایت کاربر به صفحه لاگین (ترجیحاً بهتره اول لاگین کنه)
                goToLogin();
            } else {
                errorLabel.setText("نام کاربری تکراری است یا خطایی رخ داده!");
            }
        });

        registerTask.setOnFailed(event -> {
            Throwable e = registerTask.getException();
            e.printStackTrace();
            errorLabel.setText("خطا در اتصال به سرور!");
        });

        // اجرای ترد
        new Thread(registerTask).start();
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 500, 420));
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("خطا در بارگذاری صفحه ورود");
        }
    }
}