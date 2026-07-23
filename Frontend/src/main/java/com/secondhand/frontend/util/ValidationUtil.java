package com.secondhand.frontend.util;

/**
 * Utility class providing "validation util" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ValidationUtil {

    /**
     * Checks whether the "valid email" condition holds.
     *
     * @param email the email address
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Performs the "normalize digits" operation.
     *
     * @param input the "input" value of type {@code String}
     * @return the resulting string
     */
    public static String normalizeDigits(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '۰' && c <= '۹') { // ارقام فارسی
                sb.append((char) (c - '۰' + '0'));
            } else if (c >= '٠' && c <= '٩') { // ارقام عربی
                sb.append((char) (c - '٠' + '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Checks whether the "valid iranian phone" condition holds.
     *
     * @param phone the "phone" value of type {@code String}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isValidIranianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return normalizeDigits(phone.trim()).matches("^09[0-9]{9}$");
    }

    /**
     * Checks whether the "valid username" condition holds.
     *
     * @param username the username
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return username.trim().matches("^[A-Za-z0-9_]{3,20}$");
    }

    /**
     * Checks whether the "valid full name" condition holds.
     *
     * @param fullName the "full name" value of type {@code String}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return false;
        return fullName.trim().matches("^[\\p{L} ]{3,50}$");
    }

    /**
     * Checks whether the "valid password" condition holds.
     *
     * @param password the password
     * @param minLength the "min length" value of type {@code int}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    /**
     * Checks whether the "password too long" condition holds.
     *
     * @param password the password
     * @param maxLength the "max length" value of type {@code int}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isPasswordTooLong(String password, int maxLength) {
        return password != null && password.length() > maxLength;
    }

    /**
     * Performs the "contains space" operation.
     *
     * @param password the password
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean containsSpace(String password) {
        return password != null && password.contains(" ");
    }
}