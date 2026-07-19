package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * صفحه پروفایل کاربر:
 * - نمایش و تغییر عکس پروفایل
 * - ویرایش اطلاعات حساب (نام، ایمیل، تلفن)
 * - تغییر رمز عبور
 */
public class ProfileController extends BaseController {
    @FXML private HBox titleBar;
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        // عکس پروفایل دایره‌ای
        avatarImageView.setClip(new Circle(55, 55, 55));
        loadProfile();
    }

    private void loadProfile() {
        // اول از کش سشن، بعد تازه‌سازی از سرور
        User cached = SessionManager.getCurrentUser();
        if (cached != null) {
            fillForm(cached);
        }
        new Thread(() -> {
            try {
                User fresh = UserService.getCurrentUser();
                Platform.runLater(() -> {
                    SessionManager.setCurrentUser(fresh);
                    fillForm(fresh);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا در دریافت اطلاعات پروفایل: " + e.getMessage(), false));
            }
        }).start();
    }

    private void fillForm(User user) {
        usernameLabel.setText("👤 " + user.getUsername());
        roleLabel.setText("ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "کاربر عادی");
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        phoneField.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        loadAvatar(user);
    }

    private void loadAvatar(User user) {
        try {
            if (user.getProfileImageUrl() != null) {
                avatarImageView.setImage(new Image(user.getProfileImageUrl(), 110, 110, false, true, true));
            } else {
                avatarImageView.setImage(new Image(
                        getClass().getResourceAsStream("/com/secondhand/frontend/images/default-item.png")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void changeProfilePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب عکس پروفایل");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        File file = fileChooser.showOpenDialog(avatarImageView.getScene().getWindow());
        if (file == null) return;

        new Thread(() -> {
            try {
                User updated = UserService.uploadProfileImage(file);
                Platform.runLater(() -> {
                    SessionManager.setCurrentUser(updated);
                    loadAvatar(updated);
                    showMessage("✅ عکس پروفایل با موفقیت به‌روزرسانی شد", true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا در آپلود عکس: " + e.getMessage(), false));
            }
        }).start();
    }

    @FXML
    private void saveProfile() {
        String fullName = fullNameField.getText() != null ? fullNameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";

        if (fullName.isEmpty()) {
            showMessage("نام و نام خانوادگی نمی‌تواند خالی باشد", false);
            return;
        }

        new Thread(() -> {
            try {
                User updated = UserService.updateProfile(fullName, phone, email);
                Platform.runLater(() -> {
                    SessionManager.setCurrentUser(updated);
                    fillForm(updated);
                    showMessage("✅ اطلاعات پروفایل با موفقیت ذخیره شد", true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا در ذخیره پروفایل: " + e.getMessage(), false));
            }
        }).start();
    }

    @FXML
    private void changePassword() {
        String oldPass = oldPasswordField.getText() != null ? oldPasswordField.getText() : "";
        String newPass = newPasswordField.getText() != null ? newPasswordField.getText() : "";
        String confirm = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

        if (oldPass.isBlank() || newPass.isBlank()) {
            showMessage("رمز فعلی و رمز جدید را وارد کنید", false);
            return;
        }
        if (!newPass.equals(confirm)) {
            showMessage("رمز جدید با تکرار آن یکسان نیست", false);
            return;
        }

        new Thread(() -> {
            try {
                UserService.changePassword(oldPass, newPass);
                Platform.runLater(() -> {
                    oldPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    showMessage("✅ رمز عبور با موفقیت تغییر کرد", true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا در تغییر رمز: " + e.getMessage(), false));
            }
        }).start();
    }

    private void showMessage(String text, boolean success) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (success ? "#38ef7d" : "#ff4757") + ";");
        messageLabel.setVisible(true);
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
