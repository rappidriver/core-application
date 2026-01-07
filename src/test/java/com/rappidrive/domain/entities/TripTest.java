package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.exceptions.InvalidTripStateException;
import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import com.rappidrive.domain.valueobjects.TripId;
import com.rappidrive.domain.valueobjects.PassengerId;
import com.rappidrive.domain.valueobjects.DriverId;

import static org.junit.jupiter.api.Assertions.*;

class TripTest {
    
    private TripId id;
    private TenantId tenantId;
    private PassengerId passengerId;
    private DriverId driverId;
    private Location origin;
    private Location destination;
    
    @BeforeEach
    void setUp() {
        id = TripId.generate();
        tenantId = TenantId.generate();
        passengerId = new PassengerId(UUID.randomUUID());
        driverId = new DriverId(UUID.randomUUID());
        origin = new Location(-23.550520, -46.633308); // São Paulo
        destination = new Location(-23.561684, -46.655981); // Paulista Ave
    }
    
    @Test
    void shouldCreateTripWithValidFields() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        assertNotNull(trip);
        assertEquals(id, trip.getId());
        assertEquals(tenantId, trip.getTenantId());
        assertEquals(passengerId, trip.getPassengerId());
        assertTrue(trip.getDriverId().isEmpty());
        assertEquals(origin, trip.getOrigin());
        assertEquals(destination, trip.getDestination());
        assertEquals(TripStatus.REQUESTED, trip.getStatus());
        assertTrue(trip.getEstimatedDistanceKm() > 0);
        assertNotNull(trip.getEstimatedFare());
        assertTrue(trip.getActualFare().isEmpty());
        assertNotNull(trip.getRequestedAt());
        assertTrue(trip.getStartedAt().isEmpty());
        assertTrue(trip.getCompletedAt().isEmpty());
        assertTrue(trip.getCancellationReason().isEmpty());
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Trip(null, tenantId, passengerId, origin, destination)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenTenantIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Trip(id, null, passengerId, origin, destination)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenPassengerIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Trip(id, tenantId, null, origin, destination)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Trip(id, tenantId, passengerId, null, destination)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenDestinationIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Trip(id, tenantId, passengerId, origin, null)
        );
    }
    
    @Test
    void shouldCalculateEstimatedDistance() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        double distance = trip.getEstimatedDistanceKm();
        
        assertTrue(distance > 2.0 && distance < 3.0, 
            "Distance should be around 2.5km");
    }
    
    @Test
    void shouldCalculateEstimatedFare() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        Money fare = trip.getEstimatedFare();
        
        assertEquals(Currency.BRL, fare.getCurrency());
        // BASE_FARE (5.00) + ~2.5km * PRICE_PER_KM (2.50) = ~11.25
        assertTrue(fare.getAmount().compareTo(new BigDecimal("11.00")) > 0);
        assertTrue(fare.getAmount().compareTo(new BigDecimal("12.00")) < 0);
    }
    
    @Test
    void shouldAssignDriverToRequestedTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        trip.assignDriver(driverId);
        
        assertEquals(TripStatus.DRIVER_ASSIGNED, trip.getStatus());
        assertTrue(trip.getDriverId().isPresent());
        assertEquals(driverId.getValue(), trip.getDriverId().get());
    }
    
    @Test
    void shouldThrowExceptionWhenAssigningNullDriver() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        assertThrows(IllegalArgumentException.class, () ->
            trip.assignDriver(null)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenAssigningDriverToNonRequestedTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        
        assertThrows(InvalidTripStateException.class, () ->
            trip.assignDriver(new DriverId(UUID.randomUUID()))
        );
    }
    
    @Test
    void shouldStartTripWithAssignedDriver() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        
        trip.start();
        
        assertEquals(TripStatus.IN_PROGRESS, trip.getStatus());
        assertTrue(trip.getStartedAt().isPresent());
    }
    
    @Test
    void shouldThrowExceptionWhenStartingTripWithoutDriver() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        assertThrows(InvalidTripStateException.class, trip::start);
    }
    
    @Test
    void shouldThrowExceptionWhenStartingAlreadyStartedTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        trip.start();
        
        assertThrows(InvalidTripStateException.class, trip::start);
    }
    
    @Test
    void shouldCompleteTripInProgress() throws InterruptedException {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        trip.start();
        Thread.sleep(10); // Small delay to ensure duration > 0
        
        Money actualFare = new Money(new BigDecimal("15.00"), Currency.BRL);
        trip.complete(actualFare);
        
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertTrue(trip.getActualFare().isPresent());
        assertEquals(actualFare, trip.getActualFare().get());
        assertTrue(trip.getCompletedAt().isPresent());
    }
    
    @Test
    void shouldThrowExceptionWhenCompletingWithNullFare() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        trip.start();
        
        assertThrows(IllegalArgumentException.class, () ->
            trip.complete(null)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenCompletingNonInProgressTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        Money actualFare = new Money(new BigDecimal("15.00"), Currency.BRL);
        
        assertThrows(InvalidTripStateException.class, () ->
            trip.complete(actualFare)
        );
    }
    
    @Test
    void shouldCancelRequestedTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        trip.cancel("Passenger cancelled");
        
        assertEquals(TripStatus.CANCELLED, trip.getStatus());
        assertTrue(trip.getCancellationReason().isPresent());
        assertEquals("Passenger cancelled", trip.getCancellationReason().get());
        assertTrue(trip.getCompletedAt().isPresent());
    }
    
    @Test
    void shouldCancelTripWithAssignedDriver() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        
        trip.cancel("Driver not available");
        
        assertEquals(TripStatus.CANCELLED, trip.getStatus());
    }
    
    @Test
    void shouldCancelTripInProgress() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        trip.start();
        
        trip.cancel("Emergency");
        
        assertEquals(TripStatus.CANCELLED, trip.getStatus());
    }
    
    @Test
    void shouldThrowExceptionWhenCancellingWithNullReason() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        assertThrows(IllegalArgumentException.class, () ->
            trip.cancel(null)
        );
    }
    
    @Test
    void shouldThrowExceptionWhenCancellingWithBlankReason() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        assertThrows(IllegalArgumentException.class, () ->
            trip.cancel("   ")
        );
    }
    
    @Test
    void shouldThrowExceptionWhenCancellingCompletedTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        trip.start();
        trip.complete(new Money(new BigDecimal("15.00"), Currency.BRL));
        
        assertThrows(InvalidTripStateException.class, () ->
            trip.cancel("Too late")
        );
    }
    
    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledTrip() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.cancel("First cancellation");
        
        assertThrows(InvalidTripStateException.class, () ->
            trip.cancel("Second cancellation")
        );
    }
    
    @Test
    void shouldTrimCancellationReason() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        trip.cancel("  Passenger cancelled  ");
        
        assertEquals("Passenger cancelled", trip.getCancellationReason().get());
    }
    
    @Test
    void shouldCalculateTripDuration() throws InterruptedException {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        trip.start();
        Thread.sleep(100); // 100ms delay
        trip.complete(new Money(new BigDecimal("15.00"), Currency.BRL));
        
        assertTrue(trip.getDuration().isPresent());
        Duration duration = trip.getDuration().get();
        assertTrue(duration.toMillis() >= 100, 
            "Duration should be at least 100ms");
    }
    
    @Test
    void shouldReturnEmptyDurationWhenNotCompleted() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        assertTrue(trip.getDuration().isEmpty());
    }
    
    @Test
    void shouldReturnEmptyDurationWhenNotStarted() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        trip.assignDriver(driverId);
        
        assertTrue(trip.getDuration().isEmpty());
    }
    
    @Test
    void shouldBeEqualWhenSameId() {
        Trip trip1 = new Trip(id, tenantId, passengerId, origin, destination);
        Trip trip2 = new Trip(id, TenantId.generate(), new PassengerId(UUID.randomUUID()),
            new Location(0, 0), new Location(1, 1));
        
        assertEquals(trip1, trip2);
        assertEquals(trip1.hashCode(), trip2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Trip trip1 = new Trip(id, tenantId, passengerId, origin, destination);
        Trip trip2 = new Trip(TripId.generate(), tenantId, passengerId, 
            origin, destination);
        
        assertNotEquals(trip1, trip2);
    }
    
    @Test
    void shouldHaveMeaningfulToString() {
        Trip trip = new Trip(id, tenantId, passengerId, origin, destination);
        
        String str = trip.toString();
        
        assertTrue(str.contains(id.toString()));
        assertTrue(str.contains(passengerId.toString()));
        assertTrue(str.contains("REQUESTED"));
    }
}
