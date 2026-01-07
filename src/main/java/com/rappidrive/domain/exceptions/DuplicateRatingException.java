package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando se tenta criar uma avaliação duplicada.
 */
public class DuplicateRatingException extends DomainException {
    
    public DuplicateRatingException(String message) {
        super(message);
    }
}
