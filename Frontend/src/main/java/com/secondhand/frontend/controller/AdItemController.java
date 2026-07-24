package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

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
import javafx.scene.layout.StackPane;
import com.secondhand.frontend.util.ImageLoaderUtil;

/**
 * JavaFX controller of the "ad item" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
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
    @FXML private StackPane sellerAvatarStack;
    @FXML private Label sellerAvatarIcon;
    @FXML private Label cityLabel;
    @FXML private Label ratingLabel;
    @FXML private Label statusLabel;
    @FXML private Label cardBadgeLabel;
    @FXML private Button favoriteButton;

    private Item item;
    private boolean isFavorite;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        // کلیک روی قلب نباید به کارت برسد و صفحهٔ جزئیات را باز کند
        if (favoriteButton != null) favoriteButton.setOnMouseClicked(Event::consume);
    }

    /**
     * Sets item.
     *
     * @param item the ad (item) object
     */
    public void setItem(Item item) {
        this.item = item;

        if (adTitleLabel != null) adTitleLabel.setText(item.getTitle());
        if (adPriceLabel != null) adPriceLabel.setText(item.getFormattedPrice());
        if (usernameLabel != null) usernameLabel.setText(item.getOwnerUsername());
        if (sellerAvatarStack != null) {
            sellerAvatarStack.getChildren().removeIf(node -> node instanceof ImageView);
            ImageView sellerAvatar = ImageLoaderUtil.circularAvatar(item.getOwnerProfileImageUrl(), 22);
            if (sellerAvatar != null) {
                if (sellerAvatarIcon != null) sellerAvatarIcon.setVisible(false);
                sellerAvatarStack.getChildren().add(sellerAvatar);
            } else if (sellerAvatarIcon != null) {
                sellerAvatarIcon.setVisible(true);
            }
        }
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

    /**
     * Updates badge.
     *
     * @param item the ad (item) object
     */
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

    /**
     * Loads favorite state.
     *
     * @param item the ad (item) object
     */
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
            } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        }).start();
    }

    /**
     * Applies heart.
     *
     * @param fav the "fav" value of type {@code boolean}
     */
    private void applyHeart(boolean fav) {
        isFavorite = fav;
        if (favoriteButton == null) return;
        favoriteButton.setText(fav ? "❤" : "♡");
        favoriteButton.setStyle(fav ? HEART_ON : HEART_OFF);
    }

    /**
     * Toggles favorite.
     */
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

    /**
     * Loads default image.
     */
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

    /**
     * Gets item.
     *
     * @return the resulting {@code Item} instance
     */
    public Item getItem() {
        return item;
    }

    /**
     * Handles card click.
     */
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
