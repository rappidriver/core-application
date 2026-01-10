package com.rappidrive.presentation.dto.approval;

import java.util.UUID;

public record RejectResponse(
    UUID approvalId,
    UUID driverId,
    String status,
    String rejectionReason
) {}
