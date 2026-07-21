package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Comment;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.Image;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.service.CommentService;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.util.ImageLoaderUtil;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ItemDetailController extends BaseController {

    @FXML private javafx.scene.image.ImageView mainImageView;
    @FXML private HBox   thumbnailContainer;
    @FXML private Label  titleLabel;
    @FXML private Label  priceLabel;
    @FXML private Label  statusLabel;
    @FXML private Label  descriptionLabel;
    @FXML private Label  cityLabel;
    @FXML private Label  categoryLabel;
    @FXML private Label  ratingLabel;
    @FXML private Label  ownerLabel;
    @FXML private Label  errorLabel;
    @FXML private HBox   buyerActions;
    @FXML private Button buyButton;
    @FXML private Button chatButton;
    @FXML private Button favoriteButton;
    @FXML private Button ratingButton;
    @FXML private HBox   ownerActions;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button soldButton;
    @FXML private HBox   addCommentBox;
    @FXML private TextArea newCommentArea;
    @FXML private VBox   commentsListBox;
    @FXML private Label  commentCountLabel;
    @FXML private Label  noCommentsLabel;
    @FXML private HBox   titleBar;

    private Item   currentItem;
    private Long   currentUserId;
    private boolean isFavorite   = false;
    private static Long pendingItemId;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        currentUserId = SessionManager.getCurrentUserId();
        bindManaged(buyButton); bindManaged(chatButton);
        bindManaged(ratingButton); bindManaged(favoriteButton); bindManaged(addCommentBox);
        if (ownerActions != null) ownerActions.managedProperty().bind(ownerActions.visibleProperty());
        if (buyerActions != null) buyerActions.managedProperty().bind(buyerActions.visibleProperty());
        if (pendingItemId != null) { Long id = pendingItemId; pendingItemId = null; loadItemById(id); }
    }

    private void bindManaged(Region node) {
        if (node != null) node.managedProperty().bind(node.visibleProperty());
    }

    public void setItem(Item item) {
        this.currentItem = item;
        displayItemDetails();
        configureActions();
        checkFavoriteStatus();
        loadComments();
    }

    public static void setItemId(Long id) { pendingItemId = id; }

    private void loadItemById(Long id) {
        ItemService.getItemByIdAsync(id)
                .thenAccept(item -> Platform.runLater(() -> setItem(item)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (errorLabel != null) { errorLabel.setText("خطا در بارگذاری آگهی: " + ex.getMessage()); errorLabel.setVisible(true); }
                    });
                    return null;
                });
    }

    private void displayItemDetails() {
        if (currentItem == null) return;
        titleLabel.setText(currentItem.getTitle());
        priceLabel.setText(currentItem.getFormattedPrice());
        statusLabel.setText(currentItem.getPersianStatus());
        statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        descriptionLabel.setText(currentItem.getDescription());
        cityLabel.setText("\uD83D\uDCCD " + currentItem.getCityName());
        categoryLabel.setText("\uD83D\uDCC2 " + currentItem.getCategoryName());
        ratingLabel.setText(currentItem.getFormattedRating());
        ownerLabel.setText("\uD83D\uDC64 " + currentItem.getOwnerUsername());
        loadImages();
    }

    private void loadImages() {
        List<Image> images = currentItem.getImages();
        if (images != null && !images.isEmpty()) {
            ImageLoaderUtil.loadImageWithDefault(mainImageView, images.get(0).getFullUrl());
            thumbnailContainer.getChildren().clear();
            for (int i = 1; i < Math.min(images.size(), 5); i++) {
                javafx.scene.image.ImageView thumb = new javafx.scene.image.ImageView();
                thumb.setFitHeight(60); thumb.setFitWidth(60); thumb.setPreserveRatio(true);
                thumb.setStyle("-fx-cursor: hand; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");
                final String url = images.get(i).getFullUrl();
                ImageLoaderUtil.loadImageWithDefault(thumb, url);
                thumb.setOnMouseClicked(e -> ImageLoaderUtil.loadImageWithDefault(mainImageView, url));
                thumbnailContainer.getChildren().add(thumb);
            }
        } else { ImageLoaderUtil.loadDefaultImage(mainImageView); }
    }

    private void configureActions() {
        if (currentItem == null || currentUserId == null) return;
        boolean isOwner    = currentItem.isOwner(currentUserId);
        boolean isActive   = currentItem.isActive();
        boolean isBuyer    = currentItem.isPurchasedBy(currentUserId);
        boolean isLoggedIn = SessionManager.isLoggedIn();
        ownerActions.setVisible(isOwner);
        buyerActions.setVisible(!isOwner);
        if (!isOwner) {
            buyButton.setVisible(isActive);
            chatButton.setVisible(isActive);
            favoriteButton.setVisible(true);
            ratingButton.setVisible(isBuyer);
        }
        if (addCommentBox != null) addCommentBox.setVisible(isLoggedIn && !isOwner);
    }

    private void checkFavoriteStatus() {
        if (currentItem == null || currentUserId == null || currentItem.isOwner(currentUserId)) return;
        new Thread(() -> {
            try {
                boolean fav = FavoriteService.isFavorite(currentItem.getId());
                Platform.runLater(() -> { isFavorite = fav; updateFavoriteButton(); });
            } catch (Exception e) { System.err.println("خطا در بررسی علاقه‌مندی: " + e.getMessage()); }
        }).start();
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setText("\u2764\uFE0F");
            favoriteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-font-size: 24px; -fx-cursor: hand;");
        } else {
            favoriteButton.setText("\uD83E\uDD0D");
            favoriteButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-font-size: 17px; -fx-cursor: hand; -fx-padding: 8 14;");
        }
    }

    @FXML
    private void toggleFavorite() {
        try {
            if (isFavorite) { FavoriteService.removeFavorite(currentItem.getId()); isFavorite = false; showMessage("آگهی از علاقه‌مندی‌ها حذف شد", "success"); }
            else             { FavoriteService.addFavorite(currentItem.getId());    isFavorite = true;  showMessage("آگهی به علاقه‌مندی‌ها اضافه شد", "success"); }
            updateFavoriteButton();
        } catch (Exception e) { showMessage("خطا: " + e.getMessage(), "error"); }
    }

    @FXML
    private void buyItem() {
        if (currentItem == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("خرید کالا");
        dialog.setHeaderText("خرید «" + currentItem.getTitle() + "»");
        styleDialog(dialog);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");
        Label info = new Label("\uD83D\uDCB0 قیمت: " + currentItem.getFormattedPrice()
                + "\n\uD83D\uDC64 فروشنده: " + currentItem.getOwnerUsername()
                + "\n\nآیا از خرید این کالا اطمینان دارید؟");
        info.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px;"); info.setWrapText(true);

        CheckBox rateCheck = new CheckBox("همزمان با خرید، به فروشنده امتیاز می‌دهم");
        rateCheck.setSelected(true); rateCheck.setStyle("-fx-text-fill: #1f2937;");

        Label scoreLabel = new Label("امتیاز (۱ تا ۵):"); scoreLabel.setStyle("-fx-text-fill: #1f2937;");
        ComboBox<Integer> scoreComboBox = new ComboBox<>();
        scoreComboBox.getItems().addAll(1, 2, 3, 4, 5); scoreComboBox.setValue(5);
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر شما درباره فروشنده (اختیاری)..."); commentArea.setPrefHeight(70);

        VBox ratingBox = new VBox(8, scoreLabel, scoreComboBox, commentArea);
        ratingBox.visibleProperty().bind(rateCheck.selectedProperty());
        ratingBox.managedProperty().bind(rateCheck.selectedProperty());
        content.getChildren().addAll(info, new Separator(), rateCheck, ratingBox);
        dialog.getDialogPane().setContent(content);

        ButtonType buyType = new ButtonType("\u2705 خرید", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buyType, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != buyType) return;

        final boolean rateToo  = rateCheck.isSelected();
        final int score        = scoreComboBox.getValue() != null ? scoreComboBox.getValue() : 5;
        final String ratingMsg = commentArea.getText() != null ? commentArea.getText().trim() : "";

        ItemService.purchaseItemAsync(currentItem.getId())
                .thenCompose(v -> {
                    if (rateToo) return RatingService.rateSellerAsync(currentItem.getId(), score, ratingMsg).thenApply(r -> " و امتیاز شما ثبت شد");
                    return java.util.concurrent.CompletableFuture.completedFuture("");
                })
                .thenAccept(extra -> Platform.runLater(() -> {
                    currentItem.setStatus("SOLD"); currentItem.setBuyerId(currentUserId);
                    statusLabel.setText(currentItem.getPersianStatus());
                    statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
                    configureActions();
                    showMessage("\u2705 خرید با موفقیت انجام شد" + extra + " — در بخش «خریدها» قابل مشاهده است", "success");
                }))
                .exceptionally(ex -> { showMessage("خطا در خرید: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
    }

    @FXML
    private void startChat() {
        if (currentItem == null) return;
        new Thread(() -> {
            try {
                Conversation conversation = ChatService.startConversation(currentItem.getId(), null);
                ChatsController.setInitialConversationId(conversation.getId());
                Platform.runLater(() -> {
                    try { MainApplication.changeScene(Routes.CHATS, "گفت‌وگوها"); }
                    catch (Exception e) { showMessage("خطا در باز کردن صفحه چت: " + e.getMessage(), "error"); }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا در شروع گفت‌وگو: " + e.getMessage(), "error"));
            }
        }).start();
    }

    @FXML
    private void showRatingDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("امتیازدهی به فروشنده"); dialog.setHeaderText("به فروشنده امتیاز دهید");
        styleDialog(dialog);
        VBox content = new VBox(10); content.setStyle("-fx-padding: 20;");
        Label scoreLabel = new Label("امتیاز (1 تا 5):"); scoreLabel.setStyle("-fx-text-fill: #1f2937;");
        ComboBox<Integer> scoreComboBox = new ComboBox<>(); scoreComboBox.getItems().addAll(1, 2, 3, 4, 5); scoreComboBox.setValue(5);
        Label commentLabel = new Label("نظر (اختیاری):"); commentLabel.setStyle("-fx-text-fill: #1f2937;");
        TextArea commentArea = new TextArea(); commentArea.setPromptText("نظر خود را بنویسید..."); commentArea.setPrefHeight(80);
        content.getChildren().addAll(scoreLabel, scoreComboBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int score = scoreComboBox.getValue();
            String comment = commentArea.getText().trim();
            RatingService.rateSellerAsync(currentItem.getId(), score, comment)
                    .thenAccept(v -> showMessage("امتیاز با موفقیت ثبت شد", "success"))
                    .exceptionally(ex -> { showMessage("خطا در ثبت امتیاز: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
        }
    }

    @FXML
    private void editItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.CREATE_AD));
            Parent root = loader.load();
            CreateAdController controller = loader.getController();
            controller.setItemForEdit(currentItem);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("ویرایش آگهی");
        } catch (Exception e) { showMessage("خطا در بارگذاری صفحه ویرایش: " + e.getMessage(), "error"); }
    }

    @FXML
    private void deleteItem() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("حذف آگهی"); confirm.setHeaderText("آیا از حذف این آگهی اطمینان دارید؟");
        confirm.setContentText("این عمل قابل بازگشت نیست!");
        try { confirm.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) {}
        confirm.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ItemService.deleteItemAsync(currentItem.getId())
                    .thenAccept(v -> Platform.runLater(() -> { showMessage("آگهی با موفقیت حذف شد", "success"); goBack(); }))
                    .exceptionally(ex -> { showMessage("خطا در حذف آگهی: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
        }
    }

    @FXML
    private void markAsSold() {
        ItemService.markAsSoldAsync(currentItem.getId())
                .thenAccept(v -> Platform.runLater(() -> {
                    currentItem.setStatus("SOLD");
                    statusLabel.setText(currentItem.getPersianStatus());
                    statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
                    showMessage("وضعیت آگهی به فروخته شده تغییر کرد", "success");
                }))
                .exceptionally(ex -> { showMessage("خطا در تغییر وضعیت: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
    }

    private void loadComments() {
        if (currentItem == null) return;
        new Thread(() -> {
            try {
                List<Comment> comments = CommentService.getComments(currentItem.getId());
                Platform.runLater(() -> renderComments(comments));
            } catch (Exception e) { System.err.println("خطا در دریافت نظرات: " + e.getMessage()); }
        }).start();
    }

    private void renderComments(List<Comment> comments) {
        commentsListBox.getChildren().clear();
        if (comments == null || comments.isEmpty()) { noCommentsLabel.setVisible(true); commentCountLabel.setText("(0)"); return; }
        noCommentsLabel.setVisible(false); commentCountLabel.setText("(" + comments.size() + ")");
        for (Comment comment : comments) commentsListBox.getChildren().add(buildCommentCard(comment));
    }

    private VBox buildCommentCard(Comment comment) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-padding: 12 16;");
        HBox header = new HBox(8); header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label usernameLabel = new Label("\uD83D\uDC64 " + comment.getUsername());
        usernameLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label dateLabel = new Label(comment.getShortDate());
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(usernameLabel, dateLabel, spacer);
        boolean isMyComment = currentUserId != null && currentUserId.equals(comment.getUserId());
        if (isMyComment || SessionManager.isAdmin()) {
            Button deleteBtn = new Button("\uD83D\uDDD1");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 0 4;");
            Tooltip.install(deleteBtn, new Tooltip("حذف نظر"));
            deleteBtn.setOnAction(e -> deleteComment(comment.getId(), card));
            header.getChildren().add(deleteBtn);
        }
        Label textLabel = new Label(comment.getText()); textLabel.setWrapText(true);
        textLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
        card.getChildren().addAll(header, textLabel);
        return card;
    }

    @FXML
    private void submitComment() {
        if (currentItem == null || newCommentArea == null) return;
        String text = newCommentArea.getText() != null ? newCommentArea.getText().trim() : "";
        if (text.isEmpty()) { showMessage("لطفاً متن نظر را بنویسید", "error"); return; }
        CommentService.addComment(currentItem.getId(), text)
                .thenAccept(newComment -> Platform.runLater(() -> {
                    newCommentArea.clear();
                    commentsListBox.getChildren().add(0, buildCommentCard(newComment));
                    noCommentsLabel.setVisible(false);
                    int current = parseCount(commentCountLabel.getText());
                    commentCountLabel.setText("(" + (current + 1) + ")");
                    showMessage("نظر شما با موفقیت ثبت شد", "success");
                }))
                .exceptionally(ex -> { showMessage("خطا در ثبت نظر: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
    }

    private void deleteComment(Long commentId, VBox card) {
        CommentService.deleteComment(commentId)
                .thenRun(() -> Platform.runLater(() -> {
                    commentsListBox.getChildren().remove(card);
                    int updated = Math.max(0, parseCount(commentCountLabel.getText()) - 1);
                    commentCountLabel.setText("(" + updated + ")");
                    if (commentsListBox.getChildren().isEmpty()) noCommentsLabel.setVisible(true);
                    showMessage("نظر حذف شد", "success");
                }))
                .exceptionally(ex -> { showMessage("خطا در حذف نظر: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
    }

    private int parseCount(String text) {
        if (text == null) return 0;
        try { return Integer.parseInt(text.replaceAll("[^0-9]", "")); } catch (NumberFormatException e) { return 0; }
    }

    @FXML
    private void goBack() {
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void styleDialog(Dialog<?> dialog) {
        try { dialog.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) {}
        dialog.getDialogPane().setStyle("-fx-background-color: #ffffff;");
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message); errorLabel.setVisible(true);
                errorLabel.setStyle("success".equals(type) ? "-fx-text-fill: #16a34a; -fx-font-size: 13px;" : "-fx-text-fill: #dc2626; -fx-font-size: 13px;");
            }
        });
    }
}