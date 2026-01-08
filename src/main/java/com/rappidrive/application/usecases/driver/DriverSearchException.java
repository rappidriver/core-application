package com.rappidrive.application.usecases.driver;

/**
 * Exception thrown when driver search operations fail.
 */
public class DriverSearchException extends RuntimeException {
    
    public DriverSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
