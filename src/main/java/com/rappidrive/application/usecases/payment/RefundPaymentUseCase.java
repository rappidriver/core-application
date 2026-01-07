package com.rappidrive.application.usecases.payment;

import com.rappidrive.application.ports.input.payment.RefundPaymentInputPort;
import com.rappidrive.application.ports.output.PaymentGatewayPort;
import com.rappidrive.application.ports.output.PaymentRepositoryPort;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.exceptions.PaymentNotFoundException;
import com.rappidrive.domain.exceptions.PaymentProcessingException;

import java.util.UUID;

/**
 * Use case for refunding payments.
 */
public class RefundPaymentUseCase implements RefundPaymentInputPort {
    
    private final PaymentRepositoryPort paymentRepository;
    private final PaymentGatewayPort paymentGateway;
    
    public RefundPaymentUseCase(PaymentRepositoryPort paymentRepository,
                               PaymentGatewayPort paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }
    
    @Override
    public Payment execute(UUID paymentId, String reason) {
        // Find payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.withId(paymentId));
        
        // Validate refund is possible
        if (!payment.canBeRefunded()) {
            if (!payment.getPaymentMethod().canBeRefunded()) {
                throw PaymentProcessingException.cannotRefundCashPayment();
            } else {
                throw PaymentProcessingException.cannotRefundNonCompletedPayment(payment.getStatus().name());
            }
        }
        
        // Process refund through gateway
        PaymentGatewayPort.PaymentGatewayResponse response = paymentGateway.refundPayment(
                payment.getGatewayTransactionId(),
                payment.getAmount()
        );
        
        if (!response.success()) {
            throw PaymentProcessingException.gatewayFailure(
                    "Refund failed: " + response.failureReason());
        }
        
        // Mark payment as refunded
        payment.refund();
        
        // Save and return
        return paymentRepository.save(payment);
    }
}
