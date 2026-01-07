package com.rappidrive.domain.enums;

/**
 * Type of payment method used for a transaction.
 */
public enum PaymentMethodType {
    /**
     * Credit card payment.
     */
    CREDIT_CARD,
    
    /**
     * Debit card payment.
     */
    DEBIT_CARD,
    
    /**
     * PIX instant payment (Brazil).
     */
    PIX,
    
    /**
     * Cash payment (no electronic processing).
     */
    CASH;
    
    /**
     * Checks if this payment method can be refunded electronically.
     *
     * @return true for CREDIT_CARD, DEBIT_CARD, and PIX; false for CASH
     */
    public boolean isRefundable() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PIX;
    }
    
    /**
     * Checks if this payment method requires gateway processing.
     *
     * @return true for CREDIT_CARD, DEBIT_CARD, and PIX; false for CASH
     */
    public boolean requiresGatewayProcessing() {
        return this != CASH;
    }
}
