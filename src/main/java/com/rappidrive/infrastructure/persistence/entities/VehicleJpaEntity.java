package com.rappidrive.infrastructure.persistence.entities;

import com.rappidrive.domain.enums.VehicleStatus;
import com.rappidrive.domain.enums.VehicleType;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para persistência de veículos.
 * Não deve ser exposta fora da camada de infraestrutura.
 */
@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicles_license_plate_tenant", columnList = "license_plate,tenant_id", unique = true),
    @Index(name = "idx_vehicles_driver", columnList = "driver_id"),
    @Index(name = "idx_vehicles_tenant", columnList = "tenant_id"),
    @Index(name = "idx_vehicles_status_tenant", columnList = "status,tenant_id")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class VehicleJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "driver_id")
    private UUID driverId;
    
    @Column(name = "license_plate", nullable = false, length = 8)
    private String licensePlate;
    
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;
    
    @Column(name = "model", nullable = false, length = 100)
    private String model;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "color", nullable = false, length = 30)
    private String color;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType type;
    
    @Column(name = "number_of_doors", nullable = false)
    private Integer numberOfDoors;
    
    @Column(name = "seats", nullable = false)
    private Integer seats;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VehicleStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // JPA requires no-arg constructor
    protected VehicleJpaEntity() {
    }
    
    public VehicleJpaEntity(UUID id, UUID tenantId, UUID driverId, String licensePlate,
                            String brand, String model, Integer year, String color,
                            VehicleType type, Integer numberOfDoors, Integer seats,
                            VehicleStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.driverId = driverId;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.type = type;
        this.numberOfDoors = numberOfDoors;
        this.seats = seats;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    
    public UUID getDriverId() {
        return driverId;
    }
    
    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    public void setType(VehicleType type) {
        this.type = type;
    }
    
    public Integer getNumberOfDoors() {
        return numberOfDoors;
    }
    
    public void setNumberOfDoors(Integer numberOfDoors) {
        this.numberOfDoors = numberOfDoors;
    }
    
    public Integer getSeats() {
        return seats;
    }
    
    public void setSeats(Integer seats) {
        this.seats = seats;
    }
    
    public VehicleStatus getStatus() {
        return status;
    }
    
    public void setStatus(VehicleStatus status) {
        this.status = status;
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
