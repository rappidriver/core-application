package com.rappidrive.application.usecases.payment;

import com.rappidrive.application.ports.input.payment.GetPaymentInputPort;
import com.rappidrive.application.ports.output.PaymentRepositoryPort;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.exceptions.PaymentNotFoundException;
import com.rappidrive.domain.valueobjects.TenantId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Use case for getting payments.
 */
public class GetPaymentUseCase implements GetPaymentInputPort {
    
    private final PaymentRepositoryPort paymentRepository;
    
    public GetPaymentUseCase(PaymentRepositoryPort paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    @Override
    public Payment execute(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.withId(paymentId));
    }
    
    @Override
    public List<Payment> findByTrip(UUID tripId) {
        return paymentRepository.findByTripId(tripId)
                .map(List::of)
                .orElse(List.of());
    }
    
    @Override
    public List<Payment> findByTenant(TenantId tenantId, LocalDateTime from, LocalDateTime to) {
        return paymentRepository.findByTenantIdAndCreatedAtBetween(tenantId, from, to);
    }
}
