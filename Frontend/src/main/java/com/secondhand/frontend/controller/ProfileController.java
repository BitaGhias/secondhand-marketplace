package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Rating;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * JavaFX controller of the "profile" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ProfileController extends BaseController {

    @FXML private HBox titleBar;
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label ratingLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // امتیازهای فروشندگی
    @FXML private VBox sellerRatingCard;
    @FXML private Label ratingCountLabel;
    @FXML private VBox sellerRatingsBox;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        if (avatarImageView != null) avatarImageView.setClip(new Circle(55, 55, 55));
        loadProfile();
    }

    /**
     * Loads profile.
     */
    private void loadProfile() {
        User cached = SessionManager.getCurrentUser();
        if (cached != null) fillForm(cached);

        UserService.getCurrentUserAsync()
                .thenAccept(fresh -> Platform.runLater(() -> {
                    SessionManager.setCurrentUser(fresh);
                    fillForm(fresh);
                    loadSellerRating(fresh.getId());
                    loadSellerRatings(fresh.getId());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showMessage("خطا در دریافت اطلاعات پروفایل: " + ex.getMessage(), false));
                    return null;
                });
    }

    /**
     * Loads seller rating.
     *
     * @param userId id of the user
     */
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

    /**
     * Loads seller ratings.
     *
     * @param userId id of the user
     */
    private void loadSellerRatings(Long userId) {
        if (sellerRatingCard == null || sellerRatingsBox == null) return;

        RatingService.getSellerRatingsAsync(userId)
                .thenAccept(ratings -> Platform.runLater(() -> {
                    if (ratings == null || ratings.isEmpty()) {
                        sellerRatingCard.setVisible(false);
                        sellerRatingCard.setManaged(false);
                        return;
                    }

                    sellerRatingCard.setVisible(true);
                    sellerRatingCard.setManaged(true);
                    ratingCountLabel.setText(ratings.size() + " امتیاز");

                    sellerRatingsBox.getChildren().clear();
                    for (Rating rating : ratings) {
                        sellerRatingsBox.getChildren().add(buildRatingCard(rating));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (sellerRatingCard != null) {
                            sellerRatingCard.setVisible(false);
                            sellerRatingCard.setManaged(false);
                        }
                    });
                    return null;
                });
    }

    /**
     * Builds rating card.
     *
     * @param rating the rating object
     * @return the resulting {@code VBox} instance
     */
    private VBox buildRatingCard(Rating rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating.getScore(); i++) stars.append("\u2B50");
        for (int i = rating.getScore(); i < 5; i++) stars.append("\u2606");

        Label starsLabel = new Label(stars.toString());
        starsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f97316;");

        Label scoreLabel = new Label("امتیاز: " + rating.getScore() + " از 5");
        scoreLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label itemLabel = new Label("📦 " + (rating.getItemTitle() != null ? rating.getItemTitle() : "آگهی حذف شده"));
        itemLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label raterLabel = new Label("👤 " + (rating.getRaterUsername() != null ? rating.getRaterUsername() : "کاربر ناشناس"));
        raterLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        VBox topRow = new VBox(2, starsLabel, scoreLabel);
        VBox infoRow = new VBox(2, itemLabel, raterLabel);

        VBox cardContent = new VBox(8, topRow, infoRow);

        if (rating.getComment() != null && !rating.getComment().isBlank()) {
            Label commentLabel = new Label("\ud83d\udcdd " + rating.getComment());
            commentLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-style: italic;");
            commentLabel.setWrapText(true);
            commentLabel.setMaxWidth(580);
            cardContent.getChildren().add(commentLabel);
        }

        VBox card = new VBox(10, cardContent);
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-padding: 14;");
        return card;
    }

    /**
     * Fills form.
     *
     * @param user the user object
     */
    private void fillForm(User user) {
        usernameLabel.setText("👤 " + user.getUsername());
        roleLabel.setText("ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "کاربر عادی");
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        phoneField.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        loadAvatar(user);
    }

    /**
     * Loads avatar.
     *
     * @param user the user object
     */
    private void loadAvatar(User user) {
        try {
            if (user.getProfileImageUrl() != null) {
                avatarImageView.setImage(new Image(user.getProfileImageUrl(), 110, 110, false, true, true));
            } else {
                avatarImageView.setImage(new Image(
                        getClass().getResourceAsStream("/com/secondhand/frontend/images/default-item.png")));
            }
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
    }

    /**
     * Changes profile photo.
     */
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

    /**
     * Saves profile.
     */
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

    /**
     * Changes password.
     */
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

    /**
     * Shows message.
     *
     * @param text the text value
     * @param success the "success" value of type {@code boolean}
     */
    private void showMessage(String text, boolean success) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (success ? "#16a34a" : "#dc2626") + ";");
        messageLabel.setVisible(true);
    }

    /**
     * Navigates to back.
     */
    @FXML
    private void goBack() {
        try { MainApplication.changeScene("/com/secondhand/frontend/fxml/item/adlist.fxml", "لیست آگهی‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}