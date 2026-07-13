package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Ad;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ItemAdController {

    @FXML private ImageView adImageView;
    @FXML private Label adTitleLabel;
    @FXML private Label adPriceLabel;
    @FXML private Label usernameLabel;

    public void setData(Ad ad) {
        adTitleLabel.setText(ad.getTitle());
        adPriceLabel.setText(ad.getFormattedPrice());
        usernameLabel.setText(ad.getUsername() + " (" + ad.getCityName() + ")");

        // لود کردن عکس به صورت کاملاً امن که اگر فایل نبود کرش نکند
        try {
            String defaultImagePath = "/com/secondhand/frontend/images/default-item.png";
            var imageStream = getClass().getResourceAsStream(defaultImagePath);

            if (imageStream != null) {
                adImageView.setImage(new Image(imageStream));
            } else {
                // اگر عکسی پیدا نشد، ایمیج‌ویو را کلاً خالی بگذار یا مخفی نکن تا خطا ندهد
                System.out.println("⚠️ عکس پیش‌فرض در مسیر پیدا نشد، اما کارت رندر می‌شود.");
            }
        } catch (Exception e) {
            System.err.println("خطا در تنظیم تصویر: " + e.getMessage());
        }
    }
}