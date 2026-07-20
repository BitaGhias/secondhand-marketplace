package com.secondhand.frontend.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageLoaderUtil {

    private static final String DEFAULT_IMAGE_PATH = "/com/secondhand/frontend/images/default-item.png";

    /**
     * بارگذاری تصویر از URL؛ در صورت خطا یا خالی بودن، تصویر پیش‌فرض لود می‌شود.
     */
    public static void loadImageWithDefault(ImageView imageView, String url) {
        try {
            if (url != null && !url.trim().isEmpty()) {
                // لود به صورت پس‌زمینه (background loading = true) برای جلوگیری از فریز شدن UI
                imageView.setImage(new Image(url, true));
            } else {
                loadDefaultImage(imageView);
            }
        } catch (Exception e) {
            loadDefaultImage(imageView);
        }
    }

    /**
     * بارگذاری مستقیم تصویر پیش‌فرض سیستم
     */
    public static void loadDefaultImage(ImageView imageView) {
        try {
            var stream = ImageLoaderUtil.class.getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (stream != null) {
                imageView.setImage(new Image(stream));
            } else {
                System.err.println("تصویر پیش‌فرض در مسیر یافت نشد: " + DEFAULT_IMAGE_PATH);
            }
        } catch (Exception e) {
            System.err.println("خطا در بارگذاری تصویر پیش‌فرض: " + e.getMessage());
        }
    }
}