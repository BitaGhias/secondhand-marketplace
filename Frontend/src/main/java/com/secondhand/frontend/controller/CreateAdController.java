package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.CategoryPicker;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * فرم ثبت/ویرایش آگهی به صورت ویزارد ۳ مرحله‌ای:
 *   ۱) مشخصات (عنوان + دسته‌بندی + شهر)   ۲) توضیحات و قیمت   ۳) تصاویر و ثبت نهایی
 * تصاویر هم با دکمه انتخاب فایل و هم با کشیدن و رها کردن (Drag & Drop) اضافه می‌شوند.
 */
public class CreateAdController extends BaseController {

    @FXML private Label      pageTitle;
    @FXML private TextField  titleField;
    @FXML private TextArea   descriptionArea;
    @FXML private TextField  priceField;
    @FXML private MenuButton categoryMenuButton;
    @FXML private ComboBox<City> cityComboBox;
    @FXML private FlowPane   imagePreviewContainer;
    @FXML private Button     submitButton;
    @FXML private Label      errorLabel;
    @FXML private HBox       titleBar;

    // ── ویزارد ──
    @FXML private VBox   step1Box;
    @FXML private VBox   step2Box;
    @FXML private VBox   step3Box;
    @FXML private Label  stepLabel1;
    @FXML private Label  stepLabel2;
    @FXML private Label  stepLabel3;
    @FXML private ProgressBar wizardProgressBar;
    @FXML private Button backStepButton;
    @FXML private Button nextStepButton;
    @FXML private VBox   dropZone;

    private static final List<String> IMAGE_EXTENSIONS = List.of(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");
    // FIX: محدودیت‌های تصویر هم‌راستا با بک‌اند که اکنون سمت کلاینت هم بررسی می‌شود
    private static final int  MAX_IMAGES = 5;
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private List<String>   imagePaths      = new ArrayList<>();
    // FIX: تصاویر موجود آگهی هنگام ویرایش و شناسه تصاویری که قرار است حذف شوند
    private List<com.secondhand.frontend.model.Image> existingImages = new ArrayList<>();
    private List<Long>     removedImageIds = new ArrayList<>();
    private List<Category> allCategories   = new ArrayList<>();
    private Category       selectedCategory;
    private Item           editingItem;
    private boolean        isEditMode      = false;
    private int            currentStep     = 1;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadCategories();
        loadCities();
        setupDragAndDrop();
        showStep(1);
    }

    public void setItemForEdit(Item item) {
        this.editingItem = item;
        this.isEditMode  = true;
        submitButton.setText("💾 ذخیره تغییرات");
        if (pageTitle != null) pageTitle.setText("✏️ ویرایش آگهی");
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
        // FIX: تصاویر فعلی آگهی در حالت ویرایش بارگذاری و قابل حذف می‌شوند
        if (editingItem.getImages() != null) {
            existingImages = new ArrayList<>(editingItem.getImages());
            for (com.secondhand.frontend.model.Image img : existingImages) {
                addExistingImagePreview(img);
            }
        }
    }

    // ===================== ویزارد =====================

    @FXML
    private void nextStep() {
        hideErrorLabel();
        if (currentStep == 1 && !validateStep1()) return;
        if (currentStep == 2 && !validateStep2()) return;
        if (currentStep < 3) showStep(currentStep + 1);
    }

    @FXML
    private void prevStep() {
        hideErrorLabel();
        if (currentStep > 1) showStep(currentStep - 1);
    }

    private void showStep(int step) {
        currentStep = step;
        setStepVisible(step1Box, step == 1);
        setStepVisible(step2Box, step == 2);
        setStepVisible(step3Box, step == 3);

        styleStepIndicator(stepLabel1, 1, step);
        styleStepIndicator(stepLabel2, 2, step);
        styleStepIndicator(stepLabel3, 3, step);

        if (wizardProgressBar != null) wizardProgressBar.setProgress(step / 3.0);

        if (backStepButton != null) {
            backStepButton.setVisible(step > 1);
            backStepButton.setManaged(step > 1);
        }
        if (nextStepButton != null) {
            nextStepButton.setVisible(step < 3);
            nextStepButton.setManaged(step < 3);
        }
        if (submitButton != null) {
            submitButton.setVisible(step == 3);
            submitButton.setManaged(step == 3);
        }
    }

