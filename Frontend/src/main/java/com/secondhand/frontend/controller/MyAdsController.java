package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ItemService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class MyAdsController {

    @FXML private VBox myAdsVBox;
    @FXML private Label noMyAdsLabel;

    @FXML
    public void initialize() {
        loadMyAds();
    }

    private void loadMyAds() {
        try {
            List<Item> myItems = ItemService.getMyItems();
            Platform.runLater(() -> {
                myAdsVBox.getChildren().clear();

                if (myItems.isEmpty()) {
                    noMyAdsLabel.setVisible(true);
                } else {
                    noMyAdsLabel.setVisible(false);
                    for (Item item : myItems) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                            Parent card = loader.load();
                            ItemAdController controller = loader.getController();
                            controller.setItem(item);
                            myAdsVBox.getChildren().add(card);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}