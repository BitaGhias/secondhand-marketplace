package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class AdListController {

    @FXML
    private ListView<String> adListView;

    @FXML
    private void initialize() {
    }

    @FXML
    private void goToLogin() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "ورود");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/register.fxml", "ثبت‌نام");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}