package com.rappidrive.domain.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a trip is not found.
 */
public class TripNotFoundException extends DomainException {
    
    public TripNotFoundException(UUID id) {
        super(String.format("Trip not found with ID: %s", id));
    }
    
    public TripNotFoundException(String message) {
        super(message);
    }
}
