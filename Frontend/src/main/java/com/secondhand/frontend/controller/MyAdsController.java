package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class MyAdsController extends BaseController {
    // ✅ مثل صفحه adlist، کارت‌های item-ad کنار هم در FlowPane چیده می‌شوند
    @FXML private FlowPane myAdsFlowPane;
    @FXML private HBox titleBar;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadMyAds();
    }

    private void loadMyAds() {
        new Thread(() -> {
            try {
                List<Item> myItems = ItemService.getMyItems();
                Platform.runLater(() -> renderItems(myItems));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showEmptyMessage("خطا در دریافت آگهی‌های من: " + e.getMessage()));
            }
        }).start();
    }

    private void renderItems(List<Item> items) {
        myAdsFlowPane.getChildren().clear();

        if (items == null || items.isEmpty()) {
            showEmptyMessage("هنوز هیچ آگهی‌ای ثبت نکرده‌اید");
            return;
        }

        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                Parent card = loader.load();
                ItemAdController controller = loader.getController();
                controller.setItem(item);
                card.setOnMouseClicked(event -> goToItemDetail(item));
                myAdsFlowPane.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showEmptyMessage(String text) {
        myAdsFlowPane.getChildren().clear();
        Label emptyLabel = new Label(text);
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: rgba(255,255,255,0.45);");
        // نمایش پیام در مرکز صفحه
        StackPane emptyPane = new StackPane(emptyLabel);
        emptyPane.setAlignment(Pos.CENTER);
        emptyPane.prefWidthProperty().bind(myAdsFlowPane.widthProperty());
        emptyPane.setPrefHeight(400);
        myAdsFlowPane.getChildren().add(emptyPane);
    }

    private void goToItemDetail(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_detail.fxml"));
            Parent root = loader.load();
            ItemDetailController controller = loader.getController();
            controller.setItem(item);

            Stage stage = (Stage) myAdsFlowPane.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("جزئیات آگهی");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
