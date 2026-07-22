package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.RatingService;
import com.secondhand.frontend.util.Routes;
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
        setupRowDoubleClick();
        loadPurchases();
    }

    private void setupRowDoubleClick() {
        purchasesTable.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openItemDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private void openItemDetail(Item item) {
        try {
            ItemDetailController.setItemId(item.getId());
            MainApplication.changeScene(Routes.ITEM_DETAIL, "جزئیات آگهی");
        } catch (Exception e) {
            showAlert("خطا در باز کردن جزئیات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupColumns() {
        TableColumn<Item, String> titleCol = new TableColumn<>("عنوان کالا");
        titleCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitle()));
        titleCol.setPrefWidth(220);

        TableColumn<Item, String> priceCol = new TableColumn<>("قیمت");
        priceCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFormattedPrice()));
        priceCol.setPrefWidth(140);

        TableColumn<Item, String> sellerCol = new TableColumn<>("فروشنده");
        sellerCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getOwnerUsername()));
        sellerCol.setPrefWidth(130);

        TableColumn<Item, String> statusCol = new TableColumn<>("وضعیت");
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPersianStatus()));
        statusCol.setPrefWidth(110);

        TableColumn<Item, Void> rateCol = new TableColumn<>("امتیازدهی");
        rateCol.setPrefWidth(150);
        rateCol.setCellFactory(col -> new TableCell<>() {
            private final Button rateBtn  = new Button("\u2B50 ثبت امتیاز");
            private final Label  ratedLbl = new Label("\u2705 امتیاز داده شده");
            {
                rateBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white;"
                        + " -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 6 12;");
                ratedLbl.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px;");
                rateBtn.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    showRatingDialog(item, rateBtn, ratedLbl);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Item item = getTableView().getItems().get(getIndex());
                setGraphic(rateBtn);
                rateBtn.setDisable(true);
                RatingService.hasRatedAsync(item.getId()).thenAccept(rated ->
                        Platform.runLater(() -> {
                            if (rated) {
                                setGraphic(ratedLbl);
                            } else {
                                setGraphic(rateBtn);
                                rateBtn.setDisable(false);
                            }
                        }));
            }
        });

        purchasesTable.getColumns().setAll(List.of(titleCol, priceCol, sellerCol, statusCol, rateCol));
        purchasesTable.setPlaceholder(new Label("خریدی در تاریخچه ندارید"));
    }

    private void loadPurchases() {
        ItemService.getPurchasedItemsAsync()
                .thenAccept(list -> Platform.runLater(() -> purchasesTable.getItems().setAll(list)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("خطا: " + ex.getMessage(), Alert.AlertType.ERROR));
                    return null;
                });
    }

    private void showRatingDialog(Item item, Button rateBtn, Label ratedLbl) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("امتیازدهی به فروشنده");
        dialog.setHeaderText("امتیاز شما به «" + item.getOwnerUsername() + "» برای خرید «" + item.getTitle() + "»");
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource(Routes.STYLESHEET).toExternalForm());
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
        dialog.getDialogPane().setStyle("-fx-background-color: #ffffff;");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        Label scoreLabel = new Label("امتیاز (۱ تا ۵):");
        scoreLabel.setStyle("-fx-text-fill: #1f2937;");
        ComboBox<Integer> scoreBox = new ComboBox<>();
        scoreBox.getItems().addAll(1, 2, 3, 4, 5);
        scoreBox.setValue(5);

        Label commentLabel = new Label("نظر (اختیاری):");
        commentLabel.setStyle("-fx-text-fill: #1f2937;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر خود را بنویسید...");
        commentArea.setPrefHeight(80);

        content.getChildren().addAll(scoreLabel, scoreBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        int score      = scoreBox.getValue() != null ? scoreBox.getValue() : 5;
        String comment = commentArea.getText() != null ? commentArea.getText().trim() : "";

        rateBtn.setDisable(true);
        RatingService.rateSellerAsync(item.getId(), score, comment)
                .thenRun(() -> Platform.runLater(() -> {
                    purchasesTable.refresh();
                    showAlert("✅ امتیاز با موفقیت ثبت شد", Alert.AlertType.INFORMATION);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        rateBtn.setDisable(false);
                        showAlert("خطا در ثبت امتیاز: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها");
        } catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}