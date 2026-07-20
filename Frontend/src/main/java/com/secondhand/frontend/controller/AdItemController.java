package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Item;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ItemAdController {

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

        if (adTitleLabel != null) {
            adTitleLabel.setText(item.getTitle());
        }

        if (adPriceLabel != null) {
            adPriceLabel.setText(item.getFormattedPrice());
        }

        if (usernameLabel != null) {
            usernameLabel.setText(item.getOwnerUsername());
        }

        if (cityLabel != null) {
            cityLabel.setText(item.getCityName());
        }

        if (ratingLabel != null) {
            ratingLabel.setText(item.getFormattedRating());
        }

        if (statusLabel != null) {
            statusLabel.setText(item.getPersianStatus());
            statusLabel.setStyle("-fx-text-fill: " + item.getStatusColor() + ";");
        }

        // لود کردن تصویر آگهی یا تصویر پیش‌فرض
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
            String defaultImagePath = "/com/secondhand/frontend/images/default-item.png";
            var imageStream = getClass().getResourceAsStream(defaultImagePath);
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
            // رفتن به صفحه جزئیات آگهی
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_detail.fxml"));
            Parent root = loader.load();

            ItemDetailController controller = loader.getController();
            controller.setItem(item);

            Stage stage = (Stage) adTitleLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("جزئیات آگهی");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
