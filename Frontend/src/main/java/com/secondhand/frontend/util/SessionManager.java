package com.secondhand.frontend.util;

import com.secondhand.frontend.model.User;

/**
 * Holds the client-side session state (current user and JWT token) for the running application.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class SessionManager {

    private static User currentUser;

    /**
     * Gets current user.
     *
     * @return the resulting {@code User} instance
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets current user.
     *
     * @param user the user object
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Gets current user id.
     *
     * @return the resulting numeric value
     */
    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Gets current user username.
     *
     * @return the resulting string
     */
    public static String getCurrentUserUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    /**
     * Gets current user full name.
     *
     * @return the resulting string
     */
    public static String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    /**
     * Checks whether the "logged in" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isLoggedIn() {
        return currentUser != null && ApiClient.isAuthenticated();
    }

    /**
     * Logs out.
     */
    public static void logout() {
        currentUser = null;
        ApiClient.clearToken();
        // حذف نشست ذخیره‌شده روی دیسک تا در اجرای بعدی ورود خودکار انجام نشود
        SessionStore.clear();
    }

    /**
     * Checks whether the "admin" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Checks whether the "user" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isUser() {
        return currentUser != null && "USER".equalsIgnoreCase(currentUser.getRole());
    }
}
