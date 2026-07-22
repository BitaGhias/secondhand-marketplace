package com.secondhand.frontend.util;

public class ValidationUtil {

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    public static boolean isValidIranianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return phone.matches("^09[0-9]{9}$");
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return username.trim().matches("^[A-Za-z0-9_]{3,20}$");
    }

    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return false;
        return fullName.trim().matches("^[\\p{L} ]{3,50}$");
    }

    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    public static boolean isPasswordTooLong(String password, int maxLength) {
        return password != null && password.length() > maxLength;
    }
}