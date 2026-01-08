package com.rappidrive.application.usecases.driver;

/**
 * Exception thrown when driver geospatial query operations fail.
 * Wraps database or infrastructure errors to prevent leaking implementation details.
 */
public class DriverQueryException extends RuntimeException {
    
    public DriverQueryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DriverQueryException(String message) {
        super(message);
    }
}
