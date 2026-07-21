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
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

public class MyAdsController extends BaseController {
    @FXML private FlowPane myAdsFlowPane;
    @FXML private HBox titleBar;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadMyAds();
    }

    private void loadMyAds() {
        ItemService.getMyItemsAsync()
                .thenAccept(myItems -> Platform.runLater(() -> renderItems(myItems)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showEmptyMessage("خطا در دریافت آگهی‌های من: " + ex.getMessage()));
                    return null;
                });
    }

    private void renderItems(List<Item> items) {
        myAdsFlowPane.getChildren().clear();
        if (items == null || items.isEmpty()) {
            showEmptyMessage("هنوز هیچ آگهی‌ای ثبت نکرده‌اید");
            return;
        }
        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/secondhand/frontend/item_ad.fxml"));
                Parent card = loader.load();
                AdItemController controller = loader.getController();
                controller.setItem(item);
                card.setOnMouseClicked(event -> goToItemDetail(item));
                myAdsFlowPane.getChildren().add(card);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void showEmptyMessage(String text) {
        myAdsFlowPane.getChildren().clear();
        Label emptyLabel = new Label(text);
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
        StackPane emptyPane = new StackPane(emptyLabel);
        emptyPane.setAlignment(Pos.CENTER);
        emptyPane.prefWidthProperty().bind(myAdsFlowPane.widthProperty());
        emptyPane.setPrefHeight(400);
        myAdsFlowPane.getChildren().add(emptyPane);
    }

    private void goToItemDetail(Item item) {
        try {
            MainApplication.goToItemDetail(item);
        } catch (Exception e) {
            System.err.println("❌ خطا در رفتن به جزئیات: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try { MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها"); }
        catch (Exception e) { e.printStackTrace(); }
    }
}