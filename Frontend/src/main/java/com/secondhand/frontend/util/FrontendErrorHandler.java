package com.secondhand.frontend.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Centralized logging for non-fatal JavaFX/controller errors. */
public final class FrontendErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(FrontendErrorHandler.class.getName());

    /**
     * Creates a new {@code FrontendErrorHandler} instance.
     */
    private FrontendErrorHandler() {
    }

    /**
     * Performs the "log" operation.
     *
     * @param error the "error" value of type {@code Throwable}
     */
    public static void log(Throwable error) {
        if (error == null) return;
        LOGGER.log(Level.WARNING, error.getMessage(), error);
    }

    /**
     * Performs the "message" operation.
     *
     * @param error the "error" value of type {@code Throwable}
     * @param fallback the "fallback" value of type {@code String}
     * @return the resulting string
     */
    public static String message(Throwable error, String fallback) {
        if (error == null || error.getMessage() == null || error.getMessage().isBlank()) {
            return fallback;
        }
        return error.getMessage();
    }
}
