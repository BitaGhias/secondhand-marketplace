package com.secondhand.frontend.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;//برای رفتن به صفحه ی بعدی
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class AdListController {

    @FXML
    private ListView<String> adListView;

    @FXML
    public void initialize() {
        ObservableList<String> fakeAds = FXCollections.observableArrayList(
                "گوشی سامسونگ A52 - 5,000,000 تومان - تهران",
                "لپ‌تاپ ایسوس - 12,000,000 تومان - اصفهان",
                "دوچرخه کوهستان - 2,500,000 تومان - شیراز"
        );
        adListView.setItems(fakeAds);
    }

    @FXML
    private void goToNewAd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/newad.fxml"));
            Stage stage = (Stage) adListView.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 500, 450));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
