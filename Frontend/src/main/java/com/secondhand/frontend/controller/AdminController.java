package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.UserService;
import com.secondhand.frontend.util.CategoryPicker;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AdminController extends BaseController {

    private static final String NO_PARENT_LABEL = "بدون والد (دستهٔ اصلی)";

    private static final String NAV_ACTIVE =
            "-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; "
                    + "-fx-background-radius: 10; -fx-padding: 11 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; "
                    + "-fx-effect: dropshadow(gaussian, rgba(249,115,22,0.35), 10, 0, 0, 2);";
    private static final String NAV_IDLE =
            "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.72); -fx-font-weight: bold; "
                    + "-fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 11 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    @FXML private HBox titleBar;

    // سایدبار
    @FXML private Button navDashboardButton;
    @FXML private Button navPendingButton;
    @FXML private Button navUsersButton;
    @FXML private Button navCategoriesButton;
    @FXML private Label  sidebarPendingBadge;

    // پنل‌های محتوا
    @FXML private VBox dashboardPane;
    @FXML private VBox pendingPane;
    @FXML private VBox usersPane;
    @FXML private VBox categoriesPane;

    // داشبورد
    @FXML private Label dashPendingLabel;
    @FXML private Label dashActiveLabel;
    @FXML private Label dashUsersLabel;
    @FXML private Label dashCategoriesLabel;

    // بررسی آگهی‌ها (جدول + اکشن اینلاین)
    @FXML private TableView<Item> pendingTable;
    @FXML private Label pendingCountLabel;
    @FXML private ComboBox<String> statusFilterComboBox;

    // کاربران
    @FXML private ListView<User> usersListView;
    @FXML private Label usersCountLabel;

    // دسته‌بندی‌ها
    @FXML private ListView<Category> categoriesListView;
    @FXML private Label      categoriesCountLabel;
    @FXML private TextField  categoryNameField;
    @FXML private MenuButton parentCategoryMenuButton;
    @FXML private Button     addCategoryButton;
    @FXML private Button     deleteCategoryButton;
    @FXML private Button     cancelCategoryEditButton;

    private Category selectedCategoryForEdit;
    private Category selectedParentCategory;
    private List<Category> allCategories = new ArrayList<>();

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        if (!SessionManager.isAdmin()) { showError("شما دسترسی ادمین ندارید!"); return; }
        if (statusFilterComboBox != null) {
            statusFilterComboBox.getItems().setAll("همه", "در انتظار", "تایید شده", "رد شده", "فروخته شده", "حذف شده");
            statusFilterComboBox.setValue("در انتظار");
            statusFilterComboBox.setOnAction(e -> loadPendingItems());
        }
        setupPendingTable();
        setupUsersList();
        setupCategoriesPane();
        refreshAll();
        showDashboard();
    }

    // ===================== ناوبری سایدبار =====================

    private void activate(Button active) {
        for (Button b : new Button[]{navDashboardButton, navPendingButton, navUsersButton, navCategoriesButton}) {
            if (b != null) b.setStyle(b == active ? NAV_ACTIVE : NAV_IDLE);
        }
    }

    private void showPane(VBox pane) {
        for (VBox p : new VBox[]{dashboardPane, pendingPane, usersPane, categoriesPane}) {
            if (p != null) { p.setVisible(p == pane); p.setManaged(p == pane); }
        }
    }

    @FXML private void showDashboard()  { showPane(dashboardPane);  activate(navDashboardButton); }
    @FXML private void showPending()    { showPane(pendingPane);    activate(navPendingButton); }
    @FXML private void showUsers()      { showPane(usersPane);      activate(navUsersButton); }
    @FXML private void showCategories() { showPane(categoriesPane); activate(navCategoriesButton); }

    // ===================== جدول آگهی‌های در انتظار =====================

    private void setupPendingTable() {
        if (pendingTable == null) return;

        pendingTable.getStyleClass().add("admin-pending-table");

        TableColumn<Item, String> titleCol = new TableColumn<>("عنوان آگهی");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(190);

        TableColumn<Item, String> ownerCol = new TableColumn<>("فروشنده");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        ownerCol.setPrefWidth(105);

        TableColumn<Item, String> priceCol = new TableColumn<>("قیمت");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("formattedPrice"));
        priceCol.setPrefWidth(120);

        TableColumn<Item, String> catCol = new TableColumn<>("دسته");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        catCol.setPrefWidth(105);

        TableColumn<Item, Void> actionsCol = new TableColumn<>("عملیات");
        actionsCol.setPrefWidth(215);
        actionsCol.setSortable(false);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button approve = new Button("✓ تایید");
            private final Button reject  = new Button("✕ رد");
            private final Button remove  = new Button("🗑");
            private final HBox box = new HBox(6, approve, reject, remove);
            {
                approve.getStyleClass().addAll("admin-action-btn", "approve");
                reject.getStyleClass().addAll("admin-action-btn", "reject");
                remove.getStyleClass().addAll("admin-action-btn", "delete");
                box.setAlignment(Pos.CENTER);
                approve.setOnAction(e -> { Item it = getTableView().getItems().get(getIndex()); approveItem(it); });
                reject.setOnAction(e ->  { Item it = getTableView().getItems().get(getIndex()); rejectItemWithPrompt(it); });
                remove.setOnAction(e ->  { Item it = getTableView().getItems().get(getIndex()); deleteItemByAdmin(it); });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        pendingTable.getColumns().setAll(List.of(titleCol, ownerCol, priceCol, catCol, actionsCol));
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        pendingTable.setPlaceholder(new Label("🎉 هیچ آگهی در انتظار بررسی نیست"));

        pendingTable.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showItemDetailsDialog(row.getItem());
            });
            return row;
        });
    }

    private void loadPendingItems() {
        new Thread(() -> {
            try {
                List<Item> pendingItems = ItemService.getPendingItems();
                Platform.runLater(() -> {
                    if (pendingTable != null) pendingTable.getItems().setAll(pendingItems);
                    if (pendingCountLabel != null) pendingCountLabel.setText(pendingItems.size() + " آگهی در انتظار بررسی");
                    if (dashPendingLabel != null) dashPendingLabel.setText(String.valueOf(pendingItems.size()));
                    if (sidebarPendingBadge != null) {
                        sidebarPendingBadge.setText(String.valueOf(pendingItems.size()));
                        sidebarPendingBadge.setVisible(!pendingItems.isEmpty());
                        sidebarPendingBadge.setManaged(!pendingItems.isEmpty());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در دریافت آگهی‌های در انتظار: " + e.getMessage()));
            }
        }).start();
    }

    private void loadActiveCount() {
        new Thread(() -> {
            try {
                List<Item> active = ItemService.getActiveItems();
                Platform.runLater(() -> {
                    if (dashActiveLabel != null) dashActiveLabel.setText(String.valueOf(active.size()));
                });
            } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        }).start();
    }

    private void approveItem(Item item) {
        if (item == null) return;
        new Thread(() -> {
            try {
                ItemService.approveItem(item.getId());
                Platform.runLater(() -> { showSuccess("✅ آگهی «" + item.getTitle() + "» تایید شد"); loadPendingItems(); loadActiveCount(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در تایید آگهی: " + e.getMessage()));
            }
        }).start();
    }

    private void rejectItemWithPrompt(Item item) {
        if (item == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("رد آگهی");
        dialog.setHeaderText("رد آگهی «" + item.getTitle() + "»");
        dialog.setContentText("دلیل رد (برای آگهی‌دهنده نمایش داده می‌شود):");
        dialog.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        String reason = result.get().trim();
        new Thread(() -> {
            try {
                ItemService.rejectItem(item.getId(), reason);
                Platform.runLater(() -> { showSuccess("❌ آگهی «" + item.getTitle() + "» رد شد"); loadPendingItems(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در رد آگهی: " + e.getMessage()));
            }
        }).start();
    }

    private void deleteItemByAdmin(Item item) {
        if (item == null) return;
        new Thread(() -> {
            try {
                ItemService.deleteItem(item.getId());
                Platform.runLater(() -> { showSuccess("🗑️ آگهی «" + item.getTitle() + "» حذف شد"); loadPendingItems(); });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در حذف آگهی: " + e.getMessage()));
            }
        }).start();
    }

    // ===================== کاربران =====================

    private void setupUsersList() {
        if (usersListView == null) return;

        usersListView.getStyleClass().add("admin-users-list-view");

        usersListView.setCellFactory(listView -> new ListCell<>() {
            private final Label nameLabel = new Label();
            private final Label metaLabel = new Label();
            private final VBox container = new VBox(2, nameLabel, metaLabel);
            private final HBox root = new HBox(8, container);

            {
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                container.setStyle("-fx-padding: 10 12; -fx-background-radius: 8;");
                root.setAlignment(Pos.CENTER_LEFT);
                setGraphic(root);
            }

            @Override protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("blocked");
                    return;
                }

                getStyleClass().remove("blocked");
                if (user.isBlocked()) getStyleClass().add("blocked");

                String role    = "ADMIN".equalsIgnoreCase(user.getRole()) ? "🛡️ ادمین" : "👤 کاربر";
                String blocked = user.isBlocked() ? "   🔒 مسدود" : "";

                nameLabel.setText(user.getUsername() + "  (" + user.getFullName() + ")");
                metaLabel.setText(role + blocked);

                setGraphic(root);
            }
        });

        usersListView.setOnMouseClicked(event -> {
            User selected = usersListView.getSelectionModel().getSelectedItem();
            if (selected != null) goToUserAdsPage(selected);
        });
    }

    private void loadAllUsers() {
        new Thread(() -> {
            try {
                List<User> users = UserService.getAllUsers();
                Platform.runLater(() -> {
                    if (usersListView != null) usersListView.getItems().setAll(users);
                    if (usersCountLabel != null) usersCountLabel.setText(users.size() + " کاربر");
                    if (dashUsersLabel != null) dashUsersLabel.setText(String.valueOf(users.size()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در دریافت کاربران: " + e.getMessage()));
            }
        }).start();
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
            FrontendErrorHandler.log(e);
            showError("خطا در باز کردن صفحه کاربر: " + e.getMessage());
        }
    }

    // ===================== دسته‌بندی‌ها =====================

    private void setupCategoriesPane() {
        if (categoriesListView == null) return;

        categoriesListView.getStyleClass().add("categories-list-view");

        categoriesListView.setCellFactory(listView -> new ListCell<>() {
            private final Label nameLabel = new Label();
            private final Label metaLabel = new Label();
            private final VBox container = new VBox(2, nameLabel, metaLabel);
            private final Region indent = new Region();
            private final HBox root = new HBox(8, indent, container);

            {
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                container.setStyle("-fx-padding: 10 12; -fx-background-radius: 8;");
                indent.setPrefWidth(24);
                indent.setMaxWidth(24);
                indent.setMinWidth(24);
                root.setAlignment(Pos.CENTER_LEFT);
                setGraphic(root);
            }

            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("editing", "subcategory");
                    return;
                }

                getStyleClass().removeAll("editing", "subcategory");
                getStyleClass().add("categories-list-cell");

                if (c.getParentId() != null) {
                    getStyleClass().add("subcategory");
                    indent.setVisible(true);
                    indent.setManaged(true);
                } else {
                    indent.setVisible(false);
                    indent.setManaged(false);
                }

                boolean isEditing = (selectedCategoryForEdit != null
                        && selectedCategoryForEdit.getId() != null
                        && selectedCategoryForEdit.getId().equals(c.getId()));
                if (isEditing) getStyleClass().add("editing");

                String parentInfo = (c.getParentName() != null && !c.getParentName().isBlank())
                        ? "زیرشاخهٔ «" + c.getParentName() + "»" : "دستهٔ اصلی";
                long count = c.getItemCount() != null ? c.getItemCount() : 0;

                nameLabel.setText("📂 " + c.getName() + (isEditing ? "  ✏️" : ""));
                metaLabel.setText(parentInfo + "  •  " + count + " آگهی");

                setGraphic(root);
            }
        });

        Tooltip deleteTooltip = new Tooltip("ابتدا یک دسته را از لیست انتخاب کنید");
        deleteTooltip.setStyle("-fx-font-size: 11px;");
        deleteCategoryButton.setTooltip(deleteTooltip);

        categoriesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            selectedCategoryForEdit = selected;
            if (selected == null) {
                resetCategoryForm();
                deleteCategoryButton.setDisable(true);
                deleteCategoryButton.setTooltip(new Tooltip("ابتدا یک دسته را از لیست انتخاب کنید"));
                return;
            }
            categoryNameField.setText(selected.getName());
            selectedParentCategory = findCategoryById(selected.getParentId());
            refreshParentMenu();
            addCategoryButton.setText("💾 ذخیره تغییرات");
            deleteCategoryButton.setDisable(false);
            deleteCategoryButton.setTooltip(new Tooltip("حذف دسته «" + selected.getName() + "»"));
            categoryNameField.requestFocus();
            categoryNameField.selectAll();
        });

        categoriesListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    Category selected = categoriesListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        categoryNameField.requestFocus();
                        categoryNameField.selectAll();
                    }
                }
                case DELETE -> {
                    Category selected = categoriesListView.getSelectionModel().getSelectedItem();
                    if (selected != null) deleteCategory();
                }
                case ESCAPE -> cancelCategoryEdit();
                default -> {}
            }
        });

        categoryNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.trim().isEmpty();
            addCategoryButton.setDisable(!hasText);
            if (hasText) {
                categoryNameField.setStyle("-fx-border-color: #dbe3ea;");
            } else {
                categoryNameField.setStyle("-fx-border-color: #fca5a5;");
            }
        });
        addCategoryButton.setDisable(true);

        categoryNameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addOrUpdateCategory();
            if (e.getCode() == KeyCode.ESCAPE) cancelCategoryEdit();
        });

        deleteCategoryButton.setDisable(true);
    }

    private void loadCategories() {
        if (categoriesListView == null) return;
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                Platform.runLater(() -> {
                    allCategories = categories;
                    categoriesListView.getItems().setAll(categories);
                    if (categoriesCountLabel != null) categoriesCountLabel.setText(categories.size() + " دسته‌بندی");
                    if (dashCategoriesLabel != null) dashCategoriesLabel.setText(String.valueOf(categories.size()));
                    refreshParentMenu();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("خطا در دریافت دسته‌بندی‌ها: " + e.getMessage()));
            }
        }).start();
    }

    private void refreshParentMenu() {
        if (parentCategoryMenuButton == null) return;

        Set<Long> excluded = new HashSet<>();
        if (selectedCategoryForEdit != null && selectedCategoryForEdit.getId() != null) {
            excluded.add(selectedCategoryForEdit.getId());
            boolean changed = true;
            int guard = 0;
            while (changed && guard++ < 16) {
                changed = false;
                for (Category c : allCategories) {
                    if (c.getId() != null && !excluded.contains(c.getId())
                            && c.getParentId() != null && excluded.contains(c.getParentId())) {
                        excluded.add(c.getId());
                        changed = true;
                    }
                }
            }
        }

        List<Category> options = new ArrayList<>();
        for (Category c : allCategories) {
            if (c.getId() == null || !excluded.contains(c.getId())) options.add(c);
        }

        CategoryPicker.populate(parentCategoryMenuButton, options, NO_PARENT_LABEL,
                cat -> selectedParentCategory = cat);

        String displayText = selectedParentCategory == null
                ? NO_PARENT_LABEL
                : "📂 " + CategoryPicker.displayName(selectedParentCategory);
        parentCategoryMenuButton.setText(displayText);

        if (selectedParentCategory != null) {
            parentCategoryMenuButton.setStyle("-fx-background-color: #fff1e6; -fx-background-radius: 10; -fx-border-color: #f97316; -fx-border-radius: 10; -fx-border-width: 1.5px; -fx-padding: 8 12; -fx-cursor: hand; -fx-text-fill: #0f172a;");
        } else {
            parentCategoryMenuButton.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #dbe3ea; -fx-border-radius: 10; -fx-padding: 8 12; -fx-cursor: hand; -fx-text-fill: #0f172a;");
        }
    }

    private Category findCategoryById(Long id) {
        if (id == null) return null;
        for (Category c : allCategories) {
            if (id.equals(c.getId())) return c;
        }
        return null;
    }

    @FXML
    private void addOrUpdateCategory() {
        String name = categoryNameField.getText() != null ? categoryNameField.getText().trim() : "";
        if (name.isEmpty()) { showError("نام دسته‌بندی را وارد کنید"); return; }
        Long parentId = selectedParentCategory != null ? selectedParentCategory.getId() : null;
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
        selectedParentCategory = null;
        categoryNameField.clear();
        categoryNameField.setStyle("-fx-border-color: #dbe3ea;");
        refreshParentMenu();
        if (parentCategoryMenuButton != null) {
            parentCategoryMenuButton.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #dbe3ea; -fx-border-radius: 10; -fx-padding: 8 12; -fx-cursor: hand; -fx-text-fill: #0f172a;");
        }
        addCategoryButton.setText("➕ افزودن دسته‌بندی");
        addCategoryButton.setDisable(true);
        deleteCategoryButton.setDisable(true);
    }

    private String rootMessage(Throwable e) {
        return e.getMessage() != null ? e.getMessage() : "خطای ناشناخته";
    }

    // ===================== دیالوگ جزئیات =====================

    private void showItemDetailsDialog(Item item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("جزئیات آگهی");
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource(Routes.STYLESHEET).toExternalForm());
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16;");
        pane.setPrefWidth(580);

        VBox content = new VBox(13);
        content.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        content.setStyle("-fx-padding: 4 2;");

        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        Label subLabel = new Label("👤 آگهی‌دهنده: " + item.getOwnerUsername());
        subLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 11px;");
        VBox headText = new VBox(4, titleLabel, subLabel);
        HBox.setHgrow(headText, Priority.ALWAYS);
        Label statusChip = new Label(item.getPersianStatus());
        statusChip.setStyle("-fx-background-color: #ffedd5; -fx-text-fill: #c2410c; -fx-background-radius: 999; -fx-padding: 4 14; -fx-font-size: 11px; -fx-font-weight: bold;");
        HBox header = new HBox(12, headText, statusChip);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to left, #143449, #0e2433); -fx-background-radius: 14; -fx-padding: 15 18;");

        Label priceCaption = new Label("قیمت پیشنهادی");
        priceCaption.setStyle("-fx-text-fill: #9a3412; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.setStyle("-fx-text-fill: #ea580c; -fx-font-size: 18px; -fx-font-weight: bold;");
        VBox priceBox = new VBox(2, priceCaption, priceLabel);
        priceBox.setStyle("-fx-background-color: #fff1e6; -fx-background-radius: 12; -fx-padding: 9 16; -fx-border-color: #fed7aa; -fx-border-radius: 12;");

        String category = item.getCategoryName();
        HBox chips = new HBox(8,
                detailChip("📂 " + (category != null ? category : "—")),
                detailChip("📍 " + (item.getCityName() != null ? item.getCityName() : "—")));
        chips.setAlignment(Pos.CENTER_LEFT);
        HBox metaRow = new HBox(12, priceBox, chips);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label descCaption = new Label("📝 توضیحات");
        descCaption.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label descLabel = new Label(item.getDescription() != null && !item.getDescription().isBlank() ? item.getDescription() : "—");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(510);
        descLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        VBox descBox = new VBox(6, descCaption, descLabel);
        descBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-padding: 11 14;");

        content.getChildren().addAll(header, metaRow, descBox);

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            HBox imagesBox = new HBox(10);
            imagesBox.setAlignment(Pos.CENTER_LEFT);
            for (int i = 0; i < Math.min(item.getImages().size(), 4); i++) {
                try {
                    ImageView imageView = new ImageView(
                            new javafx.scene.image.Image(item.getImages().get(i).getFullUrl(), 150, 105, true, true, true));
                    StackPane imgWrap = new StackPane(imageView);
                    imgWrap.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-border-color: #e7ecf2; -fx-border-radius: 10; -fx-padding: 3;");
                    imagesBox.getChildren().add(imgWrap);
                } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
            }
            Label imgCaption = new Label("🖼 تصاویر (" + item.getImages().size() + ")");
            imgCaption.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 12px; -fx-font-weight: bold;");
            VBox imgBox = new VBox(7, imgCaption, imagesBox);
            imgBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e7ecf2; -fx-border-radius: 12; -fx-padding: 11 14;");
            content.getChildren().add(imgBox);
        }

        if (item.getRejectionReason() != null && !item.getRejectionReason().isBlank()) {
            Label reason = new Label("⛔ علت رد: " + item.getRejectionReason());
            reason.setWrapText(true);
            reason.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-background-radius: 10; -fx-padding: 9 13; -fx-font-size: 12px;");
            content.getChildren().add(reason);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(520);
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        pane.setContent(scroll);

        pane.getButtonTypes().add(ButtonType.CLOSE);
        javafx.scene.Node closeBtn = pane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) closeBtn.setStyle("-fx-background-color: #143449; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-padding: 7 22; -fx-cursor: hand;");
        dialog.showAndWait();
    }

    private Label detailChip(String text) {
        Label chip = new Label(text);
        chip.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-background-radius: 999; -fx-padding: 6 13; -fx-font-size: 11px; -fx-font-weight: bold;");
        return chip;
    }

    private Label infoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px;");
        return label;
    }

    // ===================== عمومی =====================

    @FXML private void refreshAll() { loadPendingItems(); loadAllUsers(); loadCategories(); loadActiveCount(); }

    @FXML
    private void goBack() {
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}