package com.secondhand.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class PurchasesController {

    @FXML
    private TableView<?> purchasesTable;

    // TODO: پیاده‌سازی بعد از تکمیل بخش خرید

    @FXML
    public void initialize() {
        // مقداردهی اولیه
        System.out.println("🛒 صفحه خریدها باز شد");
    }
}