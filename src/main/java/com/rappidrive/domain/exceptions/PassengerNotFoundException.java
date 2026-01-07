package com.rappidrive.domain.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a passenger is not found.
 */
public class PassengerNotFoundException extends DomainException {
    
    public PassengerNotFoundException(UUID id) {
        super(String.format("Passenger not found with ID: %s", id));
    }
    
    public PassengerNotFoundException(String message) {
        super(message);
    }
}
