package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando uma avaliação é inválida.
 */
public class InvalidRatingException extends DomainException {
    
    public InvalidRatingException(String message) {
        super(message);
    }
    
    public InvalidRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
