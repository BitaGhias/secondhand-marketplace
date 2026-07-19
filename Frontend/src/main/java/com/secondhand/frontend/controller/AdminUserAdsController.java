package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.UserService;
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
 * صفحه ادمین برای یک کاربر خاص:
 * همه آگهی‌های ثبت‌شده، فروخته‌شده و حذف‌شده کاربر + مسدود/فعال‌سازی
 */
public class AdminUserAdsController extends BaseController {
    @FXML private HBox titleBar;
    @FXML private Label userTitleLabel;
    @FXML private Label userInfoLabel;
    @FXML private Label blockStatusLabel;
    @FXML private Button blockButton;
    @FXML private Button unblockButton;
    @FXML private Label postedCountLabel;
    @FXML private Label soldCountLabel;
    @FXML private Label deletedCountLabel;
    @FXML private FlowPane postedFlowPane;
    @FXML private FlowPane soldFlowPane;
    @FXML private FlowPane deletedFlowPane;
    @FXML private Label messageLabel;

    private User user;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        blockButton.managedProperty().bind(blockButton.visibleProperty());
        unblockButton.managedProperty().bind(unblockButton.visibleProperty());
    }

    public void setUser(User user) {
        this.user = user;
        renderUserInfo();
        loadUserAds();
    }

    private void renderUserInfo() {
        userTitleLabel.setText("👤 " + user.getUsername());

        String role = "ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "کاربر عادی";
        userInfoLabel.setText(safe(user.getFullName())
                + "  |  " + role
                + "  |  📧 " + safe(user.getEmail())
                + "  |  📞 " + safe(user.getPhoneNumber()));

        if (user.isBlocked()) {
            blockStatusLabel.setText("🔒 مسدود");
            blockStatusLabel.setStyle("-fx-text-fill: #ff4757; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            blockStatusLabel.setText("🟢 فعال");
            blockStatusLabel.setStyle("-fx-text-fill: #38ef7d; -fx-font-size: 14px; -fx-font-weight: bold;");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        blockButton.setVisible(!user.isBlocked() && !isAdmin);
        unblockButton.setVisible(user.isBlocked());
    }

    private String safe(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private void loadUserAds() {
        new Thread(() -> {
            try {
                List<Item> items = ItemService.getUserItemsForAdmin(user.getId());

                List<Item> posted = new ArrayList<>();
                List<Item> sold = new ArrayList<>();
                List<Item> deleted = new ArrayList<>();
                for (Item item : items) {
                    if (item.isSold()) {
                        sold.add(item);
                    } else if ("DELETED".equalsIgnoreCase(item.getStatus())) {
                        deleted.add(item);
                    } else {
                        posted.add(item);
                    }
                }

                Platform.runLater(() -> {
                    fillSection(postedFlowPane, postedCountLabel, posted, "این کاربر آگهی ثبت‌شده‌ای ندارد");
                    fillSection(soldFlowPane, soldCountLabel, sold, "این کاربر آگهی فروخته‌شده‌ای ندارد");
                    fillSection(deletedFlowPane, deletedCountLabel, deleted, "این کاربر آگهی حذف‌شده‌ای ندارد");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا در دریافت آگهی‌های کاربر: " + e.getMessage(), false));
            }
        }).start();
    }

    private void fillSection(FlowPane pane, Label countLabel, List<Item> items, String emptyText) {
        pane.getChildren().clear();
        countLabel.setText(items.size() + " مورد");

        if (items.isEmpty()) {
            Label empty = new Label(emptyText);
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 13px;");
            pane.getChildren().add(empty);
            return;
        }

        for (Item item : items) {
            pane.getChildren().add(buildAdCard(item));
        }
    }

    private VBox buildAdCard(Item item) {
        Label title = new Label(item.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        title.setWrapText(true);

        Label price = new Label(item.getFormattedPrice());
        price.setStyle("-fx-text-fill: #ff758c; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label status = new Label(item.getPersianStatus());
        status.setStyle("-fx-text-fill: " + item.getStatusColor() + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label meta = new Label("📂 " + safe(item.getCategoryName()) + "   📍 " + safe(item.getCityName()));
        meta.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");

        VBox card = new VBox(6, title, price, status, meta);
        card.setPrefWidth(290);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.04);"
                + " -fx-background-radius: 14;"
                + " -fx-border-color: rgba(255,255,255,0.08);"
                + " -fx-border-radius: 14;"
                + " -fx-padding: 14;");
        return card;
    }

    @FXML
    private void blockUser() {
        toggleBlock(true);
    }

    @FXML
    private void unblockUser() {
        toggleBlock(false);
    }

    private void toggleBlock(boolean block) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(block ? "مسدود کردن کاربر" : "فعال‌سازی کاربر");
        confirm.setHeaderText((block ? "آیا از مسدود کردن «" : "آیا از فعال‌سازی «")
                + user.getUsername() + "» اطمینان دارید؟");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        confirm.getDialogPane().setStyle("-fx-background-color: #1a1936;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        new Thread(() -> {
            try {
                User updated = UserService.toggleBlock(user.getId(), block);
                Platform.runLater(() -> {
                    user.setBlocked(updated.isBlocked());
                    renderUserInfo();
                    showMessage(block ? "🔒 کاربر مسدود شد" : "🔓 کاربر فعال شد", true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showMessage("خطا: " + e.getMessage(), false));
            }
        }).start();
    }

    private void showMessage(String text, boolean success) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (success ? "#38ef7d" : "#ff4757") + ";");
        messageLabel.setVisible(true);
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/admin_panel.fxml", "پنل مدیریت");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
