package com.rappidrive.presentation.dto.approval;

import java.util.UUID;

public record ApproveResponse(
    UUID approvalId,
    UUID driverId,
    String status
) {}
