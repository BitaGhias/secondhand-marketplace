package com.secondhand.frontend.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * ابزار کنترل پنجره برای پنجره‌های بدون فریم (StageStyle.TRANSPARENT).
 * چون پنجره فریم سیستم‌عامل ندارد، بزرگ‌نمایی باید به صورت دستی
 * با تنظیم ابعاد پنجره روی ابعاد صفحه‌نمایش انجام شود
 * (setMaximized روی این نوع پنجره‌ها به‌خصوص وقتی resizable=false است کار نمی‌کند).
 */
public final class WindowUtil {

    private static final String PREV_BOUNDS_KEY = "windowUtil.prevBounds";

    private WindowUtil() {
    }

    public static void minimize(Node anyNode) {
        Stage stage = getStage(anyNode);
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    /** بزرگ‌نمایی / بازگرداندن به اندازه قبلی */
    public static void toggleMaximize(Node anyNode) {
        Stage stage = getStage(anyNode);
        if (stage == null) return;

        Object prev = stage.getProperties().get(PREV_BOUNDS_KEY);
        if (prev instanceof Rectangle2D prevBounds) {
            // بازگشت به اندازه قبلی
            stage.getProperties().remove(PREV_BOUNDS_KEY);
            stage.setX(prevBounds.getMinX());
            stage.setY(prevBounds.getMinY());
            stage.setWidth(prevBounds.getWidth());
            stage.setHeight(prevBounds.getHeight());
        } else {
            // ذخیره اندازه فعلی و پر کردن کل صفحه‌نمایش (بدون روی تسک‌بار)
            stage.getProperties().put(PREV_BOUNDS_KEY,
                    new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());
        }
    }

    public static void close(Node anyNode) {
        Stage stage = getStage(anyNode);
        if (stage != null) {
            stage.close();
        }
    }

    private static Stage getStage(Node node) {
        if (node == null || node.getScene() == null) return null;
        return node.getScene().getWindow() instanceof Stage stage ? stage : null;
    }

    public static void makeDraggable(Node handle) {
        final double[] offset = new double[2];
        handle.setOnMousePressed(e -> {
            Stage stage = getStage(handle);
            if (stage != null) {
                offset[0] = stage.getX() - e.getScreenX();
                offset[1] = stage.getY() - e.getScreenY();
            }
        });
        handle.setOnMouseDragged(e -> {
            Stage stage = getStage(handle);
            if (stage != null) {
                stage.setX(e.getScreenX() + offset[0]);
                stage.setY(e.getScreenY() + offset[1]);
            }
        });
    }
}
