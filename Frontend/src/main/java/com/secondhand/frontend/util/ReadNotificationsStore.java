package com.secondhand.frontend.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ذخیرهٔ محلی کلید اعلان‌های خوانده‌شده (به تفکیک هر کاربر)
 * در ~/.dastdovom-market/read-notifs-<userId>.txt
 */
public final class ReadNotificationsStore {

    private ReadNotificationsStore() {}

    private static Path fileFor(String userKey) {
        Path dir = Paths.get(System.getProperty("user.home"), ".dastdovom-market");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir.resolve("read-notifs-" + userKey + ".txt");
    }

    public static synchronized Set<String> readKeys(String userKey) {
        Set<String> keys = new HashSet<>();
        try {
            Path f = fileFor(userKey);
            if (Files.exists(f)) {
                for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                    if (!line.isBlank()) keys.add(line.trim());
                }
            }
        } catch (IOException ignored) {}
        return keys;
    }

    public static synchronized void markRead(String userKey, String key) {
        Set<String> keys = readKeys(userKey);
        if (keys.add(key)) save(userKey, keys);
    }

    public static synchronized void markAllRead(String userKey, Collection<String> newKeys) {
        Set<String> keys = readKeys(userKey);
        if (keys.addAll(newKeys)) save(userKey, keys);
    }

    private static void save(String userKey, Set<String> keys) {
        try {
            Files.write(fileFor(userKey), String.join("\n", keys).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }
}
