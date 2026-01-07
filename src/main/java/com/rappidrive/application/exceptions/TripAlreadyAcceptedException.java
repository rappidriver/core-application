package com.rappidrive.application.exceptions;

/**
 * Thrown by application layer when a trip has already been accepted by another driver.
 */
public class TripAlreadyAcceptedException extends ApplicationException {
    public TripAlreadyAcceptedException(String message) {
        super(message);
    }

    public TripAlreadyAcceptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
