package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando um veículo não é encontrado.
 */
public class VehicleNotFoundException extends DomainException {
    
    public VehicleNotFoundException(String message) {
        super(message);
    }
    
    public static VehicleNotFoundException withId(java.util.UUID id) {
        return new VehicleNotFoundException("Vehicle not found with id: " + id);
    }
    
    public static VehicleNotFoundException withLicensePlate(String licensePlate) {
        return new VehicleNotFoundException("Vehicle not found with license plate: " + licensePlate);
    }
}
