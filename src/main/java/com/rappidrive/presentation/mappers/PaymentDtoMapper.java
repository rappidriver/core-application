package com.rappidrive.presentation.mappers;

import com.rappidrive.application.ports.input.payment.CalculateFareInputPort.CalculateFareCommand;
import com.rappidrive.application.ports.input.payment.ProcessPaymentInputPort.ProcessPaymentCommand;
import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.enums.PaymentMethodType;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.PaymentMethod;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.presentation.dto.request.CalculateFareRequest;
import com.rappidrive.presentation.dto.request.ProcessPaymentRequest;
import com.rappidrive.presentation.dto.response.FareResponse;
import com.rappidrive.presentation.dto.response.PaymentResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapper for conversion between DTOs and Commands/Entities for payment and fare.
 */
@Component
public class PaymentDtoMapper {
    
    /**
     * Converts CalculateFareRequest to CalculateFareCommand.
     */
    public CalculateFareCommand toCommand(CalculateFareRequest request) {
        return new CalculateFareCommand(
            UUID.randomUUID(), // tripId - gerado aqui pois é um cálculo, não uma viagem existente
            new TenantId(request.tenantId()),
            request.distanceInKm().doubleValue(),
            request.durationInMinutes().intValue(),
            request.vehicleType(),
            request.tripStartTime()
        );
    }
    
    /**
     * Converts ProcessPaymentRequest to ProcessPaymentCommand.
     */
    public ProcessPaymentCommand toCommand(ProcessPaymentRequest request) {
        PaymentMethod method = createPaymentMethod(
            request.paymentMethodType(),
            request.cardLast4(),
            request.cardBrand(),
            request.pixKey()
        );
        
        return new ProcessPaymentCommand(
            request.tripId(),
            method
        );
    }
    
    private PaymentMethod createPaymentMethod(PaymentMethodType type, String cardLast4, 
                                             String cardBrand, String pixKey) {
        return switch (type) {
            case CREDIT_CARD -> PaymentMethod.creditCard(cardLast4, cardBrand);
            case DEBIT_CARD -> PaymentMethod.debitCard(cardLast4, cardBrand);
            case PIX -> PaymentMethod.pix(pixKey);
            case CASH -> PaymentMethod.cash();
        };
    }
    
    /**
     * Converts Fare entity to FareResponse DTO.
     */
    public FareResponse toResponse(Fare fare) {
        return new FareResponse(
            fare.getBreakdown().getBaseFare().getAmount(),
            fare.getBreakdown().getDistanceFare().getAmount(),
            fare.getBreakdown().getTimeFare().getAmount(),
            fare.getBreakdown().getSubtotal().getAmount(),
            BigDecimal.valueOf(fare.getBreakdown().getTimeMultiplier()),
            BigDecimal.valueOf(fare.getBreakdown().getVehicleMultiplier()),
            fare.getTotalAmount().getAmount(),
            fare.getBreakdown().getExplanation()
        );
    }
    
    /**
     * Converts Payment entity to PaymentResponse DTO.
     */
    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getTenantId().getValue(),
            payment.getTripId(),
            payment.getAmount().getAmount(),
            payment.getAmount().getCurrency().name(),
            payment.getPlatformFee().getAmount(),
            payment.getDriverAmount().getAmount(),
            payment.getPaymentMethod().getType(),
            payment.getPaymentMethod().getCardLast4(),
            payment.getPaymentMethod().getCardBrand(),
            payment.getPaymentMethod().getPixKey(),
            payment.getStatus(),
            payment.getGatewayTransactionId(),
            payment.getFailureReason(),
            payment.getProcessedAt(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
