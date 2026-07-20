package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class PurchasesController extends BaseController {
    @FXML private TableView<Item> purchasesTable;
    @FXML private HBox titleBar;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        setupColumns();
        loadPurchases();
    }

    private void setupColumns() {
        TableColumn<Item, String> titleCol = new TableColumn<>("عنوان کالا");
        titleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTitle()));
        titleCol.setPrefWidth(220);

        TableColumn<Item, String> priceCol = new TableColumn<>("قیمت");
        priceCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFormattedPrice()));
        priceCol.setPrefWidth(140);

        TableColumn<Item, String> sellerCol = new TableColumn<>("فروشنده");
        sellerCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOwnerUsername()));
        sellerCol.setPrefWidth(130);

        TableColumn<Item, String> statusCol = new TableColumn<>("وضعیت");
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPersianStatus()));
        statusCol.setPrefWidth(110);

        TableColumn<Item, Void> rateCol = new TableColumn<>("امتیازدهی");
        rateCol.setPrefWidth(140);
        rateCol.setCellFactory(col -> new TableCell<>() {
            private final Button rateButton = new Button("⭐ ثبت امتیاز");
            {
                rateButton.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);"
                        + " -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 6 12;");
                rateButton.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    showRatingDialog(item);
                });
            }

            @Override
            protected void updateItem(Void value, boolean empty) {
                super.updateItem(value, empty);
                setGraphic(empty ? null : rateButton);
            }
        });

        purchasesTable.getColumns().setAll(List.of(titleCol, priceCol, sellerCol, statusCol, rateCol));
    }

    private void loadPurchases() {
        // تبدیل هندلینگ سنتی ترد به قابلیت‌های Async جاوا
        ItemService.getPurchasedItemsAsync()
                .thenAccept(purchased -> Platform.runLater(() -> purchasesTable.getItems().setAll(purchased)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("خطا در دریافت لیست خریدها: " + ex.getMessage(), Alert.AlertType.ERROR));
                    return null;
                });
    }

    private void showRatingDialog(Item item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("امتیازدهی به فروشنده");
        dialog.setHeaderText("امتیاز شما به «" + item.getOwnerUsername() + "» برای خرید «" + item.getTitle() + "»");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1936;");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        Label scoreLabel = new Label("امتیاز (۱ تا ۵):");
        scoreLabel.setStyle("-fx-text-fill: white;");

        ComboBox<Integer> scoreComboBox = new ComboBox<>();
        scoreComboBox.getItems().addAll(1, 2, 3, 4, 5);
        scoreComboBox.setValue(5);

        Label commentLabel = new Label("نظر (اختیاری):");
        commentLabel.setStyle("-fx-text-fill: white;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر خود را بنویسید...");
        commentArea.setPrefHeight(80);

        content.getChildren().addAll(scoreLabel, scoreComboBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        int score = scoreComboBox.getValue() != null ? scoreComboBox.getValue() : 5;
        String comment = commentArea.getText() != null ? commentArea.getText().trim() : "";

        RatingService.rateSellerAsync(item.getId(), score, comment)
                .thenRun(() -> Platform.runLater(() -> showAlert("✅ امتیاز شما با موفقیت ثبت شد", Alert.AlertType.INFORMATION)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("خطا در ثبت امتیاز: " + ex.getMessage(), Alert.AlertType.ERROR));
                    return null;
                });
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "خطا" : "موفق");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        alert.getDialogPane().setStyle("-fx-background-color: #1a1936;");
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}