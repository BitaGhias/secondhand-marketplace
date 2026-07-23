package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX controller of the admin page that shows a selected user ads history split into registered, sold, deleted and rejected sections.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class AdminUserAdListController extends BaseController {

    @FXML private HBox    titleBar;
    @FXML private Label   userTitleLabel;
    @FXML private Label   userInfoLabel;
    @FXML private Label   blockStatusLabel;
    @FXML private Button  blockButton;
    @FXML private Button  unblockButton;
    @FXML private Label   postedCountLabel;
    @FXML private Label   soldCountLabel;
    @FXML private Label   deletedCountLabel;
    @FXML private Label   rejectedCountLabel;
    @FXML private FlowPane postedFlowPane;
    @FXML private FlowPane soldFlowPane;
    @FXML private FlowPane deletedFlowPane;
    @FXML private FlowPane rejectedFlowPane;
    @FXML private Label   messageLabel;

    private User user;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        blockButton.managedProperty().bind(blockButton.visibleProperty());
        unblockButton.managedProperty().bind(unblockButton.visibleProperty());
    }

    /**
     * Sets user.
     *
     * @param user the user object
     */
    public void setUser(User user) {
        this.user = user;
        renderUserInfo();
        loadUserAds();
    }

    /**
     * Performs the "render user info" operation.
     */
    private void renderUserInfo() {
        userTitleLabel.setText("👤 " + user.getUsername());
        String role = "ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "کاربر عادی";
        userInfoLabel.setText(safe(user.getFullName())
                + "  |  " + role
                + "  |  📧 " + safe(user.getEmail())
                + "  |  📞 " + safe(user.getPhoneNumber()));

        if (user.isBlocked()) {
            blockStatusLabel.setText("🔒 مسدود");
            blockStatusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            blockStatusLabel.setText("🟢 فعال");
            blockStatusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 14px; -fx-font-weight: bold;");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        blockButton.setVisible(!user.isBlocked() && !isAdmin);
        unblockButton.setVisible(user.isBlocked());
    }

    /**
     * Performs the "safe" operation.
     *
     * @param value the "value" value of type {@code String}
     * @return the resulting string
     */
    private String safe(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    /**
     * Loads the ads of the selected user and splits them into four sections: registered, sold, deleted and rejected.
     */
    private void loadUserAds() {
        ItemService.getUserItemsForAdminAsync(user.getId())
                .thenAccept(items -> {
                    List<Item> posted  = new ArrayList<>();
                    List<Item> sold    = new ArrayList<>();
                    List<Item> deleted = new ArrayList<>();
                    List<Item> rejected = new ArrayList<>();
                    for (Item item : items) {
                        if (item.isSold()) sold.add(item);
                        else if ("DELETED".equalsIgnoreCase(item.getStatus())) deleted.add(item);
                        else if ("REJECTED".equalsIgnoreCase(item.getStatus())) rejected.add(item);
                        else posted.add(item);
                    }
                    Platform.runLater(() -> {
                        fillSection(postedFlowPane,  postedCountLabel,  posted,  "این کاربر آگهی ثبت‌شده‌ای ندارد");
                        fillSection(soldFlowPane,    soldCountLabel,    sold,    "این کاربر آگهی فروخته‌شده‌ای ندارد");
                        fillSection(deletedFlowPane, deletedCountLabel, deleted, "این کاربر آگهی حذف‌شده‌ای ندارد");
                        fillSection(rejectedFlowPane, rejectedCountLabel, rejected, "این کاربر آگهی ردشده‌ای ندارد");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showMessage("خطا در دریافت آگهی‌ها: " + ex.getMessage(), false));
                    return null;
                });
    }

    /**
     * Fills section.
     *
     * @param pane the "pane" value of type {@code FlowPane}
     * @param countLabel the "count label" value of type {@code Label}
     * @param items the "items" value of type {@code List<Item>}
     * @param emptyText the "empty text" value of type {@code String}
     */
    private void fillSection(FlowPane pane, Label countLabel, List<Item> items, String emptyText) {
        pane.getChildren().clear();
        countLabel.setText(items.size() + " مورد");
        if (items.isEmpty()) {
            Label empty = new Label(emptyText);
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            pane.getChildren().add(empty);
            return;
        }
        for (Item item : items) pane.getChildren().add(buildAdCard(item));
    }

    /**
     * Builds ad card.
     *
     * @param item the ad (item) object
     * @return the resulting {@code VBox} instance
     */
    private VBox buildAdCard(Item item) {
        Label title  = new Label(item.getTitle());
        title.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label price  = new Label(item.getFormattedPrice());
        price.setStyle("-fx-text-fill: #f97316; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label status = new Label(item.getPersianStatus());
        status.setStyle("-fx-text-fill: " + item.getStatusColor() + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label meta   = new Label("📂 " + safe(item.getCategoryName()) + "   📍 " + safe(item.getCityName()));
        meta.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        VBox card = new VBox(6, title, price, status, meta);
        card.setPrefWidth(290);
        // استفاده از CSS class به جای inline style
        card.getStyleClass().add("admin-ad-card");
        return card;
    }

    @FXML private void blockUser()   { toggleBlock(true);  }
    @FXML private void unblockUser() { toggleBlock(false); }

    /**
     * Toggles block.
     *
     * @param block the "block" value of type {@code boolean}
     */
    private void toggleBlock(boolean block) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(block ? "مسدود کردن کاربر" : "فعال‌سازی کاربر");
        confirm.setHeaderText((block ? "آیا از مسدود کردن «" : "آیا از فعال‌سازی «") + user.getUsername() + "» اطمینان دارید؟");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        UserService.toggleBlockAsync(user.getId(), block)
                .thenAccept(updated -> Platform.runLater(() -> {
                    user.setBlocked(updated != null ? updated.isBlocked() : block);
                    renderUserInfo();
                    showMessage(block ? "🔒 کاربر مسدود شد" : "🔓 کاربر فعال شد", true);
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
        try { MainApplication.changeScene(Routes.ADMIN_PANEL, "پنل مدیریت"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}