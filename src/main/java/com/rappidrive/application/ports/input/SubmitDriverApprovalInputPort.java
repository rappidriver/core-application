package com.rappidrive.application.ports.input;

import java.util.List;
import java.util.UUID;

public interface SubmitDriverApprovalInputPort {

    SubmitDriverApprovalResponse execute(SubmitDriverApprovalCommand command);

    record SubmitDriverApprovalCommand(
        UUID driverId,
        List<String> documentUrls
    ) {}

    record SubmitDriverApprovalResponse(
        UUID approvalRequestId,
        String status
    ) {}
}
