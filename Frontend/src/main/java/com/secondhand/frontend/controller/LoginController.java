package com.secondhand.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;
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
        // فکوس هوشمند با اینتر
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

        // فعال‌سازی حالت لودینگ درست مثل رجیستر
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

            // ارسال غیرهمزمان و مدیریت درست هندلرها بدون تداخل بلوک finally
            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        // پاس دادن کل آبجکت پاسخ برای بررسی وضعیت HTTP Status Code
                        handleLoginResponse(response);
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        showError("خطا در ارتباط با سرور: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            showError("خطا: " + e.getMessage());
        }
        // بلوک finally حذف شد چون مدیریت المان‌ها باید پس از دریافت پاسخ واقعی انجام شود
    }

    private void handleLoginResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        String responseBody = response.body();

        System.out.println("Status Code: " + statusCode);
        System.out.println("Response Body: " + responseBody);

        // ۱. مدیریت وضعیت‌های ناامیدکننده بک‌اَند
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

            // استخراج آبجکت کاربر به صورت امن
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
                // ذخیره کاربر در سشن یا استفاده از fullName آن در کنترلر بعدی در صورت نیاز
            }

            // ۲. تغییر به پنجره لیست آگهی‌ها (adlist) به صورت ریسک‌فری و با پرینت استک ترس خطاها
            javafx.application.Platform.runLater(() -> {
                try {
                    // تغییر مسیر قطعی به فایل خوش‌استایل adlist.fxml که ساختیم
                    MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "بازار سفید - لیست آگهی‌ها");
                } catch (Exception e) {
                    System.err.println("❌ خطا در متد تغییر سین به adlist.fxml اتفاق افتاد:");
                    e.printStackTrace(); // اینجا اگر کدهای FXML یا CSS مورد داشته باشند لو می‌روند
                    showError("خطا در رندر و بارگذاری صفحه اصلی");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("خطا در پردازش اطلاعات دریافتی از سرور");
        }
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #ff576c;"); // تنظیم رنگ قرمز جهت خوانایی در پوسته دارک
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
}