package com.rappidrive.domain.exceptions;

/**
 * Exception thrown when driver state transition is invalid.
 */
public class InvalidDriverStateException extends DomainException {
    
    public InvalidDriverStateException(String message) {
        super(message);
    }
    
    public InvalidDriverStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
