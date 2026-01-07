package com.rappidrive.domain.exceptions;

/**
 * Exception thrown when fare calculation or validation fails.
 */
public class InvalidFareException extends DomainException {
    
    public InvalidFareException(String message) {
        super(message);
    }
    
    public static InvalidFareException negativeDistance() {
        return new InvalidFareException("Distance cannot be negative");
    }
    
    public static InvalidFareException negativeDuration() {
        return new InvalidFareException("Duration cannot be negative");
    }
    
    public static InvalidFareException zeroDistance() {
        return new InvalidFareException("Distance must be greater than zero");
    }
    
    public static InvalidFareException zeroDuration() {
        return new InvalidFareException("Duration must be greater than zero");
    }
    
    public static InvalidFareException invalidAmount(String reason) {
        return new InvalidFareException("Invalid fare amount: " + reason);
    }
}
