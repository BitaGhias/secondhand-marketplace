package com.secondhand.frontend.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * لوگوی برند «بازار سفید» — کیسه خرید سفید با تیک سبز (نماد خرید مطمئن)
 * روی کاشی گرادیان زمردی با گوشه‌های گرد. کاملاً وکتوری و بدون فایل تصویری.
 */
public final class BrandLogo {

    private BrandLogo() {}

    /**
     * ساخت نود لوگو با اندازه دلخواه (طول ضلع کاشی بر حسب پیکسل)
     */
    public static Node create(double size) {
        // کاشی پس‌زمینه با گرادیان زمردی
        Region tile = new Region();
        tile.setMinSize(size, size);
        tile.setMaxSize(size, size);
        tile.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #10b981, #047857);" +
                "-fx-background-radius: " + (size * 0.26) + ";"
        );
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(4, 120, 87, 0.35));
        shadow.setRadius(size * 0.25);
        shadow.setOffsetY(size * 0.06);
        tile.setEffect(shadow);

        double s = size / 24.0; // مقیاس نسبت به شبکه 24تایی

        // بدنه کیسه خرید (سفید)
        SVGPath bag = new SVGPath();
        bag.setContent("M5.6 8 H18.4 L19.5 18.9 A2.3 2.3 0 0 1 17.2 21.4 H6.8 A2.3 2.3 0 0 1 4.5 18.9 Z");
        bag.setFill(Color.WHITE);
        bag.setScaleX(s);
        bag.setScaleY(s);

        // دسته کیسه (خط سفید منحنی بالای بدنه)
        SVGPath handle = new SVGPath();
        handle.setContent("M9 9.6 V7.4 A3 3 0 0 1 15 7.4 V9.6");
        handle.setFill(Color.TRANSPARENT);
        handle.setStroke(Color.WHITE);
        handle.setStrokeWidth(1.7);
        handle.setStrokeLineCap(StrokeLineCap.ROUND);
        handle.setScaleX(s);
        handle.setScaleY(s);

        // تیک سبز روی بدنه کیسه — نماد اعتماد و تایید
        SVGPath check = new SVGPath();
        check.setContent("M8.7 14.4 L11.1 16.8 L15.6 12.1");
        check.setFill(Color.TRANSPARENT);
        check.setStroke(Color.web("#059669"));
        check.setStrokeWidth(2.0);
        check.setStrokeLineCap(StrokeLineCap.ROUND);
        check.setStrokeLineJoin(StrokeLineJoin.ROUND);
        check.setScaleX(s);
        check.setScaleY(s);

        Group art = new Group(bag, handle, check);

        StackPane logo = new StackPane(tile, art);
        logo.setMinSize(size, size);
        logo.setMaxSize(size, size);
        return logo;
    }
}
