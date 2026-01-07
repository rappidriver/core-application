package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.PaymentStatus;
import com.rappidrive.domain.exceptions.PaymentProcessingException;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.PaymentMethod;
import com.rappidrive.domain.valueobjects.TenantId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Payment entity - represents a payment transaction for a trip.
 * Aggregate root for payment processing.
 */
public class Payment {
    private final UUID id;
    private final UUID tripId;
    private final TenantId tenantId;
    private final Money amount;
    private final Money platformFee;
    private final Money driverAmount;
    private final PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String failureReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    
    public Payment(UUID id, UUID tripId, TenantId tenantId, Money amount,
                  Money platformFee, Money driverAmount, PaymentMethod paymentMethod,
                  PaymentStatus status, String gatewayTransactionId, String failureReason,
                  LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime processedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.tripId = Objects.requireNonNull(tripId, "Trip ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.platformFee = Objects.requireNonNull(platformFee, "Platform fee cannot be null");
        this.driverAmount = Objects.requireNonNull(driverAmount, "Driver amount cannot be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.gatewayTransactionId = gatewayTransactionId;
        this.failureReason = failureReason;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
        this.processedAt = processedAt;
        
        validate();
    }
    
    /**
     * Creates a new payment with PENDING status.
     */
    public static Payment create(UUID tripId, TenantId tenantId, Money amount,
                                Money platformFee, Money driverAmount,
                                PaymentMethod paymentMethod) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(
                UUID.randomUUID(),
                tripId,
                tenantId,
                amount,
                platformFee,
                driverAmount,
                paymentMethod,
                PaymentStatus.PENDING,
                null,
                null,
                now,
                now,
                null
        );
    }
    
    private void validate() {
        if (amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (platformFee.isNegative()) {
            throw new IllegalArgumentException("Platform fee cannot be negative");
        }
        if (driverAmount.isNegative()) {
            throw new IllegalArgumentException("Driver amount cannot be negative");
        }
        
        // Validate that platformFee + driverAmount = amount
        Money sum = platformFee.add(driverAmount);
        if (!sum.equals(amount)) {
            throw new IllegalArgumentException(
                    String.format("Platform fee (%s) + Driver amount (%s) must equal total amount (%s)",
                            platformFee, driverAmount, amount));
        }
    }
    
    /**
     * Marks the payment as being processed by the gateway.
     */
    public void process(String gatewayTransactionId) {
        if (status != PaymentStatus.PENDING) {
            throw PaymentProcessingException.invalidStatusTransition(
                    status.name(), PaymentStatus.PROCESSING.name());
        }
        
        Objects.requireNonNull(gatewayTransactionId, "Gateway transaction ID cannot be null");
        
        this.status = PaymentStatus.PROCESSING;
        this.gatewayTransactionId = gatewayTransactionId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marks the payment as successfully completed.
     */
    public void complete() {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw PaymentProcessingException.invalidStatusTransition(
                    status.name(), PaymentStatus.COMPLETED.name());
        }
        
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = this.processedAt;
    }
    
    /**
     * Marks the payment as failed with a reason.
     */
    public void fail(String reason) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw PaymentProcessingException.invalidStatusTransition(
                    status.name(), PaymentStatus.FAILED.name());
        }
        
        Objects.requireNonNull(reason, "Failure reason cannot be null");
        
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = this.processedAt;
    }
    
    /**
     * Marks the payment as refunded.
     */
    public void refund() {
        if (!canBeRefunded()) {
            throw PaymentProcessingException.cannotRefundNonCompletedPayment(status.name());
        }
        
        if (!paymentMethod.canBeRefunded()) {
            throw PaymentProcessingException.cannotRefundCashPayment();
        }
        
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if this payment can be refunded.
     */
    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED && paymentMethod.canBeRefunded();
    }
    
    /**
     * Checks if this payment belongs to the given tenant.
     */
    public boolean belongsToTenant(TenantId tenantId) {
        return this.tenantId.equals(tenantId);
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getTripId() {
        return tripId;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public Money getAmount() {
        return amount;
    }
    
    public Money getPlatformFee() {
        return platformFee;
    }
    
    public Money getDriverAmount() {
        return driverAmount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Payment{" +
               "id=" + id +
               ", tripId=" + tripId +
               ", tenantId=" + tenantId +
               ", amount=" + amount +
               ", platformFee=" + platformFee +
               ", driverAmount=" + driverAmount +
               ", paymentMethod=" + paymentMethod +
               ", status=" + status +
               ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
               ", failureReason='" + failureReason + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", processedAt=" + processedAt +
               '}';
    }
}
