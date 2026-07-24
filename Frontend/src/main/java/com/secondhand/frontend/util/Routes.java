package com.secondhand.frontend.util;

/**
 * Centralized FXML and resource path constants.
 * All view paths are defined here — update in one place if files move.
 */
public final class Routes {

    /**
     * Creates a new {@code Routes} instance.
     */
    private Routes() {}

    private static final String BASE = "/com/secondhand/frontend";

    // ── Auth ────────────────────────────────────────────────────────────────
    public static final String LOGIN    = BASE + "/fxml/auth/login.fxml";
    public static final String REGISTER = BASE + "/fxml/auth/register.fxml";

    // ── Item / Ad ────────────────────────────────────────────────────────────
    public static final String AD_LIST       = BASE + "/fxml/item/adlist.fxml";
    /** Main user panel after a regular user logs in. */
    public static final String USER_PANEL   = AD_LIST;
    public static final String ITEM_AD       = BASE + "/fxml/item/item_ad.fxml";
    public static final String ITEM_DETAIL   = BASE + "/fxml/item/item_detail.fxml";
    public static final String CREATE_AD     = BASE + "/fxml/item/create_ad.fxml";
    public static final String MY_ADS        = BASE + "/fxml/item/my_ads.fxml";
    public static final String FILTER_DIALOG = BASE + "/fxml/item/filter_dialogue.fxml";

    // ── User ─────────────────────────────────────────────────────────────────
    public static final String PROFILE       = BASE + "/fxml/user/profile.fxml";
    public static final String PURCHASES     = BASE + "/fxml/user/purchases.fxml";
    public static final String FAVORITES     = BASE + "/fxml/user/favorites.fxml";
    public static final String NOTIFICATIONS = BASE + "/fxml/user/notifications.fxml";

    // ── Chat ─────────────────────────────────────────────────────────────────
    public static final String CHATS = BASE + "/fxml/chat/chats.fxml";

    // ── Admin ────────────────────────────────────────────────────────────────
    public static final String ADMIN_PANEL    = BASE + "/fxml/admin/admin_panel.fxml";
    public static final String ADMIN_USER_ADS = BASE + "/fxml/admin/admin_user_ads.fxml";

    // ── Static assets ────────────────────────────────────────────────────────
    public static final String STYLESHEET    = BASE + "/css/styles.css";
    public static final String DEFAULT_IMAGE = BASE + "/images/default-item.png";
}
