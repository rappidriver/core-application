package com.rappidrive.domain.services;

import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancellationPolicyServiceTest {

    private CancellationPolicyService service;

    @BeforeEach
    void setUp() {
        service = new CancellationPolicyService();
    }

    @Test
    void shouldChargeNoFeeWhenPassengerCancelsRequestedWithin5Minutes() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.REQUESTED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(2));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertTrue(fee.isFree());
    }

    @Test
    void shouldChargeFeeWhenPassengerCancelsRequestedAfter5Minutes() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.REQUESTED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertFalse(fee.isFree());
        assertEquals(5.0, fee.amount().getAmount().doubleValue(), 0.01);
    }

    @Test
    void shouldChargeNoFeeWhenPassengerCancelsAssignedWithin2Minutes() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.DRIVER_ASSIGNED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(trip.getAssignedAt()).thenReturn(LocalDateTime.now().minusMinutes(1));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertTrue(fee.isFree());
    }

    @Test
    void shouldChargeFeeWhenPassengerCancelsAssignedAfter2Minutes() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.DRIVER_ASSIGNED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(trip.getAssignedAt()).thenReturn(LocalDateTime.now().minusMinutes(5));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertFalse(fee.isFree());
        assertEquals(8.0, fee.amount().getAmount().doubleValue(), 0.01);
    }

    @Test
    void shouldChargeNoFeeWhenDriverCancels() {
        Trip trip = mock(Trip.class);
        
        CancellationFee fee = service.calculateFee(trip, ActorType.DRIVER, LocalDateTime.now());

        assertTrue(fee.isFree());
    }

    @Test
    void shouldChargeNoFeeWhenCancellingBeforeRequestedFeeWindow() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.REQUESTED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusSeconds(30));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertTrue(fee.isFree());
    }

    @Test
    void shouldChargeFeeBoundary5MinutesRequested() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.REQUESTED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(5).minusSeconds(1));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertFalse(fee.isFree());
    }

    @Test
    void shouldChargeFeeBoundary2MinutesAssigned() {
        Trip trip = mock(Trip.class);
        when(trip.getStatus()).thenReturn(TripStatus.DRIVER_ASSIGNED);
        when(trip.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(trip.getAssignedAt()).thenReturn(LocalDateTime.now().minusMinutes(2).minusSeconds(1));

        CancellationFee fee = service.calculateFee(trip, ActorType.PASSENGER, LocalDateTime.now());

        assertFalse(fee.isFree());
    }
}