    private void setStepVisible(VBox box, boolean visible) {
        if (box == null) return;
        box.setVisible(visible);
        box.setManaged(visible);
    }

    private void styleStepIndicator(Label label, int stepOfLabel, int activeStep) {
        if (label == null) return;
        if (stepOfLabel == activeStep) {
            label.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold;"
                    + "-fx-background-radius: 999; -fx-padding: 6 16; -fx-font-size: 12px;");
        } else if (stepOfLabel < activeStep) {
            label.setStyle("-fx-background-color: #143449; -fx-text-fill: white;"
                    + "-fx-background-radius: 999; -fx-padding: 6 16; -fx-font-size: 12px;");
        } else {
            label.setStyle("-fx-background-color: #eef2f6; -fx-text-fill: #94a3b8;"
                    + "-fx-background-radius: 999; -fx-padding: 6 16; -fx-font-size: 12px;");
        }
    }

    private boolean validateStep1() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showErrorLabel("لطفاً عنوان آگهی را وارد کنید");
            return false;
        }
        if (selectedCategory == null) {
            showErrorLabel("لطفاً دسته‌بندی را انتخاب کنید");
            return false;
        }
        if (cityComboBox.getValue() == null) {
            showErrorLabel("لطفاً شهر را انتخاب کنید");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showErrorLabel("لطفاً توضیحات را وارد کنید");
            return false;
        }
        String priceText = priceField.getText() != null ? priceField.getText().trim() : "";
        if (priceText.isEmpty()) {
            showErrorLabel("لطفاً قیمت را وارد کنید");
            return false;
        }
        try {
            Long.parseLong(priceText.replace(",", ""));
        } catch (NumberFormatException e) {
            showErrorLabel("قیمت وارد شده معتبر نیست");
            return false;
        }
        return true;
    }

    // ===================== دسته‌بندی و شهر =====================

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                Platform.runLater(() -> {
                    allCategories = categories;
                    CategoryPicker.populate(categoryMenuButton, categories, null, cat -> selectedCategory = cat);
                    applyPendingCategorySelection();
                });
            } catch (Exception e) {
                showErrorLabel("خطا در بارگذاری دسته‌بندی‌ها: " + e.getMessage());
            }
        }).start();
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        categoryMenuButton.setText("📂 " + CategoryPicker.displayName(category));
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
        new Thread(() -> {
            try {
                List<City> cities = CityService.getAllCities();
                Platform.runLater(() -> {
                    cityComboBox.getItems().addAll(cities);
                    if (!cities.isEmpty()) cityComboBox.getSelectionModel().selectFirst();
                });
            } catch (Exception e) {
                showErrorLabel("خطا در بارگذاری شهرها: " + e.getMessage());
            }
        }).start();
    }

    // ===================== تصاویر =====================

    @FXML
    private void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب تصویر");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            for (File file : selectedFiles) addImageFile(file);
        }
    }

    /** پشتیبانی از کشیدن و رها کردن تصویر روی ناحیه آپلود */
    private void setupDragAndDrop() {
        if (dropZone == null) return;

        dropZone.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() && db.getFiles().stream().anyMatch(CreateAdController::isImageFile)) {
                event.acceptTransferModes(TransferMode.COPY);
                dropZone.setStyle(dropZoneStyle(true));
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle(dropZoneStyle(false));
            event.consume();
        });

        dropZone.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (isImageFile(file)) {
                        addImageFile(file);
                        success = true;
                    }
                }
            }
            dropZone.setStyle(dropZoneStyle(false));
            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setStyle(dropZoneStyle(false));
    }

    private static String dropZoneStyle(boolean active) {
        return "-fx-background-color: " + (active ? "#fff1e6" : "#f8fafc") + ";"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: " + (active ? "#f97316" : "#cbd5e1") + ";"
                + "-fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 14;";
    }

    private static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String ext : IMAGE_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    // FIX: تعداد کل تصاویر (موجود + جدید) را برای بررسی محدودیت محاسبه می‌کند
    private int totalImageCount() {
        return existingImages.size() + imagePaths.size();
    }

    private void addImageFile(File file) {
        String imagePath = file.getAbsolutePath();
        if (imagePaths.contains(imagePath)) return;
        // FIX: محدودیت حداکثر ۵ تصویر اکنون سمت کلاینت هم قبل از ارسال به سرور بررسی می‌شود
        if (totalImageCount() >= MAX_IMAGES) {
            showErrorLabel("حداکثر " + MAX_IMAGES + " تصویر مجاز است!");
            return;
        }
        // FIX: محدودیت حجم ۵ مگابایتی اکنون سمت کلاینت هم قبل از ارسال به سرور بررسی می‌شود
        if (file.length() > MAX_IMAGE_SIZE_BYTES) {
            showErrorLabel("حجم تصویر «" + file.getName() + "» نباید بیشتر از ۵ مگابایت باشد!");
            return;
        }
        imagePaths.add(imagePath);
        addImagePreview(imagePath);
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

    // FIX: نمایش تصاویر موجود آگهی در حالت ویرایش به همراه امکان حذف
    private void addExistingImagePreview(com.secondhand.frontend.model.Image img) {
        try {
            ImageView imageView = new ImageView(new Image(img.getFullUrl()));
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-cursor: hand;");
            imageView.setOnMouseClicked(e -> {
                existingImages.remove(img);
                if (img.getId() != null) removedImageIds.add(img.getId());
                imagePreviewContainer.getChildren().remove(imageView);
            });
            Tooltip.install(imageView, new Tooltip("برای حذف کلیک کنید"));
            imagePreviewContainer.getChildren().add(imageView);
        } catch (Exception e) {
            showErrorLabel("خطا در بارگذاری تصویر: " + e.getMessage());
        }
    }

    // ===================== ثبت نهایی =====================

    @FXML
    private void submitAd() {
        String title       = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText   = priceField.getText().trim();
        City   city        = cityComboBox.getValue();

        if (title.isEmpty())       { showStep(1); showErrorLabel("لطفاً عنوان آگهی را وارد کنید");    return; }
        if (selectedCategory == null) { showStep(1); showErrorLabel("لطفاً دسته‌بندی را انتخاب کنید"); return; }
        if (city == null)             { showStep(1); showErrorLabel("لطفاً شهر را انتخاب کنید");        return; }
        if (description.isEmpty())    { showStep(2); showErrorLabel("لطفاً توضیحات را وارد کنید");        return; }
        if (priceText.isEmpty())      { showStep(2); showErrorLabel("لطفاً قیمت را وارد کنید");           return; }

        Long price;
        try {
            price = Long.parseLong(priceText.replace(",", ""));
        } catch (NumberFormatException e) {
            showStep(2);
            showErrorLabel("قیمت وارد شده معتبر نیست");
            return;
        }

        try {
            if (isEditMode && editingItem != null) {
                ItemService.ItemUpdateRequest req = new ItemService.ItemUpdateRequest(
                        title, description, price, selectedCategory.getId(), city.getId(), editingItem.getStatus());
                // FIX: تصاویر حذف‌شده و تصاویر تازه هم همراه ویرایش ارسال می‌شوند
                req.removedImageIds = removedImageIds;
                req.newImagePaths = imagePaths;
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

    // ─── Label helpers (روی Label داخل فرم نمایش می‌دهند) ───

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

    private void hideErrorLabel() {
        if (errorLabel != null) errorLabel.setVisible(false);
    }
}