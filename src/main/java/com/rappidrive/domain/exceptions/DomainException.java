package com.rappidrive.domain.exceptions;

/**
 * Base exception for all domain-related errors.
 * All domain exceptions should extend this class.
 * Domain exceptions represent business rule violations.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

}
