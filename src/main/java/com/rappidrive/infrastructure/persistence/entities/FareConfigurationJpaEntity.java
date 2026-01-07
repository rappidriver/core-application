package com.rappidrive.infrastructure.persistence.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for FareConfiguration persistence.
 * Not exposed outside infrastructure layer.
 */
@Entity
@Table(name = "fare_configurations", indexes = {
    @Index(name = "idx_fare_config_tenant", columnList = "tenant_id", unique = true)
})
public class FareConfigurationJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(name = "price_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKm;
    
    @Column(name = "price_per_minute", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerMinute;
    
    @Column(name = "minimum_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumFare;
    
    @Column(name = "platform_commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal platformCommissionRate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    protected FareConfigurationJpaEntity() {
    }
    
    public FareConfigurationJpaEntity(UUID id, UUID tenantId, BigDecimal baseFare,
                                      BigDecimal pricePerKm, BigDecimal pricePerMinute,
                                      BigDecimal minimumFare, BigDecimal platformCommissionRate,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.baseFare = baseFare;
        this.pricePerKm = pricePerKm;
        this.pricePerMinute = pricePerMinute;
        this.minimumFare = minimumFare;
        this.platformCommissionRate = platformCommissionRate;
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
    
    public BigDecimal getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }
    
    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }
    
    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
    
    public BigDecimal getPricePerMinute() {
        return pricePerMinute;
    }
    
    public void setPricePerMinute(BigDecimal pricePerMinute) {
        this.pricePerMinute = pricePerMinute;
    }
    
    public BigDecimal getMinimumFare() {
        return minimumFare;
    }
    
    public void setMinimumFare(BigDecimal minimumFare) {
        this.minimumFare = minimumFare;
    }
    
    public BigDecimal getPlatformCommissionRate() {
        return platformCommissionRate;
    }
    
    public void setPlatformCommissionRate(BigDecimal platformCommissionRate) {
        this.platformCommissionRate = platformCommissionRate;
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
