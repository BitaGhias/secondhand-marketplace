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
 * Utility class providing "read notifications store" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public final class ReadNotificationsStore {

    /**
     * Creates a new {@code ReadNotificationsStore} instance.
     */
    private ReadNotificationsStore() {}

    private static Path fileFor(String userKey) {
        Path dir = Paths.get(System.getProperty("user.home"), ".dastdovom-market");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir.resolve("read-notifs-" + userKey + ".txt");
    }

    /**
     * Performs the "read keys" operation.
     *
     * @param userKey the "user key" value of type {@code String}
     * @return a {@code Set<String>} with the results; empty if nothing matches
     */
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

    /**
     * Marks read.
     *
     * @param userKey the "user key" value of type {@code String}
     * @param key the "key" value of type {@code String}
     */
    public static synchronized void markRead(String userKey, String key) {
        Set<String> keys = readKeys(userKey);
        if (keys.add(key)) save(userKey, keys);
    }

    /**
     * Marks all read.
     *
     * @param userKey the "user key" value of type {@code String}
     * @param newKeys the "new keys" value of type {@code Collection<String>}
     */
    public static synchronized void markAllRead(String userKey, Collection<String> newKeys) {
        Set<String> keys = readKeys(userKey);
        if (keys.addAll(newKeys)) save(userKey, keys);
    }

    /**
     * Saves.
     *
     * @param userKey the "user key" value of type {@code String}
     * @param keys the "keys" value of type {@code Set<String>}
     */
    private static void save(String userKey, Set<String> keys) {
        try {
            Files.write(fileFor(userKey), String.join("\n", keys).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }
}
