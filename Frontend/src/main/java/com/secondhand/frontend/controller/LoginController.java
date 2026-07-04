package com.secondhand.frontend.controller;

import com.secondhand.frontend.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // اول چک کن فیلدها خالی نباشند (این کار سریع است و روی ترد اصلی می‌ماند)
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("لطفاً همه فیلدها را پر کنید");
            return;
        }

        // ساخت یک Task (ترد جداگانه) مخصوص عملیات لاگین
        javafx.concurrent.Task<Boolean> loginTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // این خط داخل یک ترد پس‌زمینه اجرا می‌شود و UI فریز نمی‌شود
                return authService.login(username, password);
            }
        };

        // وقتی کارِ ترد با موفقیت تمام شد (خروجی متد بالا آماده شد):
        loginTask.setOnSucceeded(event -> {
            boolean success = loginTask.getValue(); // نتیجه رو از ترد می‌گیریم

            // این بخش به صورت خودکار روی ترد اصلی (UI Thread) اجرا می‌شود، پس تغییر ظاهر امن است
            if (success) {
                System.out.println("ورود موفقیت‌آمیز!");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/adlist.fxml"));
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(loader.load(), 600, 500));
                } catch (Exception e) {
                    e.printStackTrace();
                    errorLabel.setText("خطای سیستمی در بارگذاری صفحه");
                }
            } else {
                errorLabel.setText("نام کاربری یا رمز عبور اشتباه است!");
            }
        });

        // اگر در طول اجرای ترد پس‌زمینه (مثلاً به خاطر قطعی سرور) خطایی رخ داد
        loginTask.setOnFailed(event -> {
            Throwable e = loginTask.getException();
            e.printStackTrace();
            errorLabel.setText("خطا در اتصال به سرور!");
        });

        // در نهایت، به سیستم می‌گوییم این ترد را استارت بزند و در پس‌زمینه اجرا کند
        new Thread(loginTask).start();
    }

    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/secondhand/frontend/register.fxml")
            );
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 500, 500));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}