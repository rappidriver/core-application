package com.rappidrive.domain.exceptions;

public class PaymentServiceUnavailableException extends DomainException {
    public PaymentServiceUnavailableException(String message) {
        super(message);
    }
    public PaymentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
