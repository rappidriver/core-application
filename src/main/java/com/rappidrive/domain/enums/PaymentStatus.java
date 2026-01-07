package com.rappidrive.domain.enums;

/**
 * Status of a payment transaction.
 * Represents the lifecycle of a payment from pending to final state.
 */
public enum PaymentStatus {
    /**
     * Payment has been created but not yet processed.
     */
    PENDING,
    
    /**
     * Payment is currently being processed by the payment gateway.
     */
    PROCESSING,
    
    /**
     * Payment has been successfully completed.
     */
    COMPLETED,
    
    /**
     * Payment processing failed.
     */
    FAILED,
    
    /**
     * Payment has been refunded to the customer.
     */
    REFUNDED;
    
    /**
     * Checks if the payment is in a final state (cannot change anymore).
     *
     * @return true if status is COMPLETED, FAILED, or REFUNDED
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED;
    }
    
    /**
     * Checks if the payment can be refunded.
     *
     * @return true if status is COMPLETED
     */
    public boolean canBeRefunded() {
        return this == COMPLETED;
    }
}
