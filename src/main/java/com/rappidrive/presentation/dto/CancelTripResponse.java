package com.rappidrive.presentation.dto;

import com.rappidrive.presentation.dto.common.MoneyDto;
import java.time.LocalDateTime;
import java.util.UUID;

public record CancelTripResponse(
    UUID tripId,
    boolean cancelled,
    String cancelledBy,
    String reason,
    MoneyDto feeCharged,
    LocalDateTime cancelledAt,
    String message
) {}
