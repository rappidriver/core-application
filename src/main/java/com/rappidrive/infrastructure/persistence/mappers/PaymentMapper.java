package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.PaymentMethod;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.PaymentJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for conversion between Payment (domain) and PaymentJpaEntity (JPA).
 * Manual implementation due to final fields and value objects.
 */
@Component
public class PaymentMapper {
    
    /**
     * Converts domain Payment to JPA entity.
     */
    public PaymentJpaEntity toJpaEntity(Payment payment) {
        PaymentMethod method = payment.getPaymentMethod();
        
        return new PaymentJpaEntity(
            payment.getId(),
            payment.getTenantId().getValue(),
            payment.getTripId(),
            payment.getAmount().getAmount(),
            payment.getAmount().getCurrency().name(),
            payment.getPlatformFee().getAmount(),
            payment.getDriverAmount().getAmount(),
            method.getType(),
            method.getCardLast4(),
            method.getCardBrand(),
            method.getPixKey(),
            payment.getStatus(),
            payment.getGatewayTransactionId(),
            payment.getFailureReason(),
            payment.getProcessedAt(),
            null,
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
    
    /**
     * Converts JPA entity to domain Payment.
     */
    public Payment toDomain(PaymentJpaEntity entity) {
        Money amount = new Money(entity.getAmount(), Currency.valueOf(entity.getCurrency()));
        Money platformFee = new Money(entity.getPlatformFee(), Currency.valueOf(entity.getCurrency()));
        Money driverAmount = new Money(entity.getDriverAmount(), Currency.valueOf(entity.getCurrency()));
        
        PaymentMethod method = createPaymentMethod(entity);
        
        return new Payment(
            entity.getId(),
            entity.getTripId(),
            new TenantId(entity.getTenantId()),
            amount,
            platformFee,
            driverAmount,
            method,
            entity.getStatus(),
            entity.getGatewayTransactionId(),
            entity.getFailureReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getProcessedAt()
        );
    }
    
    private PaymentMethod createPaymentMethod(PaymentJpaEntity entity) {
        return switch (entity.getPaymentMethodType()) {
            case CREDIT_CARD -> PaymentMethod.creditCard(entity.getCardLast4(), entity.getCardBrand());
            case DEBIT_CARD -> PaymentMethod.debitCard(entity.getCardLast4(), entity.getCardBrand());
            case PIX -> PaymentMethod.pix(entity.getPixKey());
            case CASH -> PaymentMethod.cash();
        };
    }
}
