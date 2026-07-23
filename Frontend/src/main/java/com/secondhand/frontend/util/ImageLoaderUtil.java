package com.secondhand.frontend.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class providing "image loader util" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ImageLoaderUtil {

    private static final String DEFAULT_IMAGE_PATH = "/com/secondhand/frontend/images/default-item.png";

    /**
     * Loads image with default.
     *
     * @param imageView the "image view" value of type {@code ImageView}
     * @param url the target URL
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
     * Loads default image.
     *
     * @param imageView the "image view" value of type {@code ImageView}
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