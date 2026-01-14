package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.VehicleStatus;
import com.rappidrive.domain.enums.VehicleType;
import com.rappidrive.domain.exceptions.InvalidVehicleStateException;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.VehicleYear;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representando um veículo na plataforma.
 * Regras de negócio:
 * - Apenas veículos de passeio (SEDAN, HATCHBACK, SUV)
 * - Exatamente 4 portas
 * - Máximo 10 anos de uso
 * - Apenas 1 veículo ACTIVE por motorista
 */
public class Vehicle {
    
    private static final int REQUIRED_DOORS = 4;
    private static final int MAX_VEHICLE_AGE = 10;
    
    private final UUID id;
    private final TenantId tenantId;
    private final LicensePlate licensePlate;
    private final String brand;
    private final String model;
    private final VehicleYear year;
    private final String color;
    private final VehicleType type;
    private final int numberOfDoors;
    private final int seats;
    
    private UUID driverId;
    private VehicleStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating a new vehicle (without driver assignment).
     */
    public Vehicle(UUID id, TenantId tenantId, LicensePlate licensePlate,
                   String brand, String model, VehicleYear year, String color,
                   VehicleType type, int numberOfDoors, int seats) {
        this(id, tenantId, null, licensePlate, brand, model, year, color,
             type, numberOfDoors, seats, VehicleStatus.INACTIVE, 
             LocalDateTime.now(), LocalDateTime.now());
    }
    
    /**
     * Reconstruction constructor (for persistence layer).
     */
    public Vehicle(UUID id, TenantId tenantId, UUID driverId,
                   LicensePlate licensePlate, String brand, String model,
                   VehicleYear year, String color, VehicleType type,
                   int numberOfDoors, int seats, VehicleStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateInvariants(licensePlate, brand, model, year, color, type, numberOfDoors, seats);
        
        this.id = Objects.requireNonNull(id, "Vehicle ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.driverId = driverId;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.type = type;
        this.numberOfDoors = numberOfDoors;
        this.seats = seats;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }
    
    private void validateInvariants(LicensePlate licensePlate, String brand, String model,
                                    VehicleYear year, String color, VehicleType type,
                                    int numberOfDoors, int seats) {
        Objects.requireNonNull(licensePlate, "License plate cannot be null");
        Objects.requireNonNull(year, "Vehicle year cannot be null");
        Objects.requireNonNull(type, "Vehicle type cannot be null");
        
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand cannot be null or empty");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        if (numberOfDoors != REQUIRED_DOORS) {
            throw InvalidVehicleStateException.invalidNumberOfDoors(numberOfDoors);
        }
        if (seats < 1 || seats > 12) {
            throw new IllegalArgumentException("Seats must be between 1 and 12");
        }
        if (!type.isPassengerVehicle()) {
            throw InvalidVehicleStateException.notPassengerVehicle(type.name());
        }
    }
    
    /**
     * Ativa o veículo, tornando-o disponível para corridas.
     * Validações:
     * - Deve ter motorista associado
     * - Não pode ter mais de 10 anos
     * - Deve ter 4 portas
     * - Deve ser veículo de passeio
     */
    public void activate() {
        if (driverId == null) {
            throw InvalidVehicleStateException.cannotActivateWithoutDriver();
        }
        
        int age = year.getAge();
        if (age > MAX_VEHICLE_AGE) {
            throw InvalidVehicleStateException.vehicleTooOld(age, MAX_VEHICLE_AGE);
        }
        
        if (numberOfDoors != REQUIRED_DOORS) {
            throw InvalidVehicleStateException.invalidNumberOfDoors(numberOfDoors);
        }
        
        if (!isPassengerVehicle()) {
            throw InvalidVehicleStateException.notPassengerVehicle(type.name());
        }
        
        this.status = VehicleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Desativa o veículo.
     */
    public void deactivate() {
        this.status = VehicleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca o veículo como em manutenção.
     */
    public void markAsInMaintenance() {
        this.status = VehicleStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Associa o veículo a um motorista.
     */
    public void assignToDriver(UUID driverId) {
        this.driverId = Objects.requireNonNull(driverId, "Driver ID cannot be null");
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove a associação com motorista.
     */
    public void unassignDriver() {
        if (status == VehicleStatus.ACTIVE) {
            throw new InvalidVehicleStateException(
                "Cannot unassign driver from active vehicle. Deactivate first."
            );
        }
        this.driverId = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Atualiza informações do veículo.
     */
    public void updateColor(String color) {
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o veículo pode aceitar corridas.
     */
    public boolean canAcceptRides() {
        return status.canAcceptRides();
    }
    
    /**
     * Verifica se o veículo pertence ao motorista especificado.
     */
    public boolean isOwnedBy(UUID driverId) {
        return this.driverId != null && this.driverId.equals(driverId);
    }
    
    /**
     * Verifica se é um veículo de passeio.
     */
    public boolean isPassengerVehicle() {
        return type.isPassengerVehicle();
    }
    
    /**
     * Verifica se o veículo está ativo.
     */
    public boolean isActive() {
        return status == VehicleStatus.ACTIVE;
    }
    
    // Getters
    
    public UUID getId() {
        return id;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public UUID getDriverId() {
        return driverId;
    }
    
    public LicensePlate getLicensePlate() {
        return licensePlate;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public VehicleYear getYear() {
        return year;
    }
    
    public String getColor() {
        return color;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    public int getNumberOfDoors() {
        return numberOfDoors;
    }
    
    public int getSeats() {
        return seats;
    }
    
    public VehicleStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", licensePlate=" + licensePlate +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}
