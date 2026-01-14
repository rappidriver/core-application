package com.rappidrive.presentation.dto;

import com.rappidrive.domain.valueobjects.CancellationReason;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.ActorType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CancelTripRequest(
    String reason,
    String additionalNotes
) {}
