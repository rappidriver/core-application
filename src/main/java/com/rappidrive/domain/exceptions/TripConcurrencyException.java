package com.rappidrive.domain.exceptions;

/**
 * Thrown when a concurrent modification conflict occurs on a Trip aggregate (optimistic locking).
 */
public class TripConcurrencyException extends DomainException {

    public TripConcurrencyException(String message) {
        super(message);
    }

    public TripConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

}
