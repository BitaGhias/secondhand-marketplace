package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Comment;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.Image;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.PurchaseRequest;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.service.CommentService;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.PurchaseRequestService;
import com.secondhand.frontend.service.RatingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.frontend.util.ApiClient;
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
import java.util.Objects;
import java.util.Optional;

/**
 * JavaFX controller of the "item detail" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
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
    @FXML private VBox   purchaseRequestsCard;
    @FXML private VBox   purchaseRequestsBox;
    @FXML private Label  purchaseRequestsCount;

    private Item   currentItem;
    private Long   currentUserId;
    private boolean isFavorite   = false;
    private static Long pendingItemId;
    /** Keeps the original item state while the user is editing the ad. */
    private Item   editingItem; // برای ذخیره حالت اصلی در ویرایش

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        currentUserId = SessionManager.getCurrentUserId();
        bindManaged(buyButton); bindManaged(chatButton);
        bindManaged(ratingButton); bindManaged(favoriteButton); bindManaged(addCommentBox);
        bindManaged(editButton); bindManaged(deleteButton); bindManaged(soldButton);
        if (ownerActions != null) ownerActions.managedProperty().bind(ownerActions.visibleProperty());
        if (buyerActions != null) buyerActions.managedProperty().bind(buyerActions.visibleProperty());
        if (purchaseRequestsCard != null) purchaseRequestsCard.managedProperty().bind(purchaseRequestsCard.visibleProperty());
        if (pendingItemId != null) { Long id = pendingItemId; pendingItemId = null; loadItemById(id); }
    }

    /**
     * Performs the "bind managed" operation.
     *
     * @param node the "node" value of type {@code Region}
     */
    private void bindManaged(Region node) {
        if (node != null) node.managedProperty().bind(node.visibleProperty());
    }

    /**
     * Sets item.
     *
     * @param item the ad (item) object
     */
    public void setItem(Item item) {
        this.currentItem = item;
        this.editingItem = item; // ذخیره برای مقایسه در ویرایش
        displayItemDetails();
        configureActions();
        checkFavoriteStatus();
        loadComments();
        loadPurchaseRequests();
        checkMyPendingRequest();
    }

    /**
     * Sets item id.
     *
     * @param id unique identifier of the record
     */
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

    /**
     * Displays item details.
     */
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
        javafx.scene.image.ImageView ownerAvatar = ImageLoaderUtil.circularAvatar(currentItem.getOwnerProfileImageUrl(), 24);
        if (ownerAvatar != null) {
            ownerLabel.setGraphic(ownerAvatar);
            ownerLabel.setGraphicTextGap(6);
            ownerLabel.setText(currentItem.getOwnerUsername());
        } else {
            ownerLabel.setGraphic(null);
            ownerLabel.setText("\uD83D\uDC64 " + currentItem.getOwnerUsername());
        }
        loadImages();
    }

    /**
     * Loads images.
     */
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

    /**
     * Performs the "configure actions" operation.
     */
    private void configureActions() {
        if (currentItem == null || currentUserId == null) return;
        boolean isOwner    = currentItem.isOwner(currentUserId);
        boolean isActive   = currentItem.isActive();
        boolean isBuyer    = currentItem.isPurchasedBy(currentUserId);
        boolean isLoggedIn = SessionManager.isLoggedIn();
        ownerActions.setVisible(isOwner);
        buyerActions.setVisible(!isOwner);
        if (isOwner) {
            editButton.setVisible(currentItem.isEditable());
            deleteButton.setVisible(currentItem.isDeletable());
            soldButton.setVisible(isActive);
        }
        if (!isOwner) {
            buyButton.setVisible(isActive);
            chatButton.setVisible(isActive);
            favoriteButton.setVisible(isActive);
            // دکمه امتیازدهی به فروشنده - نمایش اگر کاربر مجاز است (بک‌اند چک می‌کند)
            ratingButton.setVisible(isActive);
            if (isBuyer) checkRatingStatus();
        }
        if (addCommentBox != null) addCommentBox.setVisible(isLoggedIn && !isOwner);
    }

    /**
     * Checks rating status.
     */
    private void checkRatingStatus() {
        if (currentItem == null) return;
        RatingService.hasRatedAsync(currentItem.getId())
                .thenAccept(rated -> Platform.runLater(() -> {
                    if (rated) {
                        ratingButton.setDisable(true);
                        ratingButton.setText("\u2705 امتیاز ثبت شده");
                    } else {
                        ratingButton.setDisable(false);
                        ratingButton.setText("\u2B50 امتیازدهی به فروشنده");
                    }
                })).exceptionally(ex -> null);
    }

    /**
     * Checks favorite status.
     */
    private void checkFavoriteStatus() {
        if (currentItem == null || currentUserId == null || currentItem.isOwner(currentUserId)) return;
        new Thread(() -> {
            try {
                boolean fav = FavoriteService.isFavorite(currentItem.getId());
                Platform.runLater(() -> { isFavorite = fav; updateFavoriteButton(); });
            } catch (Exception e) { System.err.println("خطا در بررسی علاقه‌مندی: " + e.getMessage()); }
        }).start();
    }

    /**
     * Updates favorite button.
     */
    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setText("\u2764\uFE0F");
            favoriteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-font-size: 24px; -fx-cursor: hand;");
        } else {
            favoriteButton.setText("\uD83E\uDD0D");
            favoriteButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-font-size: 17px; -fx-cursor: hand; -fx-padding: 8 14;");
        }
    }

    /**
     * Toggles favorite.
     */
    @FXML
    private void toggleFavorite() {
        try {
            if (isFavorite) { FavoriteService.removeFavorite(currentItem.getId()); isFavorite = false; showMessage("آگهی از علاقه‌مندی‌ها حذف شد", "success"); }
            else             { FavoriteService.addFavorite(currentItem.getId());    isFavorite = true;  showMessage("آگهی به علاقه‌مندی‌ها اضافه شد", "success"); }
            updateFavoriteButton();
        } catch (Exception e) { showMessage("خطا: " + e.getMessage(), "error"); }
    }

    /**
     * Buys item.
     */
    @FXML
    private void buyItem() {
        if (currentItem == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("درخواست خرید");
        confirm.setHeaderText("درخواست خرید «" + currentItem.getTitle() + "»");
        confirm.setContentText("\ud83d\udcb0 قیمت: " + currentItem.getFormattedPrice()
                + "\n\ud83d\udc64 فروشنده: " + currentItem.getOwnerUsername()
                + "\n\nدرخواست شما برای فروشنده ارسال می‌شود و پس از تایید او، خرید قطعی می‌شود.");
        try { confirm.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        confirm.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        buyButton.setDisable(true);
        PurchaseRequestService.createAsync(currentItem.getId())
                .thenAccept(pr -> Platform.runLater(() -> {
                    setPendingRequestState();
                    showMessage("\ud83d\udce9 درخواست خرید شما برای فروشنده ارسال شد — نتیجه در بخش اعلان‌ها اطلاع داده می‌شود", "success");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> buyButton.setDisable(false));
                    showMessage("خطا در ثبت درخواست خرید: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error");
                    return null;
                });
    }

    /**
     * Checks my pending request.
     */
    private void checkMyPendingRequest() {
        if (currentItem == null || currentUserId == null || currentItem.isOwner(currentUserId)) return;
        PurchaseRequestService.mineAsync()
                .thenAccept(list -> Platform.runLater(() -> {
                    for (PurchaseRequest pr : list) {
                        if (pr.getItemId() != null && pr.getItemId().equals(currentItem.getId()) && pr.isPending()) {
                            setPendingRequestState();
                            return;
                        }
                    }
                })).exceptionally(ex -> null);
    }

    /**
     * Sets pending request state.
     */
    private void setPendingRequestState() {
        buyButton.setDisable(true);
        buyButton.setText("\u23f3 در ان��ظ��ر تایید فروشنده");
        buyButton.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 11 24; -fx-opacity: 1;");
    }

    /**
     * Loads purchase requests.
     */
    private void loadPurchaseRequests() {
        if (currentItem == null || currentUserId == null || !currentItem.isOwner(currentUserId)) return;
        if (purchaseRequestsCard == null) return;
        PurchaseRequestService.listForItemAsync(currentItem.getId())
                .thenAccept(list -> Platform.runLater(() -> renderPurchaseRequests(list)))
                .exceptionally(ex -> null);
    }

    /**
     * Performs the "render purchase requests" operation.
     *
     * @param list the "list" value of type {@code List<PurchaseRequest>}
     */
    private void renderPurchaseRequests(List<PurchaseRequest> list) {
        purchaseRequestsBox.getChildren().clear();
        purchaseRequestsCard.setVisible(list != null && !list.isEmpty());
        if (list == null || list.isEmpty()) return;
        long pending = list.stream().filter(PurchaseRequest::isPending).count();
        purchaseRequestsCount.setText(pending > 0 ? pending + " در انتظار تصمیم شما" : list.size() + " درخواست");
        for (PurchaseRequest pr : list) purchaseRequestsBox.getChildren().add(buildRequestRow(pr));
    }

    /**
     * Builds request row.
     *
     * @param pr the "pr" value of type {@code PurchaseRequest}
     * @return the resulting {@code HBox} instance
     */
    private HBox buildRequestRow(PurchaseRequest pr) {
        Label avatar = new Label("\ud83d\udc64");
        avatar.setStyle("-fx-background-color: #ffedd5; -fx-background-radius: 50; -fx-padding: 7 10; -fx-font-size: 14px;");
        Label name = new Label(pr.getBuyerFullName() != null && !pr.getBuyerFullName().isBlank() ? pr.getBuyerFullName() : pr.getBuyerUsername());
        name.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label sub = new Label("@" + pr.getBuyerUsername());
        sub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        VBox nameBox = new VBox(1, name, sub);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Button profileBtn = new Button("\ud83d\udc64 پروفایل خریدار");
        profileBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #143449; -fx-border-color: #cbd5e1; -fx-border-radius: 9; -fx-background-radius: 9; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 13;");
        profileBtn.setOnAction(e -> showBuyerProfileDialog(pr));

        HBox row = new HBox(10, avatar, nameBox, profileBtn);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-padding: 10 14;");

        if (pr.isPending()) {
            Button acceptBtn = new Button("\u2713 تایید فروش");
            acceptBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-background-radius: 9; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 14;");
            acceptBtn.setOnAction(e -> respondToRequest(pr, true));
            Button declineBtn = new Button("\u2715 رد");
            declineBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #dc2626; -fx-border-color: #fecaca; -fx-border-radius: 9; -fx-background-radius: 9; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 13;");
            declineBtn.setOnAction(e -> respondToRequest(pr, false));
            row.getChildren().addAll(acceptBtn, declineBtn);
        } else {
            Label st = new Label(pr.getPersianStatus());
            st.setStyle(pr.isAccepted()
                    ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 999; -fx-padding: 3 12; -fx-font-size: 10px; -fx-font-weight: bold;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 999; -fx-padding: 3 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            row.getChildren().add(st);
        }
        return row;
    }

    /**
     * Performs the "respond to request" operation.
     *
     * @param pr the "pr" value of type {@code PurchaseRequest}
     * @param accept the "accept" value of type {@code boolean}
     */
    private void respondToRequest(PurchaseRequest pr, boolean accept) {
        (accept ? PurchaseRequestService.acceptAsync(pr.getId()) : PurchaseRequestService.declineAsync(pr.getId()))
                .thenAccept(updated -> Platform.runLater(() -> {
                    if (accept) {
                        currentItem.setStatus("SOLD");
                        currentItem.setBuyerId(updated.getBuyerId());
                        statusLabel.setText(currentItem.getPersianStatus());
                        statusLabel.setStyle("-fx-text-fill: " + currentItem.getStatusColor() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
                        showMessage("\u2705 فروش تایید شد — کالا به «" + updated.getBuyerUsername() + "» فروخته شد", "success");
                    } else {
                        showMessage("درخواست خرید رد شد", "success");
                    }
                    loadPurchaseRequests();
                })).exceptionally(ex -> { showMessage("خطا: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
    }

    /**
     * Shows buyer profile dialog.
     *
     * @param pr the "pr" value of type {@code PurchaseRequest}
     */
    private void showBuyerProfileDialog(PurchaseRequest pr) {
        new Thread(() -> {
            String fullName = pr.getBuyerFullName();
            String phone = pr.getBuyerPhone();
            String email = pr.getBuyerEmail();
            String imagePath = null;
            try {
                java.net.http.HttpResponse<String> res = ApiClient.get("/auth/" + pr.getBuyerId());
                if (res.statusCode() >= 200 && res.statusCode() < 300) {
                    JsonNode node = ApiClient.getMapper().readTree(res.body());
                    if (node.hasNonNull("fullName")) fullName = node.get("fullName").asText();
                    if (node.hasNonNull("phoneNumber")) phone = node.get("phoneNumber").asText();
                    if (node.hasNonNull("email")) email = node.get("email").asText();
                    if (node.hasNonNull("profileImagePath")) imagePath = node.get("profileImagePath").asText();
                }
            } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
            final String fFull = fullName;
            final String fPhone = phone;
            final String fEmail = email;
            final String fImage = imagePath;
            Platform.runLater(() -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("پروفایل خریدار");
                styleDialog(dialog);
                DialogPane pane = dialog.getDialogPane();
                pane.setPrefWidth(420);

                javafx.scene.Node avatar = buildProfileAvatar(fImage, 46);
                Label nm = new Label(fFull != null && !fFull.isBlank() ? fFull : pr.getBuyerUsername());
                nm.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
                Label un = new Label("@" + pr.getBuyerUsername());
                un.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 11px;");
                VBox nameBox = new VBox(2, nm, un);
                HBox head = new HBox(12, avatar, nameBox);
                head.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                head.setStyle("-fx-background-color: linear-gradient(to left, #143449, #0e2433); -fx-background-radius: 14; -fx-padding: 14 16;");

                VBox info = new VBox(8,
                        profileRow("\ud83d\udcde تلفن", fPhone),
                        profileRow("\u2709 ایمیل", fEmail));

                VBox content = new VBox(12, head, info);
                content.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
                pane.setContent(content);
                pane.getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
            });
        }).start();
    }

    /**
     * Shows the seller-profile dialog including name, contact info and average rating with vote count.
     */
    @FXML
    private void showSellerProfile() {
        if (currentItem == null || currentItem.getOwnerId() == null) return;
        new Thread(() -> {
            String fullName = null, phone = null, email = null, imagePath = null;
            try {
                java.net.http.HttpResponse<String> res = ApiClient.get("/auth/" + currentItem.getOwnerId());
                if (res.statusCode() >= 200 && res.statusCode() < 300) {
                    JsonNode node = ApiClient.getMapper().readTree(res.body());
                    if (node.hasNonNull("fullName"))    fullName = node.get("fullName").asText();
                    if (node.hasNonNull("phoneNumber")) phone    = node.get("phoneNumber").asText();
                    if (node.hasNonNull("email"))       email    = node.get("email").asText();
                    if (node.hasNonNull("profileImagePath")) imagePath = node.get("profileImagePath").asText();
                }
            } catch (Exception e) { FrontendErrorHandler.log(e); }
            Double avg = null;
            Long ratingCount = null;
            try {
                avg = RatingService.getSellerAverageAsync(currentItem.getOwnerId()).join();
                ratingCount = RatingService.getSellerRatingCountAsync(currentItem.getOwnerId()).join();
            } catch (Exception e) { FrontendErrorHandler.log(e); }

            final String fRating = (avg == null || avg <= 0 || (ratingCount != null && ratingCount == 0))
                    ? "بدون امتیاز"
                    : String.format("%.1f", avg) + " ⭐ )" + ratingCount + " رأی)";
            final String fFull = fullName;
            final String fPhone = phone;
            final String fEmail = email;
            final String fImage = imagePath;
            Platform.runLater(() -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("پروفایل فروشنده");
                styleDialog(dialog);
                DialogPane pane = dialog.getDialogPane();
                pane.setPrefWidth(420);

                javafx.scene.Node avatar = buildProfileAvatar(fImage, 46);
                Label nm = new Label(fFull != null && !fFull.isBlank() ? fFull : currentItem.getOwnerUsername());
                nm.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
                Label un = new Label("@" + currentItem.getOwnerUsername());
                un.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 11px;");
                VBox nameBox = new VBox(2, nm, un);
                HBox head = new HBox(12, avatar, nameBox);
                head.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                head.setStyle("-fx-background-color: linear-gradient(to left, #143449, #0e2433); -fx-background-radius: 14; -fx-padding: 14 16;");

                VBox info = new VBox(8,
                        profileRow("⭐ امتیاز", fRating),
                        profileRow("\ud83d\udcde تلفن", fPhone),
                        profileRow("\u2709 ایمیل", fEmail));

                VBox content = new VBox(12, head, info);
                content.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
                pane.setContent(content);
                pane.getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
            });
        }).start();
    }

    /**
     * Builds a circular avatar node for profile dialogs: shows the user's real
     * profile image when available, otherwise falls back to a person-emoji placeholder.
     *
     * @param profileImagePath the raw profile image path returned by the backend (may be {@code null})
     * @param size the avatar diameter in pixels
     * @return an {@code ImageView} with the profile photo, or a placeholder {@code Label}
     */
    private javafx.scene.Node buildProfileAvatar(String profileImagePath, double size) {
        if (profileImagePath != null && !profileImagePath.isBlank()) {
            com.secondhand.frontend.model.User tmp = new com.secondhand.frontend.model.User();
            tmp.setProfileImagePath(profileImagePath);
            String url = tmp.getProfileImageUrl();
            if (url != null) {
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(url, size, size, false, true, true);
                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                    iv.setFitWidth(size);
                    iv.setFitHeight(size);
                    iv.setClip(new javafx.scene.shape.Circle(size / 2, size / 2, size / 2));
                    return iv;
                } catch (Exception e) { FrontendErrorHandler.log(e); }
            }
        }
        Label fallback = new Label("\ud83d\udc64");
        fallback.setStyle("-fx-background-color: rgba(249,115,22,0.22); -fx-background-radius: 50; -fx-padding: 12 16; -fx-font-size: 20px;");
        return fallback;
    }

    /**
     * Performs the "profile row" operation.
     *
     * @param caption the "caption" value of type {@code String}
     * @param value the "value" value of type {@code String}
     * @return the resulting {@code HBox} instance
     */
    private HBox profileRow(String caption, String value) {
        Label c = new Label(caption);
        c.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-min-width: 70;");
        Label v = new Label(value != null && !value.isBlank() ? value : "\u2014");
        v.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox row = new HBox(10, c, v);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-border-color: #e7ecf2; -fx-border-radius: 10; -fx-padding: 9 13;");
        return row;
    }

    /**
     * Starts chat.
     */
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
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("مسدود")) {
                        showAlert("🔒 " + msg, Alert.AlertType.WARNING);
                    } else {
                        showAlert("خطا در شروع گفت‌وگو: " + msg, Alert.AlertType.ERROR);
                    }
                });
            }
        }).start();
    }

    /**
     * Shows rating dialog.
     */
    @FXML
    private void showRatingDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("امتیازدهی به فروشنده"); dialog.setHeaderText("به فروشنده «" + currentItem.getOwnerUsername() + "» امتیاز دهید");
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
                    .thenAccept(v -> Platform.runLater(() -> { showMessage("امتیاز با موفقیت ثبت شد", "success"); ratingButton.setDisable(true); ratingButton.setText("\u2705 امتیاز ثبت شده"); }))
                    .exceptionally(ex -> { showMessage("خطا در ثبت امتیاز: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
        }
    }

    /**
     * Edits item.
     */
    @FXML
    private void editItem() {
        if (currentItem == null) return;
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

    /**
     * Deletes item.
     */
    @FXML
    private void deleteItem() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("حذف آگهی"); confirm.setHeaderText("آیا از حذف این آگهی اطمینان دارید؟");
        confirm.setContentText("این عمل قابل بازگشت نیست!");
        try { confirm.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        confirm.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ItemService.deleteItemAsync(currentItem.getId())
                    .thenAccept(v -> Platform.runLater(() -> { showMessage("آگهی با موفقیت حذف شد", "success"); goBack(); }))
                    .exceptionally(ex -> { showMessage("خطا در حذف آگهی: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error"); return null; });
        }
    }

    /**
     * Marks as sold.
     */
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

    /**
     * Loads comments.
     */
    private void loadComments() {
        if (currentItem == null) return;
        new Thread(() -> {
            try {
                List<Comment> comments = CommentService.getComments(currentItem.getId());
                Platform.runLater(() -> renderComments(comments));
            } catch (Exception e) { System.err.println("خطا در دریافت نظرات: " + e.getMessage()); }
        }).start();
    }

    /**
     * Performs the "render comments" operation.
     *
     * @param comments the "comments" value of type {@code List<Comment>}
     */
    private void renderComments(List<Comment> comments) {
        commentsListBox.getChildren().clear();
        if (comments == null || comments.isEmpty()) { noCommentsLabel.setVisible(true); commentCountLabel.setText("(0)"); return; }
        noCommentsLabel.setVisible(false); commentCountLabel.setText("(" + comments.size() + ")");
        for (Comment comment : comments) commentsListBox.getChildren().add(buildCommentCard(comment));
    }

    /**
     * Builds comment card.
     *
     * @param comment the comment object
     * @return the resulting {@code VBox} instance
     */
    private VBox buildCommentCard(Comment comment) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-padding: 12 16;");
        HBox header = new HBox(8); header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label usernameLabel = new Label("\uD83D\uDC64 " + comment.getUsername());
        usernameLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label dateLabel = new Label("🕐 " + comment.getShortTime() + "  " + comment.getShortDate());
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        // FIX (مورد ۴): نشانگر «ویرایش شده» برای کامنت‌هایی که بعد از ثبت تغییر داده شده‌اند
        Label editedTag = new Label("(ویرایش شده)");
        editedTag.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-style: italic;");
        editedTag.setVisible(comment.isEdited());
        editedTag.setManaged(comment.isEdited());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(usernameLabel, dateLabel, editedTag, spacer);
        boolean isMyComment = currentUserId != null && currentUserId.equals(comment.getUserId());
        Label textLabel = new Label(comment.getText()); textLabel.setWrapText(true);
        textLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
        // FIX: امکان ویرایش نظر خود کاربر - قبلاً فقط دکمه حذف وجود داشت، درحالی‌که بک‌اند از ویرایش نظر پشتیبانی می‌کند
        if (isMyComment) {
            Button editBtn = new Button("\u270F");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563eb; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 0 4;");
            Tooltip.install(editBtn, new Tooltip("ویرایش نظر"));
            editBtn.setOnAction(e -> startEditComment(comment, card, textLabel, editedTag));
            header.getChildren().add(editBtn);
        }
        if (isMyComment || SessionManager.isAdmin()) {
            Button deleteBtn = new Button("\uD83D\uDDD1");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 0 4;");
            Tooltip.install(deleteBtn, new Tooltip("حذف نظر"));
            deleteBtn.setOnAction(e -> deleteComment(comment.getId(), card));
            header.getChildren().add(deleteBtn);
        }
        card.getChildren().addAll(header, textLabel);
        return card;
    }

    // FIX: حالت ویرایش درون‌خطی برای نظر - از متد از قبل موجود CommentService.editComment استفاده می‌کند
    /**
     * Starts edit comment.
     *
     * @param comment the comment object
     * @param card the "card" value of type {@code VBox}
     * @param textLabel the "text label" value of type {@code Label}
     * @param editedTag the "edited tag" value of type {@code Label}
     */
    private void startEditComment(Comment comment, VBox card, Label textLabel, Label editedTag) {
        TextArea editArea = new TextArea(comment.getText());
        editArea.setWrapText(true);
        editArea.setPrefRowCount(2);
        editArea.setStyle("-fx-font-size: 13px;");

        Button saveBtn = new Button("ذخیره");
        saveBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 12;");
        Button cancelBtn = new Button("انصراف");
        cancelBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 12;");
        HBox editActions = new HBox(8, saveBtn, cancelBtn);

        int textIndex = card.getChildren().indexOf(textLabel);
        card.getChildren().set(textIndex, editArea);
        card.getChildren().add(editActions);

        cancelBtn.setOnAction(e -> {
            card.getChildren().remove(editActions);
            card.getChildren().set(card.getChildren().indexOf(editArea), textLabel);
        });

        saveBtn.setOnAction(e -> {
            String newText = editArea.getText() != null ? editArea.getText().trim() : "";
            if (newText.isEmpty()) { showMessage("متن نظر نمی‌تواند خالی باشد", "error"); return; }
            saveBtn.setDisable(true);
            CommentService.editComment(comment.getId(), newText)
                    .thenAccept(updated -> Platform.runLater(() -> {
                        comment.setText(updated.getText());
                        comment.setEdited(true); // FIX (مورد ۴)
                        textLabel.setText(updated.getText());
                        editedTag.setVisible(true);
                        editedTag.setManaged(true);
                        card.getChildren().remove(editActions);
                        card.getChildren().set(card.getChildren().indexOf(editArea), textLabel);
                        showMessage("نظر ویرایش شد", "success");
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            saveBtn.setDisable(false);
                            showMessage("خطا در ویرایش نظر: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), "error");
                        });
                        return null;
                    });
        });
    }

    /**
     * Submits comment.
     */
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

    /**
     * Deletes comment.
     *
     * @param commentId id of the comment
     * @param card the "card" value of type {@code VBox}
     */
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

    /**
     * Parses count.
     *
     * @param text the text value
     * @return the resulting numeric value
     */
    private int parseCount(String text) {
        if (text == null) return 0;
        try { return Integer.parseInt(text.replaceAll("[^0-9]", "")); } catch (NumberFormatException e) { return 0; }
    }

    /**
     * Navigates to back.
     */
    @FXML
    private void goBack() {
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }

    /**
     * Styles dialog.
     *
     * @param dialog the "dialog" value of type {@code Dialog<?>}
     */
    private void styleDialog(Dialog<?> dialog) {
        try { dialog.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        dialog.getDialogPane().setStyle("-fx-background-color: #ffffff;");
    }

    /**
     * Shows message.
     *
     * @param message the message text
     * @param type the "type" value of type {@code String}
     */
    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message); errorLabel.setVisible(true);
                errorLabel.setStyle("success".equals(type) ? "-fx-text-fill: #16a34a; -fx-font-size: 13px;" : "-fx-text-fill: #dc2626; -fx-font-size: 13px;");
            }
        });
    }
}