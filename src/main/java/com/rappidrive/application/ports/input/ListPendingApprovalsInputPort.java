package com.rappidrive.application.ports.input;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListPendingApprovalsInputPort {

    ListPendingApprovalsResponse execute(ListPendingApprovalsCommand command);

    record ListPendingApprovalsCommand(
        UUID adminId,
        int pageNumber,
        int pageSize
    ) {}

    record ListPendingApprovalsResponse(
        List<PendingApprovalDto> approvals,
        long totalCount,
        int pageNumber,
        int pageSize
    ) {}

    record PendingApprovalDto(
        UUID approvalId,
        UUID driverId,
        String driverName,
        String driverEmail,
        LocalDateTime submittedAt,
        List<String> documents
    ) {}
}
