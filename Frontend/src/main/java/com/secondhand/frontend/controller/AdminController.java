package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AdminController {

    @FXML private TabPane mainTabPane;

    // ===== تب آگهی‌ها =====
    @FXML private ListView<Item> pendingItemsListView;
    @FXML private Label pendingCountLabel;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button deleteButton;

    // ===== تب کاربران =====
    @FXML private ListView<User> usersListView;
    @FXML private Label usersCountLabel;
    @FXML private Button blockButton;
    @FXML private Button unblockButton;

    @FXML
    public void initialize() {
        // بررسی دسترسی ادمین
        if (!SessionManager.isAdmin()) {
            showError("شما دسترسی ادمین ندارید!");
            return;
        }

        loadPendingItems();
        loadAllUsers();
        setupListeners();
    }

    // ============================================
    // 📋 بخش آگهی‌های در انتظار بررسی
    // ============================================

    private void loadPendingItems() {
        try {
            List<Item> pendingItems = ItemService.getPendingItems();
            Platform.runLater(() -> {
                pendingItemsListView.getItems().setAll(pendingItems);
                if (pendingCountLabel != null) {
                    pendingCountLabel.setText("📋 " + pendingItems.size() + " آگهی در انتظار بررسی");
                }
            });
        } catch (Exception e) {
            showError("خطا در دریافت آگهی‌های در انتظار: " + e.getMessage());
        }
    }

    @FXML
    private void approveItem() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک آگهی را انتخاب کنید");
            return;
        }

        try {
            ItemService.approveItem(selected.getId());
            showSuccess("✅ آگهی با موفقیت تایید شد");
            loadPendingItems(); // Refresh
        } catch (Exception e) {
            showError("خطا در تایید آگهی: " + e.getMessage());
        }
    }

    @FXML
    private void rejectItem() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک آگهی را انتخاب کنید");
            return;
        }

        String reason = rejectionReasonArea != null ? rejectionReasonArea.getText().trim() : "";

        try {
            ItemService.rejectItem(selected.getId(), reason);
            showSuccess("❌ آگهی با موفقیت رد شد");
            if (rejectionReasonArea != null) {
                rejectionReasonArea.clear();
            }
            loadPendingItems(); // Refresh
        } catch (Exception e) {
            showError("خطا در رد آگهی: " + e.getMessage());
        }
    }

    @FXML
    private void deleteItemByAdmin() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک آگهی را انتخاب کنید");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("حذف آگهی");
        confirm.setHeaderText("آیا از حذف این آگهی اطمینان دارید؟");
        confirm.setContentText("این عمل قابل بازگشت نیست!");
        confirm.getDialogPane().setStyle("-fx-background-color: #14142b;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ItemService.deleteItem(selected.getId());
                showSuccess("🗑️ آگهی با موفقیت حذف شد");
                loadPendingItems(); // Refresh
            } catch (Exception e) {
                showError("خطا در حذف آگهی: " + e.getMessage());
            }
        }
    }

    // ============================================
    // 👤 بخش مدیریت کاربران
    // ============================================

    private void loadAllUsers() {
        try {
            List<User> users = UserService.getAllUsers();
            Platform.runLater(() -> {
                usersListView.getItems().setAll(users);
                if (usersCountLabel != null) {
                    usersCountLabel.setText("👥 " + users.size() + " کاربر");
                }
            });
        } catch (Exception e) {
            showError("خطا در دریافت کاربران: " + e.getMessage());
        }
    }

    @FXML
    private void blockUser() {
        User selected = usersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک کاربر را انتخاب کنید");
            return;
        }

        if (selected.isBlocked()) {
            showError("این کاربر قبلاً مسدود شده است");
            return;
        }

        if ("ADMIN".equalsIgnoreCase(selected.getRole())) {
            showError("❌ نمی‌توانید ادمین را مسدود کنید!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("مسدود کردن کاربر");
        confirm.setHeaderText("آیا از مسدود کردن " + selected.getFullName() + " اطمینان دارید؟");
        confirm.setContentText("این کاربر نمی‌تواند وارد سیستم شود.");
        confirm.getDialogPane().setStyle("-fx-background-color: #14142b;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserService.toggleBlock(selected.getId(), true);
                showSuccess("🔒 کاربر " + selected.getFullName() + " مسدود شد");
                loadAllUsers(); // Refresh
            } catch (Exception e) {
                showError("خطا در مسدود کردن کاربر: " + e.getMessage());
            }
        }
    }

    @FXML
    private void unblockUser() {
        User selected = usersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("لطفاً یک کاربر را انتخاب کنید");
            return;
        }

        if (!selected.isBlocked()) {
            showError("این کاربر مسدود نیست");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("فعال‌سازی کاربر");
        confirm.setHeaderText("آیا از فعال‌سازی " + selected.getFullName() + " اطمینان دارید؟");
        confirm.getDialogPane().setStyle("-fx-background-color: #14142b;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserService.toggleBlock(selected.getId(), false);
                showSuccess("🔓 کاربر " + selected.getFullName() + " فعال شد");
                loadAllUsers(); // Refresh
            } catch (Exception e) {
                showError("خطا در فعال‌سازی کاربر: " + e.getMessage());
            }
        }
    }

    // ============================================
    // 🔧 متدهای کمکی
    // ============================================

    private void setupListeners() {
        // نمایش جزئیات آگهی هنگام انتخاب
        pendingItemsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        System.out.println("📋 انتخاب: " + newVal.getTitle());
                    }
                }
        );

        // نمایش جزئیات کاربر هنگام انتخاب
        usersListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        System.out.println("👤 انتخاب: " + newVal.getFullName());
                    }
                }
        );
    }

    @FXML
    private void refreshAll() {
        loadPendingItems();
        loadAllUsers();
        showSuccess("🔄 همه لیست‌ها به‌روزرسانی شدند");
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "بازار سفید - لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("خطا");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().setStyle("-fx-background-color: #14142b;");
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("موفق");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().setStyle("-fx-background-color: #14142b;");
            alert.showAndWait();
        });
    }
}