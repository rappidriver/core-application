package com.rappidrive.infrastructure.persistence.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for ServiceArea.
 * Uses PostGIS geometry type for storing GeoJSON polygons.
 */
@Entity
@Table(name = "service_areas", indexes = {
        @Index(name = "idx_service_area_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_service_area_active", columnList = "active")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class ServiceAreaJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    /**
     * GeoJSON polygon stored as JSONB for flexibility.
     * PostGIS geometry column can be added later for spatial queries if needed.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geojson_polygon", nullable = false, columnDefinition = "jsonb")
    private String geoJsonPolygon;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // JPA requires default constructor
    protected ServiceAreaJpaEntity() {
    }
    
    public ServiceAreaJpaEntity(UUID id, UUID tenantId, String name, String geoJsonPolygon, 
                                boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.geoJsonPolygon = geoJsonPolygon;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGeoJsonPolygon() {
        return geoJsonPolygon;
    }
    
    public void setGeoJsonPolygon(String geoJsonPolygon) {
        this.geoJsonPolygon = geoJsonPolygon;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
