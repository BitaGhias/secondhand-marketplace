package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AdItemController {

    @FXML private ImageView adImageView;
    @FXML private Label adTitleLabel;
    @FXML private Label adPriceLabel;
    @FXML private Label usernameLabel;
    @FXML private Label cityLabel;
    @FXML private Label ratingLabel;
    @FXML private Label statusLabel;

    private Item item;

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