package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.geometry.NodeOrientation;
import com.secondhand.frontend.MainApplication;

public class AdListController {

    @FXML
    private MenuButton userMenuButton;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ad> adListView;

    private ObservableList<Ad> adList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // ۱. ست کردن اکشن روی آیتم‌های منوی کاربر
        setupMenuActions();

        // ۲. گوش دادن به تغییرات فیلد جستجو به صورت لحظه‌ای (Real-time Search)
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch(newValue);
            });
        }

        if (adListView != null) {
            adListView.setCellFactory(param -> new ListCell<Ad>() {
                @Override
                protected void updateItem(Ad item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getTitle() + "  |  " + item.getPrice() + "  (" + item.getLocation() + ")");
                        setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT); // راست‌چین کردن متون لیست
                    }
                }
            });

            adListView.setItems(adList);

            // ۵. مدیریت کلیک روی هر آگهی در لیست
            adListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    System.out.println("آگهی انتخاب شد: " + newValue.getTitle());
                }
            });
        }
    }

    /**
     * متد کمکی برای ست کردن نام کاربر به صورت داینامیک از طریق دیتای دریافتی لاگین
     */
    public void setUserProfile(String fullName) {
        if (userMenuButton != null && fullName != null && !fullName.isEmpty()) {
            javafx.application.Platform.runLater(() -> userMenuButton.setText("👤 " + fullName));
        }
    }

    private void setupMenuActions() {
        if (userMenuButton != null && userMenuButton.getItems() != null) {
            for (MenuItem item : userMenuButton.getItems()) {
                item.setOnAction(event -> {
                    String itemText = item.getText();
                    if (itemText == null) return;

                    switch (itemText.trim()) {
                        case "📝 آگهی‌های من":
                            System.out.println("تغییر مسیر به آگهی‌های من...");
                            break;
                        case "❤️ علاقه‌مندی‌ها":
                            System.out.println("باز کردن علاقه‌مندی‌ها...");
                            break;
                        case "💬 گفت‌وگوها":
                            System.out.println("باز کردن چت‌ها...");
                            break;
                        case "🛒 خریدها":
                            System.out.println("باز کردن تاریخچه خرید...");
                            break;
                        case "خروج":
                        case "🚪 خروج":
                            handleLogout();
                            break;
                    }
                });
            }
        }
    }

    private void handleSearch(String query) {
        System.out.println("در حال جستجو برای: " + query);
        // اینجا در آینده منطق فیلتر کردن داینامیک adList بر اساس متن وارد شده قرار می‌گیرد.
    }

    private void handleLogout() {
        try {
            System.out.println("در حال خروج از حساب کاربری...");
            // هدایت امن کاربر به صفحه لاگین با آدرس‌دهی دقیق پکیج
            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "فروشگاه دست دوم - ورود");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // --- کلاس مدل داخلی برای مدیریت ساختار داده آگهی‌ها ---
    public static class Ad {
        private final String title;
        private final String price;
        private final String location;

        public Ad(String title, String price, String location) {
            this.title = title;
            this.price = price;
            this.location = location;
        }

        public String getTitle() { return title; }
        public String getPrice() { return price; }
        public String getLocation() { return location; }
    }
}