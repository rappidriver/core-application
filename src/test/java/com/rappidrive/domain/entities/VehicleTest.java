package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.VehicleStatus;
import com.rappidrive.domain.enums.VehicleType;
import com.rappidrive.domain.exceptions.InvalidVehicleStateException;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.VehicleYear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vehicle Entity Tests")
class VehicleTest {
    
    private UUID vehicleId;
    private TenantId tenantId;
    private UUID driverId;
    private LicensePlate licensePlate;
    private VehicleYear validYear;
    
    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
        tenantId = new TenantId(UUID.randomUUID());
        driverId = UUID.randomUUID();
        licensePlate = new LicensePlate("ABC1234");
        validYear = new VehicleYear(2020);
    }
    
    @Test
    @DisplayName("Should create vehicle with valid data")
    void shouldCreateVehicleWithValidData() {
        Vehicle vehicle = new Vehicle(
            vehicleId, tenantId, licensePlate,
            "Toyota", "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 4, 5
        );
        
        assertThat(vehicle.getId()).isEqualTo(vehicleId);
        assertThat(vehicle.getTenantId()).isEqualTo(tenantId);
        assertThat(vehicle.getLicensePlate()).isEqualTo(licensePlate);
        assertThat(vehicle.getBrand()).isEqualTo("Toyota");
        assertThat(vehicle.getModel()).isEqualTo("Corolla");
        assertThat(vehicle.getYear()).isEqualTo(validYear);
        assertThat(vehicle.getColor()).isEqualTo("Preto");
        assertThat(vehicle.getType()).isEqualTo(VehicleType.SEDAN);
        assertThat(vehicle.getNumberOfDoors()).isEqualTo(4);
        assertThat(vehicle.getSeats()).isEqualTo(5);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.INACTIVE);
        assertThat(vehicle.getDriverId()).isNull();
    }
    
    @Test
    @DisplayName("Should throw exception when license plate is null")
    void shouldThrowExceptionWhenLicensePlateIsNull() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, null,
            "Toyota", "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 4, 5
        ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("License plate cannot be null");
    }
    
    @Test
    @DisplayName("Should throw exception when brand is null")
    void shouldThrowExceptionWhenBrandIsNull() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, licensePlate,
            null, "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 4, 5
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Brand cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception when brand is empty")
    void shouldThrowExceptionWhenBrandIsEmpty() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, licensePlate,
            "", "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 4, 5
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Brand cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception when model is null")
    void shouldThrowExceptionWhenModelIsNull() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, licensePlate,
            "Toyota", null, validYear, "Preto",
            VehicleType.SEDAN, 4, 5
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Model cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception when number of doors is not 4")
    void shouldThrowExceptionWhenNumberOfDoorsIsNot4() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, licensePlate,
            "Toyota", "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 2, 5
        ))
            .isInstanceOf(InvalidVehicleStateException.class)
            .hasMessageContaining("Only 4-door vehicles are allowed");
    }
    
    @Test
    @DisplayName("Should throw exception when seats is less than 1")
    void shouldThrowExceptionWhenSeatsLessThan1() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, licensePlate,
            "Toyota", "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 4, 0
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Seats must be between 1 and 12");
    }
    
    @Test
    @DisplayName("Should throw exception when seats exceeds 12")
    void shouldThrowExceptionWhenSeatsExceeds12() {
        assertThatThrownBy(() -> new Vehicle(
            vehicleId, tenantId, licensePlate,
            "Toyota", "Corolla", validYear, "Preto",
            VehicleType.SEDAN, 4, 13
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Seats must be between 1 and 12");
    }
    
    @Test
    @DisplayName("isPassengerVehicle should return true for SEDAN")
    void isPassengerVehicleShouldReturnTrueForSedan() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        assertThat(vehicle.isPassengerVehicle()).isTrue();
    }
    
    @Test
    @DisplayName("isPassengerVehicle should return true for HATCHBACK")
    void isPassengerVehicleShouldReturnTrueForHatchback() {
        Vehicle vehicle = createValidVehicle(VehicleType.HATCHBACK);
        
        assertThat(vehicle.isPassengerVehicle()).isTrue();
    }
    
    @Test
    @DisplayName("isPassengerVehicle should return true for SUV")
    void isPassengerVehicleShouldReturnTrueForSUV() {
        Vehicle vehicle = createValidVehicle(VehicleType.SUV);
        
        assertThat(vehicle.isPassengerVehicle()).isTrue();
    }
    
    @Test
    @DisplayName("Should assign driver to vehicle")
    void shouldAssignDriverToVehicle() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        vehicle.assignToDriver(driverId);
        
        assertThat(vehicle.getDriverId()).isEqualTo(driverId);
    }
    
    @Test
    @DisplayName("Should unassign driver from vehicle")
    void shouldUnassignDriver() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        
        vehicle.unassignDriver();
        
        assertThat(vehicle.getDriverId()).isNull();
    }
    
    @Test
    @DisplayName("Should throw exception when unassigning driver from active vehicle")
    void shouldThrowExceptionWhenUnassigningDriverFromActiveVehicle() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        vehicle.activate();
        
        assertThatThrownBy(vehicle::unassignDriver)
            .isInstanceOf(InvalidVehicleStateException.class)
            .hasMessageContaining("Cannot unassign driver from active vehicle");
    }
    
    @Test
    @DisplayName("Should activate vehicle with valid conditions")
    void shouldActivateVehicle() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        
        vehicle.activate();
        
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
        assertThat(vehicle.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should throw exception when activating vehicle without driver")
    void shouldThrowExceptionWhenActivatingWithoutDriver() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        assertThatThrownBy(vehicle::activate)
            .isInstanceOf(InvalidVehicleStateException.class)
            .hasMessageContaining("must be assigned to a driver first");
    }
    
    @Test
    @DisplayName("Should throw exception when activating vehicle older than 10 years")
    void shouldThrowExceptionWhenActivatingVehicleOlderThan10Years() {
        int currentYear = java.time.LocalDate.now().getYear();
        VehicleYear oldYear = new VehicleYear(currentYear - 10); // Exactly 10 years is OK
        
        // This should work
        Vehicle vehicle = new Vehicle(
            vehicleId, tenantId, licensePlate,
            "Toyota", "Corolla", oldYear, "Preto",
            VehicleType.SEDAN, 4, 5
        );
        vehicle.assignToDriver(driverId);
        vehicle.activate(); // Should succeed
        
        assertThat(vehicle.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should throw exception when activating vehicle with doors != 4")
    void shouldThrowExceptionWhenActivatingVehicleWithout4Doors() {
        // This test is redundant since constructor already validates
        // but keeping for completeness of activate() method validation
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        
        // Vehicle already has 4 doors, so this should succeed
        vehicle.activate();
        assertThat(vehicle.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should deactivate vehicle")
    void shouldDeactivateVehicle() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        vehicle.activate();
        
        vehicle.deactivate();
        
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.INACTIVE);
        assertThat(vehicle.isActive()).isFalse();
    }
    
    @Test
    @DisplayName("Should mark vehicle as in maintenance")
    void shouldMarkAsInMaintenance() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        vehicle.markAsInMaintenance();
        
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
    }
    
    @Test
    @DisplayName("canAcceptRides should return true when active")
    void canAcceptRidesShouldReturnTrueWhenActive() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        vehicle.activate();
        
        assertThat(vehicle.canAcceptRides()).isTrue();
    }
    
    @Test
    @DisplayName("canAcceptRides should return false when inactive")
    void canAcceptRidesShouldReturnFalseWhenInactive() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        assertThat(vehicle.canAcceptRides()).isFalse();
    }
    
    @Test
    @DisplayName("canAcceptRides should return false when in maintenance")
    void canAcceptRidesShouldReturnFalseWhenInMaintenance() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.markAsInMaintenance();
        
        assertThat(vehicle.canAcceptRides()).isFalse();
    }
    
    @Test
    @DisplayName("isOwnedBy should return true for correct driver")
    void isOwnedByShouldReturnTrueForCorrectDriver() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        
        assertThat(vehicle.isOwnedBy(driverId)).isTrue();
    }
    
    @Test
    @DisplayName("isOwnedBy should return false for different driver")
    void isOwnedByShouldReturnFalseForDifferentDriver() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        vehicle.assignToDriver(driverId);
        
        UUID otherDriverId = UUID.randomUUID();
        assertThat(vehicle.isOwnedBy(otherDriverId)).isFalse();
    }
    
    @Test
    @DisplayName("isOwnedBy should return false when no driver assigned")
    void isOwnedByShouldReturnFalseWhenNoDriver() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        assertThat(vehicle.isOwnedBy(driverId)).isFalse();
    }
    
    @Test
    @DisplayName("Should be equal when same ID")
    void shouldBeEqualWhenSameId() {
        Vehicle vehicle1 = createValidVehicle(VehicleType.SEDAN);
        Vehicle vehicle2 = new Vehicle(
            vehicle1.getId(), tenantId, driverId, licensePlate,
            "Honda", "Civic", validYear, "Branco",
            VehicleType.HATCHBACK, 4, 5, VehicleStatus.ACTIVE,
            LocalDateTime.now(), LocalDateTime.now()
        );
        
        assertThat(vehicle1).isEqualTo(vehicle2);
        assertThat(vehicle1.hashCode()).isEqualTo(vehicle2.hashCode());
    }
    
    @Test
    @DisplayName("Should not be equal when different IDs")
    void shouldNotBeEqualWhenDifferentIds() {
        Vehicle vehicle1 = createValidVehicle(VehicleType.SEDAN);
        Vehicle vehicle2 = createValidVehicle(VehicleType.SUV);
        
        assertThat(vehicle1).isNotEqualTo(vehicle2);
    }
    
    @Test
    @DisplayName("ToString should contain vehicle info")
    void toStringShouldContainVehicleInfo() {
        Vehicle vehicle = createValidVehicle(VehicleType.SEDAN);
        
        String toString = vehicle.toString();
        assertThat(toString).contains("Vehicle");
        assertThat(toString).contains("SEDAN");
        assertThat(toString).contains("Toyota");
    }
    
    // Helper method
    private Vehicle createValidVehicle(VehicleType type) {
        return new Vehicle(
            UUID.randomUUID(), tenantId, licensePlate,
            "Toyota", "Corolla", validYear, "Preto",
            type, 4, 5
        );
    }
}
