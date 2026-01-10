package com.rappidrive.application.exceptions;

/**
 * Thrown when an admin lacks permission to perform an approval operation.
 */
public class AdminUnauthorizedException extends ApplicationException {
    public AdminUnauthorizedException(String message) {
        super(message);
    }
}
