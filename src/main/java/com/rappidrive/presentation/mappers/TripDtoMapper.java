package com.rappidrive.presentation.mappers;

import com.rappidrive.application.ports.input.CompleteTripWithPaymentInputPort;
import com.rappidrive.application.ports.input.GetTripWithPaymentDetailsInputPort;
import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.enums.PaymentMethodType;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.PaymentMethod;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.presentation.dto.common.LocationDto;
import com.rappidrive.presentation.dto.request.CompleteTripWithPaymentRequest;
import com.rappidrive.presentation.dto.response.FareResponse;
import com.rappidrive.presentation.dto.response.PaymentResponse;
import com.rappidrive.presentation.dto.response.TripResponse;
import com.rappidrive.presentation.dto.response.TripWithPaymentDetailsResponse;
import com.rappidrive.presentation.mappers.PaymentDtoMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * MapStruct mapper for Trip DTO conversions.
 */
@Mapper(componentModel = "spring")
public abstract class TripDtoMapper {
    
    @Autowired
    protected PaymentDtoMapper paymentDtoMapper;
    
    /**
     * Converts UUID to TenantId value object.
     */
    public TenantId toTenantId(java.util.UUID tenantId) {
        return tenantId != null ? new TenantId(tenantId) : null;
    }
    
    /**
     * Converts LocationDto to Location value object.
     */
    public Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        return new Location(dto.latitude(), dto.longitude());
    }
    
    /**
     * Converts Location value object to LocationDto.
     */
    public LocationDto toLocationDto(Location location) {
        if (location == null) return null;
        return new LocationDto(location.getLatitude(), location.getLongitude());
    }
    
    /**
     * Converts CompleteTripWithPaymentRequest to Command.
     */
    public CompleteTripWithPaymentInputPort.CompleteTripWithPaymentCommand toCommand(
            UUID tripId,
            CompleteTripWithPaymentRequest request) {
        Location dropoff = toLocation(request.dropoffLocation());
        PaymentMethod paymentMethod = toPaymentMethod(request.paymentMethod());
        return new CompleteTripWithPaymentInputPort.CompleteTripWithPaymentCommand(
            tripId,
            dropoff,
            paymentMethod
        );
    }
    
    /**
     * Converts PaymentMethodDto to PaymentMethod value object.
     */
    private PaymentMethod toPaymentMethod(CompleteTripWithPaymentRequest.PaymentMethodDto dto) {
        PaymentMethodType type = PaymentMethodType.valueOf(dto.type());
        
        if (type == PaymentMethodType.CREDIT_CARD) {
            return PaymentMethod.creditCard(dto.cardLast4(), dto.cardBrand());
        }
        if (type == PaymentMethodType.DEBIT_CARD) {
            return PaymentMethod.debitCard(dto.cardLast4(), dto.cardBrand());
        }
        if (type == PaymentMethodType.PIX) {
            return PaymentMethod.pix(dto.pixKey());
        }
        if (type == PaymentMethodType.CASH) {
            return PaymentMethod.cash();
        }
        
        throw new IllegalArgumentException("Unknown payment method type: " + dto.type());
    }
    
    /**
     * Converts Trip domain entity to TripResponse.
     */
    public TripResponse toResponse(Trip trip) {
        if (trip == null) return null;
        
        return new TripResponse(
            trip.getId().getValue(),
            trip.getTenantId().getValue(),
            trip.getPassengerId().getValue(),
            trip.getDriverId().orElse(null),
            toLocationDto(trip.getOrigin()),
            toLocationDto(trip.getDestination()),
            trip.getStatus().name(),
            BigDecimal.valueOf(trip.getEstimatedDistanceKm()),
            trip.getEstimatedFare().getAmount(),
            trip.getActualFare().map(money -> money.getAmount()).orElse(null),
            trip.getRequestedAt(),
            trip.getStartedAt().orElse(null),
            trip.getCompletedAt().orElse(null),
            trip.getFareId().orElse(null),
            trip.getPaymentId().orElse(null),
            trip.getPaymentStatus() != null ? trip.getPaymentStatus().name() : null
        );
    }
    
    /**
     * Converts TripWithPaymentDetails to TripWithPaymentDetailsResponse.
     */
    public TripWithPaymentDetailsResponse toTripWithPaymentDetailsResponse(
            GetTripWithPaymentDetailsInputPort.TripWithPaymentDetails details) {
        if (details == null) return null;
        
        TripResponse tripResponse = toResponse(details.trip());
        FareResponse fareResponse = details.hasFare() ? 
            paymentDtoMapper.toResponse(details.fare()) : null;
        PaymentResponse paymentResponse = details.hasPayment() ? 
            paymentDtoMapper.toResponse(details.payment()) : null;
        
        return new TripWithPaymentDetailsResponse(
            details.trip().getId().getValue(),
            tripResponse,
            fareResponse,
            paymentResponse,
            details.isFullyPaid()
        );
    }
    
    /**
     * Converts TripCompletionResult to TripWithPaymentDetailsResponse.
     */
    public TripWithPaymentDetailsResponse toTripWithPaymentDetailsResponse(
            CompleteTripWithPaymentInputPort.TripCompletionResult result) {
        if (result == null) return null;
        
        TripResponse tripResponse = toResponse(result.trip());
        FareResponse fareResponse = paymentDtoMapper.toResponse(result.fare());
        PaymentResponse paymentResponse = paymentDtoMapper.toResponse(result.payment());
        
        return new TripWithPaymentDetailsResponse(
            result.trip().getId().getValue(),
            tripResponse,
            fareResponse,
            paymentResponse,
            result.paymentSuccessful()
        );
    }
}
