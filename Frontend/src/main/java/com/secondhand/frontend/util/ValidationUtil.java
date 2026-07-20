package com.secondhand.frontend.util;

public class ValidationUtil {

    /**
     * بررسی معتبر بودن ساختار ایمیل
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // یک رگکس استاندارد و ساده برای ایمیل
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    /**
     * بررسی معتبر بودن شماره تلفن همراه ایران (مثلا 09123456789)
     */
    public static boolean isValidIranianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^09[0-9]{9}$");
    }

    /**
     * بررسی حداقل طول رمز عبور (مثلاً حداقل ۶ کاراکتر)
     */
    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }
}