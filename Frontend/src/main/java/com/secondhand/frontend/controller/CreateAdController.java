package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateAdController extends BaseController {

    @FXML private TextField  titleField;
    @FXML private TextArea   descriptionArea;
    @FXML private TextField  priceField;
    @FXML private MenuButton categoryMenuButton;
    @FXML private ComboBox<City> cityComboBox;
    @FXML private FlowPane   imagePreviewContainer;
    @FXML private Button     submitButton;
    @FXML private Label      errorLabel;
    @FXML private HBox       titleBar;

    private List<String>   imagePaths      = new ArrayList<>();
    private List<Category> allCategories   = new ArrayList<>();
    private Category       selectedCategory;
    private Item           editingItem;
    private boolean        isEditMode      = false;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadCategories();
        loadCities();
    }

    public void setItemForEdit(Item item) {
        this.editingItem = item;
        this.isEditMode  = true;
        submitButton.setText("💾 ذخیره تغییرات");
        Platform.runLater(this::fillFormWithItemData);
    }

    private void fillFormWithItemData() {
        if (editingItem == null) return;
        titleField.setText(editingItem.getTitle());
        descriptionArea.setText(editingItem.getDescription());
        priceField.setText(String.valueOf((long) editingItem.getPrice()));
        applyPendingCategorySelection();
        if (editingItem.getCityName() != null) {
            for (City city : cityComboBox.getItems()) {
                if (city.getName().equals(editingItem.getCityName())) {
                    cityComboBox.setValue(city);
                    break;
                }
            }
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = CategoryService.getAllCategories();
            Platform.runLater(() -> {
                allCategories = categories;
                buildCategoryMenu();
                applyPendingCategorySelection();
            });
        } catch (Exception e) {
            showErrorLabel("خطا در بارگذاری دسته‌بندی‌ها: " + e.getMessage());
        }
    }

    private void buildCategoryMenu() {
        categoryMenuButton.getItems().clear();
        for (Category root : allCategories) {
            if (root.getParentId() != null) continue;
            List<Category> children = new ArrayList<>();
            for (Category c : allCategories) {
                if (root.getId() != null && root.getId().equals(c.getParentId()))
                    children.add(c);
            }
            if (children.isEmpty()) {
                MenuItem item = new MenuItem(root.getName());
                item.setOnAction(e -> selectCategory(root));
                categoryMenuButton.getItems().add(item);
            } else {
                Menu menu = new Menu(root.getName());
                MenuItem selfItem = new MenuItem("📂 همه‌ی «" + root.getName() + "»");
                selfItem.setOnAction(e -> selectCategory(root));
                menu.getItems().add(selfItem);
                menu.getItems().add(new SeparatorMenuItem());
                for (Category child : children) {
                    MenuItem childItem = new MenuItem(child.getName());
                    childItem.setOnAction(e -> selectCategory(child));
                    menu.getItems().add(childItem);
                }
                categoryMenuButton.getItems().add(menu);
            }
        }
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        String label = category.getName();
        if (category.getParentName() != null && !category.getParentName().isEmpty())
            label = category.getParentName() + " › " + category.getName();
        categoryMenuButton.setText("📂 " + label);
    }

    private void applyPendingCategorySelection() {
        if (!isEditMode || editingItem == null || editingItem.getCategoryName() == null) return;
        for (Category cat : allCategories) {
            if (editingItem.getCategoryName().equals(cat.getName())) {
                selectCategory(cat);
                return;
            }
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityService.getAllCities();
            Platform.runLater(() -> {
                cityComboBox.getItems().addAll(cities);
                if (!cities.isEmpty()) cityComboBox.getSelectionModel().selectFirst();
            });
        } catch (Exception e) {
            showErrorLabel("خطا در بارگذاری شهرها: " + e.getMessage());
        }
    }

    @FXML
    private void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب تصویر");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            imagePaths.add(imagePath);
            addImagePreview(imagePath);
        }
    }

    private void addImagePreview(String imagePath) {
        try {
            ImageView imageView = new ImageView(new Image(new File(imagePath).toURI().toString()));
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-cursor: hand;");
            imageView.setOnMouseClicked(e -> {
                imagePaths.remove(imagePath);
                imagePreviewContainer.getChildren().remove(imageView);
            });
            Tooltip.install(imageView, new Tooltip("برای حذف کلیک کنید"));
            imagePreviewContainer.getChildren().add(imageView);
        } catch (Exception e) {
            showErrorLabel("خطا در بارگذاری تصویر: " + e.getMessage());
        }
    }

    @FXML
    private void submitAd() {
        String title       = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText   = priceField.getText().trim();
        City   city        = cityComboBox.getValue();

        if (title.isEmpty())       { showErrorLabel("لطفاً عنوان آگهی را وارد کنید");    return; }
        if (description.isEmpty()) { showErrorLabel("لطفاً توضیحات را وارد کنید");        return; }
        if (priceText.isEmpty())   { showErrorLabel("لطفاً قیمت را وارد کنید");           return; }

        Long price;
        try {
            price = Long.parseLong(priceText.replace(",", ""));
        } catch (NumberFormatException e) {
            showErrorLabel("قیمت وارد شده معتبر نیست");
            return;
        }

        if (selectedCategory == null) { showErrorLabel("لطفاً دسته‌بندی را انتخاب کنید"); return; }
        if (city == null)             { showErrorLabel("لطفاً شهر را انتخاب کنید");        return; }

        try {
            if (isEditMode && editingItem != null) {
                ItemService.ItemUpdateRequest req = new ItemService.ItemUpdateRequest(
                        title, description, price, selectedCategory.getId(), city.getId(), editingItem.getStatus());
                ItemService.updateItem(editingItem.getId(), req);
                showSuccessLabel("آگهی با موفقیت ویرایش شد");
            } else {
                ItemService.ItemCreateRequest req = new ItemService.ItemCreateRequest(
                        title, description, price, selectedCategory.getId(), city.getId(), imagePaths);
                ItemService.createItem(req);
                showSuccessLabel("آگهی با موفقیت ثبت شد و در انتظار بررسی است");
            }
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::goToAdList);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (Exception e) {
            showErrorLabel("خطا: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() { goToAdList(); }

    private void goToAdList() {
        try {
            MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Label helpers (≠ BaseController — روی Label داخل فرم نمایش می‌دن) ───

    private void showErrorLabel(String message) {
        Platform.runLater(() -> {
            errorLabel.setText("❌ " + message);
            errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
        });
    }

    private void showSuccessLabel(String message) {
        Platform.runLater(() -> {
            errorLabel.setText("✅ " + message);
            errorLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
        });
    }
}