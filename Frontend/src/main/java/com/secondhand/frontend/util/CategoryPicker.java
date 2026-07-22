package com.secondhand.frontend.util;

import com.secondhand.frontend.model.Category;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * کامپوننت مشترک انتخاب دسته‌بندی به صورت فلای‌اوت (منوی تو در تو).
 * به جای ComboBox تختِ قبلی، دسته‌های مادر به صورت زیرمنوی بازشو (flyout)
 * نمایش داده می‌شوند — همان الگویی که در فرم ثبت آگهی وجود داشت،
 * حالا به صورت یک کلاس مشترک برای کل اپ (فیلتر، پنل ادمین، ثبت آگهی، نوار دسته‌ها).
 */
public final class CategoryPicker {

    /** حداکثر عمق درخت برای جلوگیری از حلقه بی‌پایان در داده خراب */
    private static final int MAX_DEPTH = 8;

    private CategoryPicker() {}

    /**
     * پر کردن MenuButton با ساختار درختی دسته‌بندی‌ها.
     *
     * @param button          دکمه منو که آیتم‌ها داخل آن ساخته می‌شوند
     * @param categories      همه دسته‌بندی‌ها (ریشه + زیردسته‌ها)
     * @param nullOptionLabel اگر null نباشد، گزینه «بدون انتخاب» با این برچسب در ابتدای منو اضافه می‌شود
     * @param onSelect        callback انتخاب (با null برای گزینه «بدون انتخاب»)
     */
    public static void populate(MenuButton button,
                                List<Category> categories,
                                String nullOptionLabel,
                                Consumer<Category> onSelect) {
        if (button == null || categories == null) return;
        button.getItems().clear();

        if (nullOptionLabel != null) {
            MenuItem noneItem = new MenuItem(nullOptionLabel);
            noneItem.setOnAction(e -> {
                button.setText(nullOptionLabel);
                if (onSelect != null) onSelect.accept(null);
            });
            button.getItems().add(noneItem);
            button.getItems().add(new SeparatorMenuItem());
        }

        List<Category> roots = new ArrayList<>();
        for (Category c : categories) {
            if (c.getParentId() == null) roots.add(c);
        }
        roots.sort(Comparator.comparing(c -> c.getName() == null ? "" : c.getName()));
        for (Category root : roots) {
            button.getItems().add(buildNode(button, root, categories, onSelect, 0));
        }

        // دسته‌هایی که والدشان در لیست نیست (ایمنی در برابر داده ناقص) به صورت تخت اضافه می‌شوند
        for (Category c : categories) {
            if (c.getParentId() != null && findById(categories, c.getParentId()) == null) {
                MenuItem orphan = new MenuItem(c.getName());
                orphan.setOnAction(e -> select(button, c, onSelect));
                button.getItems().add(orphan);
            }
        }
    }

    private static MenuItem buildNode(MenuButton button, Category cat, List<Category> all,
                                      Consumer<Category> onSelect, int depth) {
        List<Category> children = new ArrayList<>();
        if (depth < MAX_DEPTH) {
            for (Category c : all) {
                if (cat.getId() != null && cat.getId().equals(c.getParentId())) children.add(c);
            }
            children.sort(Comparator.comparing(c -> c.getName() == null ? "" : c.getName()));
        }

        if (children.isEmpty()) {
            MenuItem item = new MenuItem(cat.getName());
            item.setOnAction(e -> select(button, cat, onSelect));
            return item;
        }

        Menu menu = new Menu(cat.getName());
        MenuItem selfItem = new MenuItem("📂 همه‌ی «" + cat.getName() + "»");
        selfItem.setOnAction(e -> select(button, cat, onSelect));
        menu.getItems().add(selfItem);
        menu.getItems().add(new SeparatorMenuItem());
        for (Category child : children) {
            menu.getItems().add(buildNode(button, child, all, onSelect, depth + 1));
        }
        return menu;
    }

    private static void select(MenuButton button, Category cat, Consumer<Category> onSelect) {
        button.setText("📂 " + displayName(cat));
        if (onSelect != null) onSelect.accept(cat);
    }

    /** برچسب نمایشی دسته (با نام والد در صورت وجود) */
    public static String displayName(Category c) {
        if (c == null) return "";
        if (c.getParentName() != null && !c.getParentName().isBlank()) {
            return c.getParentName() + " › " + c.getName();
        }
        return c.getName();
    }

    private static Category findById(List<Category> all, Long id) {
        if (id == null) return null;
        for (Category c : all) {
            if (id.equals(c.getId())) return c;
        }
        return null;
    }
}
