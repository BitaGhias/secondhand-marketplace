package com.secondhand.frontend.util;

import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;

public class SessionManager {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static String getCurrentUserUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public static String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null && ApiClient.isAuthenticated();
    }

    public static void logout() {
        currentUser = null;
        ApiClient.clearToken();
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isUser() {
        return currentUser != null && "USER".equalsIgnoreCase(currentUser.getRole());
    }
}