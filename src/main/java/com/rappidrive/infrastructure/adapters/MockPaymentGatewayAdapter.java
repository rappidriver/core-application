package com.rappidrive.infrastructure.adapters;

import com.rappidrive.application.ports.output.PaymentGatewayPort;
import com.rappidrive.domain.valueobjects.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Mock implementation of PaymentGatewayPort for MVP.
 * Simulates payment gateway behavior with 95% success rate.
 * 
 * In production, replace with real payment gateway integration
 * (Stripe, PayPal, PagSeguro, Mercado Pago, etc.).
 */
@Component
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {
    
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentGatewayAdapter.class);
    private static final double SUCCESS_RATE = 0.95; // 95% success rate
    private static final String[] FAILURE_REASONS = {
        "Insufficient funds",
        "Card expired",
        "Invalid card number",
        "Transaction declined by issuer",
        "Gateway timeout"
    };
    
    private final Random random = new Random();
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        logger.info("Processing payment: amount={}, method={}, description={}",
            request.amount(), request.paymentMethod(), request.description());
        
        // Simulate processing delay
        simulateDelay(500);
        
        boolean success = random.nextDouble() < SUCCESS_RATE;
        
        if (success) {
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            logger.info("Payment processed successfully: transactionId={}", transactionId);
            return new PaymentGatewayResponse(
                true,
                transactionId,
                null
            );
        } else {
            String failureReason = FAILURE_REASONS[random.nextInt(FAILURE_REASONS.length)];
            logger.warn("Payment processing failed: reason={}", failureReason);
            return new PaymentGatewayResponse(
                false,
                null,
                failureReason
            );
        }
    }
    
    @Override
    public PaymentGatewayResponse refundPayment(String transactionId, Money amount) {
        logger.info("Processing refund: transactionId={}, amount={}",
            transactionId, amount);
        
        // Simulate processing delay
        simulateDelay(300);
        
        // Refunds have higher success rate (98%)
        boolean success = random.nextDouble() < 0.98;
        
        if (success) {
            String refundId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            logger.info("Refund processed successfully: refundId={}", refundId);
            return new PaymentGatewayResponse(
                true,
                refundId,
                null
            );
        } else {
            String failureReason = "Refund window expired";
            logger.warn("Refund processing failed: reason={}", failureReason);
            return new PaymentGatewayResponse(
                false,
                null,
                failureReason
            );
        }
    }
    
    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while simulating gateway delay", e);
        }
    }
}
