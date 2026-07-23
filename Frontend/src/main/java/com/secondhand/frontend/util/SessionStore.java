package com.secondhand.frontend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class providing "session store" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public final class SessionStore {

    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".dastdovom-market");
    private static final Path FILE = DIR.resolve("session.json");
    private static final ObjectMapper mapper = ApiClient.getMapper();

    /**
     * Creates a new {@code SessionStore} instance.
     */
    private SessionStore() {}

    public static class SavedSession {
        public String token;
        public User user;

        /**
         * Performs the "saved session" operation.
         */
        public SavedSession() {}

        public SavedSession(String token, User user) {
            this.token = token;
            this.user = user;
        }
    }

    /**
     * Saves.
     *
     * @param token JWT authentication token
     * @param user the user object
     */
    public static void save(String token, User user) {
        if (token == null || token.isBlank() || user == null) return;
        try {
            Files.createDirectories(DIR);
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), new SavedSession(token, user));
        } catch (IOException ignored) {
            // ذخیره نشست اختیاری است؛ خطای آن نباید جریان برنامه را بشکند
        }
    }

    /**
     * Loads.
     *
     * @return the resulting {@code SavedSession} instance
     */
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

    /**
     * Clears.
     */
    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException ignored) {
        }
    }
}
