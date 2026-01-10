package com.rappidrive.application.ports.input;

import java.util.UUID;

public interface RejectDriverInputPort {

    RejectDriverResponse execute(RejectDriverCommand command);

    record RejectDriverCommand(
        UUID adminId,
        UUID approvalId,
        String rejectionReason,
        boolean permanentBan
    ) {}

    record RejectDriverResponse(
        UUID approvalId,
        UUID driverId,
        String status,
        String rejectionReason
    ) {}
}
