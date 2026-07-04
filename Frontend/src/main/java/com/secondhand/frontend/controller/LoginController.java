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

        // اول چک کن فیلدها خالی نباشند
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("لطفاً همه فیلدها را پر کنید");
            return; // از متد خارج شو و بقیه کدها رو اجرا نکن
        }

        // حالا از سرویس بپرس
        boolean success = authService.login(username, password);

        // اگر موفق بود، برو به صفحه بعدی
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
            // اگر موفق نبود، فقط پیام خطا بده (به صفحه بعدی نرو)
            errorLabel.setText("نام کاربری یا رمز عبور اشتباه است!");
        }
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