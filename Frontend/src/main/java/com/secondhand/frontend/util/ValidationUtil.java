package com.secondhand.frontend.util;

public class ValidationUtil {

    /**
     * بررسی معتبر بودن ساختار ایمیل
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * تبدیل ارقام فارسی/عربی به انگلیسی (مثلاً شماره تلفن با کیبورد فارسی تایپ‌شده)
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
     * بررسی معتبر بودن شماره تلفن همراه ایران (مثلا 09123456789)؛ ارقام فارسی/عربی هم پذیرفته می‌شوند
     */
    public static boolean isValidIranianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return normalizeDigits(phone.trim()).matches("^09[0-9]{9}$");
    }

    /**
     * بررسی فرمت نام کاربری (فقط حروف انگلیسی، عدد و _ ، بین ۳ تا ۲۰ کاراکتر)
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return username.trim().matches("^[A-Za-z0-9_]{3,20}$");
    }

    /**
     * بررسی فرمت نام کامل (فقط حروف و فاصله، بین ۳ تا ۵۰ کاراکتر)
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return false;
        return fullName.trim().matches("^[\\p{L} ]{3,50}$");
    }

    /**
     * بررسی حداقل طول رمز عبور (مثلاً حداقل ۶ کاراکتر)
     */
    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    /**
     * بررسی حداکثر طول رمز عبور
     */
    public static boolean isPasswordTooLong(String password, int maxLength) {
        return password != null && password.length() > maxLength;
    }

    /**
     * بررسی وجود فاصله در رمز عبور (رمز عبور نباید فاصله داشته باشد)
     */
    public static boolean containsSpace(String password) {
        return password != null && password.contains(" ");
    }
}