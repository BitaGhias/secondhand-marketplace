package com.secondhand.frontend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ذخیره نشست کاربر (توکن JWT + اطلاعات کاربر) بین اجراهای برنامه
 * تا کاربر با هر بار باز کردن اپ مجبور به ورود دوباره نباشد.
 * فایل نشست در پوشه خانه کاربر ذخیره می‌شود: ~/.dastdovom-market/session.json
 * (در شروع برنامه، اعتبار توکن با فراخوانی پروفایل از سرور بررسی می‌شود.)
 */
public final class SessionStore {

    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".dastdovom-market");
    private static final Path FILE = DIR.resolve("session.json");
    private static final ObjectMapper mapper = ApiClient.getMapper();

    private SessionStore() {}

    public static class SavedSession {
        public String token;
        public User user;

        public SavedSession() {}

        public SavedSession(String token, User user) {
            this.token = token;
            this.user = user;
        }
    }

    /** ذخیره نشست پس از ورود موفق */
    public static void save(String token, User user) {
        if (token == null || token.isBlank() || user == null) return;
        try {
            Files.createDirectories(DIR);
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), new SavedSession(token, user));
        } catch (IOException ignored) {
            // ذخیره نشست اختیاری است؛ خطای آن نباید جریان برنامه را بشکند
        }
    }

    /** خواندن نشست ذخیره‌شده (null اگر وجود نداشته یا ناقص باشد) */
    public static SavedSession load() {
        try {
            if (!Files.exists(FILE)) return null;
            SavedSession s = mapper.readValue(FILE.toFile(), SavedSession.class);
            if (s == null || s.token == null || s.token.isBlank() || s.user == null) return null;
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    /** حذف نشست (هنگام خروج یا نامعتبر شدن توکن) */
    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException ignored) {
        }
    }
}
