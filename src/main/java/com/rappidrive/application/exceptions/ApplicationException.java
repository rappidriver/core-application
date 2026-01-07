package com.rappidrive.application.exceptions;

/**
 * Base exception for all application-related errors.
 * Application exceptions represent use case execution failures.
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
