package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class AdminController extends BaseController {
    @FXML private TabPane mainTabPane;
    @FXML private HBox titleBar;

    // تب آگهی‌های در انتظار
    @FXML private ListView<Item> pendingItemsListView;
    @FXML private Label pendingCountLabel;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button deleteButton;

    // تب مدیریت کاربران
    @FXML private ListView<User> usersListView;
    @FXML private Label usersCountLabel;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);

        if (!SessionManager.isAdmin()) {
            showError("شما دسترسی ادمین ندارید!");
            return;
        }

        setupCellFactories();
        setupClickHandlers();
        loadPendingItems();
        loadAllUsers();
    }

    private void setupCellFactories() {
        // نمایش خوانای آگهی‌های در انتظار
        pendingItemsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText("📦 " + item.getTitle()
                            + "\n💰 " + item.getFormattedPrice()
                            + "   👤 " + item.getOwnerUsername());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2937; -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });

        // ✅ نمایش نام کاربری کاربر به جای آدرس آبجکت دیتابیس
        usersListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String role = "ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "👤 کاربر";
                    String blocked = user.isBlocked() ? "   🔒 مسدود" : "";
                    setText(user.getUsername() + "  (" + user.getFullName() + ")\n" + role + blocked);
                    setStyle("-fx-background-color: transparent; -fx-text-fill: "
                            + (user.isBlocked() ? "#dc2626" : "white")
                            + "; -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });
    }

    private void setupClickHandlers() {
        // ✅ کلیک روی آگهی در انتظار ← نمایش جزئیات کامل آن
        pendingItemsListView.setOnMouseClicked(event -> {
            Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showItemDetailsDialog(selected);
            }
        });

        // ✅ کلیک روی کاربر ← رفتن به صفحه آگهی‌ها و مدیریت همان کاربر
        usersListView.setOnMouseClicked(event -> {
            User selected = usersListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                goToUserAdsPage(selected);
            }
        });
    }

    /**
     * نمایش جزئیات کامل یک آگهی در انتظار تایید (با تصاویر)
     */
    private void showItemDetailsDialog(Item item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("جزئیات آگهی");
        dialog.setHeaderText("📦 " + item.getTitle());
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: #ffffff;");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");
        content.setPrefWidth(500);
        content.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        String category = item.getCategoryName();
        content.getChildren().addAll(
                infoLabel("💰 قیمت: " + item.getFormattedPrice()),
                infoLabel("📂 دسته‌بندی: " + (category != null ? category : "-")),
                infoLabel("📍 شهر: " + item.getCityName()),
                infoLabel("👤 آگهی‌دهنده: " + item.getOwnerUsername()),
                infoLabel("📌 وضعیت: " + item.getPersianStatus()),
                new Separator(),
                infoLabel("📝 توضیحات:")
        );

        Label descLabel = new Label(item.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(460);
        descLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        content.getChildren().add(descLabel);

        // تصاویر آگهی
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            HBox imagesBox = new HBox(10);
            for (int i = 0; i < Math.min(item.getImages().size(), 4); i++) {
                try {
                    ImageView imageView = new ImageView(
                            new javafx.scene.image.Image(item.getImages().get(i).getFullUrl(), 110, 110, true, true, true));
                    imageView.setStyle("-fx-border-color: #cbd5e1;");
                    imagesBox.getChildren().add(imageView);
                } catch (Exception ignored) {
                }
            }
            content.getChildren().addAll(new Separator(), infoLabel("🖼️ تصاویر:"), imagesBox);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Label infoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px;");
        return label;
    }

    /**
     * رفتن به صفحه آگهی‌های یک کاربر (ثبت‌شده / فروخته‌شده / حذف‌شده)
     */
    private void goToUserAdsPage(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/admin_user_ads.fxml"));
            Parent root = loader.load();
            AdminUserAdListController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usersListView.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("آگهی‌های کاربر " + user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            showError("خطا در باز کردن صفحه کاربر: " + e.getMessage());
        }
    }

    private void loadPendingItems() {
        new Thread(() -> {
            try {
                List<Item> pendingItems = ItemService.getPendingItems();
                Platform.runLater(() -> {
                    pendingItemsListView.getItems().setAll(pendingItems);
                    pendingCountLabel.setText("📋 " + pendingItems.size() + " آگهی در انتظار بررسی");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در دریافت آگهی‌های در انتظار: " + e.getMessage()));
            }
        }).start();
    }

    private void loadAllUsers() {
        new Thread(() -> {
            try {
                List<User> users = UserService.getAllUsers();
                Platform.runLater(() -> {
                    usersListView.getItems().setAll(users);
                    usersCountLabel.setText("👥 " + users.size() + " کاربر");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در دریافت کاربران: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void approveItem() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک آگهی را انتخاب کنید");
            return;
        }
        new Thread(() -> {
            try {
                ItemService.approveItem(selected.getId());
                Platform.runLater(() -> {
                    showSuccess("✅ آگهی «" + selected.getTitle() + "» تایید شد");
                    loadPendingItems();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در تایید آگهی: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void rejectItem() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک آگهی را انتخاب کنید");
            return;
        }
        String reason = rejectionReasonArea.getText() != null ? rejectionReasonArea.getText().trim() : "";
        new Thread(() -> {
            try {
                ItemService.rejectItem(selected.getId(), reason);
                Platform.runLater(() -> {
                    rejectionReasonArea.clear();
                    showSuccess("❌ آگهی «" + selected.getTitle() + "» رد شد");
                    loadPendingItems();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در رد آگهی: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void deleteItemByAdmin() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک آگهی را انتخاب کنید");
            return;
        }
        new Thread(() -> {
            try {
                ItemService.deleteItem(selected.getId());
                Platform.runLater(() -> {
                    showSuccess("🗑️ آگهی «" + selected.getTitle() + "» حذف شد");
                    loadPendingItems();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در حذف آگهی: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void refreshAll() {
        loadPendingItems();
        loadAllUsers();
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطا");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("موفق");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        alert.getDialogPane().setStyle("-fx-background-color: #ffffff;");
    }
}
