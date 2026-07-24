package com.secondhand.frontend.util;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.PurchaseRequest;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.service.PurchaseRequestService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class providing "notification center" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public final class NotificationCenter {

    /**
     * Creates a new {@code NotificationCenter} instance.
     */
    private NotificationCenter() {}

    public static class Entry {
        public String key;
        public String icon;
        public String title;
        public String message;
        public String edgeColor;
        public String chipBg;
        public String chipFg;
        public String chipText;
        public Long itemId;
        public Item item;
        public String rejectionReason;
        public boolean openableItem;
    }

    /**
     * Performs the "user key" operation.
     *
     * @return the resulting string
     */
    private static String userKey() {
        Long id = SessionManager.getCurrentUserId();
        return id != null ? String.valueOf(id) : "anonymous";
    }

    /**
     * Fetches all.
     *
     * @return a {@code List<Entry>} with the results; empty if nothing matches
     */
    public static List<Entry> fetchAll() {
        List<Entry> out = new ArrayList<>();

        // وضعیت آگهی‌های من
        try {
            for (Item i : ItemService.getMyItems()) {
                String s = i.getStatus() != null ? i.getStatus().toUpperCase() : "";
                Entry e = new Entry();
                e.itemId = i.getId();
                e.item = i;
                e.key = "item-" + i.getId() + "-" + s;
                e.title = i.getTitle();
                switch (s) {
                    case "REJECTED" -> {
                        e.icon = "\u274c"; e.message = "آگهی شما رد شد";
                        e.edgeColor = "#dc2626"; e.chipBg = "#fee2e2"; e.chipFg = "#b91c1c"; e.chipText = "رد شد";
                        e.rejectionReason = i.getRejectionReason();
                    }
                    case "PENDING" -> {
                        e.icon = "\u23f3"; e.message = "آگهی شما در انتظار بررسی مدیر است";
                        e.edgeColor = "#d97706"; e.chipBg = "#fef3c7"; e.chipFg = "#b45309"; e.chipText = "در انتظار";
                    }
                    case "SOLD" -> {
                        e.icon = "\ud83d\udcb0";
                        e.message = "آگهی شما فروخته شد" + (i.getBuyerUsername() != null ? " — خریدار: " + i.getBuyerUsername() : "");
                        e.edgeColor = "#2563eb"; e.chipBg = "#dbeafe"; e.chipFg = "#1d4ed8"; e.chipText = "فروخته شد";
                    }
                    default -> {
                        e.icon = "\u2705"; e.message = "آگهی شما تایید شد و اکنون برای همه قابل مشاهده است";
                        e.edgeColor = "#16a34a"; e.chipBg = "#dcfce7"; e.chipFg = "#15803d"; e.chipText = "تایید شد";
                    }
                }
                out.add(e);
            }
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }

        // درخواست‌های خرید رسیده (برای فروشنده)
        try {
            for (PurchaseRequest pr : PurchaseRequestService.incoming()) {
                if (!pr.isPending()) continue;
                Entry e = new Entry();
                e.key = "pr-in-" + pr.getId();
                e.icon = "\ud83d\udce9";
                e.title = pr.getItemTitle();
                e.message = "«" + pr.getBuyerUsername() + "» برای این آگهی درخواست خرید ثبت کرده — از صفحهٔ جزئیات آگهی تایید یا رد کنید";
                e.edgeColor = "#f97316"; e.chipBg = "#ffedd5"; e.chipFg = "#c2410c"; e.chipText = "درخواست خرید";
                e.itemId = pr.getItemId();
                e.openableItem = true;
                out.add(e);
            }
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }

        // پاسخ درخواست‌های من (برای خریدار)
        try {
            for (PurchaseRequest pr : PurchaseRequestService.mine()) {
                if (pr.isPending()) continue;
                Entry e = new Entry();
                e.key = "pr-my-" + pr.getId() + "-" + pr.getStatus();
                e.title = pr.getItemTitle();
                e.itemId = pr.getItemId();
                e.openableItem = true;
                if (pr.isAccepted()) {
                    e.icon = "\ud83c\udf89";
                    e.message = "فروشنده درخواست خرید شما را تایید کرد — این کالا در بخش «خریدهای من» قابل مشاهده است";
                    e.edgeColor = "#16a34a"; e.chipBg = "#dcfce7"; e.chipFg = "#15803d"; e.chipText = "تایید شد";
                } else {
                    e.icon = "\ud83d\udeab";
                    e.message = "فروشنده درخواست خرید شما را رد کرد";
                    e.edgeColor = "#dc2626"; e.chipBg = "#fee2e2"; e.chipFg = "#b91c1c"; e.chipText = "رد شد";
                }
                out.add(e);
            }
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }

        return out;
    }

    /**
     * Performs the "read keys" operation.
     *
     * @return a {@code Set<String>} with the results; empty if nothing matches
     */
    public static Set<String> readKeys() {
        return ReadNotificationsStore.readKeys(userKey());
    }

    /**
     * Marks read.
     *
     * @param key the "key" value of type {@code String}
     */
    public static void markRead(String key) {
        ReadNotificationsStore.markRead(userKey(), key);
    }

    /**
     * Marks all read.
     *
     * @param entries the "entries" value of type {@code List<Entry>}
     */
    public static void markAllRead(List<Entry> entries) {
        List<String> keys = new ArrayList<>();
        for (Entry e : entries) keys.add(e.key);
        ReadNotificationsStore.markAllRead(userKey(), keys);
    }

    /**
     * Performs the "unread count" operation.
     *
     * @return the resulting numeric value
     */
    public static long unreadCount() {
        Set<String> read = readKeys();
        return fetchAll().stream().filter(e -> !read.contains(e.key)).count();
    }
}
