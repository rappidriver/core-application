package com.rappidrive.presentation.dto.approval;

import java.util.List;

public record PendingApprovalsPageResponse(
    List<PendingApprovalResponse> approvals,
    long totalCount,
    int pageNumber,
    int pageSize
) {}
