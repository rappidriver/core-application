package com.rappidrive.application.ports.output;

import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.PaymentMethod;

/**
 * Output port for payment gateway integration.
 * Implementations handle communication with external payment processors.
 */
public interface PaymentGatewayPort {
    
    /**
     * Processes a payment through the payment gateway.
     *
     * @param request the payment request
     * @return the payment response
     */
    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);
    
    /**
     * Refunds a payment through the payment gateway.
     *
     * @param transactionId the original transaction ID
     * @param amount the amount to refund
     * @return the refund response
     */
    PaymentGatewayResponse refundPayment(String transactionId, Money amount);
    
    /**
     * Payment gateway request.
     */
    record PaymentGatewayRequest(
            Money amount,
            PaymentMethod paymentMethod,
            String description
    ) {
        public PaymentGatewayRequest {
            if (amount == null) {
                throw new IllegalArgumentException("Amount cannot be null");
            }
            if (paymentMethod == null) {
                throw new IllegalArgumentException("Payment method cannot be null");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty");
            }
        }
    }
    
    /**
     * Payment gateway response.
     */
    record PaymentGatewayResponse(
            boolean success,
            String transactionId,
            String failureReason
    ) {
        public PaymentGatewayResponse {
            if (success && transactionId == null) {
                throw new IllegalArgumentException("Transaction ID cannot be null for successful payments");
            }
            if (!success && failureReason == null) {
                throw new IllegalArgumentException("Failure reason cannot be null for failed payments");
            }
        }
    }
}
