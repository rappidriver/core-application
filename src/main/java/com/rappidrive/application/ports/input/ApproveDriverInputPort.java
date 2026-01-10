package com.rappidrive.application.ports.input;

import java.util.UUID;

public interface ApproveDriverInputPort {

    ApproveDriverResponse execute(ApproveDriverCommand command);

    record ApproveDriverCommand(
        UUID adminId,
        UUID approvalId
    ) {}

    record ApproveDriverResponse(
        UUID approvalId,
        UUID driverId,
        String status
    ) {}
}
