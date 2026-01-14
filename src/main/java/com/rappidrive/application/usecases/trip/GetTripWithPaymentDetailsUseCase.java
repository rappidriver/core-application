package com.rappidrive.application.usecases.trip;

import com.rappidrive.application.ports.input.GetTripWithPaymentDetailsInputPort;
import com.rappidrive.application.ports.output.FareRepositoryPort;
import com.rappidrive.application.ports.output.PaymentRepositoryPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.TripNotFoundException;

import java.util.UUID;

public class GetTripWithPaymentDetailsUseCase implements GetTripWithPaymentDetailsInputPort {
    
    private final TripRepositoryPort tripRepository;
    private final FareRepositoryPort fareRepository;
    private final PaymentRepositoryPort paymentRepository;
    
    public GetTripWithPaymentDetailsUseCase(
            TripRepositoryPort tripRepository,
            FareRepositoryPort fareRepository,
            PaymentRepositoryPort paymentRepository) {
        this.tripRepository = tripRepository;
        this.fareRepository = fareRepository;
        this.paymentRepository = paymentRepository;
    }
    
    @Override
    public TripWithPaymentDetails execute(UUID tripId) {
        if (tripId == null) {
            throw new IllegalArgumentException("Trip ID cannot be null");
        }
        
        // Retrieve trip
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException(tripId));
        
        // Retrieve fare if exists
        Fare fare = null;
        if (trip.getFareId().isPresent()) {
            fare = fareRepository.findById(trip.getFareId().get()).orElse(null);
        }
        
        // Retrieve payment if exists
        Payment payment = null;
        if (trip.getPaymentId().isPresent()) {
            payment = paymentRepository.findById(trip.getPaymentId().get()).orElse(null);
        }
        
        return new TripWithPaymentDetails(trip, fare, payment);
    }
}
