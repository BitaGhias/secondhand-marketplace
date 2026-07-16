package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.Image;  // ← مدل Image
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.RatingService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ItemDetailController {

    @FXML private javafx.scene.image.ImageView mainImageView;  // ← Fully Qualified
    @FXML private HBox thumbnailContainer;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label ratingLabel;
    @FXML private Label ownerLabel;
    @FXML private Button chatButton;
    @FXML private Button favoriteButton;
    @FXML private Button ratingButton;
    @FXML private HBox ownerActions;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button soldButton;
    @FXML private Label errorLabel;

    private Item currentItem;
    private Long currentUserId;
    private boolean isFavorite = false;

    @FXML
    public void initialize() {
        currentUserId = getCurrentUserId();
    }

    private Long getCurrentUserId() {
        try {
            return 1L;  // TODO: از توکن بگیر
        } catch (Exception e) {
            return null;
        }
    }

    public void setItem(Item item) {
        this.currentItem = item;
        displayItemDetails();
        checkOwnership();
        checkFavoriteStatus();
    }

    private void displayItemDetails() {
        if (currentItem == null) return;

        titleLabel.setText(currentItem.getTitle());
        priceLabel.setText(currentItem.getFormattedPrice());
        statusLabel.setText(currentItem.getPersianStatus());
        statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + ";");
        descriptionLabel.setText(currentItem.getDescription());
        cityLabel.setText("📍 " + currentItem.getCityName());
        categoryLabel.setText("📂 " + currentItem.getCategoryName());
        ratingLabel.setText(currentItem.getFormattedRating());
        ownerLabel.setText("👤 " + currentItem.getOwnerUsername());

        loadImages();
    }

    private void loadImages() {
        List<Image> images = currentItem.getImages();  // ← از مدل Image استفاده میکنه
        if (images != null && !images.isEmpty()) {
            String firstImageUrl = images.get(0).getFullUrl();
            loadImage(mainImageView, firstImageUrl);

            thumbnailContainer.getChildren().clear();
            for (int i = 1; i < Math.min(images.size(), 5); i++) {
                javafx.scene.image.ImageView thumb = new javafx.scene.image.ImageView();  // ← Fully Qualified
                thumb.setFitHeight(60);
                thumb.setFitWidth(60);
                thumb.setPreserveRatio(true);
                thumb.setStyle("-fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 8;");

                final String imageUrl = images.get(i).getFullUrl();
                loadImage(thumb, imageUrl);

                thumb.setOnMouseClicked(e -> loadImage(mainImageView, imageUrl));
                thumbnailContainer.getChildren().add(thumb);
            }
        } else {
            loadDefaultImage(mainImageView);
        }
    }

    private void loadImage(javafx.scene.image.ImageView imageView, String url) {  // ← Fully Qualified
        try {
            if (url != null && !url.isEmpty()) {
                imageView.setImage(new javafx.scene.image.Image(url, true));  // ← Fully Qualified
            } else {
                loadDefaultImage(imageView);
            }
        } catch (Exception e) {
            loadDefaultImage(imageView);
        }
    }

    private void loadDefaultImage(javafx.scene.image.ImageView imageView) {  // ← Fully Qualified
        try {
            String defaultPath = "/com/secondhand/frontend/images/default-item.png";
            var stream = getClass().getResourceAsStream(defaultPath);
            if (stream != null) {
                imageView.setImage(new javafx.scene.image.Image(stream));  // ← Fully Qualified
            }
        } catch (Exception e) {
            System.err.println("خطا در بارگذاری تصویر پیش‌فرض: " + e.getMessage());
        }
    }

    private void checkOwnership() {
        if (currentItem != null && currentUserId != null) {
            if (currentItem.isOwner(currentUserId)) {
                ownerActions.setVisible(true);
                chatButton.setVisible(false);
                ratingButton.setVisible(false);
                favoriteButton.setVisible(false);
            } else {
                ownerActions.setVisible(false);
                chatButton.setVisible(true);
                ratingButton.setVisible(true);
                favoriteButton.setVisible(true);
            }
        }
    }

    private void checkFavoriteStatus() {
        try {
            isFavorite = FavoriteService.isFavorite(currentItem.getId());
            updateFavoriteButton();
        } catch (Exception e) {
            System.err.println("خطا در بررسی علاقه‌مندی: " + e.getMessage());
        }
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setText("❤️");
            favoriteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4757; -fx-font-size: 24px; -fx-cursor: hand;");
        } else {
            favoriteButton.setText("🤍");
            favoriteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 24px; -fx-cursor: hand;");
        }
    }

    @FXML
    private void toggleFavorite() {
        try {
            if (isFavorite) {
                FavoriteService.removeFavorite(currentItem.getId());
                isFavorite = false;
                showMessage("آگهی از علاقه‌مندی‌ها حذف شد", "success");
            } else {
                FavoriteService.addFavorite(currentItem.getId());
                isFavorite = true;
                showMessage("آگهی به علاقه‌مندی‌ها اضافه شد", "success");
            }
            updateFavoriteButton();
        } catch (Exception e) {
            showMessage("خطا: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void startChat() {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("ارسال پیام");
            dialog.setHeaderText("پیام خود را به فروشنده بنویسید:");
            dialog.setContentText("متن پیام:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String message = result.get().trim();
                // TODO: ChatService.startConversation(currentItem.getId(), message);
                showMessage("پیام با موفقیت ارسال شد", "success");
            }
        } catch (Exception e) {
            showMessage("خطا در ارسال پیام: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void showRatingDialog() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("امتیازدهی به فروشنده");
            dialog.setHeaderText("به فروشنده امتیاز دهید");

            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");

            Label scoreLabel = new Label("امتیاز (1 تا 5):");
            scoreLabel.setStyle("-fx-text-fill: white;");

            ComboBox<Integer> scoreComboBox = new ComboBox<>();
            scoreComboBox.getItems().addAll(1, 2, 3, 4, 5);
            scoreComboBox.setValue(5);
            scoreComboBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white;");

            Label commentLabel = new Label("نظر (اختیاری):");
            commentLabel.setStyle("-fx-text-fill: white;");

            TextArea commentArea = new TextArea();
            commentArea.setPromptText("نظر خود را بنویسید...");
            commentArea.setPrefHeight(80);
            commentArea.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white;");

            content.getChildren().addAll(scoreLabel, scoreComboBox, commentLabel, commentArea);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setStyle("-fx-background-color: #14142b;");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 20;");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int score = scoreComboBox.getValue();
                String comment = commentArea.getText().trim();
                RatingService.rateSeller(currentItem.getId(), score, comment);
                showMessage("امتیاز با موفقیت ثبت شد", "success");
            }
        } catch (Exception e) {
            showMessage("خطا در ثبت امتیاز: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void editItem() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/create_ad.fxml", "ویرایش آگهی");
        } catch (Exception e) {
            showMessage("خطا در بارگذاری صفحه ویرایش: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void deleteItem() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("حذف آگهی");
        confirm.setHeaderText("آیا از حذف این آگهی اطمینان دارید؟");
        confirm.setContentText("این عمل قابل بازگشت نیست!");
        confirm.getDialogPane().setStyle("-fx-background-color: #14142b;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ItemService.deleteItem(currentItem.getId());
                showMessage("آگهی با موفقیت حذف شد", "success");
                goBack();
            } catch (Exception e) {
                showMessage("خطا در حذف آگهی: " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void markAsSold() {
        try {
            ItemService.updateItem(currentItem.getId(),
                    new ItemService.ItemUpdateRequest(
                            currentItem.getTitle(),
                            currentItem.getDescription(),
                            (long) currentItem.getPrice(),
                            currentItem.getCategoryId(),
                            currentItem.getCityId(),
                            "SOLD"
                    )
            );
            showMessage("وضعیت آگهی به فروخته شده تغییر کرد", "success");
            currentItem.setStatus("SOLD");
            statusLabel.setText(currentItem.getPersianStatus());
            statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + ";");
        } catch (Exception e) {
            showMessage("خطا در تغییر وضعیت: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setVisible(true);
                if ("success".equals(type)) {
                    errorLabel.setStyle("-fx-text-fill: #38ef7d; -fx-font-size: 13px;");
                } else {
                    errorLabel.setStyle("-fx-text-fill: #ff4757; -fx-font-size: 13px;");
                }
            }
        });
    }
}