package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.Routes;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminController extends BaseController {

    @FXML private TabPane mainTabPane;
    @FXML private HBox    titleBar;

    @FXML private ListView<Item> pendingItemsListView;
    @FXML private Label          pendingCountLabel;
    @FXML private TextArea       rejectionReasonArea;
    @FXML private Button         approveButton;
    @FXML private Button         rejectButton;
    @FXML private Button         deleteButton;

    @FXML private ListView<User> usersListView;
    @FXML private Label          usersCountLabel;

    @FXML private ListView<Category> categoriesListView;
    @FXML private Label              categoriesCountLabel;
    @FXML private TextField          categoryNameField;
    @FXML private ComboBox<Category> parentCategoryComboBox;
    @FXML private Button             addCategoryButton;
    @FXML private Button             deleteCategoryButton;
    @FXML private Button             cancelCategoryEditButton;

    private Category selectedCategoryForEdit;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        if (!SessionManager.isAdmin()) { showError("شما دسترسی ادمین ندارید!"); return; }
        setupCellFactories();
        setupClickHandlers();
        setupCategoriesTab();
        loadPendingItems();
        loadAllUsers();
        loadCategories();
    }

    private void setupCellFactories() {
        pendingItemsListView.setCellFactory(listView -> new ListCell<>() {
            @Override protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    setText("📦 " + item.getTitle()
                            + "\n💰 " + item.getFormattedPrice()
                            + "   👤 " + item.getOwnerUsername());
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2937; -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });

        usersListView.setCellFactory(listView -> new ListCell<>() {
            @Override protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) { setText(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    String role    = "ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "👤 کاربر";
                    String blocked = user.isBlocked() ? "   🔒 مسدود" : "";
                    setText(user.getUsername() + "  (" + user.getFullName() + ")\n" + role + blocked);
                    setStyle("-fx-background-color: transparent; -fx-text-fill: "
                            + (user.isBlocked() ? "#dc2626" : "#1f2937")
                            + "; -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });
    }

    private void setupClickHandlers() {
        pendingItemsListView.setOnMouseClicked(event -> {
            Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
            if (selected != null) showItemDetailsDialog(selected);
        });
        usersListView.setOnMouseClicked(event -> {
            User selected = usersListView.getSelectionModel().getSelectedItem();
            if (selected != null) goToUserAdsPage(selected);
        });
    }

    // ===================== مدیریت دسته‌بندی‌ها =====================

    private void setupCategoriesTab() {
        if (categoriesListView == null) return;

        categoriesListView.setCellFactory(listView -> new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    String parentInfo = (c.getParentName() != null && !c.getParentName().isBlank())
                            ? "   ← زیرشاخهٔ «" + c.getParentName() + "»" : "   (دستهٔ اصلی)";
                    long count = c.getItemCount() != null ? c.getItemCount() : 0;
                    setText("📂 " + c.getName() + parentInfo + "\n🗃 " + count + " آگهی");
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2937; -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });

        parentCategoryComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName());
            }
        });
        parentCategoryComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "بدون والد (دستهٔ اصلی)" : c.getName());
            }
        });

        categoriesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            selectedCategoryForEdit = selected;
            if (selected == null) { resetCategoryForm(); return; }
            categoryNameField.setText(selected.getName());
            Category parentMatch = null;
            for (Category c : parentCategoryComboBox.getItems()) {
                if (selected.getParentId() != null && selected.getParentId().equals(c.getId())) { parentMatch = c; break; }
            }
            parentCategoryComboBox.setValue(parentMatch);
            addCategoryButton.setText("💾 ذخیره تغییرات");
            deleteCategoryButton.setDisable(false);
        });

        deleteCategoryButton.setDisable(true);
    }

    private void loadCategories() {
        if (categoriesListView == null) return;
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                Platform.runLater(() -> {
                    Category previouslySelected = selectedCategoryForEdit;
                    categoriesListView.getItems().setAll(categories);
                    categoriesCountLabel.setText("📂 " + categories.size() + " دسته‌بندی");

                    List<Category> roots = new ArrayList<>();
                    for (Category c : categories) {
                        if (c.isRoot() && (previouslySelected == null || !previouslySelected.getId().equals(c.getId()))) {
                            roots.add(c);
                        }
                    }
                    parentCategoryComboBox.getItems().setAll(roots);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در دریافت دسته‌بندی‌ها: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void addOrUpdateCategory() {
        String name = categoryNameField.getText() != null ? categoryNameField.getText().trim() : "";
        if (name.isEmpty()) { showError("نام دسته‌بندی را وارد کنید"); return; }
        Category parent = parentCategoryComboBox.getValue();
        Long parentId = parent != null ? parent.getId() : null;
        Category editing = selectedCategoryForEdit;

        new Thread(() -> {
            try {
                if (editing != null) {
                    CategoryService.updateCategory(editing.getId(), name, parentId);
                    Platform.runLater(() -> { showSuccess("✅ دسته‌بندی «" + name + "» ویرایش شد"); resetCategoryForm(); loadCategories(); });
                } else {
                    CategoryService.createCategory(name, parentId);
                    Platform.runLater(() -> { showSuccess("✅ دسته‌بندی «" + name + "» اضافه شد"); resetCategoryForm(); loadCategories(); });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError(rootMessage(e)));
            }
        }).start();
    }

    @FXML
    private void deleteCategory() {
        Category selected = selectedCategoryForEdit;
        if (selected == null) { showError("لطفاً یک دسته‌بندی را انتخاب کنید"); return; }
        new Thread(() -> {
            try {
                CategoryService.deleteCategory(selected.getId());
                Platform.runLater(() -> { showSuccess("🗑️ دسته‌بندی حذف شد"); resetCategoryForm(); loadCategories(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError(rootMessage(e)));
            }
        }).start();
    }

    @FXML
    private void cancelCategoryEdit() {
        categoriesListView.getSelectionModel().clearSelection();
        resetCategoryForm();
    }

    private void resetCategoryForm() {
        selectedCategoryForEdit = null;
        categoryNameField.clear();
        parentCategoryComboBox.setValue(null);
        addCategoryButton.setText("➕ افزودن دسته‌بندی");
        deleteCategoryButton.setDisable(true);
    }

    private String rootMessage(Throwable e) {
        return e.getMessage() != null ? e.getMessage() : "خطای ناشناخته";
    }

    private void showItemDetailsDialog(Item item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("جزئیات آگهی");
        dialog.setHeaderText("📦 " + item.getTitle());
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource(Routes.STYLESHEET).toExternalForm());
        } catch (Exception ignored) {}
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

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            HBox imagesBox = new HBox(10);
            for (int i = 0; i < Math.min(item.getImages().size(), 4); i++) {
                try {
                    ImageView imageView = new ImageView(
                            new javafx.scene.image.Image(item.getImages().get(i).getFullUrl(), 110, 110, true, true, true));
                    imageView.setStyle("-fx-border-color: #cbd5e1;");
                    imagesBox.getChildren().add(imageView);
                } catch (Exception ignored) {}
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

    private void goToUserAdsPage(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.ADMIN_USER_ADS));
            Parent root = loader.load();
            AdminUserAdListController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usersListView.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());
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
        if (selected == null) { showError("لطفاً یک آگهی را انتخاب کنید"); return; }
        new Thread(() -> {
            try {
                ItemService.approveItem(selected.getId());
                Platform.runLater(() -> { showSuccess("✅ آگهی «" + selected.getTitle() + "» تایید شد"); loadPendingItems(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در تایید آگهی: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void rejectItem() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("لطفاً یک آگهی را انتخاب کنید"); return; }
        String reason = rejectionReasonArea.getText() != null ? rejectionReasonArea.getText().trim() : "";
        new Thread(() -> {
            try {
                ItemService.rejectItem(selected.getId(), reason);
                Platform.runLater(() -> { rejectionReasonArea.clear(); showSuccess("❌ آگهی «" + selected.getTitle() + "» رد شد"); loadPendingItems(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در رد آگهی: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void deleteItemByAdmin() {
        Item selected = pendingItemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("لطفاً یک آگهی را انتخاب کنید"); return; }
        new Thread(() -> {
            try {
                ItemService.deleteItem(selected.getId());
                Platform.runLater(() -> { showSuccess("🗑️ آگهی «" + selected.getTitle() + "» حذف شد"); loadPendingItems(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در حذف آگهی: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void refreshAll() { loadPendingItems(); loadAllUsers(); loadCategories(); }

    @FXML
    private void goBack() {
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { e.printStackTrace(); }
    }
}