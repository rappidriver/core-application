package com.rappidrive.domain.exceptions;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentProcessingException extends DomainException {
    
    public PaymentProcessingException(String message) {
        super(message);
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static PaymentProcessingException gatewayFailure(String reason) {
        return new PaymentProcessingException("Payment gateway processing failed: " + reason);
    }
    
    public static PaymentProcessingException cannotRefundCashPayment() {
        return new PaymentProcessingException("Cannot refund cash payment electronically");
    }
    
    public static PaymentProcessingException cannotRefundNonCompletedPayment(String currentStatus) {
        return new PaymentProcessingException("Cannot refund payment with status: " + currentStatus + 
                ". Only COMPLETED payments can be refunded");
    }
    
    public static PaymentProcessingException invalidStatusTransition(String from, String to) {
        return new PaymentProcessingException("Invalid payment status transition from " + from + " to " + to);
    }
}
