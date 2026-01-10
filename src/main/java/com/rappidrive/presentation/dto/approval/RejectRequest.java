package com.rappidrive.presentation.dto.approval;

import java.util.UUID;

public record RejectRequest(
    UUID adminId,
    String rejectionReason,
    boolean permanentBan
) {}
