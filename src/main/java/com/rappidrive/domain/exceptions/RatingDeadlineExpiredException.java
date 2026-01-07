package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando o prazo para avaliar expirou.
 */
public class RatingDeadlineExpiredException extends DomainException {
    
    public RatingDeadlineExpiredException(String message) {
        super(message);
    }
}
