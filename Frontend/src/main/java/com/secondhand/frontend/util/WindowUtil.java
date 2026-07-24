package com.secondhand.frontend.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Utility class providing "window util" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public final class WindowUtil {

    private static final String PREV_BOUNDS_KEY = "windowUtil.prevBounds";

    /**
     * Creates a new {@code WindowUtil} instance.
     */
    private WindowUtil() {
    }

    /**
     * Performs the "minimize" operation.
     *
     * @param anyNode the "any node" value of type {@code Node}
     */
    public static void minimize(Node anyNode) {
        Stage stage = getStage(anyNode);
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    /**
     * Toggles maximize.
     *
     * @param anyNode the "any node" value of type {@code Node}
     */
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

    /**
     * Closes.
     *
     * @param anyNode the "any node" value of type {@code Node}
     */
    public static void close(Node anyNode) {
        Stage stage = getStage(anyNode);
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Gets stage.
     *
     * @param node the "node" value of type {@code Node}
     * @return the resulting {@code Stage} instance
     */
    private static Stage getStage(Node node) {
        if (node == null || node.getScene() == null) return null;
        return node.getScene().getWindow() instanceof Stage stage ? stage : null;
    }

    /**
     * Performs the "make draggable" operation.
     *
     * @param handle the "handle" value of type {@code Node}
     */
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
