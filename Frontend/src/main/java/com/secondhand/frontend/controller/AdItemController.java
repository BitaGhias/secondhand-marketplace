package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * کارت آگهی:
 * • badge وضعیت روی تصویر (کارکرده / فروخته شد / در انتظار / رد شد)
 * • دکمهٔ قلب علاقه‌مندی با توگل زنده (بدون باز شدن صفحهٔ جزئیات)
 */
public class AdItemController {

    private static final String HEART_ON  =
            "-fx-background-color: #ffffff; -fx-background-radius: 999; -fx-text-fill: #f97316; -fx-font-size: 13px; -fx-padding: 3 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.20), 6, 0, 0, 1);";
    private static final String HEART_OFF =
            "-fx-background-color: #ffffff; -fx-background-radius: 999; -fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-padding: 3 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.20), 6, 0, 0, 1);";

    @FXML private ImageView adImageView;
    @FXML private Label adTitleLabel;
    @FXML private Label adPriceLabel;
    @FXML private Label usernameLabel;
    @FXML private Label cityLabel;
    @FXML private Label ratingLabel;
    @FXML private Label statusLabel;
    @FXML private Label cardBadgeLabel;
    @FXML private Button favoriteButton;

    private Item item;
    private boolean isFavorite;

    @FXML
    public void initialize() {
        // کلیک روی قلب نباید به کارت برسد و صفحهٔ جزئیات را باز کند
        if (favoriteButton != null) favoriteButton.setOnMouseClicked(Event::consume);
    }

    public void setItem(Item item) {
        this.item = item;

        if (adTitleLabel != null) adTitleLabel.setText(item.getTitle());
        if (adPriceLabel != null) adPriceLabel.setText(item.getFormattedPrice());
        if (usernameLabel != null) usernameLabel.setText(item.getOwnerUsername());
        if (cityLabel != null) cityLabel.setText(item.getCityName());
        if (ratingLabel != null) ratingLabel.setText(item.getFormattedRating());
        if (statusLabel != null) {
            statusLabel.setText(item.getPersianStatus());
            statusLabel.setStyle("-fx-text-fill: " + item.getStatusColor() + ";");
        }

        updateBadge(item);
        loadFavoriteState(item);

        if (adImageView != null) {
            String imageUrl = item.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    adImageView.setImage(new Image(imageUrl, true));
                } catch (Exception e) {
                    loadDefaultImage();
                }
            } else {
                loadDefaultImage();
            }
        }
    }

    /** badge وضعیت گوشهٔ تصویر — همهٔ کالاهای فعال در این بازار دست‌دوم (کارکرده) هستند */
    private void updateBadge(Item item) {
        if (cardBadgeLabel == null) return;
        String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "";
        String text;
        String bg;
        String fg;
        switch (status) {
            case "SOLD"     -> { text = "فروخته شد"; bg = "#dbeafe"; fg = "#1d4ed8"; }
            case "PENDING"  -> { text = "در انتظار";  bg = "#fef3c7"; fg = "#b45309"; }
            case "REJECTED" -> { text = "رد شد";      bg = "#fee2e2"; fg = "#b91c1c"; }
            default         -> { text = "کارکرده";    bg = "#ffedd5"; fg = "#c2410c"; }
        }
        cardBadgeLabel.setText(text);
        cardBadgeLabel.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 3 10;");
    }

    /** وضعیت اولیهٔ قلب را از سرور می‌خواند (فقط برای کاربر لاگین‌کرده) */
    private void loadFavoriteState(Item item) {
        if (favoriteButton == null) return;
        if (SessionManager.getCurrentUser() == null || item.getId() == null) {
            favoriteButton.setVisible(false);
            favoriteButton.setManaged(false);
            return;
        }
        applyHeart(false);
        new Thread(() -> {
            try {
                boolean fav = FavoriteService.isFavorite(item.getId());
                Platform.runLater(() -> applyHeart(fav));
            } catch (Exception ignored) { }
        }).start();
    }

    private void applyHeart(boolean fav) {
        isFavorite = fav;
        if (favoriteButton == null) return;
        favoriteButton.setText(fav ? "❤" : "♡");
        favoriteButton.setStyle(fav ? HEART_ON : HEART_OFF);
    }

    @FXML
    private void toggleFavorite() {
        if (item == null || item.getId() == null) return;
        boolean target = !isFavorite;
        applyHeart(target); // بازخورد فوری؛ در صورت خطا برمی‌گردد
        new Thread(() -> {
            try {
                if (target) FavoriteService.addFavorite(item.getId());
                else        FavoriteService.removeFavorite(item.getId());
            } catch (Exception e) {
                Platform.runLater(() -> applyHeart(!target));
            }
        }).start();
    }

    private void loadDefaultImage() {
        try {
            var imageStream = getClass().getResourceAsStream(
                    "/com/secondhand/frontend/images/default-item.png");
            if (imageStream != null) {
                adImageView.setImage(new Image(imageStream));
            }
        } catch (Exception e) {
            System.err.println("خطا در تنظیم تصویر پیش‌فرض: " + e.getMessage());
        }
    }

    public Item getItem() {
        return item;
    }

    @FXML
    private void handleCardClick() {
        if (item == null) return;
        try {
            MainApplication.goToItemDetail(item);
        } catch (Exception e) {
            System.err.println("❌ خطا در باز کردن جزئیات: " + e.getMessage());
        }
    }
}
