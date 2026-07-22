package com.secondhand.frontend.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Centralized logging for non-fatal JavaFX/controller errors. */
public final class FrontendErrorHandler {

    private static final Logger LOGGER =
            Logger.getLogger(FrontendErrorHandler.class.getName());

    private FrontendErrorHandler() {
    }

    public static void log(Throwable error) {
        if (error == null) return;

        LOGGER.log(
                Level.WARNING,
                error.getMessage(),
                error
        );
    }

    public static String message(Throwable error, String fallback) {
        if (error == null
                || error.getMessage() == null
                || error.getMessage().isBlank()) {
            return fallback;
        }

        return error.getMessage();
    }
}