package com.rappidrive.application.usecases.payment;

import com.rappidrive.application.ports.input.payment.ProcessPaymentInputPort;
import com.rappidrive.application.ports.output.FareConfigurationRepositoryPort;
import com.rappidrive.application.ports.output.PaymentGatewayPort;
import com.rappidrive.application.ports.output.PaymentRepositoryPort;
import com.rappidrive.application.ports.output.TelemetryPort;
import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.exceptions.PaymentProcessingException;
import com.rappidrive.domain.valueobjects.Money;

import java.util.Objects;

/**
 * Use case for processing payments.
 * 
 * Note: This is a simplified version. In a real system, you would:
 * 1. Get the Fare entity from a FareRepositoryPort
 * 2. Verify the trip exists and is completed
 * 3. Update the trip status to COMPLETED after payment
 */
public class ProcessPaymentUseCase implements ProcessPaymentInputPort {
    
    private final PaymentRepositoryPort paymentRepository;
    private final PaymentGatewayPort paymentGateway;
    private final FareConfigurationRepositoryPort fareConfigurationRepository;
    private final TelemetryPort telemetryPort;
    // TODO: Add TripRepositoryPort and FareRepositoryPort when Trip management is implemented
    
    public ProcessPaymentUseCase(PaymentRepositoryPort paymentRepository,
                                PaymentGatewayPort paymentGateway,
                                FareConfigurationRepositoryPort fareConfigurationRepository,
                                TelemetryPort telemetryPort) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.fareConfigurationRepository = fareConfigurationRepository;
        this.telemetryPort = Objects.requireNonNull(telemetryPort, "telemetryPort must not be null");
    }
    
    @Override
    public Payment execute(ProcessPaymentCommand command) {
        return telemetryPort.traceUseCase("usecase.process_payment", () -> processPayment(command));
    }

    private Payment processPayment(ProcessPaymentCommand command) {
        // Check if payment already exists for this trip
        if (paymentRepository.existsByTripId(command.tripId())) {
            throw new PaymentProcessingException("Payment already exists for trip: " + command.tripId());
        }
        
        // TODO: In real implementation, get Fare from FareRepositoryPort
        // For now, we'll create a simplified version with a mock amount
        // Fare fare = fareRepository.findByTripId(command.tripId())
        //         .orElseThrow(() -> new FareNotFoundException("Fare not found for trip: " + command.tripId()));
        
        // Mock fare amount for demonstration (R$ 25.00)
        // In real implementation, this would come from the Fare entity
        Money fareAmount = new Money(25.00);
        
        // TODO: Get tenant ID from Trip entity
        // Trip trip = tripRepository.findById(command.tripId())
        //         .orElseThrow(() -> new TripNotFoundException(command.tripId()));
        // TenantId tenantId = trip.getTenantId();
        
        // For now, using a mock tenant ID (should come from Trip)
        // This is a temporary solution until Trip integration is complete
        Money baseFare = new Money(4.00); // Mock base fare
        FareConfiguration config = fareConfigurationRepository.findByTenantId(
                new com.rappidrive.domain.valueobjects.TenantId(java.util.UUID.randomUUID())
        ).orElse(null); // Will use default values if not found
        
        // Calculate platform fee and driver amount
        Money platformFee;
        Money driverAmount;
        
        if (config != null) {
            platformFee = config.calculatePlatformFee(fareAmount);
            driverAmount = config.calculateDriverAmount(fareAmount);
        } else {
            // Default: 20% platform, 80% driver
            platformFee = fareAmount.multiply(java.math.BigDecimal.valueOf(0.20));
            driverAmount = fareAmount.multiply(java.math.BigDecimal.valueOf(0.80));
        }
        
        // Create payment with PENDING status
        Payment payment = Payment.create(
                command.tripId(),
                new com.rappidrive.domain.valueobjects.TenantId(java.util.UUID.randomUUID()), // Mock tenant ID
                fareAmount,
                platformFee,
                driverAmount,
                command.paymentMethod()
        );
        
        // Process based on payment method
        if (command.paymentMethod().requiresGatewayProcessing()) {
            // Process through gateway
            PaymentGatewayPort.PaymentGatewayRequest request = new PaymentGatewayPort.PaymentGatewayRequest(
                    fareAmount,
                    command.paymentMethod(),
                    "Payment for trip " + command.tripId()
            );
            
            payment.process("pending-txn-" + java.util.UUID.randomUUID()); // Mark as processing
            payment = paymentRepository.save(payment);
            
            PaymentGatewayPort.PaymentGatewayResponse response = paymentGateway.processPayment(request);
            
            if (response.success()) {
                payment.complete();
            } else {
                payment.fail(response.failureReason());
            }
        } else {
            // Cash payment - mark as completed without processing
            payment.complete();
        }
        
        // Save final state
        payment = paymentRepository.save(payment);
        
        // TODO: Update trip status to COMPLETED
        // trip.complete();
        // tripRepository.save(trip);
        
        return payment;
    }
}
