package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando se tenta avaliar uma viagem não completada.
 */
public class TripNotCompletedException extends DomainException {
    
    public TripNotCompletedException(String message) {
        super(message);
    }
}
