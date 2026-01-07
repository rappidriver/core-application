package com.rappidrive.domain.exceptions;

/**
 * Exception thrown when passenger state transition is invalid.
 */
public class InvalidPassengerStateException extends DomainException {
    
    public InvalidPassengerStateException(String message) {
        super(message);
    }
    
    public InvalidPassengerStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
