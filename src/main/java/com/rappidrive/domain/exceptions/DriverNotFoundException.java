package com.rappidrive.domain.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a driver is not found.
 */
public class DriverNotFoundException extends DomainException {
    
    public DriverNotFoundException(UUID id) {
        super(String.format("Driver not found with ID: %s", id));
    }
    
    public DriverNotFoundException(String message) {
        super(message);
    }
}
