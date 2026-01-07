package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando uma operação viola as regras de negócio de um veículo.
 */
public class InvalidVehicleStateException extends DomainException {
    
    public InvalidVehicleStateException(String message) {
        super(message);
    }
    
    public static InvalidVehicleStateException cannotActivateWithoutDriver() {
        return new InvalidVehicleStateException(
            "Cannot activate vehicle: vehicle must be assigned to a driver first"
        );
    }
    
    public static InvalidVehicleStateException vehicleTooOld(int age, int maxAge) {
        return new InvalidVehicleStateException(
            String.format("Cannot activate vehicle: vehicle is %d years old, maximum allowed is %d years", 
                age, maxAge)
        );
    }
    
    public static InvalidVehicleStateException invalidNumberOfDoors(int doors) {
        return new InvalidVehicleStateException(
            String.format("Invalid number of doors: %d. Only 4-door vehicles are allowed", doors)
        );
    }
    
    public static InvalidVehicleStateException notPassengerVehicle(String type) {
        return new InvalidVehicleStateException(
            String.format("Invalid vehicle type: %s. Only passenger vehicles (SEDAN, HATCHBACK, SUV) are allowed", 
                type)
        );
    }
    
    public static InvalidVehicleStateException duplicateLicensePlate(String licensePlate) {
        return new InvalidVehicleStateException(
            "License plate already exists: " + licensePlate
        );
    }
}
