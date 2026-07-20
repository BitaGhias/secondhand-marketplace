package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.Image;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ItemDetailController extends BaseController {
    @FXML private javafx.scene.image.ImageView mainImageView;
    @FXML private HBox thumbnailContainer;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label ratingLabel;
    @FXML private Label ownerLabel;
    @FXML private HBox buyerActions;
    @FXML private Button buyButton;
    @FXML private Button chatButton;
    @FXML private Button favoriteButton;
    @FXML private Button ratingButton;
    @FXML private HBox ownerActions;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button soldButton;
    @FXML private Label errorLabel;
    @FXML private HBox titleBar;

    private Item currentItem;
    private Long currentUserId;
    private boolean isFavorite = false;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        currentUserId = getCurrentUserId();

        // دکمه‌های مخفی نباید فضای صفحه را اشغال کنند
        bindManaged(buyButton);
        bindManaged(chatButton);
        bindManaged(ratingButton);
        bindManaged(favoriteButton);
        if (ownerActions != null) ownerActions.managedProperty().bind(ownerActions.visibleProperty());
        if (buyerActions != null) buyerActions.managedProperty().bind(buyerActions.visibleProperty());
    }

    private void bindManaged(Button button) {
        if (button != null) {
            button.managedProperty().bind(button.visibleProperty());
        }
    }

    private Long getCurrentUserId() {
        // ✅ شناسه کاربر جاری از SessionManager (بعد از لاگین پر می‌شود)
        return SessionManager.getCurrentUserId();
    }

    public void setItem(Item item) {
        this.currentItem = item;
        displayItemDetails();
        configureActions();
        checkFavoriteStatus();
    }

    private void displayItemDetails() {
        if (currentItem == null) return;

        titleLabel.setText(currentItem.getTitle());
        priceLabel.setText(currentItem.getFormattedPrice());
        statusLabel.setText(currentItem.getPersianStatus());
        statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        descriptionLabel.setText(currentItem.getDescription());
        cityLabel.setText("📍 " + currentItem.getCityName());
        categoryLabel.setText("📂 " + currentItem.getCategoryName());
        ratingLabel.setText(currentItem.getFormattedRating());
        ownerLabel.setText("👤 " + currentItem.getOwnerUsername());

        loadImages();
    }

    private void loadImages() {
        List<Image> images = currentItem.getImages();
        if (images != null && !images.isEmpty()) {
            String firstImageUrl = images.get(0).getFullUrl();
            loadImage(mainImageView, firstImageUrl);

            thumbnailContainer.getChildren().clear();
            for (int i = 1; i < Math.min(images.size(), 5); i++) {
                javafx.scene.image.ImageView thumb = new javafx.scene.image.ImageView();
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

    private void loadImage(javafx.scene.image.ImageView imageView, String url) {
        try {
            if (url != null && !url.isEmpty()) {
                imageView.setImage(new javafx.scene.image.Image(url, true));
            } else {
                loadDefaultImage(imageView);
            }
        } catch (Exception e) {
            loadDefaultImage(imageView);
        }
    }

    private void loadDefaultImage(javafx.scene.image.ImageView imageView) {
        try {
            String defaultPath = "/com/secondhand/frontend/images/default-item.png";
            var stream = getClass().getResourceAsStream(defaultPath);
            if (stream != null) {
                imageView.setImage(new javafx.scene.image.Image(stream));
            }
        } catch (Exception e) {
            System.err.println("خطا در بارگذاری تصویر پیش‌فرض: " + e.getMessage());
        }
    }

    /**
     * تعیین دکمه‌های قابل نمایش بر اساس نقش کاربر و وضعیت آگهی:
     * - مالک: دکمه‌های مدیریتی
     * - غیرمالک: خرید و چت (فقط برای آگهی فعال)، علاقه‌مندی،
     *   و امتیازدهی فقط اگر همین کالا را خریده باشد
     */
    private void configureActions() {
        if (currentItem == null || currentUserId == null) return;

        boolean owner = currentItem.isOwner(currentUserId);
        boolean approved = currentItem.isActive();
        boolean iAmBuyer = currentItem.isPurchasedBy(currentUserId);

        ownerActions.setVisible(owner);
        buyerActions.setVisible(!owner);

        if (!owner) {
            buyButton.setVisible(approved);
            chatButton.setVisible(approved);
            favoriteButton.setVisible(true);
            // ⭐ امتیازدهی فقط بعد از خرید همین کالا مجاز است
            ratingButton.setVisible(iAmBuyer);
        }
    }

    private void checkFavoriteStatus() {
        // فقط برای غیرمالک معنا دارد
        if (currentItem == null || currentUserId == null || currentItem.isOwner(currentUserId)) return;
        new Thread(() -> {
            try {
                boolean fav = FavoriteService.isFavorite(currentItem.getId());
                Platform.runLater(() -> {
                    isFavorite = fav;
                    updateFavoriteButton();
                });
            } catch (Exception e) {
                System.err.println("خطا در بررسی علاقه‌مندی: " + e.getMessage());
            }
        }).start();
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

    /**
     * 🛒 خرید کالا + امکان امتیازدهی همزمان به فروشنده (در حین خرید)
     */
    @FXML
    private void buyItem() {
        if (currentItem == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("خرید کالا");
        dialog.setHeaderText("خرید «" + currentItem.getTitle() + "»");
        styleDialog(dialog);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");

        Label info = new Label("💰 قیمت: " + currentItem.getFormattedPrice()
                + "\n👤 فروشنده: " + currentItem.getOwnerUsername()
                + "\n\nآیا از خرید این کالا اطمینان دارید؟");
        info.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        info.setWrapText(true);

        CheckBox rateCheck = new CheckBox("همزمان با خرید، به فروشنده امتیاز می‌دهم");
        rateCheck.setSelected(true);
        rateCheck.setStyle("-fx-text-fill: white;");

        Label scoreLabel = new Label("امتیاز (۱ تا ۵):");
        scoreLabel.setStyle("-fx-text-fill: white;");

        ComboBox<Integer> scoreComboBox = new ComboBox<>();
        scoreComboBox.getItems().addAll(1, 2, 3, 4, 5);
        scoreComboBox.setValue(5);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر شما درباره فروشنده (اختیاری)...");
        commentArea.setPrefHeight(70);

        VBox ratingBox = new VBox(8, scoreLabel, scoreComboBox, commentArea);
        ratingBox.visibleProperty().bind(rateCheck.selectedProperty());
        ratingBox.managedProperty().bind(rateCheck.selectedProperty());

        content.getChildren().addAll(info, new Separator(), rateCheck, ratingBox);
        dialog.getDialogPane().setContent(content);

        ButtonType buyType = new ButtonType("✅ خرید", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buyType, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != buyType) return;

        final boolean rateToo = rateCheck.isSelected();
        final int score = scoreComboBox.getValue() != null ? scoreComboBox.getValue() : 5;
        final String comment = commentArea.getText() != null ? commentArea.getText().trim() : "";

        new Thread(() -> {
            try {
                ItemService.purchaseItem(currentItem.getId());

                String ratingMsg = "";
                if (rateToo) {
                    try {
                        RatingService.rateSeller(currentItem.getId(), score, comment);
                        ratingMsg = " و امتیاز شما ثبت شد";
                    } catch (Exception ratingError) {
                        ratingMsg = " (ثبت امتیاز ناموفق بود: " + ratingError.getMessage() + ")";
                    }
                }

                final String msg = "✅ خرید با موفقیت انجام شد" + ratingMsg + " — در بخش «خریدها» قابل مشاهده است";
                Platform.runLater(() -> {
                    currentItem.setStatus("SOLD");
                    currentItem.setBuyerId(currentUserId);
                    statusLabel.setText(currentItem.getPersianStatus());
                    statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
                    configureActions();
                    showMessage(msg, "success");
                });
            } catch (Exception e) {
                showMessage("خطا در خرید: " + e.getMessage(), "error");
            }
        }).start();
    }

    /**
     * 💬 شروع/باز کردن گفت‌وگو با فروشنده و رفتن مستقیم به صفحه چت
     */
    @FXML
    private void startChat() {
        if (currentItem == null) return;
        new Thread(() -> {
            try {
                // اگر گفت‌وگو از قبل وجود داشته باشد، بک‌اند همان را برمی‌گرداند
                Conversation conversation = ChatService.startConversation(currentItem.getId(), null);
                ChatsController.setInitialConversationId(conversation.getId());
                Platform.runLater(() -> {
                    try {
                        MainApplication.changeScene("/com/secondhand/frontend/chats.fxml", "گفت‌وگوها");
                    } catch (Exception e) {
                        showMessage("خطا در باز کردن صفحه چت: " + e.getMessage(), "error");
                    }
                });
            } catch (Exception e) {
                showMessage("خطا در شروع گفت‌وگو: " + e.getMessage(), "error");
            }
        }).start();
    }

    @FXML
    private void showRatingDialog() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("امتیازدهی به فروشنده");
            dialog.setHeaderText("به فروشنده امتیاز دهید");
            styleDialog(dialog);

            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");

            Label scoreLabel = new Label("امتیاز (1 تا 5):");
            scoreLabel.setStyle("-fx-text-fill: white;");

            ComboBox<Integer> scoreComboBox = new ComboBox<>();
            scoreComboBox.getItems().addAll(1, 2, 3, 4, 5);
            scoreComboBox.setValue(5);

            Label commentLabel = new Label("نظر (اختیاری):");
            commentLabel.setStyle("-fx-text-fill: white;");

            TextArea commentArea = new TextArea();
            commentArea.setPromptText("نظر خود را بنویسید...");
            commentArea.setPrefHeight(80);

            content.getChildren().addAll(scoreLabel, scoreComboBox, commentLabel, commentArea);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int score = scoreComboBox.getValue();
                String comment = commentArea.getText().trim();

                // 🟢 انتقال عملیات شبکه به ترد پس‌زمینه برای جلوگیری از فریز شدن UI
                new Thread(() -> {
                    try {
                        RatingService.rateSeller(currentItem.getId(), score, comment);
                        showMessage("امتیاز با موفقیت ثبت شد", "success");
                    } catch (Exception e) {
                        showMessage("خطا در ثبت امتیاز: " + e.getMessage(), "error");
                    }
                }).start();
            }
        } catch (Exception e) {
            showMessage("خطا در بارگذاری دیالوگ: " + e.getMessage(), "error");
        }
    }

    private void styleDialog(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1936;");
    }

    @FXML
    private void editItem() {
        try {
            // ✅ لود صفحه ویرایش و پاس دادن آگهی جاری به آن (حالت ویرایش)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/create_ad.fxml"));
            Parent root = loader.load();
            CreateAdController controller = loader.getController();
            controller.setItemForEdit(currentItem);

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("ویرایش آگهی");
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
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        confirm.getDialogPane().setStyle("-fx-background-color: #1a1936;");

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
            // ✅ مسیر درست بک‌اند: PUT /api/items/{id}/sold
            ItemService.markAsSold(currentItem.getId());
            showMessage("وضعیت آگهی به فروخته شده تغییر کرد", "success");
            currentItem.setStatus("SOLD");
            statusLabel.setText(currentItem.getPersianStatus());
            statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
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
