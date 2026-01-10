package com.rappidrive.presentation.dto.approval;

import java.util.List;

public record SubmitApprovalRequest(
    String driverId,
    List<String> documentUrls
) {}
