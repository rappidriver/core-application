package com.rappidrive.domain.exceptions;

/**
 * Exception thrown when trip state transition is invalid.
 */
public class InvalidTripStateException extends DomainException {
    
    public InvalidTripStateException(String message) {
        super(message);
    }
    
    public InvalidTripStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
