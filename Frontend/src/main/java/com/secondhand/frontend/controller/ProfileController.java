package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.ValidationUtil;
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
 * Phase 5: نمایش میانگین امتیاز در پروفایل
 */
public class ProfileController extends BaseController {

    @FXML private HBox titleBar;
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label ratingLabel;       // Phase 5 — fx:id در fxml
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
        if (avatarImageView != null) avatarImageView.setClip(new Circle(55, 55, 55));
        loadProfile();
    }

    private void loadProfile() {
        User cached = SessionManager.getCurrentUser();
        if (cached != null) fillForm(cached);

        UserService.getCurrentUserAsync()
                .thenAccept(fresh -> Platform.runLater(() -> {
                    SessionManager.setCurrentUser(fresh);
                    fillForm(fresh);
                    loadSellerRating(fresh.getId());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showMessage("خطا در دریافت اطلاعات پروفایل: " + ex.getMessage(), false));
                    return null;
                });
    }

    /** Phase 5: دریافت میانگین امتیاز از API */
    private void loadSellerRating(Long userId) {
        if (ratingLabel == null) return;
        RatingService.getSellerAverageAsync(userId)
                .thenAccept(avg -> Platform.runLater(() -> {
                    if (avg == null || avg == 0.0) {
                        ratingLabel.setText("★ امتیاز: بدون امتیاز");
                    } else {
                        ratingLabel.setText(String.format("★ امتیاز فروشندگی: %.1f / 5", avg));
                    }
                    ratingLabel.setVisible(true);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        ratingLabel.setText("★ امتیاز: نامشخص");
                        ratingLabel.setVisible(true);
                    });
                    return null;
                });
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
        } catch (Exception ignored) {}
    }

    @FXML
    private void changeProfilePhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("انتخاب عکس پروفایل");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files",
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        File file = fc.showOpenDialog(avatarImageView.getScene().getWindow());
        if (file == null) return;

        UserService.uploadProfileImageAsync(file)
                .thenAccept(updated -> Platform.runLater(() -> {
                    SessionManager.setCurrentUser(updated);
                    loadAvatar(updated);
                    showMessage("✅ عکس پروفایل به‌روزرسانی شد", true);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showMessage("خطا در آپلود عکس: " + ex.getMessage(), false));
                    return null;
                });
    }

    @FXML
    private void saveProfile() {
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();

        if (fullName.isEmpty()) { showMessage("نام نمی‌تواند خالی باشد", false); return; }
        if (!ValidationUtil.isValidEmail(email)) { showMessage("ایمیل معتبر وارد کنید", false); return; }
        if (!ValidationUtil.isValidIranianPhone(phone)) { showMessage("شماره تلفن معتبر نیست", false); return; }

        UserService.updateProfileAsync(fullName, phone, email)
                .thenAccept(updated -> Platform.runLater(() -> {
                    SessionManager.setCurrentUser(updated);
                    fillForm(updated);
                    showMessage("✅ پروفایل ذخیره شد", true);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showMessage("خطا: " + ex.getMessage(), false));
                    return null;
                });
    }

    @FXML
    private void changePassword() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (oldPass.isBlank() || newPass.isBlank()) { showMessage("رمز فعلی و جدید را وارد کنید", false); return; }
        if (!ValidationUtil.isValidPassword(newPass, 6)) { showMessage("رمز جدید حداقل ۶ کاراکتر", false); return; }
        if (!newPass.equals(confirm)) { showMessage("رمز جدید و تکرار آن یکسان نیستند", false); return; }

        UserService.changePasswordAsync(oldPass, newPass)
                .thenRun(() -> Platform.runLater(() -> {
                    oldPasswordField.clear(); newPasswordField.clear(); confirmPasswordField.clear();
                    showMessage("✅ رمز عبور تغییر کرد", true);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showMessage("خطا: " + ex.getMessage(), false));
                    return null;
                });
    }

    private void showMessage(String text, boolean success) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (success ? "#16a34a" : "#dc2626") + ";");
        messageLabel.setVisible(true);
    }

    @FXML
    private void goBack() {
        try { MainApplication.changeScene("/com/secondhand/frontend/fxml/item/adlist.fxml", "لیست آگهی‌ها"); }
        catch (Exception e) { e.printStackTrace(); }
    }
}