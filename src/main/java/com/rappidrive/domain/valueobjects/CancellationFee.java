package com.rappidrive.domain.valueobjects;

import java.util.Objects;

public record CancellationFee(Money amount, String reason) {

    public CancellationFee {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
    }

    public static CancellationFee free(String reason) {
        return new CancellationFee(Money.zero(Currency.BRL), reason);
    }

    public static CancellationFee of(Money amount, String reason) {
        return new CancellationFee(amount, reason);
    }

    public boolean isFree() {
        return amount.isZero();
    }
}
