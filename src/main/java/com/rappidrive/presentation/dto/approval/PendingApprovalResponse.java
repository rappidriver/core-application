package com.rappidrive.presentation.dto.approval;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PendingApprovalResponse(
    UUID approvalId,
    UUID driverId,
    String driverName,
    String driverEmail,
    LocalDateTime submittedAt,
    List<String> documents
) {}
