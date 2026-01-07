package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando uma avaliação não é encontrada.
 */
public class RatingNotFoundException extends DomainException {
    
    public RatingNotFoundException(String message) {
        super(message);
    }
}
