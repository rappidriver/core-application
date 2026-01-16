package com.rappidrive.infrastructure.persistence.entities;

import com.rappidrive.domain.enums.PaymentMethodType;
import com.rappidrive.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Payment persistence.
 * Not exposed outside infrastructure layer.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_trip", columnList = "trip_id", unique = true),
    @Index(name = "idx_payment_tenant", columnList = "tenant_id"),
    @Index(name = "idx_payment_tenant_created", columnList = "tenant_id,created_at"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_gateway_transaction", columnList = "gateway_transaction_id")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class PaymentJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "trip_id", nullable = false)
    private UUID tripId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;
    
    @Column(name = "driver_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal driverAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", nullable = false, length = 20)
    private PaymentMethodType paymentMethodType;
    
    @Column(name = "card_last_4", length = 4)
    private String cardLast4;
    
    @Column(name = "card_brand", length = 50)
    private String cardBrand;
    
    @Column(name = "pix_key", length = 200)
    private String pixKey;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;
    
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    protected PaymentJpaEntity() {
    }
    
    public PaymentJpaEntity(UUID id, UUID tenantId, UUID tripId, BigDecimal amount, String currency,
                           BigDecimal platformFee, BigDecimal driverAmount,
                           PaymentMethodType paymentMethodType, String cardLast4, String cardBrand,
                           String pixKey, PaymentStatus status, String gatewayTransactionId,
                           String failureReason, LocalDateTime processedAt, LocalDateTime refundedAt,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.tripId = tripId;
        this.amount = amount;
        this.currency = currency;
        this.platformFee = platformFee;
        this.driverAmount = driverAmount;
        this.paymentMethodType = paymentMethodType;
        this.cardLast4 = cardLast4;
        this.cardBrand = cardBrand;
        this.pixKey = pixKey;
        this.status = status;
        this.gatewayTransactionId = gatewayTransactionId;
        this.failureReason = failureReason;
        this.processedAt = processedAt;
        this.refundedAt = refundedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
    
    public UUID getTripId() {
        return tripId;
    }
    
    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public BigDecimal getPlatformFee() {
        return platformFee;
    }
    
    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }
    
    public BigDecimal getDriverAmount() {
        return driverAmount;
    }
    
    public void setDriverAmount(BigDecimal driverAmount) {
        this.driverAmount = driverAmount;
    }
    
    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    
    public void setPaymentMethodType(PaymentMethodType paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }
    
    public String getCardLast4() {
        return cardLast4;
    }
    
    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }
    
    public String getCardBrand() {
        return cardBrand;
    }
    
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
    
    public String getPixKey() {
        return pixKey;
    }
    
    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    
    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
